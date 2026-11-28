package com.example.melirutas.data

import android.content.Context
import com.example.melirutas.reparto.RepartoLogic
import com.example.melirutas.reparto.ZonaApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ZonasAppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _zonasApp = MutableStateFlow(leer())
    val zonasApp: StateFlow<List<ZonaApp>> = _zonasApp.asStateFlow()

    fun agregar(nombre: String): String? {
        val actual = _zonasApp.value
        val error = RepartoLogic.validarNombreZonaApp(nombre, actual)
        if (error != null) return error

        val zona = ZonaApp(id = UUID.randomUUID().toString(), nombre = nombre.trim())
        guardar(actual + zona)
        return null
    }

    fun editar(id: String, nombre: String): String? {
        val actual = _zonasApp.value
        if (actual.none { it.id == id }) return "La zona de la app no existe."

        val error = RepartoLogic.validarNombreZonaApp(nombre, actual, idEditando = id)
        if (error != null) return error

        guardar(actual.map { if (it.id == id) it.copy(nombre = nombre.trim()) else it })
        return null
    }

    fun eliminar(id: String) {
        guardar(_zonasApp.value.filterNot { it.id == id })
    }

    private fun guardar(zonas: List<ZonaApp>) {
        _zonasApp.value = zonas
        prefs.edit().putString(KEY_ZONAS_APP, serializar(zonas)).apply()
    }

    private fun leer(): List<ZonaApp> =
        prefs.getString(KEY_ZONAS_APP, null)?.let(::deserializar) ?: emptyList()

    private fun serializar(zonas: List<ZonaApp>): String {
        val json = JSONArray()
        zonas.forEach {
            json.put(JSONObject().put("id", it.id).put("nombre", it.nombre))
        }
        return JSONObject().put("zonas", json).toString()
    }

    private fun deserializar(raw: String): List<ZonaApp> = runCatching {
        val json = JSONObject(raw).optJSONArray("zonas") ?: JSONArray()
        (0 until json.length()).map { i ->
            val zona = json.getJSONObject(i)
            ZonaApp(id = zona.getString("id"), nombre = zona.getString("nombre"))
        }
    }.getOrNull() ?: emptyList()

    private companion object {
        const val PREFS_NAME = "meli_rutas_zonas_app"
        const val KEY_ZONAS_APP = "zonas_app"
    }
}
