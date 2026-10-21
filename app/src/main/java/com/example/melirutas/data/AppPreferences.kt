package com.example.melirutas.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencias persistentes de la app (SharedPreferences): los códigos de viaje y
 * los valores por defecto de la calculadora (precio de la nafta por litro y rendimiento km/l).
 *
 * Se escriben al instante y se exponen como StateFlow para que la UI se actualice sola.
 */
class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _codes = MutableStateFlow(readCodes())
    val codes: StateFlow<List<String>> = _codes.asStateFlow()

    private val _pricePerLiter = MutableStateFlow(readDouble(KEY_PRICE_PER_LITER))
    val pricePerLiter: StateFlow<Double> = _pricePerLiter.asStateFlow()

    private val _kmPerLiter = MutableStateFlow(readDouble(KEY_KM_PER_LITER))
    val kmPerLiter: StateFlow<Double> = _kmPerLiter.asStateFlow()

    /**
     * Agrega un código. Devuelve false si está vacío o si ya existía (no distingue mayúsculas).
     */
    fun addCode(code: String): Boolean {
        val clean = code.trim()
        if (clean.isEmpty()) return false
        if (_codes.value.any { it.equals(clean, ignoreCase = true) }) return false

        _codes.value = _codes.value + clean
        persistCodes()
        return true
    }

    fun removeCode(code: String) {
        val updated = _codes.value.filterNot { it == code }
        if (updated.size == _codes.value.size) return
        _codes.value = updated
        persistCodes()
    }

    fun clearCodes() {
        if (_codes.value.isEmpty()) return
        _codes.value = emptyList()
        persistCodes()
    }

    fun setPricePerLiter(value: Double) {
        if (value < 0.0) return
        _pricePerLiter.value = value
        prefs.edit().putString(KEY_PRICE_PER_LITER, value.toString()).apply()
    }

    /** Rendimiento del auto en km por litro. Ignora valores <= 0. */
    fun setKmPerLiter(value: Double) {
        if (value <= 0.0) return
        _kmPerLiter.value = value
        prefs.edit().putString(KEY_KM_PER_LITER, value.toString()).apply()
    }

    private fun persistCodes() {
        prefs.edit().putString(KEY_CODES, _codes.value.joinToString(SEPARATOR)).apply()
    }

    private fun readCodes(): List<String> {
        val raw = prefs.getString(KEY_CODES, null) ?: return emptyList()
        return raw.split(SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun readDouble(key: String): Double =
        prefs.getString(key, null)?.toDoubleOrNull() ?: 0.0

    private companion object {
        const val PREFS_NAME = "meli_rutas_prefs"
        const val KEY_CODES = "trip_codes"
        const val KEY_PRICE_PER_LITER = "price_per_liter"
        const val KEY_KM_PER_LITER = "km_per_liter"
        const val SEPARATOR = "\n"
    }
}
