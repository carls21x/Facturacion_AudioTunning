package com.tuempresa.facturacion

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class HistorialVentasActivity : AppCompatActivity() {

    private lateinit var tvSucursal: TextView
    private lateinit var rvHistorial: RecyclerView
    private lateinit var cardVolver: MaterialCardView
    
    private lateinit var dbRefVentas: DatabaseReference
    private val listaVentas = mutableListOf<Venta>()
    private lateinit var adapter: HistorialVentasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_ventas)

        val sucursalActual = PreferenciasConfig.getSucursal(this)
        val pathVentas = PreferenciasConfig.getPathVentas(this)
        dbRefVentas = FirebaseDatabase.getInstance().getReference(pathVentas)

        tvSucursal = findViewById(R.id.tvSucursalHistorial)
        rvHistorial = findViewById(R.id.rvHistorialVentas)
        cardVolver = findViewById(R.id.btnVolverHistorialCard)

        tvSucursal.text = "Sucursal: $sucursalActual"

        rvHistorial.layoutManager = LinearLayoutManager(this)
        adapter = HistorialVentasAdapter(listaVentas) { venta ->
            generarReciboHTML(venta)
        }
        rvHistorial.adapter = adapter

        cargarHistorial()

        cardVolver.setOnClickListener { finish() }
    }

    private fun cargarHistorial() {
        dbRefVentas.orderByChild("fecha").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaVentas.clear()
                for (ventaSnapshot in snapshot.children) {
                    val venta = ventaSnapshot.getValue(Venta::class.java)
                    if (venta != null) {
                        listaVentas.add(venta)
                    }
                }
                listaVentas.reverse()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistorialVentasActivity, "Error al cargar historial", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun obtenerLogoBase64(): String {
        val drawable = ContextCompat.getDrawable(this, R.drawable.logo) ?: return ""
        val bitmap = if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp
        }
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun generarReciboHTML(venta: Venta) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaStr = sdf.format(Date(venta.fecha ?: 0L))
        val logoBase64 = obtenerLogoBase64()
        val sucursalActual = PreferenciasConfig.getSucursal(this)
        
        // ASIGNACIÓN DE RUC SEGÚN SUCURSAL
        val rucSucursal = if (sucursalActual == "Esteli") "RUC 0011105970017Q" else "RUC 2411702011002A"

        var itemsHtml = ""
        venta.productos.forEach {
            val subtotal = (it.precioSeleccionado ?: 0.0) * (it.cantidad ?: 0)
            itemsHtml += """
            <tr>
                <td>${it.nombre}</td>
                <td style="text-align: center;">${it.cantidad}</td>
                <td style="text-align: right;">$${String.format("%.2f", it.precioSeleccionado)}</td>
                <td style="text-align: right;">$${String.format("%.2f", subtotal)}</td>
            </tr>
        """.trimIndent()
        }

        val htmlContent = """
        <html>
        <head>
            <link href="https://fonts.googleapis.com/css2?family=Afacad:wght@500&display=swap" rel="stylesheet">
            <style>
                @media print { body { padding: 0; margin: 0; } }
                body { font-family: 'Afacad', sans-serif; padding: 60px 20px 20px 20px; color: #333; max-width: 800px; margin: auto; }
                .logo-container { text-align: center; margin-bottom: 15px; margin-top: 40px; }
                .logo-img { height: 80px; width: auto; }
                h1 { text-align: center; color: #B71C1C; margin-top: 5px; text-transform: uppercase; font-size: 24px; font-weight: 600; }
                .sucursal { text-align: center; font-weight: bold; margin-bottom: 2px; color: #555; }
                .ruc { text-align: center; font-size: 13px; color: #777; margin-bottom: 10px; }
                .header { display: flex; justify-content: space-between; margin-bottom: 20px; border-bottom: 2px solid #B71C1C; padding-bottom: 10px; }
                .header p { margin: 3px 0; font-size: 15px; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                th { background-color: #f8f8f8; color: #555; font-weight: 600; text-transform: uppercase; font-size: 13px; border-bottom: 1px solid #ddd; padding: 10px 8px; }
                td { border-bottom: 1px solid #eee; padding: 12px 8px; text-align: left; font-size: 15px; }
                .summary { text-align: right; margin-top: 15px; }
                .summary p { margin: 5px 0; font-size: 15px; }
                .total-box { background-color: #fef2f2; padding: 12px; display: inline-block; border-radius: 6px; margin-top: 10px; border: 1px solid #fee2e2; }
                .total { font-size: 22px; font-weight: 600; color: #B71C1C; }
                .payment-info { font-style: italic; color: #666; font-size: 14px; margin-top: 8px; }
                .footer { text-align: center; margin-top: 40px; font-size: 13px; color: #888; border-top: 1px dashed #ccc; padding-top: 20px; }
            </style>
        </head>
        <body>
            <div class="logo-container">
                <img src="data:image/png;base64,$logoBase64" class="logo-img">
            </div>
            <h1>Copia de Recibo</h1>
            <p class="sucursal">Sucursal: $sucursalActual</p>
            <p class="ruc">$rucSucursal</p>
            <div class="header">
                <div>
                    <p><b>Factura #:</b> ${venta.id}</p>
                    <p><b>Fecha:</b> $fechaStr</p>
                </div>
                <div style="text-align: right;">
                    <p><b>Cliente:</b> ${venta.clienteNombre}</p>
                    <p><b>Atendido por:</b> ${venta.vendedorNombre ?: "Vendedor"}</p>
                </div>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>Producto</th>
                        <th style="text-align: center;">Cant.</th>
                        <th style="text-align: right;">Precio</th>
                        <th style="text-align: right;">Subtotal</th>
                    </tr>
                </thead>
                <tbody>
                    $itemsHtml
                </tbody>
            </table>
            <div class="summary">
                <p>Subtotal: <b>$${String.format("%.2f", venta.subtotal)}</b></p>
                <p>Descuento (${venta.descuentoPorcentaje}%): <span style="color: #d32f2f;">-$${String.format("%.2f", venta.descuentoMonto)}</span></p>
                <div class="total-box">
                    <span class="total">TOTAL: $${String.format("%.2f", venta.total)}</span>
                </div>
                <div style="margin-top: 15px;">
                    <p class="payment-info">Pagó con: $${String.format("%.2f", venta.pagoCon)}</p>
                    <p class="payment-info">Su cambio: $${String.format("%.2f", venta.cambio)}</p>
                </div>
            </div>
            <div class="footer">
                <p>Reimpresión de Comprobante</p>
            </div>
        </body>
        </html>
    """.trimIndent()

        imprimirHTML(htmlContent)
    }

    private fun imprimirHTML(html: String) {
        val webView = WebView(this)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Recibo_${System.currentTimeMillis()}")
                printManager.print("Documento de Impresión", printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
    }
}
