package com.example.melirutas.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencias persistentes de la app (SharedPreferences): valores por defecto de la
 * calculadora (precio de la nafta por litro, rendimiento km/l y precio del peaje).
 *
 * Se escriben al instante y se exponen como StateFlow para que la UI se actualice sola.
 */
class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _pricePerLiter = MutableStateFlow(readDouble(KEY_PRICE_PER_LITER))
    val pricePerLiter: StateFlow<Double> = _pricePerLiter.asStateFlow()

    private val _kmPerLiter = MutableStateFlow(readDouble(KEY_KM_PER_LITER))
    val kmPerLiter: StateFlow<Double> = _kmPerLiter.asStateFlow()

    private val _tollPrice = MutableStateFlow(readDouble(KEY_TOLL_PRICE))
    val tollPrice: StateFlow<Double> = _tollPrice.asStateFlow()

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

    fun setTollPrice(value: Double) {
        if (value < 0.0) return
        _tollPrice.value = value
        prefs.edit().putString(KEY_TOLL_PRICE, value.toString()).apply()
    }

    private fun readDouble(key: String): Double =
        prefs.getString(key, null)?.toDoubleOrNull() ?: 0.0

    private companion object {
        const val PREFS_NAME = "meli_rutas_prefs"
        const val KEY_PRICE_PER_LITER = "price_per_liter"
        const val KEY_KM_PER_LITER = "km_per_liter"
        const val KEY_TOLL_PRICE = "toll_price"
    }
}
