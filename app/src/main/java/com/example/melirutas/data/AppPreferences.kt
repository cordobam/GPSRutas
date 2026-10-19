package com.example.melirutas.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencias persistentes de la app (SharedPreferences, sin dependencias nuevas):
 * la lista de códigos de viaje y el precio por km por defecto.
 *
 * Se escriben al instante y se exponen como StateFlow para que la UI se actualice sola.
 */
class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _codes = MutableStateFlow(readCodes())
    val codes: StateFlow<List<String>> = _codes.asStateFlow()

    private val _pricePerKm = MutableStateFlow(readPricePerKm())
    val pricePerKm: StateFlow<Double> = _pricePerKm.asStateFlow()

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

    fun setPricePerKm(value: Double) {
        if (value < 0.0) return
        _pricePerKm.value = value
        prefs.edit().putString(KEY_PRICE_PER_KM, value.toString()).apply()
    }

    private fun persistCodes() {
        prefs.edit().putString(KEY_CODES, _codes.value.joinToString(SEPARATOR)).apply()
    }

    private fun readCodes(): List<String> {
        val raw = prefs.getString(KEY_CODES, null) ?: return emptyList()
        return raw.split(SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun readPricePerKm(): Double =
        prefs.getString(KEY_PRICE_PER_KM, null)?.toDoubleOrNull() ?: 0.0

    private companion object {
        const val PREFS_NAME = "meli_rutas_prefs"
        const val KEY_CODES = "trip_codes"
        const val KEY_PRICE_PER_KM = "price_per_km"
        const val SEPARATOR = "\n"
    }
}
