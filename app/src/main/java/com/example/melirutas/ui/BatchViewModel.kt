package com.example.melirutas.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.melirutas.ocr.AddressParser
import com.example.melirutas.ocr.BitmapLoader
import com.example.melirutas.ocr.TesseractEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Estado y lógica del lote: fotos elegidas → OCR + parser → lista de paradas.
 * Sobrevive a rotación de pantalla (es un ViewModel).
 */
class BatchViewModel(application: Application) : AndroidViewModel(application) {

    sealed class BatchState {
        data object Home : BatchState()
        data object Capture : BatchState()
        data class Processing(val current: Int, val total: Int) : BatchState()
        data class Review(val stops: List<StopUi>) : BatchState()
        data class Error(val message: String) : BatchState()
    }

    /** Parada detectada en una foto, con los datos crudos para poder editarlos. */
    data class StopUi(
        val photoUri: Uri?,
        val street: String,
        val city: String,
        val postalCode: String?,
        val trackId: String?,
        val ocrText: String,
        val meanConfidence: Int
    )

    private val _state = MutableStateFlow<BatchState>(BatchState.Home)
    val state: StateFlow<BatchState> = _state.asStateFlow()

    private val _captures = MutableStateFlow<List<Uri>>(emptyList())
    val captures: StateFlow<List<Uri>> = _captures.asStateFlow()

    fun abrirCaptura() {
        _captures.value = emptyList()
        _state.value = BatchState.Capture
    }

    fun addCaptura(uri: Uri) {
        _captures.value = _captures.value + uri
    }

    fun procesarCapturas() {
        val uris = _captures.value
        if (uris.isEmpty()) return
        _captures.value = emptyList()
        procesarFotos(uris)
    }

    fun cancelarCaptura() {
        _captures.value = emptyList()
        _state.value = BatchState.Home
    }

    fun procesarFotos(uris: List<Uri>) {
        if (uris.isEmpty() || _state.value is BatchState.Processing) {
            return
        }
        viewModelScope.launch {
            _state.value = BatchState.Processing(current = 0, total = uris.size)
            val stops = withContext(Dispatchers.Default) { runBatch(uris) }
            _state.value = if (stops.isEmpty()) {
                BatchState.Error("No se pudo leer ninguna foto. Probá con fotos más nítidas.")
            } else {
                BatchState.Review(stops)
            }
        }
    }

    fun nuevoLote() {
        _state.value = BatchState.Home
    }

    fun updateStop(index: Int, stop: StopUi) {
        val review = _state.value as? BatchState.Review ?: return
        if (index !in review.stops.indices) return
        val stops = review.stops.toMutableList()
        stops[index] = stop
        _state.value = BatchState.Review(stops)
    }

    fun removeStop(index: Int) {
        val review = _state.value as? BatchState.Review ?: return
        if (index !in review.stops.indices) return
        val stops = review.stops.toMutableList()
        stops.removeAt(index)
        _state.value = if (stops.isEmpty()) {
            BatchState.Error("El lote quedó sin paradas.")
        } else {
            BatchState.Review(stops)
        }
    }

    /** Agrega una parada en blanco al final (alta manual, sin foto). */
    fun addStop() {
        val review = _state.value as? BatchState.Review ?: return
        val blank = StopUi(
            photoUri = null,
            street = "",
            city = "",
            postalCode = null,
            trackId = null,
            ocrText = "",
            meanConfidence = 0
        )
        _state.value = BatchState.Review(review.stops + blank)
    }

    private fun runBatch(uris: List<Uri>): List<StopUi> {
        val engine = try {
            TesseractEngine(getApplication())
        } catch (expected: Exception) {
            return emptyList()
        }
        return try {
            val stops = ArrayList<StopUi>(uris.size)
            for ((index, uri) in uris.withIndex()) {
                _state.value = BatchState.Processing(current = index + 1, total = uris.size)
                val bitmap = BitmapLoader.load(getApplication(), uri) ?: continue
                val result = engine.recognize(bitmap)
                bitmap.recycle()
                val parsed = AddressParser.parse(result.text)
                stops += StopUi(
                    photoUri = uri,
                    street = parsed.street,
                    city = parsed.city,
                    postalCode = parsed.postalCode,
                    trackId = parsed.trackId,
                    ocrText = result.text,
                    meanConfidence = result.meanConfidence
                )
            }
            stops
        } finally {
            engine.close()
        }
    }
}
