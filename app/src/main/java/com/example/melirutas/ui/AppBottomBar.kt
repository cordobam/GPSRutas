package com.example.melirutas.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.melirutas.R

/** Barra inferior con las secciones de la app. */
@Composable
fun AppBottomBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == AppTab.LOTE,
            onClick = { onSelect(AppTab.LOTE) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("Lote") }
        )
        NavigationBarItem(
            selected = selected == AppTab.REPARTO,
            onClick = { onSelect(AppTab.REPARTO) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_tab_reparto),
                    contentDescription = null
                )
            },
            label = { Text("Reparto") }
        )
        NavigationBarItem(
            selected = selected == AppTab.COSTOS,
            onClick = { onSelect(AppTab.COSTOS) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_tab_cost),
                    contentDescription = null
                )
            },
            label = { Text("Costos") }
        )
        NavigationBarItem(
            selected = selected == AppTab.CODIGOS,
            onClick = { onSelect(AppTab.CODIGOS) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_tab_codes),
                    contentDescription = null
                )
            },
            label = { Text("Códigos") }
        )
        NavigationBarItem(
            selected = selected == AppTab.CONFIG,
            onClick = { onSelect(AppTab.CONFIG) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text("Config") }
        )
    }
}
