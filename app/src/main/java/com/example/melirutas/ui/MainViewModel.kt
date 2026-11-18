package com.example.melirutas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.melirutas.codigos.RutaLogic
import com.example.melirutas.codigos.ResumenRuta
import com.example.melirutas.codigos.ValidacionConteo
import com.example.melirutas.cost.CostCalculator
import com.example.melirutas.cost.PeajeDireccion
import com.example.melirutas.cost.Semana
import com.example.melirutas.data.AppPreferences
import com.example.melirutas.data.ResumenRutaRepository
import com.example.melirutas.data.TripRepository
import com.example.melirutas.data.db.MeliRutasDatabase
import com.example.melirutas.data.db.TripEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Pestañas de la barra inferior. */
enum class AppTab { LOTE, REPARTO, COSTOS, RESUMEN, CONFIG }

/** Secciones de la pestaña Costos. */
enum class CostosModo { VIAJE, CONSOLIDADO }

/**
 * Estado de nivel app: pestaña activa, resumen de ruta y calculadora de costos.
 * Sobrevive a rotación de pantalla; además el viaje en curso se autoguarda en la DB.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = AppPreferences(application)
    private val database = MeliRutasDatabase.getInstance(application)
    private val repository = TripRepository(database.tripDao())
    private val resumenRepository = ResumenRutaRepository(database.resumenRutaDao())

    private val _selectedTab = MutableStateFlow(AppTab.LOTE)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _kmInicialText = MutableStateFlow("")
    val kmInicialText: StateFlow<String> = _kmInicialText.asStateFlow()

    private val _kmFinalText = MutableStateFlow("")
    val kmFinalText: StateFlow<String> = _kmFinalText.asStateFlow()

    private val _rendimientoText = MutableStateFlow(plainNumber(prefs.kmPerLiter.value))
    val rendimientoText: StateFlow<String> = _rendimientoText.asStateFlow()

    private val _precioText = MutableStateFlow(plainNumber(prefs.pricePerLiter.value))
    val precioText: StateFlow<String> = _precioText.asStateFlow()

    /** Precio de la nafta por litro ("$ 1.500,00"). */
    val precioNaftaActualText: StateFlow<String> = prefs.pricePerLiter
        .map { CostCalculator.formatCurrency(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CostCalculator.formatCurrency(0.0))

    /** Rendimiento del auto ("12,5 km/l") o "—" si no está configurado. */
    val rendimientoActualText: StateFlow<String> = prefs.kmPerLiter
        .map { kmPorLitro ->
            if (kmPorLitro <= 0.0) {
                SIN_DATO
            } else {
                "${CostCalculator.formatNumber(kmPorLitro, 2)} km/l"
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SIN_DATO)

    private val _pagoViajeText = MutableStateFlow("")
    val pagoViajeText: StateFlow<String> = _pagoViajeText.asStateFlow()

    private val _peajeUsado = MutableStateFlow(false)
    val peajeUsado: StateFlow<Boolean> = _peajeUsado.asStateFlow()

    private val _peajeDireccion = MutableStateFlow(PeajeDireccion.IDA_VUELTA)
    val peajeDireccion: StateFlow<PeajeDireccion> = _peajeDireccion.asStateFlow()

    private val _precioPeajeText = MutableStateFlow(plainNumber(prefs.tollPrice.value))
    val precioPeajeText: StateFlow<String> = _precioPeajeText.asStateFlow()

    /** Fecha elegida para el viaje en curso (la usa el date picker de Costos). */
    private val _fechaViaje = MutableStateFlow(System.currentTimeMillis())
    val fechaViaje: StateFlow<Long> = _fechaViaje.asStateFlow()

    private val _fechaResumenSeleccionada =
        MutableStateFlow(RutaLogic.fechaDia(System.currentTimeMillis()))
    val fechaResumenSeleccionada: StateFlow<String> = _fechaResumenSeleccionada.asStateFlow()

    private val _diaResumenElegidoManualmente = MutableStateFlow(false)

    val diaEditableResumen: StateFlow<String> = _fechaViaje
        .map { RutaLogic.fechaDia(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            RutaLogic.fechaDia(System.currentTimeMillis())
        )

    private val diasConViajes: Flow<Set<String>> = combine(
        repository.viajeAbierto(),
        repository.todosLosViajes()
    ) { abierto, cerrados ->
        (listOfNotNull(abierto) + cerrados)
            .map { RutaLogic.fechaDia(it.fechaInicio) }
            .toSet()
    }

    val diasDisponibles: StateFlow<List<String>> = combine(
        diasConViajes,
        resumenRepository.fechasConResumen(),
        _fechaResumenSeleccionada,
        diaEditableResumen
    ) { conViajes, conResumen, seleccionada, editable ->
        (conViajes + conResumen +
            RutaLogic.fechaDia(System.currentTimeMillis()) +
            editable +
            seleccionada).sortedDescending()
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        listOf(RutaLogic.fechaDia(System.currentTimeMillis()))
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val resumenDelDia: StateFlow<ResumenRuta> = _fechaResumenSeleccionada
        .flatMapLatest { resumenRepository.porFecha(it) }
        .map { RutaLogic.resumen(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RutaLogic.resumen(null))

    private val _paquetesText = MutableStateFlow("")
    val paquetesText: StateFlow<String> = _paquetesText.asStateFlow()

    private val _paradasText = MutableStateFlow("")
    val paradasText: StateFlow<String> = _paradasText.asStateFlow()

    private val _puntosText = MutableStateFlow("")
    val puntosText: StateFlow<String> = _puntosText.asStateFlow()

    private val _primerPedidoEn = MutableStateFlow<Long?>(null)
    private val _ultimoPedidoEn = MutableStateFlow<Long?>(null)

    val puedeEditarResumen: StateFlow<Boolean> = combine(
        _fechaResumenSeleccionada,
        diaEditableResumen
    ) { seleccionada, editable ->
        seleccionada == RutaLogic.fechaDia(System.currentTimeMillis()) || seleccionada == editable
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _mensajeResumen = MutableStateFlow<String?>(null)
    val mensajeResumen: StateFlow<String?> = _mensajeResumen.asStateFlow()

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

    private val costoCombustible = combine(litrosEstimados, _precioText) { litros, precio ->
        CostCalculator.costo(litros, CostCalculator.parseDecimal(precio))
    }

    /** Costo total ("$ 52.500,00") o "—". */
    val costoTotalText: StateFlow<String> = costoCombustible
        .map { costo -> costo?.let { CostCalculator.formatCurrency(it) } ?: SIN_DATO }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SIN_DATO)

    private val pago = _pagoViajeText.map { CostCalculator.parseDecimal(it) }

    private val peajeTotal = combine(_peajeUsado, _peajeDireccion, prefs.tollPrice) { usado, direccion, tarifa ->
        if (usado) CostCalculator.peaje(tarifa, direccion) else null
    }

    /** Pago del viaje ("$ 80.000,00") o "—". */
    val pagoText: StateFlow<String> = pago
        .map { valor -> valor?.let { CostCalculator.formatCurrency(it) } ?: SIN_DATO }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SIN_DATO)

    /** Precio del peaje por cruce ("$ 1.500,00"). */
    val tarifaPeajeText: StateFlow<String> = prefs.tollPrice
        .map { CostCalculator.formatCurrency(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CostCalculator.formatCurrency(0.0))

    /** Peaje total del viaje ("$ 3.000,00") o "$ 0,00" si no se usó. */
    val peajeTotalText: StateFlow<String> = peajeTotal
        .map { valor -> valor?.let { CostCalculator.formatCurrency(it) } ?: CostCalculator.formatCurrency(0.0) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CostCalculator.formatCurrency(0.0))

    private val neto = combine(pago, costoCombustible, peajeTotal) { pago, costo, peaje ->
        CostCalculator.neto(pago, costo, peaje)
    }

    /** Lo que queda después de combustible y peaje ("$ 26.500,00") o "—". */
    val netoText: StateFlow<String> = neto
        .map { valor -> valor?.let { CostCalculator.formatCurrency(it) } ?: SIN_DATO }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SIN_DATO)

    val netoNegativo: StateFlow<Boolean> = neto
        .map { valor -> valor != null && valor < 0.0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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
    private var guardadoResumenJob: Job? = null

    init {
        viewModelScope.launch {
            repository.viajeAbierto().first()?.let { viaje ->
                viaje.kmInicial?.let { _kmInicialText.value = plainNumber(it) }
                viaje.kmFinal?.let { _kmFinalText.value = plainNumber(it) }
                viaje.rendimiento?.let { _rendimientoText.value = plainNumber(it) }
                viaje.precioPorLitro?.let { _precioText.value = plainNumber(it) }
                viaje.pagoViaje?.let { _pagoViajeText.value = plainNumber(it) }
                if (viaje.peajeDireccion != null || viaje.peajeMonto != null) {
                    _peajeUsado.value = true
                    viaje.peajeDireccion?.let { nombre ->
                        _peajeDireccion.value = runCatching { PeajeDireccion.valueOf(nombre) }
                            .getOrDefault(PeajeDireccion.IDA_VUELTA)
                    }
                }
                _fechaViaje.value = viaje.fechaInicio
            }
        }
        cargarResumen(_fechaResumenSeleccionada.value)
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
        if (tab == AppTab.RESUMEN && !_diaResumenElegidoManualmente.value) {
            irAHoy()
        }
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

    fun onPagoViajeChange(value: String) {
        _pagoViajeText.value = value
        programarGuardado()
    }

    fun onPeajeUsadoChange(usado: Boolean) {
        _peajeUsado.value = usado
        programarGuardado()
    }

    fun onPeajeDireccionChange(direccion: PeajeDireccion) {
        _peajeDireccion.value = direccion
        programarGuardado()
    }

    fun onPrecioPeajeChange(value: String) {
        _precioPeajeText.value = value
        CostCalculator.parseDecimal(value)?.let { prefs.setTollPrice(it) }
        programarGuardado()
    }

    /** Cambia la fecha del viaje en curso (para cargar viajes que olvidaron anotar ese día). */
    fun onFechaChange(millis: Long) {
        val diaAnterior = RutaLogic.fechaDia(_fechaViaje.value)
        val diaNuevo = RutaLogic.fechaDia(millis)
        val seguirSeleccion = _fechaResumenSeleccionada.value == diaAnterior

        _fechaViaje.value = millis
        if (seguirSeleccion) {
            _fechaResumenSeleccionada.value = diaNuevo
            _diaResumenElegidoManualmente.value =
                diaNuevo != RutaLogic.fechaDia(System.currentTimeMillis())
            cargarResumen(diaNuevo)
        }
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
                fechaInicio = _fechaViaje.value,
                pagoViaje = CostCalculator.parseDecimal(_pagoViajeText.value),
                peajeMonto = peajeActual(),
                peajeDireccion = if (_peajeUsado.value) _peajeDireccion.value.name else null
            )
            repository.nuevoViaje()
            _kmInicialText.value = ""
            _kmFinalText.value = ""
            _rendimientoText.value = plainNumber(prefs.kmPerLiter.value)
            _precioText.value = plainNumber(prefs.pricePerLiter.value)
            _pagoViajeText.value = ""
            _peajeUsado.value = false
            _peajeDireccion.value = PeajeDireccion.IDA_VUELTA
            _fechaViaje.value = System.currentTimeMillis()
            _diaResumenElegidoManualmente.value = false
            _fechaResumenSeleccionada.value = RutaLogic.fechaDia(_fechaViaje.value)
            _mensajeResumen.value = null
            cargarResumen(_fechaResumenSeleccionada.value)
        }
    }

    /** Borra un viaje ya cerrado del historial. */
    fun eliminarViaje(trip: TripEntity) {
        viewModelScope.launch { repository.eliminarViaje(trip) }
    }

    fun seleccionarDia(fecha: String) {
        _fechaResumenSeleccionada.value = fecha
        _diaResumenElegidoManualmente.value =
            fecha != RutaLogic.fechaDia(System.currentTimeMillis())
        cargarResumen(fecha)
    }

    fun irAHoy() {
        seleccionarDia(RutaLogic.fechaDia(System.currentTimeMillis()))
    }

    fun onPaquetesChange(value: String) {
        _paquetesText.value = value
        programarGuardadoResumen()
    }

    fun onParadasChange(value: String) {
        _paradasText.value = value
        programarGuardadoResumen()
    }

    fun onPuntosChange(value: String) {
        _puntosText.value = value
        programarGuardadoResumen()
    }

    fun marcarPrimerPedido() {
        if (!puedeEditarResumen.value) return mensajeSoloLectura()

        val ahora = System.currentTimeMillis()
        _primerPedidoEn.value = ahora
        if ((_ultimoPedidoEn.value ?: 0L) < ahora) {
            _ultimoPedidoEn.value = null
        }
        programarGuardadoResumen(0L)
    }

    fun marcarUltimoPedido() {
        if (!puedeEditarResumen.value) return mensajeSoloLectura()

        val primero = _primerPedidoEn.value
        if (primero == null) {
            _mensajeResumen.value = "Marcá primero el primer pedido."
            return
        }

        _ultimoPedidoEn.value = System.currentTimeMillis().coerceAtLeast(primero)
        programarGuardadoResumen(0L)
    }

    fun limpiarHorarios() {
        if (!puedeEditarResumen.value) return mensajeSoloLectura()

        _primerPedidoEn.value = null
        _ultimoPedidoEn.value = null
        programarGuardadoResumen(0L)
    }

    fun limpiarMensajeResumen() {
        _mensajeResumen.value = null
    }

    private fun cargarResumen(fecha: String) {
        guardadoResumenJob?.cancel()
        viewModelScope.launch {
            val guardado = resumenRepository.porFecha(fecha).first()
            _paquetesText.value = guardado?.paquetes?.toString() ?: ""
            _paradasText.value = guardado?.paradas?.toString() ?: ""
            _puntosText.value = guardado?.puntos?.toString() ?: ""
            _primerPedidoEn.value = guardado?.primerPedidoEn
            _ultimoPedidoEn.value = guardado?.ultimoPedidoEn
        }
    }

    private fun programarGuardadoResumen(retardo: Long = 400L) {
        if (!puedeEditarResumen.value) return mensajeSoloLectura()

        when (val validacion = RutaLogic.validarConteo(
            paquetesTexto = _paquetesText.value,
            paradasTexto = _paradasText.value,
            puntosTexto = _puntosText.value
        )) {
            is ValidacionConteo.Error -> _mensajeResumen.value = validacion.mensaje
            is ValidacionConteo.Ok -> Unit
        }

        guardadoResumenJob?.cancel()
        guardadoResumenJob = viewModelScope.launch {
            delay(retardo)
            resumenRepository.guardar(
                fecha = _fechaResumenSeleccionada.value,
                paquetes = numeroOpcional(_paquetesText.value),
                paradas = numeroOpcional(_paradasText.value),
                puntos = numeroOpcional(_puntosText.value),
                primerPedidoEn = _primerPedidoEn.value,
                ultimoPedidoEn = _ultimoPedidoEn.value
            )
        }
    }

    private fun mensajeSoloLectura() {
        _mensajeResumen.value = "Ese día es de solo lectura."
    }

    private fun numeroOpcional(texto: String): Int? =
        texto.trim().toIntOrNull()?.takeIf { it >= 0 }

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
                fechaInicio = _fechaViaje.value,
                pagoViaje = CostCalculator.parseDecimal(_pagoViajeText.value),
                peajeMonto = peajeActual(),
                peajeDireccion = if (_peajeUsado.value) _peajeDireccion.value.name else null
            )
        }
    }

    private fun peajeActual(): Double? =
        if (_peajeUsado.value) {
            CostCalculator.peaje(prefs.tollPrice.value, _peajeDireccion.value)
        } else {
            null
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
