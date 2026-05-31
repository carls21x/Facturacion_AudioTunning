package com.tuempresa.facturacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class ProductoRanking(
    val nombre: String,
    val cantidad: Int,
    val monto: Double
)

class RankingAdapter(private val lista: List<ProductoRanking>) : RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPosicion: TextView = view.findViewById(R.id.tvPosicion)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProductoRanking)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidadVendida)
        val tvMonto: TextView = view.findViewById(R.id.tvMontoTotalProducto)

        fun bind(item: ProductoRanking, position: Int) {
            tvPosicion.text = (position + 1).toString()
            tvNombre.text = item.nombre
            tvCantidad.text = "Vendidos: ${item.cantidad} unidades"
            tvMonto.text = "$${String.format("%.2f", item.monto)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_ranking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lista[position], position)
    }

    override fun getItemCount() = lista.size
}
