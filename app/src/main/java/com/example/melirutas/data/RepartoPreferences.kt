package com.example.melirutas.data

import android.content.Context
import com.example.melirutas.reparto.ReglaRango
import com.example.melirutas.reparto.RepartoDia
import com.example.melirutas.reparto.RepartoLogic
import com.example.melirutas.reparto.ZonaReparto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class RepartoPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _reparto = MutableStateFlow<RepartoDia?>(null)
    val reparto: StateFlow<RepartoDia?> = _reparto.asStateFlow()

    private var fechaActual: String? = null

    fun abrirDia(fecha: String): RepartoDia {
        val existente = cargar(fecha)
        val reparto = existente ?: run {
            val anterior = ultimaFechaAntesDe(fecha)?.let(::cargar)
            RepartoLogic.nuevoDiaDesde(fecha, anterior).also(::guardar)
        }
        fechaActual = fecha
        _reparto.value = reparto
        return reparto
    }

    fun guardar(reparto: RepartoDia) {
        prefs.edit().putString(clave(reparto.fecha), serializar(reparto)).apply()
        if (reparto.fecha == fechaActual) _reparto.value = reparto
    }

    fun limpiar(fecha: String) {
        guardar(RepartoDia(fecha = fecha))
    }

    fun cargar(fecha: String): RepartoDia? {
        val raw = prefs.getString(clave(fecha), null) ?: return null
        return deserializar(fecha, raw)
    }

    fun ultimaFechaAntesDe(fecha: String): String? =
        prefs.all.keys
            .asSequence()
            .filter { it.startsWith(KEY_PREFIX) }
            .map { it.removePrefix(KEY_PREFIX) }
            .filter { it < fecha }
            .maxOrNull()

    private fun clave(fecha: String): String = KEY_PREFIX + fecha

    private fun serializar(reparto: RepartoDia): String {
        val zonasJson = JSONArray()
        reparto.zonas.forEach { zona ->
            val rangosJson = JSONArray()
            zona.rangos.forEach { rango ->
                rangosJson.put(
                    JSONObject()
                        .put("desde", rango.desde)
                        .put("hasta", rango.hasta)
                )
            }
            val trackIdsJson = JSONArray()
            zona.trackIds.forEach { trackIdsJson.put(it) }

            zonasJson.put(
                JSONObject()
                    .put("id", zona.id)
                    .put("nombre", zona.nombre)
                    .put("rangos", rangosJson)
                    .put("trackIds", trackIdsJson)
            )
        }
        return JSONObject().put("zonas", zonasJson).toString()
    }

    private fun deserializar(fecha: String, raw: String): RepartoDia? = runCatching {
        val zonasJson = JSONObject(raw).optJSONArray("zonas") ?: JSONArray()
        val zonas = (0 until zonasJson.length()).map { i ->
            val zonaJson = zonasJson.getJSONObject(i)
            val rangosJson = zonaJson.optJSONArray("rangos") ?: JSONArray()
            val rangos = (0 until rangosJson.length()).map { j ->
                val rangoJson = rangosJson.getJSONObject(j)
                ReglaRango(
                    desde = rangoJson.getLong("desde"),
                    hasta = rangoJson.getLong("hasta")
                )
            }
            val trackIdsJson = zonaJson.optJSONArray("trackIds") ?: JSONArray()
            val trackIds = (0 until trackIdsJson.length()).map { j ->
                trackIdsJson.getLong(j)
            }
            ZonaReparto(
                id = zonaJson.getString("id"),
                nombre = zonaJson.getString("nombre"),
                rangos = rangos,
                trackIds = trackIds
            )
        }
        RepartoDia(fecha = fecha, zonas = zonas)
    }.getOrNull()

    private companion object {
        const val PREFS_NAME = "meli_rutas_reparto"
        const val KEY_PREFIX = "reparto_"
    }
}
