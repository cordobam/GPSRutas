package com.example.melirutas.ui

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.melirutas.codigos.CodigosLogic
import com.example.melirutas.cost.CostCalculator
import com.example.melirutas.data.db.DeliveryCodeEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodesScreen(
    fechaSeleccionada: String,
    diasDisponibles: List<String>,
    codigos: List<DeliveryCodeEntity>,
    puedeEditar: Boolean,
    mensaje: String?,
    onSeleccionarDia: (String) -> Unit,
    onGenerar: (String, String) -> String?,
    onMarcar: (Int, Boolean) -> Unit,
    onBorrarDia: () -> Unit,
    onLimpiarMensaje: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var menuDiaAbierto by remember { mutableStateOf(false) }
    var desde by rememberSaveable { mutableStateOf("") }
    var hasta by rememberSaveable { mutableStateOf("") }
    var errorRango by remember { mutableStateOf<String?>(null) }
    var confirmarBorrar by remember { mutableStateOf(false) }

    val hoy = CodigosLogic.fechaDia(System.currentTimeMillis())
    val resumen = remember(codigos, puedeEditar) {
        CodigosLogic.resumen(codigos, System.currentTimeMillis(), puedeEditar)
    }

    LaunchedEffect(mensaje) {
        val texto = mensaje ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(texto)
        onLimpiarMensaje()
    }

    fun copiar(texto: String, aviso: String) {
        if (clipboard == null) {
            scope.launch { snackbarHostState.showSnackbar("No se pudo copiar.") }
            return
        }
        clipboard.setPrimaryClip(ClipData.newPlainText("Códigos de entrega", texto))
        scope.launch { snackbarHostState.showSnackbar(aviso) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Códigos de entrega") },
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
                                    Text(if (puedeEditar) "Día de trabajo" else "Solo lectura")
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
                                            desde = ""
                                            hasta = ""
                                            errorRango = null
                                            menuDiaAbierto = false
                                        }
                                    )
                                }
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
                        Text(
                            text = "Resumen del día",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Metrica(
                                valor = resumen.total.toString(),
                                etiqueta = "Total",
                                modifier = Modifier.weight(1f)
                            )
                            Metrica(
                                valor = resumen.entregados.toString(),
                                etiqueta = "Entregados",
                                modifier = Modifier.weight(1f),
                                destacado = true
                            )
                            Metrica(
                                valor = resumen.pendientes.toString(),
                                etiqueta = "Pendientes",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Metrica(
                                valor = resumen.ultimaHora.toString(),
                                etiqueta = "Última hora",
                                modifier = Modifier.weight(1f)
                            )
                            Metrica(
                                valor = resumen.promedioPorHora?.let {
                                    "${CostCalculator.formatNumber(it, 1)}/h"
                                } ?: "—",
                                etiqueta = "Promedio",
                                modifier = Modifier.weight(1f)
                            )
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
                            text = "Este día no es el día de trabajo actual. " +
                                "Podés consultar y copiar los códigos, pero no modificarlos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            if (puedeEditar) {
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
                                text = "Generar códigos",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = desde,
                                    onValueChange = {
                                        desde = it
                                        errorRango = null
                                    },
                                    label = { Text("Desde") },
                                    singleLine = true,
                                    isError = errorRango != null,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = hasta,
                                    onValueChange = {
                                        hasta = it
                                        errorRango = null
                                    },
                                    label = { Text("Hasta") },
                                    singleLine = true,
                                    isError = errorRango != null,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            errorRango?.let { mensajeError ->
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = mensajeError,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            FilledTonalButton(
                                onClick = {
                                    val error = onGenerar(desde, hasta)
                                    if (error == null) {
                                        desde = ""
                                        hasta = ""
                                        errorRango = null
                                    } else {
                                        errorRango = error
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Agregar rango")
                            }
                        }
                    }
                }
            }

            if (codigos.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                copiar(
                                    codigos.joinToString("\n") { it.codigo.toString() },
                                    "Códigos copiados."
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text("Copiar todos")
                        }
                        if (puedeEditar) {
                            OutlinedButton(
                                onClick = { confirmarBorrar = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                                Spacer(Modifier.size(8.dp))
                                Text("Borrar día")
                            }
                        }
                    }
                }
            }

            if (codigos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (puedeEditar) {
                                "No hay códigos para este día.\nGenerá un rango para empezar."
                            } else {
                                "Este día no tiene códigos guardados."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(codigos, key = { it.id }) { codigo ->
                    CodigoRow(
                        codigo = codigo,
                        puedeEditar = puedeEditar,
                        onMarcar = { entregado -> onMarcar(codigo.codigo, entregado) },
                        onCopiar = {
                            copiar(codigo.codigo.toString(), "Código copiado.")
                        }
                    )
                }
            }
        }
    }

    if (confirmarBorrar) {
        AlertDialog(
            onDismissRequest = { confirmarBorrar = false },
            title = { Text("Borrar códigos del día") },
            text = {
                Text(
                    "Se van a borrar los ${codigos.size} códigos del " +
                        CodigosLogic.formatearDia(fechaSeleccionada) +
                        ". Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onBorrarDia()
                        confirmarBorrar = false
                    }
                ) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
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

@Composable
private fun CodigoRow(
    codigo: DeliveryCodeEntity,
    puedeEditar: Boolean,
    onMarcar: (Boolean) -> Unit,
    onCopiar: () -> Unit
) {
    val entregado = codigo.entregadoEn != null
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = entregado,
                onCheckedChange = { onMarcar(it) },
                enabled = puedeEditar
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = codigo.codigo.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = codigo.entregadoEn?.let {
                        "Entregado ${CodigosLogic.formatearFechaHora(it)}"
                    } ?: "Pendiente",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (entregado) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            IconButton(onClick = onCopiar) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = "Copiar código",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun etiquetaDia(fecha: String, hoy: String): String =
    if (fecha == hoy) {
        "Hoy · ${CodigosLogic.formatearDia(fecha)}"
    } else {
        CodigosLogic.formatearDia(fecha)
    }
