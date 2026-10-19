package com.example.melirutas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.melirutas.cost.CostCalculator
import com.example.melirutas.data.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Pestañas de la barra inferior. */
enum class AppTab { LOTE, COSTOS, CODIGOS }

/**
 * Estado de nivel app: pestaña activa, códigos de viaje y costo del viaje.
 * Sobrevive a rotación de pantalla (es un ViewModel).
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = AppPreferences(application)

    private val _selectedTab = MutableStateFlow(AppTab.LOTE)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    val codes: StateFlow<List<String>> = prefs.codes
    val pricePerKm: StateFlow<Double> = prefs.pricePerKm

    private val _kmText = MutableStateFlow("")
    val kmText: StateFlow<String> = _kmText.asStateFlow()

    private val _priceText = MutableStateFlow(plainNumber(prefs.pricePerKm.value))
    val priceText: StateFlow<String> = _priceText.asStateFlow()

    /** Total del viaje ya formateado ("$ 4.375,00"), recalculado al tipear. */
    val totalText: StateFlow<String> = combine(_kmText, _priceText) { km, price ->
        val kms = CostCalculator.parseDecimal(km) ?: 0.0
        val pricePerKm = CostCalculator.parseDecimal(price) ?: 0.0
        CostCalculator.formatCurrency(CostCalculator.total(kms, pricePerKm))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CostCalculator.formatCurrency(0.0)
    )

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun onKmChange(value: String) {
        _kmText.value = value
    }

    /** Acepta la edición del precio y guarda el último valor válido como default. */
    fun onPriceChange(value: String) {
        _priceText.value = value
        CostCalculator.parseDecimal(value)?.let { prefs.setPricePerKm(it) }
    }

    /** Devuelve false si el código está vacío o ya existía. */
    fun addCode(code: String): Boolean = prefs.addCode(code)

    fun removeCode(code: String) {
        prefs.removeCode(code)
    }

    fun clearCodes() {
        prefs.clearCodes()
    }

    private fun plainNumber(value: Double): String = when {
        value <= 0.0 -> ""
        value % 1.0 == 0.0 -> value.toLong().toString()
        else -> value.toString().replace('.', ',')
    }
}
