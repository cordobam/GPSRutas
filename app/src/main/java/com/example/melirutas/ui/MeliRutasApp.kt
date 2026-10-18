package com.example.melirutas.ui


import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

/** Navegación por estado: Home → Processing → Review (o Error). */
@Composable
fun MeliRutasApp() {
    val viewModel = rememberBatchViewModel()
    val state by viewModel.state.collectAsState()
    val captures by viewModel.captures.collectAsState()

    when (val current = state) {
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
            ProcessingScreen(current = current.current, total = current.total)

        is BatchViewModel.BatchState.Review ->
            ReviewScreen(
                stops = current.stops,
                onUpdateStop = viewModel::updateStop,
                onRemoveStop = viewModel::removeStop,
                onAddStop = viewModel::addStop,
                onNuevoLote = viewModel::nuevoLote
            )

        is BatchViewModel.BatchState.Error ->
            ErrorScreen(message = current.message, onVolver = viewModel::nuevoLote)
    }
}

@Composable
private fun rememberBatchViewModel(): BatchViewModel {
    val activity = LocalContext.current as ComponentActivity
    return remember(activity) {
        ViewModelProvider(activity)[BatchViewModel::class.java]
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
