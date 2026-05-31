package com.tuempresa.facturacion

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ReportesActivity : AppCompatActivity() {

    private lateinit var tvRangoFechas: TextView
    private lateinit var tvVentaBruta: TextView
    private lateinit var tvGananciaNeta: TextView
    private lateinit var tvTotalDescuentos: TextView
    private lateinit var btnFiltrar: Button
    private lateinit var rvMasVendidos: RecyclerView
    private lateinit var rvVendedores: RecyclerView

    private lateinit var dbRefVentas: DatabaseReference
    private val listaVentas = mutableListOf<Venta>()

    private var fechaInicio: Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis

    private var fechaFin: Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }.timeInMillis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportes)

        // DINÁMICO: Obtener ruta de ventas según sucursal seleccionada
        val pathVentas = PreferenciasConfig.getPathVentas(this)
        dbRefVentas = FirebaseDatabase.getInstance().getReference(pathVentas)

        inicializarVistas()
        configurarBotonFiltro()
        cargarVentas()
    }

    private fun inicializarVistas() {
        tvRangoFechas = findViewById(R.id.tvRangoFechas)
        tvVentaBruta = findViewById(R.id.tvVentaBruta)
        tvGananciaNeta = findViewById(R.id.tvGananciaNeta)
        tvTotalDescuentos = findViewById(R.id.tvTotalDescuentos)
        btnFiltrar = findViewById(R.id.btnFiltrarFechas)
        rvMasVendidos = findViewById(R.id.rvMasVendidos)
        rvVendedores = findViewById(R.id.rvVendedores)

        rvMasVendidos.layoutManager = LinearLayoutManager(this)
        rvVendedores.layoutManager = LinearLayoutManager(this)

        actualizarTextoRango()
    }

    private fun configurarBotonFiltro() {
        btnFiltrar.setOnClickListener {
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            builder.setTitleText("Selecciona el rango de fechas")
            val picker = builder.build()

            picker.addOnPositiveButtonClickListener { selection ->
                fechaInicio = selection.first ?: fechaInicio
                val cal = Calendar.getInstance()
                cal.timeInMillis = selection.second ?: fechaFin
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                fechaFin = cal.timeInMillis

                actualizarTextoRango()
                procesarDatos()
            }
            picker.show(supportFragmentManager, "DATE_RANGE_PICKER")
        }
    }

    private fun actualizarTextoRango() {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val sucursal = PreferenciasConfig.getSucursal(this)
        tvRangoFechas.text = "Sucursal: $sucursal\nDesde: ${sdf.format(Date(fechaInicio))} - Hasta: ${sdf.format(Date(fechaFin))}"
    }

    private fun cargarVentas() {
        dbRefVentas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaVentas.clear()
                for (ventaSnapshot in snapshot.children) {
                    val venta = ventaSnapshot.getValue(Venta::class.java)
                    if (venta != null) {
                        listaVentas.add(venta)
                    }
                }
                procesarDatos()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReportesActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun procesarDatos() {
        val ventasFiltradas = listaVentas.filter { it.fecha in fechaInicio..fechaFin }

        var totalIngresos = 0.0
        var totalDescuentos = 0.0
        var gananciaNetaTotal = 0.0

        val mapaProductos = mutableMapOf<String, Pair<Int, Double>>()
        val mapaVendedores = mutableMapOf<String, Pair<Int, Double>>()

        for (venta in ventasFiltradas) {
            val totalVenta = venta.total ?: 0.0
            totalIngresos += totalVenta
            totalDescuentos += venta.descuentoMonto ?: 0.0

            var costoTotalDeEstaVenta = 0.0

            for (prod in venta.productos) {
                costoTotalDeEstaVenta += (prod.precioCosto ?: 0.0) * (prod.cantidad ?: 0)

                val nombreProd = prod.nombre ?: "Desconocido"
                val actualProd = mapaProductos.getOrDefault(nombreProd, Pair(0, 0.0))
                mapaProductos[nombreProd] = Pair(
                    actualProd.first!! + (prod.cantidad ?: 0),
                    actualProd.second!! + ((prod.precioSeleccionado ?: 0.0) * (prod.cantidad ?: 0))
                )
            }

            gananciaNetaTotal += (totalVenta - costoTotalDeEstaVenta)

            val nombreVend = venta.vendedorNombre ?: "Desconocido"
            val actualVend = mapaVendedores.getOrDefault(nombreVend, Pair(0, 0.0))
            mapaVendedores[nombreVend] = Pair(
                actualVend.first!! + 1,
                actualVend.second!! + totalVenta
            )
        }

        tvVentaBruta.text = "$${String.format("%.2f", totalIngresos)}"
        tvGananciaNeta.text = "$${String.format("%.2f", gananciaNetaTotal)}"
        tvTotalDescuentos.text = "$${String.format("%.2f", totalDescuentos)}"

        val listaRankingProd = mapaProductos.map {
            ProductoRanking(it.key, it.value.first!!, it.value.second!!)
        }.sortedByDescending { it.cantidad }.take(10)
        rvMasVendidos.adapter = RankingAdapter(listaRankingProd)

        val listaRankingVend = mapaVendedores.map {
            VendedorRanking(it.key, it.value.first!!, it.value.second!!)
        }.sortedByDescending { it.montoTotal }
        rvVendedores.adapter = VendedorAdapter(listaRankingVend)
    }
}
