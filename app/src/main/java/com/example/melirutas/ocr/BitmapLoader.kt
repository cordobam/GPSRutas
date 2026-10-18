package com.example.melirutas.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri

/** Carga bitmaps desde un Uri (galería o cámara) aplicando la rotación EXIF. */
object BitmapLoader {

    private const val TARGET_WIDTH = 2000
    private const val THUMBNAIL_WIDTH = 128

    fun load(context: Context, uri: Uri): Bitmap? {
        return decode(context, uri, TARGET_WIDTH)
    }

    /** Miniatura chica (~128px) para las tiras de previews. */
    fun loadThumbnail(context: Context, uri: Uri): Bitmap? {
        return decode(context, uri, THUMBNAIL_WIDTH)
    }

    private fun decode(context: Context, uri: Uri, targetWidth: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }

        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inSampleSize = sampleSize(bounds.outWidth, targetWidth)
        }
        val raw = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        return applyOrientation(raw, readOrientation(context, uri))
    }

    private fun sampleSize(width: Int, targetWidth: Int): Int {
        var size = 1
        while (width / (size * 2) >= targetWidth) {
            size *= 2
        }
        return size
    }

    @Suppress("DEPRECATION")
    private fun readOrientation(context: Context, uri: Uri): Int = try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            ExifInterface(input)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
    } catch (expected: Exception) {
        ExifInterface.ORIENTATION_NORMAL
    }

    private fun applyOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            .also { rotated -> if (rotated !== bitmap) bitmap.recycle() }
    }
}
