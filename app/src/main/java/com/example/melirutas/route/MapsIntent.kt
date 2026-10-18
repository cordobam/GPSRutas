package com.example.melirutas.route

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

/**
 * Abre la ruta construida por [RouteUrlBuilder] en Google Maps.
 *
 * Se fuerza el paquete com.google.android.apps.maps porque los links
 * https de Google Maps solo soportan los 9 waypoints en la app; si el link
 * se abre en el navegador móvil el límite es de 3 waypoints.
 *
 * Si la app no está instalada se hace fallback al intent genérico (navegador
 * u otra app que maneje el link) y [open] devuelve false para que la UI avise.
 */
object MapsIntent {

    private const val MAPS_PACKAGE = "com.google.android.apps.maps"

    /** true si la app de Google Maps manejó el intent; false si hubo fallback o no hay app. */
    fun open(context: Context, url: String): Boolean {
        val uri = Uri.parse(url)

        val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(MAPS_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(mapsIntent)
            return true
        } catch (expected: ActivityNotFoundException) {
            // La app de Maps no está instalada: fallback al navegador u otra app.
        }

        val fallback = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(fallback)
            false
        } catch (expected: ActivityNotFoundException) {
            false
        }
    }

    fun isGoogleMapsInstalled(context: Context): Boolean = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(MAPS_PACKAGE, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(MAPS_PACKAGE, 0)
        }
        true
    } catch (expected: PackageManager.NameNotFoundException) {
        false
    }
}
