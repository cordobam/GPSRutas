package com.example.melirutas.ui


import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeliRutasApp() {
    val batchViewModel = rememberBatchViewModel()
    val mainViewModel = rememberMainViewModel()
    val repartoViewModel = rememberRepartoViewModel()

    val tab by mainViewModel.selectedTab.collectAsState()
    val state by batchViewModel.state.collectAsState()
    val captures by batchViewModel.captures.collectAsState()
    val kmInicialText by mainViewModel.kmInicialText.collectAsState()
    val kmFinalText by mainViewModel.kmFinalText.collectAsState()
    val rendimientoText by mainViewModel.rendimientoText.collectAsState()
    val precioText by mainViewModel.precioText.collectAsState()
    val precioNaftaActualText by mainViewModel.precioNaftaActualText.collectAsState()
    val rendimientoActualText by mainViewModel.rendimientoActualText.collectAsState()
    val kmRecorridosText by mainViewModel.kmRecorridosText.collectAsState()
    val litrosText by mainViewModel.litrosText.collectAsState()
    val costoTotalText by mainViewModel.costoTotalText.collectAsState()
    val pagoViajeText by mainViewModel.pagoViajeText.collectAsState()
    val peajeUsado by mainViewModel.peajeUsado.collectAsState()
    val peajeDireccion by mainViewModel.peajeDireccion.collectAsState()
    val pagoText by mainViewModel.pagoText.collectAsState()
    val peajeTotalText by mainViewModel.peajeTotalText.collectAsState()
    val netoText by mainViewModel.netoText.collectAsState()
    val netoNegativo by mainViewModel.netoNegativo.collectAsState()
    val historial by mainViewModel.historial.collectAsState()
    val costosModo by mainViewModel.costosModo.collectAsState()
    val fechaViaje by mainViewModel.fechaViaje.collectAsState()
    val todosCerrados by mainViewModel.todosCerrados.collectAsState()
    val semanaInicio by mainViewModel.semanaInicio.collectAsState()
    val viajesSeleccionados by mainViewModel.viajesSeleccionados.collectAsState()
    val fechaResumenSeleccionada by mainViewModel.fechaResumenSeleccionada.collectAsState()
    val diasDisponibles by mainViewModel.diasDisponibles.collectAsState()
    val resumenDelDia by mainViewModel.resumenDelDia.collectAsState()
    val paquetesText by mainViewModel.paquetesText.collectAsState()
    val paradasText by mainViewModel.paradasText.collectAsState()
    val puntosText by mainViewModel.puntosText.collectAsState()
    val puedeEditarResumen by mainViewModel.puedeEditarResumen.collectAsState()
    val mensajeResumen by mainViewModel.mensajeResumen.collectAsState()
    val precioPeajeText by mainViewModel.precioPeajeText.collectAsState()
    val tarifaPeajeText by mainViewModel.tarifaPeajeText.collectAsState()
    val perfilEstadisticas by mainViewModel.perfilEstadisticas.collectAsState()

    val hideBar = tab == AppTab.LOTE &&
        (state is BatchViewModel.BatchState.Capture || state is BatchViewModel.BatchState.Processing)

    var menuAbierto by remember { mutableStateOf(false) }

    BackHandler(enabled = menuAbierto) { menuAbierto = false }
    LaunchedEffect(hideBar) {
        if (hideBar) menuAbierto = false
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                if (!hideBar) {
                    TopAppBar(
                        title = { Text(tituloPantalla(tab, state)) },
                        colors = TopAppBarDefaults.mediumTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        actions = {
                            IconButton(onClick = { menuAbierto = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Abrir menú",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (!hideBar) {
                    AppBottomBar(selected = tab, onSelect = mainViewModel::selectTab)
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (tab) {
                    AppTab.LOTE -> LoteFlow(
                        state = state,
                        captures = captures,
                        viewModel = batchViewModel
                    )

                    AppTab.REPARTO -> RepartoScreen(viewModel = repartoViewModel)

                    AppTab.COSTOS -> CostsScreen(
                        modo = costosModo,
                        fechaViaje = fechaViaje,
                        kmInicialText = kmInicialText,
                        kmFinalText = kmFinalText,
                        pagoViajeText = pagoViajeText,
                        peajeUsado = peajeUsado,
                        peajeDireccion = peajeDireccion,
                        kmRecorridosText = kmRecorridosText,
                        litrosText = litrosText,
                        costoTotalText = costoTotalText,
                        tarifaPeajeText = tarifaPeajeText,
                        pagoText = pagoText,
                        peajeTotalText = peajeTotalText,
                        netoText = netoText,
                        netoNegativo = netoNegativo,
                        historial = historial,
                        todosCerrados = todosCerrados,
                        semanaInicio = semanaInicio,
                        viajesSeleccionados = viajesSeleccionados,
                        onModoChange = mainViewModel::setCostosModo,
                        onFechaChange = mainViewModel::onFechaChange,
                        onKmInicialChange = mainViewModel::onKmInicialChange,
                        onKmFinalChange = mainViewModel::onKmFinalChange,
                        onPagoViajeChange = mainViewModel::onPagoViajeChange,
                        onPeajeUsadoChange = mainViewModel::onPeajeUsadoChange,
                        onPeajeDireccionChange = mainViewModel::onPeajeDireccionChange,
                        onNuevoViaje = mainViewModel::nuevoViaje,
                        onEliminarViaje = mainViewModel::eliminarViaje,
                        onSemanaAnterior = mainViewModel::semanaAnterior,
                        onSemanaSiguiente = mainViewModel::semanaSiguiente,
                        onSemanaActual = mainViewModel::irASemanaActual,
                        onToggleViaje = mainViewModel::toggleViaje,
                        onMarcarPagados = mainViewModel::marcarSeleccionadosPagados
                    )

                    AppTab.RESUMEN -> CodesScreen(
                        fechaSeleccionada = fechaResumenSeleccionada,
                        diasDisponibles = diasDisponibles,
                        resumen = resumenDelDia,
                        paquetesText = paquetesText,
                        paradasText = paradasText,
                        puntosText = puntosText,
                        puedeEditar = puedeEditarResumen,
                        mensaje = mensajeResumen,
                        onSeleccionarDia = mainViewModel::seleccionarDia,
                        onIrAHoy = mainViewModel::irAHoy,
                        onPaquetesChange = mainViewModel::onPaquetesChange,
                        onParadasChange = mainViewModel::onParadasChange,
                        onPuntosChange = mainViewModel::onPuntosChange,
                        onMarcarPrimero = mainViewModel::marcarPrimerPedido,
                        onMarcarUltimo = mainViewModel::marcarUltimoPedido,
                        onLimpiarHorarios = mainViewModel::limpiarHorarios,
                        onLimpiarMensaje = mainViewModel::limpiarMensajeResumen
                    )

                    AppTab.CONFIG -> ConfigScreen(
                        precioNaftaText = precioText,
                        precioNaftaActualText = precioNaftaActualText,
                        rendimientoText = rendimientoText,
                        rendimientoActualText = rendimientoActualText,
                        precioPeajeText = precioPeajeText,
                        tarifaPeajeText = tarifaPeajeText,
                        onPrecioNaftaChange = mainViewModel::onPrecioChange,
                        onRendimientoChange = mainViewModel::onRendimientoChange,
                        onPrecioPeajeChange = mainViewModel::onPrecioPeajeChange
                    )

                    AppTab.PERFIL -> ProfileScreen(stats = perfilEstadisticas)
                }
            }
        }

        if (menuAbierto && !hideBar) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { menuAbierto = false }
            )
            MenuLateral(
                tab = tab,
                onPerfil = {
                    menuAbierto = false
                    mainViewModel.selectTab(AppTab.PERFIL)
                },
                onConfiguracion = {
                    menuAbierto = false
                    mainViewModel.selectTab(AppTab.CONFIG)
                },
                onCerrar = { menuAbierto = false },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

private fun tituloPantalla(
    tab: AppTab,
    state: BatchViewModel.BatchState
): String = when (tab) {
    AppTab.LOTE -> when (state) {
        is BatchViewModel.BatchState.Review -> "${state.stops.size} paradas"
        else -> "Meli Rutas"
    }
    AppTab.REPARTO -> "Reparto"
    AppTab.COSTOS -> "Costo del viaje"
    AppTab.RESUMEN -> "Resumen de ruta"
    AppTab.CONFIG -> "Configuración"
    AppTab.PERFIL -> "Perfil"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuLateral(
    tab: AppTab,
    onPerfil: () -> Unit,
    onConfiguracion: () -> Unit,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.82f)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Menú",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCerrar) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cerrar menú",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        NavigationDrawerItem(
            label = { Text("Perfil") },
            selected = tab == AppTab.PERFIL,
            onClick = onPerfil,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null
                )
            }
        )
        Spacer(Modifier.height(4.dp))
        NavigationDrawerItem(
            label = { Text("Configuración") },
            selected = tab == AppTab.CONFIG,
            onClick = onConfiguracion,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null
                )
            }
        )
    }
}

/** Flujo del lote (fotos → OCR → revisión de paradas) dentro de la pestaña "Lote". */
@Composable
private fun LoteFlow(
    state: BatchViewModel.BatchState,
    captures: List<Uri>,
    viewModel: BatchViewModel
) {
    when (state) {
        is BatchViewModel.BatchState.Home ->
            HomeScreen(
                onPickPhotos = viewModel::procesarFotos,
                onOpenCamera = viewModel::abrirCaptura
            )

        is BatchViewModel.BatchState.Capture ->
            CameraScreen(
                captures = captures,
                onCaptureSaved = viewModel::addCaptura,
                onProcesar = viewModel::procesarCapturas,
                onCancel = viewModel::cancelarCaptura
            )

        is BatchViewModel.BatchState.Processing ->
            ProcessingScreen(current = state.current, total = state.total)

        is BatchViewModel.BatchState.Review ->
            ReviewScreen(
                stops = state.stops,
                onUpdateStop = viewModel::updateStop,
                onRemoveStop = viewModel::removeStop,
                onAddStop = viewModel::addStop,
                onNuevoLote = viewModel::nuevoLote
            )

        is BatchViewModel.BatchState.Error ->
            ErrorScreen(message = state.message, onVolver = viewModel::nuevoLote)
    }
}

@Composable
private fun rememberBatchViewModel(): BatchViewModel {
    val activity = LocalContext.current as ComponentActivity
    return remember(activity) {
        ViewModelProvider(activity)[BatchViewModel::class.java]
    }
}

@Composable
private fun rememberMainViewModel(): MainViewModel {
    val activity = LocalContext.current as ComponentActivity
    return remember(activity) {
        ViewModelProvider(activity)[MainViewModel::class.java]
    }
}

@Composable
private fun rememberRepartoViewModel(): RepartoViewModel {
    val activity = LocalContext.current as ComponentActivity
    return remember(activity) {
        ViewModelProvider(activity)[RepartoViewModel::class.java]
    }
}

@Composable
private fun ErrorScreen(message: String, onVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onVolver) {
            Text("Volver al inicio")
        }
    }
}
