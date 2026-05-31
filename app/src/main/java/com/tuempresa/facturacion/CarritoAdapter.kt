package com.tuempresa.facturacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CarritoAdapter(
    private val productos: MutableList<ProductoVendido>,
    private val onEliminarClick: (Int) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvPrecioUnitario: TextView = view.findViewById(R.id.tvPrecioUnitario)
        val tvSubtotal: TextView = view.findViewById(R.id.tvSubtotal)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)

        fun bind(producto: ProductoVendido, position: Int) {
            tvNombre.text = producto.nombre
            tvPrecioUnitario.text = "$${producto.precioSeleccionado} x ${producto.cantidad}"
            val subtotal = (producto.precioSeleccionado ?: 0.0) * (producto.cantidad ?: 0)
            tvSubtotal.text = "$${String.format("%.2f", subtotal)}"

            btnEliminar.setOnClickListener { onEliminarClick(position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_carrito, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productos[position], position)
    }

    override fun getItemCount() = productos.size
}
