package com.tuempresa.facturacion

import android.content.Context
import android.content.SharedPreferences

object PreferenciasConfig {
    private const val PREFS_NAME = "AppPrefs"
    private const val KEY_SUCURSAL = "sucursal_seleccionada"

    fun setSucursal(context: Context, sucursal: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SUCURSAL, sucursal).apply()
    }

    fun getSucursal(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SUCURSAL, "Jinotega") ?: "Jinotega"
    }

    fun getPathProductos(context: Context): String {
        return if (getSucursal(context) == "Esteli") "productos_esteli" else "productos"
    }

    fun getPathVentas(context: Context): String {
        return if (getSucursal(context) == "Esteli") "ventas_esteli" else "ventas"
    }
    
    fun getPathProductosOtraSucursal(context: Context): String {
        return if (getSucursal(context) == "Esteli") "productos" else "productos_esteli"
    }
}
