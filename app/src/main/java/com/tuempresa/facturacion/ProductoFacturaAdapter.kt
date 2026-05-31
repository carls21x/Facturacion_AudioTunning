package com.tuempresa.facturacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ProductoFacturaAdapter(
    private var productos: List<Producto>,
    private val onProductoAgregado: (ProductoVendido) -> Unit,
    private val onConsultarOtraSucursal: (Producto) -> Unit // Callback para consulta cruzada
) : RecyclerView.Adapter<ProductoFacturaAdapter.ViewHolder>() {

    private var productosFiltrados: List<Producto> = productos

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProducto)
        val tvStock: TextView = view.findViewById(R.id.tvStockDisponible)
        val chipGroupPrecios: ChipGroup = view.findViewById(R.id.chipGroupPrecios)
        val chipPrecio1: Chip = view.findViewById(R.id.chipPrecio1)
        val chipPrecio2: Chip = view.findViewById(R.id.chipPrecio2)
        val chipPrecio3: Chip = view.findViewById(R.id.chipPrecio3)
        val etCantidad: EditText = view.findViewById(R.id.etCantidad)
        val btnMenos: ImageButton = view.findViewById(R.id.btnMenos)
        val btnMas: ImageButton = view.findViewById(R.id.btnMas)
        val btnAgregar: Button = view.findViewById(R.id.btnAgregarAlCarrito)
        val btnConsultarOtra: Button = view.findViewById(R.id.btnConsultarOtraSucursal)

        fun bind(producto: Producto) {
            tvNombre.text = producto.nombre
            val stock = producto.stock ?: 0
            tvStock.text = "Stock: $stock"
            
            chipPrecio1.text = "P1: $${producto.precio1 ?: 0.0}"
            chipPrecio2.text = "P2: $${producto.precio2 ?: 0.0}"
            chipPrecio3.text = "P3: $${producto.precio3 ?: 0.0}"

            // Mostrar botón de consulta cruzada si no hay stock
            btnConsultarOtra.visibility = if (stock <= 0) View.VISIBLE else View.GONE
            btnConsultarOtra.setOnClickListener { onConsultarOtraSucursal(producto) }

            btnMenos.setOnClickListener {
                val actual = etCantidad.text.toString().toIntOrNull() ?: 1
                if (actual > 1) {
                    etCantidad.setText((actual - 1).toString())
                }
            }

            btnMas.setOnClickListener {
                val actual = etCantidad.text.toString().toIntOrNull() ?: 1
                if (actual < stock) {
                    etCantidad.setText((actual + 1).toString())
                } else {
                    Toast.makeText(it.context, "Límite de stock alcanzado", Toast.LENGTH_SHORT).show()
                }
            }

            btnAgregar.setOnClickListener {
                val cantidadStr = etCantidad.text.toString()
                if (cantidadStr.isEmpty()) {
                    Toast.makeText(it.context, "Ingresa una cantidad", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val cantidad = cantidadStr.toInt()
                
                if (cantidad > stock) {
                    Toast.makeText(it.context, "No hay suficiente stock. Disponible: $stock", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (cantidad <= 0) {
                    Toast.makeText(it.context, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val precioSeleccionado = when (chipGroupPrecios.checkedChipId) {
                    R.id.chipPrecio1 -> producto.precio1 ?: 0.0
                    R.id.chipPrecio2 -> producto.precio2 ?: 0.0
                    R.id.chipPrecio3 -> producto.precio3 ?: 0.0
                    else -> {
                        Toast.makeText(it.context, "Selecciona un precio", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }

                val productoVendido = ProductoVendido(
                    productoId = producto.id,
                    nombre = producto.nombre,
                    cantidad = cantidad,
                    precioSeleccionado = precioSeleccionado,
                    precioCosto = producto.precioCosto ?: 0.0
                )
                onProductoAgregado(productoVendido)
                etCantidad.setText("1")
                chipGroupPrecios.clearCheck()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_producto_factura, parent, false)
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
        val textoBusqueda = texto.lowercase().trim()
        productosFiltrados = if (textoBusqueda.isEmpty()) {
            productos
        } else {
            productos.filter {
                it.nombre?.lowercase()?.contains(textoBusqueda) == true || 
                it.id?.lowercase()?.contains(textoBusqueda) == true
            }
        }
        notifyDataSetChanged()
    }
}
