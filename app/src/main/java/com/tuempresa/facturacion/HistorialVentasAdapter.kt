package com.tuempresa.facturacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class HistorialVentasAdapter(
    private val listaVentas: List<Venta>,
    private val onReimprimirClick: (Venta) -> Unit
) : RecyclerView.Adapter<HistorialVentasAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCliente: TextView = view.findViewById(R.id.tvClienteHistorial)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaHistorial)
        val tvTotal: TextView = view.findViewById(R.id.tvTotalHistorial)
        val tvVendedor: TextView = view.findViewById(R.id.tvVendedorHistorial)
        val btnReimprimir: ImageButton = view.findViewById(R.id.btnReimprimirFactura)

        fun bind(venta: Venta) {
            tvCliente.text = venta.clienteNombre
            tvTotal.text = "$${String.format("%.2f", venta.total)}"
            tvVendedor.text = "Vendedor: ${venta.vendedorNombre ?: "N/A"}"
            
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvFecha.text = sdf.format(Date(venta.fecha ?: 0L))

            btnReimprimir.setOnClickListener { onReimprimirClick(venta) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_historial_venta, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listaVentas[position])
    }

    override fun getItemCount() = listaVentas.size
}
