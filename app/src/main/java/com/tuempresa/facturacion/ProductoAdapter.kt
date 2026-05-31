package com.tuempresa.facturacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductoAdapter(
    private var productos: List<Producto>,
    private val onItemClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    private var productosFiltrados: List<Producto> = productos

    inner class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProducto)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionProducto)
        val tvCategoria: TextView = view.findViewById(R.id.tvCategoriaProducto)
        val tvStock: TextView = view.findViewById(R.id.tvStockProducto)
        val tvPrecio1: TextView = view.findViewById(R.id.tvPrecio1Producto)
        val tvPrecio2: TextView = view.findViewById(R.id.tvPrecio2Producto)
        val tvPrecio3: TextView = view.findViewById(R.id.tvPrecio3Producto)

        fun bind(producto: Producto) {
            tvNombre.text = producto.nombre
            tvDescripcion.text = producto.descripcion
            tvCategoria.text = producto.categoria
            tvStock.text = "Stock: ${producto.stock ?: 0}"
            tvPrecio1.text = "P1: $${producto.precio1 ?: 0.0}"
            tvPrecio2.text = "P2: $${producto.precio2 ?: 0.0}"
            tvPrecio3.text = "P3: $${producto.precio3 ?: 0.0}"

            itemView.setOnClickListener { onItemClick(producto) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(productosFiltrados[position])
    }

    override fun getItemCount(): Int = productosFiltrados.size

    fun actualizarLista(nuevaLista: List<Producto>) {
        productos = nuevaLista
        productosFiltrados = nuevaLista
        notifyDataSetChanged()
    }

    fun filtrar(texto: String) {
        val query = texto.lowercase().trim()
        productosFiltrados = if (query.isEmpty()) {
            productos
        } else {
            productos.filter {
                it.nombre?.lowercase()?.contains(query) == true ||
                it.id?.lowercase()?.contains(query) == true
            }
        }
        notifyDataSetChanged()
    }
}
