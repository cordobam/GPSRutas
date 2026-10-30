package com.example.melirutas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.melirutas.data.RepartoPreferences
import com.example.melirutas.reparto.ReglaRango
import com.example.melirutas.reparto.RepartoDia
import com.example.melirutas.reparto.RepartoLogic
import com.example.melirutas.reparto.ResolucionTrack
import com.example.melirutas.reparto.ValidacionTrack
import com.example.melirutas.reparto.ZonaReparto
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RepartoViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = RepartoPreferences(application)

    val reparto: StateFlow<RepartoDia?> = prefs.reparto

    private val fecha = fechaDeHoy()

    init {
        prefs.abrirDia(fecha)
    }

    fun agregarZona(nombre: String): String? {
        val actual = repartoActual()
        val error = RepartoLogic.validarNombreZona(nombre, actual.zonas)
        if (error != null) return error

        val zona = ZonaReparto(id = UUID.randomUUID().toString(), nombre = nombre.trim())
        prefs.guardar(actual.copy(zonas = actual.zonas + zona))
        return null
    }

    fun editarZona(id: String, nombre: String): String? {
        val actual = repartoActual()
        if (actual.zonas.none { it.id == id }) return "La zona no existe."

        val error = RepartoLogic.validarNombreZona(nombre, actual.zonas, idEditando = id)
        if (error != null) return error

        prefs.guardar(
            actual.copy(
                zonas = actual.zonas.map {
                    if (it.id == id) it.copy(nombre = nombre.trim()) else it
                }
            )
        )
        return null
    }

    fun eliminarZona(id: String) {
        val actual = repartoActual()
        if (actual.zonas.none { it.id == id }) return
        prefs.guardar(actual.copy(zonas = actual.zonas.filterNot { it.id == id }))
    }

    fun agregarRango(zonaId: String, desde: Long, hasta: Long): String? {
        val actual = repartoActual()
        if (actual.zonas.none { it.id == zonaId }) return "La zona no existe."

        val error = RepartoLogic.validarRango(desde, hasta, actual.zonas)
        if (error != null) return error

        val rango = ReglaRango(desde = desde, hasta = hasta)
        prefs.guardar(
            actual.copy(
                zonas = actual.zonas.map {
                    if (it.id == zonaId) it.copy(rangos = it.rangos + rango) else it
                }
            )
        )
        return null
    }

    fun eliminarRango(zonaId: String, rango: ReglaRango) {
        val actual = repartoActual()
        prefs.guardar(
            actual.copy(
                zonas = actual.zonas.map {
                    if (it.id == zonaId) {
                        it.copy(rangos = it.rangos.filterNot { existente -> existente == rango })
                    } else {
                        it
                    }
                }
            )
        )
    }

    fun agregarTrack(zonaId: String, trackId: Long): ValidacionTrack {
        val actual = repartoActual()
        if (actual.zonas.none { it.id == zonaId }) {
            return ValidacionTrack.Error("La zona no existe.")
        }

        val validacion = RepartoLogic.validarTrack(trackId, actual.zonas, zonaId)
        if (validacion is ValidacionTrack.Error) return validacion

        prefs.guardar(
            actual.copy(
                zonas = actual.zonas.map {
                    if (it.id == zonaId) it.copy(trackIds = it.trackIds + trackId) else it
                }
            )
        )
        return validacion
    }

    fun eliminarTrack(zonaId: String, trackId: Long) {
        val actual = repartoActual()
        prefs.guardar(
            actual.copy(
                zonas = actual.zonas.map {
                    if (it.id == zonaId) {
                        it.copy(trackIds = it.trackIds.filterNot { existente -> existente == trackId })
                    } else {
                        it
                    }
                }
            )
        )
    }

    fun limpiarDia() {
        prefs.limpiar(fecha)
    }

    fun resolver(trackId: Long): ResolucionTrack =
        RepartoLogic.resolver(trackId, repartoActual().zonas)

    private fun repartoActual(): RepartoDia =
        prefs.reparto.value ?: prefs.abrirDia(fecha)

    private companion object {
        fun fechaDeHoy(): String =
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
