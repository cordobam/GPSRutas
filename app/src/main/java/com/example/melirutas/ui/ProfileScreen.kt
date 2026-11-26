package com.example.melirutas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(stats: PerfilEstadisticas) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        PerfilCard(titulo = "Actividad y costos") {
            Dato("Viajes cerrados", stats.viajes)
            Dato("Kilómetros totales", stats.kmTotales)
            Dato("Kilómetros por viaje", stats.kmPromedio)
            Dato("Cobrado", stats.cobrado)
            Dato("Nafta", stats.nafta)
            Dato("Peaje", stats.peaje)
            Dato("Neto total", stats.neto)
            Dato("Neto por viaje", stats.netoPromedio)
            Dato("Mejor viaje", stats.mejorViaje)
            Dato("Estado de pagos", stats.pagados)
        }
        Spacer(Modifier.height(12.dp))
        PerfilCard(titulo = "Rendimiento de ruta") {
            Dato("Paquetes entregados", stats.paquetes)
            Dato("Paradas", stats.paradas)
            Dato("Puntos", stats.puntos)
            Dato("Días con resumen", stats.diasConResumen)
            Dato("Duración promedio", stats.duracionPromedio)
            Dato("Paquetes por hora", stats.paquetesPorHora)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PerfilCard(
    titulo: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
    }
}
