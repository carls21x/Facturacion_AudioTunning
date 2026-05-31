package com.tuempresa.facturacion

data class Venta(
    val id: String? = null,
    val clienteNombre: String? = null,
    val productos: List<ProductoVendido> = emptyList(),
    val subtotal: Double? = 0.0,
    val descuentoPorcentaje: Double? = 0.0,
    val descuentoMonto: Double? = 0.0,
    val total: Double? = 0.0,
    val pagoCon: Double? = 0.0,
    val cambio: Double? = 0.0,
    val fecha: Long? = System.currentTimeMillis(),
    val vendedorId: String? = null,
    val vendedorNombre: String? = null
)

data class ProductoVendido(
    val productoId: String? = null,
    val nombre: String? = null,
    val cantidad: Int? = 0,
    val precioSeleccionado: Double? = 0.0,
    val precioCosto: Double? = 0.0 // Añadido para reportes de ganancia
)
