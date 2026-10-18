package com.example.melirutas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.melirutas.route.MapsIntent
import com.example.melirutas.route.RouteUrlBuilder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    stops: List<BatchViewModel.StopUi>,
    onUpdateStop: (Int, BatchViewModel.StopUi) -> Unit,
    onRemoveStop: (Int) -> Unit,
    onAddStop: () -> Unit,
    onNuevoLote: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var notice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(notice) {
        val message = notice ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        notice = null
    }

    fun openMaps() {
        val withAddress = stops.filter { it.street.isNotBlank() }
        val skipped = stops.size - withAddress.size // <-- CORREGIDO AQUÍ (- en lugar de salto de línea)

        val routeStops = withAddress.map {
            RouteUrlBuilder.RouteStop(street = it.street, city = it.city, postalCode = it.postalCode)
        }
        val result = RouteUrlBuilder.build(routeStops)
        notice = when (result) {
            is RouteUrlBuilder.Result.Success -> {
                val openedInApp = MapsIntent.open(context, result.url)
                val skipMessage = if (skipped > 0) {
                    "$skipped paradas sin dirección quedaron fuera de la ruta."
                } else {
                    ""
                }
                if (openedInApp) {
                    "Ruta abierta en Google Maps. $skipMessage".trim()
                } else {
                    "Google Maps no está instalada: se abrió en el navegador (admite solo 3 paradas). $skipMessage"
                }
            }

            is RouteUrlBuilder.Result.Error -> when (result.reason) {
                RouteUrlBuilder.Result.Reason.NO_STOPS ->
                    "Ninguna parada tiene dirección cargada."
                RouteUrlBuilder.Result.Reason.TOO_MANY_STOPS ->
                    "Hay más de ${RouteUrlBuilder.MAX_STOPS} paradas con dirección; sacá algunas o hacé dos rutas."
                RouteUrlBuilder.Result.Reason.URL_TOO_LONG ->
                    "Las direcciones son demasiado largas para una sola ruta."
            }
        }
    }

    val stopsWithAddress = stops.count { it.street.isNotBlank() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${stops.size} paradas") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            items(stops.size) { index ->
                StopEditor(
                    stop = stops[index],
                    index = index,
                    onUpdate = { onUpdateStop(index, it) },
                    onRemove = { onRemoveStop(index) }
                )
            }
            item {
                Spacer(Modifier.height(14.dp))
                FilledTonalButton(
                    onClick = { openMaps() },
                    enabled = stopsWithAddress > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Place, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = if (stopsWithAddress > 0) {
                            "Abrir en Google Maps ($stopsWithAddress)"
                        } else {
                            "Sin direcciones para la ruta"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = onAddStop,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Agregar parada a mano",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onNuevoLote,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nuevo lote")
                }
                Spacer(Modifier.height(26.dp))
            }
        }
    }
}

@Composable
private fun StopEditor(
    stop: BatchViewModel.StopUi,
    index: Int,
    onUpdate: (BatchViewModel.StopUi) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PhotoThumbnail(
                    uri = stop.photoUri,
                    modifier = Modifier.size(58.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "PARADA ${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Confianza OCR: ${stop.meanConfidence}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Borrar parada ${index + 1}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = stop.street,
                onValueChange = { onUpdate(stop.copy(street = it)) },
                label = { Text("Dirección") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = stop.city,
                onValueChange = { onUpdate(stop.copy(city = it)) },
                label = { Text("Localidad") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row {
                OutlinedTextField(
                    value = stop.postalCode.orEmpty(),
                    onValueChange = { onUpdate(stop.copy(postalCode = it.ifBlank { null })) },
                    label = { Text("Código postal") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = stop.trackId.orEmpty(),
                    onValueChange = { onUpdate(stop.copy(trackId = it.ifBlank { null })) },
                    label = { Text("Track") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
