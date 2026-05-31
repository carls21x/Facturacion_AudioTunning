package com.tuempresa.facturacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class VendedorRanking(
    val nombre: String,
    val cantidadVentas: Int,
    val montoTotal: Double
)

class VendedorAdapter(private val lista: List<VendedorRanking>) : RecyclerView.Adapter<VendedorAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreVendedor)
        val tvVentas: TextView = view.findViewById(R.id.tvVentasRealizadas)
        val tvMonto: TextView = view.findViewById(R.id.tvMontoTotalVendedor)

        fun bind(item: VendedorRanking) {
            tvNombre.text = item.nombre
            tvVentas.text = "${item.cantidadVentas} ventas registradas"
            tvMonto.text = "$${String.format("%.2f", item.montoTotal)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_vendedor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount() = lista.size
}
