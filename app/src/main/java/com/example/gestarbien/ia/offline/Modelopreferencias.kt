package com.example.gestarbien.ia.offline

import android.content.Context
import android.content.SharedPreferences

object ModeloPreferencias {

    private const val PREFS_NAME = "modelo_ia_prefs"
    private const val KEY_MODELO = "modelo_seleccionado"
    private const val KEY_LISTO = "modelo_listo"

    // Modelo de respaldo (se resuelve al slug real del catálogo en OfflineLLM)
    const val SLUG_PREDETERMINADO = "lfm2.5-350m"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun obtenerModeloSeleccionado(context: Context): String {
        return prefs(context).getString(KEY_MODELO, SLUG_PREDETERMINADO) ?: SLUG_PREDETERMINADO
    }

    fun guardarModeloSeleccionado(context: Context, slug: String) {
        prefs(context).edit().putString(KEY_MODELO, slug).apply()
    }

    fun modeloListo(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_LISTO, false)
    }

    fun marcarModeloListo(context: Context, listo: Boolean) {
        prefs(context).edit().putBoolean(KEY_LISTO, listo).apply()
    }
}