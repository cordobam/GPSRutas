package com.example.melirutas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.melirutas.cost.CostCalculator
import com.example.melirutas.data.AppPreferences
import com.example.melirutas.data.TripRepository
import com.example.melirutas.data.db.MeliRutasDatabase
import com.example.melirutas.data.db.TripEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Pestañas de la barra inferior. */
enum class AppTab { LOTE, COSTOS, CODIGOS }

/**
 * Estado de nivel app: pestaña activa, códigos de viaje y calculadora de costos.
 * Sobrevive a rotación de pantalla; además el viaje en curso se autoguarda en la DB.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = AppPreferences(application)
    private val repository = TripRepository(MeliRutasDatabase.getInstance(application).tripDao())

    private val _selectedTab = MutableStateFlow(AppTab.LOTE)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    val codes: StateFlow<List<String>> = prefs.codes

    private val _kmInicialText = MutableStateFlow("")
    val kmInicialText: StateFlow<String> = _kmInicialText.asStateFlow()

    private val _kmFinalText = MutableStateFlow("")
    val kmFinalText: StateFlow<String> = _kmFinalText.asStateFlow()

    private val _rendimientoText = MutableStateFlow(plainNumber(prefs.kmPerLiter.value))
    val rendimientoText: StateFlow<String> = _rendimientoText.asStateFlow()

    private val _precioText = MutableStateFlow(plainNumber(prefs.pricePerLiter.value))
    val precioText: StateFlow<String> = _precioText.asStateFlow()

    private val kmRecorrido = combine(_kmInicialText, _kmFinalText) { inicial, fin ->
        CostCalculator.recorrido(
            CostCalculator.parseDecimal(inicial),
            CostCalculator.parseDecimal(fin)
        )
    }

    private val litrosEstimados = combine(kmRecorrido, _rendimientoText) { km, rendimiento ->
        CostCalculator.litros(km, CostCalculator.parseDecimal(rendimiento))
    }

    /** Km recorridos formateados ("350 km") o "—" si falta algún dato. */
    val kmRecorridosText: StateFlow<String> = kmRecorrido
        .map { km -> km?.let { "${CostCalculator.formatNumber(it, 1)} km" } ?: SIN_DATO }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SIN_DATO)

    /** Litros a cargar ("35 l") o "—". */
    val litrosText: StateFlow<String> = litrosEstimados
        .map { litros -> litros?.let { "${CostCalculator.formatNumber(it, 2)} l" } ?: SIN_DATO }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SIN_DATO)

    /** Costo total ("$ 52.500,00") o "—". */
    val costoTotalText: StateFlow<String> =
        combine(litrosEstimados, _precioText) { litros, precio ->
            CostCalculator.costo(litros, CostCalculator.parseDecimal(precio))
        }
            .map { costo -> costo?.let { CostCalculator.formatCurrency(it) } ?: SIN_DATO }
            .stateIn(viewModelScope, SharingStarted.Eagerly, SIN_DATO)

    /** Viajes finalizados de los últimos 7 días, del más reciente al más viejo. */
    val historial: StateFlow<List<TripEntity>> = repository.historial()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var guardadoJob: Job? = null

    init {
        viewModelScope.launch {
            repository.viajeAbierto().first()?.let { viaje ->
                viaje.kmInicial?.let { _kmInicialText.value = plainNumber(it) }
                viaje.kmFinal?.let { _kmFinalText.value = plainNumber(it) }
                viaje.rendimiento?.let { _rendimientoText.value = plainNumber(it) }
                viaje.precioPorLitro?.let { _precioText.value = plainNumber(it) }
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun onKmInicialChange(value: String) {
        _kmInicialText.value = value
        programarGuardado()
    }

    fun onKmFinalChange(value: String) {
        _kmFinalText.value = value
        programarGuardado()
    }

    fun onRendimientoChange(value: String) {
        _rendimientoText.value = value
        CostCalculator.parseDecimal(value)?.let { prefs.setKmPerLiter(it) }
        programarGuardado()
    }

    fun onPrecioChange(value: String) {
        _precioText.value = value
        CostCalculator.parseDecimal(value)?.let { prefs.setPricePerLiter(it) }
        programarGuardado()
    }

    /** Cierra el viaje actual (si tiene datos) y deja el formulario listo para otro. */
    fun nuevoViaje() {
        viewModelScope.launch {
            guardadoJob?.cancel()
            repository.guardarBorrador(
                kmInicial = CostCalculator.parseDecimal(_kmInicialText.value),
                kmFinal = CostCalculator.parseDecimal(_kmFinalText.value),
                rendimiento = CostCalculator.parseDecimal(_rendimientoText.value),
                precioPorLitro = CostCalculator.parseDecimal(_precioText.value)
            )
            repository.nuevoViaje()
            _kmInicialText.value = ""
            _kmFinalText.value = ""
            _rendimientoText.value = plainNumber(prefs.kmPerLiter.value)
            _precioText.value = plainNumber(prefs.pricePerLiter.value)
        }
    }

    /** Borra un viaje ya cerrado del historial. */
    fun eliminarViaje(trip: TripEntity) {
        viewModelScope.launch { repository.eliminarViaje(trip) }
    }

    /** Devuelve false si el código está vacío o ya existía. */
    fun addCode(code: String): Boolean = prefs.addCode(code)

    fun removeCode(code: String) {
        prefs.removeCode(code)
    }

    fun clearCodes() {
        prefs.clearCodes()
    }

    /** Graba el viaje abierto un ratito después de la última tecla, para no escribir de más. */
    private fun programarGuardado() {
        guardadoJob?.cancel()
        guardadoJob = viewModelScope.launch {
            delay(400)
            repository.guardarBorrador(
                kmInicial = CostCalculator.parseDecimal(_kmInicialText.value),
                kmFinal = CostCalculator.parseDecimal(_kmFinalText.value),
                rendimiento = CostCalculator.parseDecimal(_rendimientoText.value),
                precioPorLitro = CostCalculator.parseDecimal(_precioText.value)
            )
        }
    }

    private fun plainNumber(value: Double): String = when {
        value <= 0.0 -> ""
        value % 1.0 == 0.0 -> value.toLong().toString()
        else -> value.toString().replace('.', ',')
    }

    private companion object {
        const val SIN_DATO = "—"
    }
}
