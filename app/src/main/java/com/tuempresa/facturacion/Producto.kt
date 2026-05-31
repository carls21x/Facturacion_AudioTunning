package com.tuempresa.facturacion

data class Producto(
    val id: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val proveedor: String? = null,
    val precioCosto: Double? = null,
    val precio1: Double? = null,
    val precio2: Double? = null,
    val precio3: Double? = null,
    val stock: Int? = null
)
