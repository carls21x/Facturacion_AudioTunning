package com.tuempresa.facturacion

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FacturacionActivity : AppCompatActivity() {

    private lateinit var etNombreCliente: TextInputEditText
    private lateinit var etBuscarProducto: TextInputEditText
    private lateinit var etDescuento: TextInputEditText
    private lateinit var etPagoCon: TextInputEditText
    private lateinit var tilBuscarProducto: TextInputLayout
    private lateinit var tilDescuento: TextInputLayout
    private lateinit var tvTotal: TextView
    private lateinit var tvSubtotalDisplay: TextView
    private lateinit var tvDescuentoMontoDisplay: TextView
    private lateinit var tvCambio: TextView
    private lateinit var llDescuentoDisplay: LinearLayout
    private lateinit var rvProductos: RecyclerView
    private lateinit var rvCarrito: RecyclerView
    private lateinit var btnFinalizar: MaterialCardView
    private lateinit var btnCancelar: MaterialCardView

    private lateinit var dbRefProductos: DatabaseReference
    private lateinit var dbRefVentas: DatabaseReference
    private lateinit var dbRefUser: DatabaseReference

    private val listaProductos = mutableListOf<Producto>()
    private val listaCarrito = mutableListOf<ProductoVendido>()
    
    private lateinit var productoAdapter: ProductoFacturaAdapter
    private lateinit var carritoAdapter: CarritoAdapter

    private var subtotalVenta = 0.0
    private var descuentoPorcentaje = 0.0
    private var descuentoMonto = 0.0
    private var totalVenta = 0.0
    private var pagoCon = 0.0
    private var cambio = 0.0
    private var userRole: String? = null
    private var userNombre: String? = null
    
    private var progressDialog: ProgressDialog? = null

    private val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedCode = result.data?.getStringExtra("SCAN_RESULT")
            if (scannedCode != null) {
                etBuscarProducto.setText(scannedCode)
                productoAdapter.filtrar(scannedCode)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facturacion)

        val pathProductos = PreferenciasConfig.getPathProductos(this)
        val pathVentas = PreferenciasConfig.getPathVentas(this)

        dbRefProductos = FirebaseDatabase.getInstance().getReference(pathProductos)
        dbRefVentas = FirebaseDatabase.getInstance().getReference(pathVentas)
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            dbRefUser = FirebaseDatabase.getInstance().getReference("users").child(currentUser.uid)
            obtenerDatosUsuario()
        }

        inicializarVistas()
        configurarRecyclerViews()
        cargarProductos()
        configurarBuscador()
        configurarDescuento()
        configurarPago()

        btnFinalizar.setOnClickListener { finalizarVenta() }
        btnCancelar.setOnClickListener { finish() }
    }

    private fun inicializarVistas() {
        etNombreCliente = findViewById(R.id.etNombreCliente)
        etBuscarProducto = findViewById(R.id.etBuscarProducto)
        etDescuento = findViewById(R.id.etDescuento)
        etPagoCon = findViewById(R.id.etPagoCon)
        tilBuscarProducto = findViewById(R.id.tilBuscarProducto)
        tilDescuento = findViewById(R.id.tilDescuento)
        tvTotal = findViewById(R.id.tvTotal)
        tvSubtotalDisplay = findViewById(R.id.tvSubtotalDisplay)
        tvDescuentoMontoDisplay = findViewById(R.id.tvDescuentoMontoDisplay)
        tvCambio = findViewById(R.id.tvCambio)
        llDescuentoDisplay = findViewById(R.id.llDescuentoDisplay)
        rvProductos = findViewById(R.id.rvSeleccionarProductos)
        rvCarrito = findViewById(R.id.rvCarrito)
        btnFinalizar = findViewById(R.id.btnFinalizarVentaCard)
        btnCancelar = findViewById(R.id.btnCancelarFacturacionCard)

        tilBuscarProducto.setEndIconOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            scannerLauncher.launch(intent)
        }
    }

    private fun obtenerDatosUsuario() {
        dbRefUser.get().addOnSuccessListener { snapshot ->
            userRole = snapshot.child("role").value?.toString()
            userNombre = snapshot.child("nombre").value?.toString()
            
            if (userRole == "gerente" || userRole == "admin") {
                tilDescuento.visibility = View.VISIBLE
                llDescuentoDisplay.visibility = View.VISIBLE
            }
        }
    }

    private fun configurarRecyclerViews() {
        rvProductos.layoutManager = LinearLayoutManager(this)
        
        productoAdapter = ProductoFacturaAdapter(
            listaProductos, 
            { productoVendido -> 
                listaCarrito.add(productoVendido)
                actualizarStockLocal(productoVendido.productoId ?: "", -(productoVendido.cantidad ?: 0))
                carritoAdapter.notifyDataSetChanged()
                actualizarTotal()
            },
            { producto -> 
                consultarEnOtraSucursal(producto)
            }
        )
        rvProductos.adapter = productoAdapter

        rvCarrito.layoutManager = LinearLayoutManager(this)
        carritoAdapter = CarritoAdapter(listaCarrito) { position ->
            val eliminado = listaCarrito[position]
            actualizarStockLocal(eliminado.productoId ?: "", eliminado.cantidad ?: 0)
            listaCarrito.removeAt(position)
            carritoAdapter.notifyDataSetChanged()
            actualizarTotal()
        }
        rvCarrito.adapter = carritoAdapter
    }

    private fun actualizarStockLocal(id: String, cambio: Int) {
        val index = listaProductos.indexOfFirst { it.id == id }
        if (index != -1) {
            val p = listaProductos[index]
            val nuevoStock = Math.max(0, (p.stock ?: 0) + cambio)
            listaProductos[index] = p.copy(stock = nuevoStock)
            
            val filtroActual = etBuscarProducto.text.toString()
            productoAdapter.actualizarLista(listaProductos)
            productoAdapter.filtrar(filtroActual)
        }
    }

    private fun consultarEnOtraSucursal(producto: Producto) {
        val otraSucursalPath = PreferenciasConfig.getPathProductosOtraSucursal(this)
        val sucursalNombre = if (PreferenciasConfig.getSucursal(this) == "Jinotega") "Esteli" else "Jinotega"
        
        val db = FirebaseDatabase.getInstance().getReference(otraSucursalPath)
        val query = db.orderByChild("nombre").equalTo(producto.nombre)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val primerProductoEncontrado = snapshot.children.first()
                    val stockRemoto = primerProductoEncontrado.child("stock").getValue(Int::class.java) ?: 0
                    Toast.makeText(this@FacturacionActivity, "En $sucursalNombre hay $stockRemoto unidades.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@FacturacionActivity, "Producto no encontrado en $sucursalNombre.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FacturacionActivity, "Error al consultar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun configurarBuscador() {
        etBuscarProducto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                productoAdapter.filtrar(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun configurarDescuento() {
        etDescuento.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                actualizarTotal()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun configurarPago() {
        etPagoCon.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                actualizarCambio()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun cargarProductos() {
        dbRefProductos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temporalList = mutableListOf<Producto>()
                for (prodSnapshot in snapshot.children) {
                    var producto = prodSnapshot.getValue(Producto::class.java)?.copy(id = prodSnapshot.key)
                    if (producto != null) {
                        val stockRealServidor = producto.stock ?: 0
                        val enCarrito = listaCarrito.find { it.productoId == producto!!.id }?.cantidad ?: 0
                        val stockVisible = Math.max(0, stockRealServidor - enCarrito)
                        producto = producto.copy(stock = stockVisible)
                        temporalList.add(producto)
                    }
                }
                listaProductos.clear()
                listaProductos.addAll(temporalList)
                
                val filtroActual = etBuscarProducto.text.toString()
                productoAdapter.actualizarLista(listaProductos)
                productoAdapter.filtrar(filtroActual)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun actualizarTotal() {
        subtotalVenta = listaCarrito.sumOf { (it.precioSeleccionado ?: 0.0) * (it.cantidad ?: 0) }
        descuentoPorcentaje = etDescuento.text.toString().toDoubleOrNull() ?: 0.0
        
        descuentoMonto = subtotalVenta * (descuentoPorcentaje / 100.0)
        totalVenta = subtotalVenta - descuentoMonto
        
        if (totalVenta < 0) totalVenta = 0.0
        
        tvSubtotalDisplay.text = "$${String.format("%.2f", subtotalVenta)}"
        tvDescuentoMontoDisplay.text = "-$${String.format("%.2f", descuentoMonto)} (${descuentoPorcentaje}%)"
        tvTotal.text = "$${String.format("%.2f", totalVenta)}"
        actualizarCambio()
    }

    private fun actualizarCambio() {
        pagoCon = etPagoCon.text.toString().toDoubleOrNull() ?: 0.0
        cambio = if (pagoCon >= totalVenta) pagoCon - totalVenta else 0.0
        tvCambio.text = "$${String.format("%.2f", cambio)}"
    }

    private fun finalizarVenta() {
        val cliente = etNombreCliente.text.toString().trim()
        val pagoConStr = etPagoCon.text.toString().trim()
        
        if (cliente.isEmpty() || listaCarrito.isEmpty()) {
            Toast.makeText(this, "Nombre de cliente y productos requeridos", Toast.LENGTH_SHORT).show()
            return
        }

        if (pagoConStr.isEmpty() || (pagoConStr.toDoubleOrNull() ?: 0.0) < totalVenta) {
            Toast.makeText(this, "El pago debe ser igual o mayor al total", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog = ProgressDialog(this).apply {
            setMessage("Verificando stock y procesando venta...")
            setCancelable(false)
            show()
        }

        descontarStockSecuencial(0, cliente)
    }

    private fun descontarStockSecuencial(index: Int, cliente: String) {
        if (index >= listaCarrito.size) {
            registrarVentaFinal(cliente)
            return
        }

        val item = listaCarrito[index]
        val productoRef = dbRefProductos.child(item.productoId ?: "")

        productoRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val p = mutableData.getValue(Producto::class.java)
                if (p == null) return Transaction.success(mutableData)

                val currentStock = p.stock ?: 0
                val requested = item.cantidad ?: 0

                if (currentStock < requested) {
                    return Transaction.abort() 
                }

                mutableData.child("stock").value = currentStock - requested
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    descontarStockSecuencial(index + 1, cliente)
                } else {
                    val itemQueFallo = listaCarrito[index]
                    revertirStock(index - 1)
                    actualizarStockLocal(itemQueFallo.productoId ?: "", itemQueFallo.cantidad ?: 0)
                    listaCarrito.removeAt(index)
                    
                    runOnUiThread {
                        carritoAdapter.notifyDataSetChanged()
                        actualizarTotal()
                        progressDialog?.dismiss()
                        Toast.makeText(this@FacturacionActivity, "¡ERROR! Stock insuficiente para: ${itemQueFallo.nombre}. Se ha quitado del carrito.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun revertirStock(index: Int) {
        if (index < 0) return
        val item = listaCarrito[index]
        val productoRef = dbRefProductos.child(item.productoId ?: "")
        
        productoRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val p = mutableData.getValue(Producto::class.java) ?: return Transaction.success(mutableData)
                mutableData.child("stock").value = (p.stock ?: 0) + (item.cantidad ?: 0)
                return Transaction.success(mutableData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                revertirStock(index - 1)
            }
        })
    }

    private fun registrarVentaFinal(cliente: String) {
        val ventaId = dbRefVentas.push().key ?: return
        val vendedorId = FirebaseAuth.getInstance().currentUser?.uid
        
        val venta = Venta(
            id = ventaId,
            clienteNombre = cliente,
            productos = ArrayList(listaCarrito),
            subtotal = subtotalVenta,
            descuentoPorcentaje = descuentoPorcentaje,
            descuentoMonto = descuentoMonto,
            total = totalVenta,
            pagoCon = pagoCon,
            cambio = cambio,
            fecha = System.currentTimeMillis(),
            vendedorId = vendedorId,
            vendedorNombre = userNombre
        )

        dbRefVentas.child(ventaId).setValue(venta).addOnSuccessListener {
            progressDialog?.dismiss()
            Toast.makeText(this, "Venta registrada con éxito", Toast.LENGTH_SHORT).show()
            generarReciboHTML(venta)
            limpiarPantalla()
        }
    }

    private fun limpiarPantalla() {
        etNombreCliente.text?.clear()
        etBuscarProducto.text?.clear()
        etDescuento.text?.clear()
        etPagoCon.text?.clear()
        listaCarrito.clear()
        carritoAdapter.notifyDataSetChanged()
        actualizarTotal()
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
            <h1>Recibo de Venta</h1>
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
                <p>¡Gracias por preferirnos!</p>
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
