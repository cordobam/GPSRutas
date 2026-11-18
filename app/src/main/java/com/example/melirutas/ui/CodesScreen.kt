package com.example.melirutas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.melirutas.codigos.RutaLogic
import com.example.melirutas.codigos.ResumenRuta
import com.example.melirutas.cost.CostCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodesScreen(
    fechaSeleccionada: String,
    diasDisponibles: List<String>,
    resumen: ResumenRuta,
    paquetesText: String,
    paradasText: String,
    puntosText: String,
    puedeEditar: Boolean,
    mensaje: String?,
    onSeleccionarDia: (String) -> Unit,
    onIrAHoy: () -> Unit,
    onPaquetesChange: (String) -> Unit,
    onParadasChange: (String) -> Unit,
    onPuntosChange: (String) -> Unit,
    onMarcarPrimero: () -> Unit,
    onMarcarUltimo: () -> Unit,
    onLimpiarHorarios: () -> Unit,
    onLimpiarMensaje: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var menuDiaAbierto by rememberSaveable { mutableStateOf(false) }
    val hoy = RutaLogic.fechaDia(System.currentTimeMillis())

    LaunchedEffect(mensaje) {
        val texto = mensaje ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(texto)
        onLimpiarMensaje()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de ruta") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Día",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = menuDiaAbierto,
                            onExpandedChange = { menuDiaAbierto = it }
                        ) {
                            OutlinedTextField(
                                value = etiquetaDia(fechaSeleccionada, hoy),
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = { Text("Fecha") },
                                leadingIcon = {
                                    Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = menuDiaAbierto
                                    )
                                },
                                supportingText = {
                                    Text(if (puedeEditar) "Día editable" else "Solo lectura")
                                },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = menuDiaAbierto,
                                onDismissRequest = { menuDiaAbierto = false }
                            ) {
                                diasDisponibles.forEach { dia ->
                                    DropdownMenuItem(
                                        text = { Text(etiquetaDia(dia, hoy)) },
                                        onClick = {
                                            onSeleccionarDia(dia)
                                            menuDiaAbierto = false
                                        }
                                    )
                                }
                            }
                        }
                        if (fechaSeleccionada != hoy) {
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = onIrAHoy) {
                                Text("Volver a hoy")
                            }
                        }
                    }
                }
            }

            if (!puedeEditar) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "Este día es de solo lectura. Los datos se cargan automáticamente.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SeccionTitulo("Conteo")
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = paquetesText,
                            onValueChange = onPaquetesChange,
                            enabled = puedeEditar,
                            label = { Text("Paquetes") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = paradasText,
                            onValueChange = onParadasChange,
                            enabled = puedeEditar,
                            label = { Text("Paradas") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = puntosText,
                            onValueChange = onPuntosChange,
                            enabled = puedeEditar,
                            label = { Text("Puntos") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SeccionTitulo("Horarios")
                        Spacer(Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onMarcarPrimero,
                                enabled = puedeEditar,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (resumen.primera == null) {
                                        "Marcar primero"
                                    } else {
                                        "Actualizar primero"
                                    }
                                )
                            }
                            FilledTonalButton(
                                onClick = onMarcarUltimo,
                                enabled = puedeEditar && resumen.primera != null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (resumen.ultima == null) {
                                        "Marcar último"
                                    } else {
                                        "Actualizar último"
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Primero: ${resumen.primera?.let { RutaLogic.formatearFechaHora(it) } ?: "—"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Último: ${resumen.ultima?.let { RutaLogic.formatearFechaHora(it) } ?: "—"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (resumen.primera != null || resumen.ultima != null) {
                            TextButton(
                                onClick = onLimpiarHorarios,
                                enabled = puedeEditar
                            ) {
                                Text("Limpiar horarios")
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SeccionTitulo("Resultado")
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Metrica(
                                valor = resumen.paquetes?.toString() ?: "—",
                                etiqueta = "Paquetes",
                                modifier = Modifier.weight(1f)
                            )
                            Metrica(
                                valor = resumen.paradas?.toString() ?: "—",
                                etiqueta = "Paradas",
                                modifier = Modifier.weight(1f)
                            )
                            Metrica(
                                valor = resumen.puntos?.toString() ?: "—",
                                etiqueta = "Puntos",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Metrica(
                                valor = resumen.duracionMillis
                                    ?.let { RutaLogic.formatearDuracion(it) } ?: "—",
                                etiqueta = "Duración",
                                modifier = Modifier.weight(1f)
                            )
                            Metrica(
                                valor = resumen.paquetesPorHora
                                    ?.let { "${CostCalculator.formatNumber(it, 1)}/h" } ?: "—",
                                etiqueta = "Paquetes/hora",
                                modifier = Modifier.weight(1f),
                                destacado = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Metrica(
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier,
    destacado: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (destacado) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun etiquetaDia(fecha: String, hoy: String): String =
    if (fecha == hoy) {
        "Hoy · ${RutaLogic.formatearDia(fecha)}"
    } else {
        RutaLogic.formatearDia(fecha)
    }
