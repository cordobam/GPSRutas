package com.example.melirutas.ui


import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider

@Composable
fun MeliRutasApp() {
    val batchViewModel = rememberBatchViewModel()
    val mainViewModel = rememberMainViewModel()

    val tab by mainViewModel.selectedTab.collectAsState()
    val state by batchViewModel.state.collectAsState()
    val captures by batchViewModel.captures.collectAsState()
    val kmInicialText by mainViewModel.kmInicialText.collectAsState()
    val kmFinalText by mainViewModel.kmFinalText.collectAsState()
    val rendimientoText by mainViewModel.rendimientoText.collectAsState()
    val precioText by mainViewModel.precioText.collectAsState()
    val kmRecorridosText by mainViewModel.kmRecorridosText.collectAsState()
    val litrosText by mainViewModel.litrosText.collectAsState()
    val costoTotalText by mainViewModel.costoTotalText.collectAsState()
    val historial by mainViewModel.historial.collectAsState()
    val codes by mainViewModel.codes.collectAsState()

    val hideBar = tab == AppTab.LOTE &&
        (state is BatchViewModel.BatchState.Capture || state is BatchViewModel.BatchState.Processing)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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

                AppTab.COSTOS -> CostsScreen(
                    kmInicialText = kmInicialText,
                    kmFinalText = kmFinalText,
                    rendimientoText = rendimientoText,
                    precioText = precioText,
                    kmRecorridosText = kmRecorridosText,
                    litrosText = litrosText,
                    costoTotalText = costoTotalText,
                    historial = historial,
                    onKmInicialChange = mainViewModel::onKmInicialChange,
                    onKmFinalChange = mainViewModel::onKmFinalChange,
                    onRendimientoChange = mainViewModel::onRendimientoChange,
                    onPrecioChange = mainViewModel::onPrecioChange,
                    onNuevoViaje = mainViewModel::nuevoViaje,
                    onEliminarViaje = mainViewModel::eliminarViaje
                )

                AppTab.CODIGOS -> CodesScreen(
                    codes = codes,
                    onAdd = mainViewModel::addCode,
                    onRemove = mainViewModel::removeCode,
                    onClear = mainViewModel::clearCodes
                )
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorScreen(message: String, onVolver: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meli Rutas") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFF0D47A1),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
}
