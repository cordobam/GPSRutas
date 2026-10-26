package com.example.melirutas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.melirutas.cost.CostCalculator
import com.example.melirutas.cost.Semana
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

/** Secciones de la pestaña Costos. */
enum class CostosModo { VIAJE, CONSOLIDADO }

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

    /** Fecha elegida para el viaje en curso (la usa el date picker de Costos). */
    private val _fechaViaje = MutableStateFlow(System.currentTimeMillis())
    val fechaViaje: StateFlow<Long> = _fechaViaje.asStateFlow()

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

    /** Todos los viajes cerrados: los que se agrupan por semana en el consolidado. */
    val todosCerrados: StateFlow<List<TripEntity>> = repository.todosLosViajes()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Sección visible de Costos: el formulario del viaje o el consolidado semanal. */
    private val _costosModo = MutableStateFlow(CostosModo.VIAJE)
    val costosModo: StateFlow<CostosModo> = _costosModo.asStateFlow()

    /** Lunes 00:00 de la semana que se está mirando en el consolidado. */
    private val _semanaInicio = MutableStateFlow(Semana.inicioDe(System.currentTimeMillis()))
    val semanaInicio: StateFlow<Long> = _semanaInicio.asStateFlow()

    /** Viajes de esa semana que el usuario marcó para ver el total. Arranca vacío. */
    private val _viajesSeleccionados = MutableStateFlow<Set<Long>>(emptySet())
    val viajesSeleccionados: StateFlow<Set<Long>> = _viajesSeleccionados.asStateFlow()

    private var guardadoJob: Job? = null

    init {
        viewModelScope.launch {
            repository.viajeAbierto().first()?.let { viaje ->
                viaje.kmInicial?.let { _kmInicialText.value = plainNumber(it) }
                viaje.kmFinal?.let { _kmFinalText.value = plainNumber(it) }
                viaje.rendimiento?.let { _rendimientoText.value = plainNumber(it) }
                viaje.precioPorLitro?.let { _precioText.value = plainNumber(it) }
                _fechaViaje.value = viaje.fechaInicio
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun setCostosModo(modo: CostosModo) {
        _costosModo.value = modo
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

    /** Cambia la fecha del viaje en curso (para cargar viajes que olvidaron anotar ese día). */
    fun onFechaChange(millis: Long) {
        _fechaViaje.value = millis
        programarGuardado()
    }

    fun semanaAnterior() {
        _semanaInicio.value = Semana.masDias(_semanaInicio.value, -7)
        _viajesSeleccionados.value = emptySet()
    }

    fun semanaSiguiente() {
        _semanaInicio.value = Semana.masDias(_semanaInicio.value, 7)
        _viajesSeleccionados.value = emptySet()
    }

    fun irASemanaActual() {
        _semanaInicio.value = Semana.inicioDe(System.currentTimeMillis())
        _viajesSeleccionados.value = emptySet()
    }

    fun toggleViaje(id: Long) {
        _viajesSeleccionados.value = _viajesSeleccionados.value.let { actual ->
            if (id in actual) actual - id else actual + id
        }
    }

    /** Marca como pagados (o pendientes) los viajes seleccionados de la semana visible. */
    fun marcarSeleccionadosPagados(pagado: Boolean) {
        val seleccionados = _viajesSeleccionados.value
        val aMarcar = todosCerrados.value.filter { viaje ->
            viaje.id in seleccionados &&
                Semana.contiene(_semanaInicio.value, viaje.fechaInicio) &&
                (viaje.pagadoEn != null) != pagado
        }
        if (aMarcar.isEmpty()) return
        viewModelScope.launch { repository.marcarPagado(aMarcar, pagado) }
    }

    /** Cierra el viaje actual (si tiene datos) y deja el formulario listo para otro. */
    fun nuevoViaje() {
        viewModelScope.launch {
            guardadoJob?.cancel()
            repository.guardarBorrador(
                kmInicial = CostCalculator.parseDecimal(_kmInicialText.value),
                kmFinal = CostCalculator.parseDecimal(_kmFinalText.value),
                rendimiento = CostCalculator.parseDecimal(_rendimientoText.value),
                precioPorLitro = CostCalculator.parseDecimal(_precioText.value),
                fechaInicio = _fechaViaje.value
            )
            repository.nuevoViaje()
            _kmInicialText.value = ""
            _kmFinalText.value = ""
            _rendimientoText.value = plainNumber(prefs.kmPerLiter.value)
            _precioText.value = plainNumber(prefs.pricePerLiter.value)
            _fechaViaje.value = System.currentTimeMillis()
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
                precioPorLitro = CostCalculator.parseDecimal(_precioText.value),
                fechaInicio = _fechaViaje.value
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
