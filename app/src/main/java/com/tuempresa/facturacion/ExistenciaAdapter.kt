package com.tuempresa.facturacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExistenciaAdapter(private var productos: List<Producto>) :
    RecyclerView.Adapter<ExistenciaAdapter.ViewHolder>() {

    private var productosFiltrados: List<Producto> = productos

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreExistencia)
        val tvCategoria: TextView = view.findViewById(R.id.tvCategoriaExistencia)
        val tvStock: TextView = view.findViewById(R.id.tvStockExistencia)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecioRefExistencia)

        fun bind(producto: Producto) {
            tvNombre.text = producto.nombre
            tvCategoria.text = producto.categoria
            tvStock.text = "Stock: ${producto.stock ?: 0}"
            tvPrecio.text = "Precio Ref: $${producto.precio1 ?: 0.0}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_existencia, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productosFiltrados[position])
    }

    override fun getItemCount() = productosFiltrados.size

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
