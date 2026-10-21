package com.example.melirutas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.melirutas.cost.CostCalculator
import com.example.melirutas.data.db.TripEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FECHA_FORMAT = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostsScreen(
    kmInicialText: String,
    kmFinalText: String,
    rendimientoText: String,
    precioText: String,
    kmRecorridosText: String,
    litrosText: String,
    costoTotalText: String,
    historial: List<TripEntity>,
    onKmInicialChange: (String) -> Unit,
    onKmFinalChange: (String) -> Unit,
    onRendimientoChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onNuevoViaje: () -> Unit,
    onEliminarViaje: (TripEntity) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var notice by remember { mutableStateOf<String?>(null) }
    var confirmarBorrarId by rememberSaveable { mutableStateOf(0L) }

    val viajePorBorrar = historial.firstOrNull { it.id == confirmarBorrarId }

    LaunchedEffect(notice) {
        val message = notice ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        notice = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Costo del viaje") },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item {
                OutlinedTextField(
                    value = kmInicialText,
                    onValueChange = onKmInicialChange,
                    label = { Text("Km inicial (odómetro)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = kmFinalText,
                    onValueChange = onKmFinalChange,
                    label = { Text("Km final (odómetro)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = rendimientoText,
                    onValueChange = onRendimientoChange,
                    label = { Text("Rendimiento del auto") },
                    suffix = { Text("km/l") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = precioText,
                    onValueChange = onPrecioChange,
                    label = { Text("Precio de la nafta") },
                    prefix = { Text("$ ") },
                    suffix = { Text("por litro") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { ResultsCard(kmRecorridosText, litrosText, costoTotalText) }

            item {
                FilledTonalButton(
                    onClick = onNuevoViaje,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nuevo viaje", style = MaterialTheme.typography.bodyLarge)
                }
            }

            item {
                Text(
                    text = "Historial de viajes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (historial.isEmpty()) {
                item {
                    Text(
                        text = "No hay viajes cargados en los últimos 7 días. Terminá un viaje tocando \"Nuevo viaje\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(historial, key = { it.id }) { trip ->
                    TripHistoryCard(
                        trip = trip,
                        onDelete = { confirmarBorrarId = trip.id }
                    )
                }
            }

            item { Spacer(Modifier.height(18.dp)) }
        }
    }

    if (viajePorBorrar != null) {
        AlertDialog(
            onDismissRequest = { confirmarBorrarId = 0L },
            title = { Text("Borrar viaje") },
            text = {
                Text(
                    "Se va a borrar el viaje del " +
                        FECHA_FORMAT.format(Date(viajePorBorrar.fechaFin ?: viajePorBorrar.fechaInicio)) +
                        ". Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEliminarViaje(viajePorBorrar)
                        confirmarBorrarId = 0L
                        notice = "Viaje borrado."
                    }
                ) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrarId = 0L }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ResultsCard(kmRecorridos: String, litros: String, costoTotal: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            ResultRow("Km recorridos", kmRecorridos)
            Spacer(Modifier.height(8.dp))
            ResultRow("Litros a cargar", litros)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Costo total del viaje",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = costoTotal,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun TripHistoryCard(trip: TripEntity, onDelete: () -> Unit) {
    val km = CostCalculator.recorrido(trip.kmInicial, trip.kmFinal)
    val litros = CostCalculator.litros(km, trip.rendimiento)
    val costo = CostCalculator.costo(litros, trip.precioPorLitro)
    val fecha = trip.fechaFin ?: trip.fechaInicio

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = FECHA_FORMAT.format(Date(fecha)),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Km: " + (km?.let { CostCalculator.formatNumber(it, 1) } ?: "—"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Litros: " + (litros?.let { CostCalculator.formatNumber(it, 2) } ?: "—"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Costo: " + (costo?.let { CostCalculator.formatCurrency(it) } ?: "—"),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar viaje",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
