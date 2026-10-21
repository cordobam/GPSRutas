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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
    onNuevoViaje: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Costo del viaje") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
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
                        text = "Todavía no cerraste ningún viaje. Cargá el km final y tocá \"Nuevo viaje\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(historial, key = { it.id }) { trip ->
                    TripHistoryCard(trip)
                }
            }

            item { Spacer(Modifier.height(18.dp)) }
        }
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
private fun TripHistoryCard(trip: TripEntity) {
    val km = CostCalculator.recorrido(trip.kmInicial, trip.kmFinal)
    val litros = CostCalculator.litros(km, trip.rendimiento)
    val costo = CostCalculator.costo(litros, trip.precioPorLitro)
    val fecha = trip.fechaFin ?: trip.fechaInicio

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
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
    }
}
