package com.example.melirutas.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.melirutas.reparto.RepartoLogic
import com.example.melirutas.reparto.ZonaApp

@Composable
fun ConfigScreen(
    precioNaftaText: String,
    precioNaftaActualText: String,
    rendimientoText: String,
    rendimientoActualText: String,
    precioPeajeText: String,
    tarifaPeajeText: String,
    zonasApp: List<ZonaApp>,
    nuevaZonaAppText: String,
    onPrecioNaftaChange: (String) -> Unit,
    onRendimientoChange: (String) -> Unit,
    onPrecioPeajeChange: (String) -> Unit,
    onNuevaZonaAppChange: (String) -> Unit,
    onAgregarZonaApp: () -> String?,
    onEditarZonaApp: (String, String) -> String?,
    onEliminarZonaApp: (String) -> String?
) {
    var errorNuevaZonaApp by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Combustible",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = precioNaftaText,
                    onValueChange = onPrecioNaftaChange,
                    label = { Text("Precio de la nafta") },
                    prefix = { Text("$ ") },
                    suffix = { Text("por litro") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Valor actual: $precioNaftaActualText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Se usa para calcular el costo de combustible.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = rendimientoText,
                    onValueChange = onRendimientoChange,
                    label = { Text("Rendimiento del auto") },
                    suffix = { Text("km/l") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Valor actual: $rendimientoActualText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Se usa para estimar los litros del viaje.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Peaje",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = precioPeajeText,
                    onValueChange = onPrecioPeajeChange,
                    label = { Text("Precio por cruce") },
                    prefix = { Text("$ ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Valor actual: $tarifaPeajeText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "En viajes de ida y vuelta se multiplica por 2.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Zonas de la app",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Se asignan en Reparto. Máximo ${RepartoLogic.MAX_ZONAS_APP}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nuevaZonaAppText,
                    onValueChange = {
                        onNuevaZonaAppChange(it)
                        errorNuevaZonaApp = null
                    },
                    label = { Text("Nueva zona de la app") },
                    singleLine = true,
                    isError = errorNuevaZonaApp != null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = { errorNuevaZonaApp = onAgregarZonaApp() },
                    enabled = zonasApp.size < RepartoLogic.MAX_ZONAS_APP &&
                        nuevaZonaAppText.isNotBlank()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Agregar")
                }
                if (zonasApp.isEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Todavía no hay zonas de la app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(Modifier.height(12.dp))
                    zonasApp.forEach { zona ->
                        ZonaAppItem(
                            zona = zona,
                            onEditar = { nombre -> onEditarZonaApp(zona.id, nombre) },
                            onEliminar = { onEliminarZonaApp(zona.id) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                errorNuevaZonaApp?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ZonaAppItem(
    zona: ZonaApp,
    onEditar: (String) -> String?,
    onEliminar: () -> String?
) {
    var texto by remember(zona.id, zona.nombre) { mutableStateOf(zona.nombre) }
    var error by remember(zona.id, zona.nombre) { mutableStateOf<String?>(null) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            OutlinedTextField(
                value = texto,
                onValueChange = {
                    texto = it
                    error = onEditar(it)
                },
                label = { Text("Nombre") },
                singleLine = true,
                isError = error != null,
                modifier = Modifier.fillMaxWidth()
            )
            error?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = { error = onEliminar() }) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Eliminar ${zona.nombre}"
            )
        }
    }
}
