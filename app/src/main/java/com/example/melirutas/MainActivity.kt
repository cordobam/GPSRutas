package com.example.melirutas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.melirutas.ui.MeliRutasApp
import com.example.melirutas.ui.theme.MeliRutasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeliRutasTheme {
                MeliRutasApp()
            }
        }
    }
}

