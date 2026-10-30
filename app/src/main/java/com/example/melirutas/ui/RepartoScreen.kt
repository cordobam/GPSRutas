package com.example.melirutas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.collectAsState
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
import com.example.melirutas.reparto.ReglaRango
import com.example.melirutas.reparto.RepartoLogic
import com.example.melirutas.reparto.ResolucionTrack
import com.example.melirutas.reparto.ValidacionTrack
import com.example.melirutas.reparto.ZonaReparto
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepartoScreen(viewModel: RepartoViewModel) {
    val reparto by viewModel.reparto.collectAsState()
    val zonas = reparto?.zonas.orEmpty()

    val snackbarHostState = remember { SnackbarHostState() }
    var notice by remember { mutableStateOf<String?>(null) }
    var consulta by rememberSaveable { mutableStateOf("") }
    var dialogoZona by remember { mutableStateOf<DialogoZona?>(null) }
    var zonaRango by remember { mutableStateOf<ZonaReparto?>(null) }
    var zonaTrack by remember { mutableStateOf<ZonaReparto?>(null) }
    var zonaEliminar by remember { mutableStateOf<ZonaReparto?>(null) }
    var confirmarLimpiar by remember { mutableStateOf(false) }

    LaunchedEffect(notice) {
        val mensaje = notice ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(mensaje)
        notice = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reparto")
                        reparto?.fecha?.let {
                            Text(
                                text = formatearFecha(it),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
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
                BuscadorCard(
                    consulta = consulta,
                    onConsultaChange = { consulta = it },
                    zonas = zonas
                )
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(onClick = { dialogoZona = DialogoZona.Nueva }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Agregar zona")
                    }
                    Spacer(Modifier.weight(1f))
                    if (zonas.isNotEmpty()) {
                        OutlinedButton(onClick = { confirmarLimpiar = true }) {
                            Text("Limpiar día")
                        }
                    }
                }
            }

            if (zonas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Todavía no hay zonas.\nAgregá una para empezar a repartir.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(zonas, key = { it.id }) { zona ->
                    ZonaCard(
                        zona = zona,
                        onEditar = { dialogoZona = DialogoZona.Editar(zona) },
                        onEliminar = { zonaEliminar = zona },
                        onAgregarRango = { zonaRango = zona },
                        onAgregarTrack = { zonaTrack = zona },
                        onQuitarRango = { viewModel.eliminarRango(zona.id, it) },
                        onQuitarTrack = { viewModel.eliminarTrack(zona.id, it) }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    dialogoZona?.let { dialogo ->
        ZonaDialog(
            dialogo = dialogo,
            onGuardar = { nombre ->
                when (dialogo) {
                    is DialogoZona.Nueva -> viewModel.agregarZona(nombre)
                    is DialogoZona.Editar -> viewModel.editarZona(dialogo.zona.id, nombre)
                }
            },
            onDismiss = { dialogoZona = null },
            onExito = { mensaje ->
                dialogoZona = null
                notice = mensaje
            }
        )
    }

    zonaRango?.let { zona ->
        RangoDialog(
            zona = zona,
            onGuardar = { desde, hasta -> viewModel.agregarRango(zona.id, desde, hasta) },
            onDismiss = { zonaRango = null },
            onExito = {
                zonaRango = null
                notice = "Rango agregado."
            }
        )
    }

    zonaTrack?.let { zona ->
        TrackDialog(
            zona = zona,
            onGuardar = { trackId -> viewModel.agregarTrack(zona.id, trackId) },
            onDismiss = { zonaTrack = null },
            onExito = { mensaje ->
                zonaTrack = null
                notice = mensaje
            }
        )
    }

    zonaEliminar?.let { zona ->
        AlertDialog(
            onDismissRequest = { zonaEliminar = null },
            title = { Text("Eliminar zona") },
            text = {
                Text(
                    "Se va a eliminar \"${zona.nombre}\" con sus rangos y tracks. " +
                        "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarZona(zona.id)
                        zonaEliminar = null
                        notice = "Zona eliminada."
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { zonaEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    if (confirmarLimpiar) {
        AlertDialog(
            onDismissRequest = { confirmarLimpiar = false },
            title = { Text("Limpiar reparto del día") },
            text = {
                Text(
                    "Se van a borrar todas las zonas, rangos y tracks de hoy. " +
                        "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.limpiarDia()
                        confirmarLimpiar = false
                        notice = "Reparto del día limpiado."
                    }
                ) {
                    Text("Limpiar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimpiar = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun BuscadorCard(
    consulta: String,
    onConsultaChange: (String) -> Unit,
    zonas: List<ZonaReparto>
) {
    val trackId = RepartoLogic.parseNumero(consulta)
    val resolucion = remember(trackId, zonas) {
        trackId?.let { RepartoLogic.resolver(it, zonas) }
    }

    val mensaje = when {
        consulta.isBlank() -> "Escribí un número de track para ver dónde va."
        trackId == null -> "El track debe ser numérico."
        else -> when (resolucion) {
            is ResolucionTrack.Asignado ->
                if (resolucion.esExcepcion) {
                    "Track $trackId → ${resolucion.zona.nombre} (excepción)."
                } else {
                    "Track $trackId → ${resolucion.zona.nombre}."
                }

            is ResolucionTrack.SinAsignar -> "Track $trackId → sin asignar."
            null -> "Track $trackId → sin asignar."
        }
    }

    val color = when {
        consulta.isBlank() -> MaterialTheme.colorScheme.onSurfaceVariant
        trackId == null -> MaterialTheme.colorScheme.error
        resolucion is ResolucionTrack.SinAsignar -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = "¿Dónde va un track?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = consulta,
                onValueChange = onConsultaChange,
                label = { Text("Número de track") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ZonaCard(
    zona: ZonaReparto,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onAgregarRango: () -> Unit,
    onAgregarTrack: () -> Unit,
    onQuitarRango: (ReglaRango) -> Unit,
    onQuitarTrack: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = zona.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEditar) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar zona",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar zona",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (zona.rangos.isEmpty() && zona.trackIds.isEmpty()) {
                Text(
                    text = "Sin rangos ni tracks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                if (zona.rangos.isNotEmpty()) {
                    Text(
                        text = "RANGOS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        zona.rangos.forEach { rango ->
                            AssistChip(
                                onClick = { onQuitarRango(rango) },
                                label = { Text("${rango.desde}–${rango.hasta}") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription =
                                            "Quitar rango ${rango.desde} a ${rango.hasta}",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                if (zona.trackIds.isNotEmpty()) {
                    if (zona.rangos.isNotEmpty()) Spacer(Modifier.height(10.dp))
                    Text(
                        text = "TRACKS PUNTUALES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        zona.trackIds.forEach { trackId ->
                            AssistChip(
                                onClick = { onQuitarTrack(trackId) },
                                label = { Text(trackId.toString()) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Quitar track $trackId",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row {
                FilledTonalButton(
                    onClick = onAgregarRango,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Agregar rango")
                }
                Spacer(Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onAgregarTrack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Agregar track")
                }
            }
        }
    }
}

@Composable
private fun ZonaDialog(
    dialogo: DialogoZona,
    onGuardar: (String) -> String?,
    onDismiss: () -> Unit,
    onExito: (String) -> Unit
) {
    val esEdicion = dialogo is DialogoZona.Editar
    val nombreInicial = (dialogo as? DialogoZona.Editar)?.zona?.nombre.orEmpty()
    var nombre by remember(dialogo) { mutableStateOf(nombreInicial) }
    var error by remember(dialogo) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (esEdicion) "Editar zona" else "Nueva zona") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        error = null
                    },
                    label = { Text("Nombre de la zona") },
                    singleLine = true,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val resultado = onGuardar(nombre)
                    if (resultado == null) {
                        onExito(if (esEdicion) "Zona actualizada." else "Zona agregada.")
                    } else {
                        error = resultado
                    }
                }
            ) {
                Text(if (esEdicion) "Guardar" else "Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun RangoDialog(
    zona: ZonaReparto,
    onGuardar: (Long, Long) -> String?,
    onDismiss: () -> Unit,
    onExito: () -> Unit
) {
    var desde by remember(zona.id) { mutableStateOf("") }
    var hasta by remember(zona.id) { mutableStateOf("") }
    var error by remember(zona.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rango para \"${zona.nombre}\"") },
        text = {
            Column {
                Row {
                    OutlinedTextField(
                        value = desde,
                        onValueChange = {
                            desde = it
                            error = null
                        },
                        label = { Text("Desde") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = error != null,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = hasta,
                        onValueChange = {
                            hasta = it
                            error = null
                        },
                        label = { Text("Hasta") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = error != null,
                        modifier = Modifier.weight(1f)
                    )
                }
                error?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Incluye ambos extremos. Los rangos no se pueden superponer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val desdeNumero = RepartoLogic.parseNumero(desde)
                    val hastaNumero = RepartoLogic.parseNumero(hasta)
                    if (desdeNumero == null || hastaNumero == null) {
                        error = "Completá desde y hasta con números."
                    } else {
                        val resultado = onGuardar(desdeNumero, hastaNumero)
                        if (resultado == null) {
                            onExito()
                        } else {
                            error = resultado
                        }
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun TrackDialog(
    zona: ZonaReparto,
    onGuardar: (Long) -> ValidacionTrack,
    onDismiss: () -> Unit,
    onExito: (String) -> Unit
) {
    var track by remember(zona.id) { mutableStateOf("") }
    var error by remember(zona.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Track para \"${zona.nombre}\"") },
        text = {
            Column {
                OutlinedTextField(
                    value = track,
                    onValueChange = {
                        track = it
                        error = null
                    },
                    label = { Text("Número de track") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Si el track cae dentro de un rango de otra zona, se guarda como excepción.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trackId = RepartoLogic.parseNumero(track)
                    if (trackId == null) {
                        error = "El track debe ser numérico."
                    } else {
                        when (val validacion = onGuardar(trackId)) {
                            is ValidacionTrack.Ok ->
                                onExito("Track $trackId agregado.")
                            is ValidacionTrack.Excepcion ->
                                onExito(
                                    "Track $trackId agregado como excepción; " +
                                        "está en el rango de \"${validacion.zonaEnRango.nombre}\"."
                                )
                            is ValidacionTrack.Error ->
                                error = validacion.mensaje
                        }
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private sealed class DialogoZona {
    data object Nueva : DialogoZona()
    data class Editar(val zona: ZonaReparto) : DialogoZona()
}

private fun formatearFecha(fecha: String): String = runCatching {
    val entrada = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha)
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(entrada!!)
}.getOrDefault(fecha)
