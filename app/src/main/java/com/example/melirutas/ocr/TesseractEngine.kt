package com.example.melirutas.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.annotation.WorkerThread
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream

/**
 * Motor OCR basado en Tesseract 5 (librería tesseract4android) con modelos spa+eng.
 *
 * En el primer arranque copia los modelos desde assets/tessdata a filesDir/tessdata
 * (obligatorio en Android 10+ no poder leerlos directo del APK).
 *
 * Preproceso aplicado antes de reconocer: escalado a un ancho objetivo
 * (mejora la lectura de texto chico en capturas chicas) y conversión a escala de grises.
 *
 * No es thread-safe: usar cada instancia desde un único hilo de trabajo
 * (una por lote de fotos es suficiente).
 */
class TesseractEngine(context: Context) {

    /** Resultado de un reconocimiento: texto crudo + confianza promedio (0-100). */
    data class OcrResult(val text: String, val meanConfidence: Int)

    private val tess: TessBaseAPI

    init {
        val tessdataDir = File(context.filesDir, TESSDATA_DIR).apply { mkdirs() }
        copyAssetIfNeeded(context, "$TESSDATA_DIR/spa.traineddata", File(tessdataDir, "spa.traineddata"))
        copyAssetIfNeeded(context, "$TESSDATA_DIR/eng.traineddata", File(tessdataDir, "eng.traineddata"))

        val engine = TessBaseAPI()
        val ok = engine.init(context.filesDir.absolutePath, LANGUAGE)
        if (!ok) {
            engine.recycle()
            throw IllegalStateException("Tesseract no pudo inicializarse con los modelos spa+eng")
        }
        engine.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK)
        tess = engine
    }

    /**
     * Reconoce texto en el bitmap provisto. El bitmap original no se recicla.
     * Debe llamarse desde un hilo de trabajo.
     */
    @WorkerThread
    fun recognize(source: Bitmap): OcrResult {
        val prepared = prepare(source)
        try {
            tess.setImage(prepared)
            val text = tess.getUTF8Text() ?: ""
            return OcrResult(text = text, meanConfidence = tess.meanConfidence())
        } finally {
            tess.clear()
            if (prepared !== source) {
                prepared.recycle()
            }
        }
    }

    /** Libera los recursos nativos de Tesseract. Llamar cuando el lote terminó. */
    fun close() {
        tess.recycle()
    }

    private fun prepare(source: Bitmap): Bitmap {
        val scaled = scale(source)
        val gray = toGrayscale(scaled)
        if (gray !== scaled) {
            scaled.recycle()
        }
        return gray
    }

    private fun scale(src: Bitmap): Bitmap {
        val targetWidth = src.width.coerceIn(MIN_WIDTH_PX, MAX_WIDTH_PX)
        if (targetWidth == src.width) {
            return src
        }
        val targetHeight = (src.height.toLong() * targetWidth / src.width)
            .toInt()
            .coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, targetWidth, targetHeight, true)
    }

    private fun toGrayscale(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        Canvas(out).drawBitmap(src, 0f, 0f, paint)
        return out
    }

    private fun copyAssetIfNeeded(context: Context, assetPath: String, dest: File) {
        if (dest.exists() && dest.length() > 0L) {
            return
        }
        context.assets.open(assetPath).use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        }
    }

    companion object {
        const val LANGUAGE = "spa+eng"
        private const val TESSDATA_DIR = "tessdata"
        private const val MIN_WIDTH_PX = 1600
        private const val MAX_WIDTH_PX = 3200
    }
}
