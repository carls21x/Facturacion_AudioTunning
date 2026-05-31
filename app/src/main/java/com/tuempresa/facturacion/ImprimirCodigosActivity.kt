package com.tuempresa.facturacion

import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.*

class ImprimirCodigosActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private val listaProductos = mutableListOf<Producto>()
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imprimir_codigos)

        val pathProductos = PreferenciasConfig.getPathProductos(this)
        dbRef = FirebaseDatabase.getInstance().getReference(pathProductos)
        webView = findViewById(R.id.webViewImpresion)

        configurarWebView()

        findViewById<MaterialCardView>(R.id.btnGenerarCodigos).setOnClickListener {
            cargarYGenerarHTML()
        }

        findViewById<MaterialCardView>(R.id.btnVolverCodigos).setOnClickListener {
            finish()
        }
    }

    private fun configurarWebView() {
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Delay para asegurar que los QRs se generen antes de imprimir
                view?.postDelayed({
                    lanzarPrintManager()
                }, 2000)
            }
        }
    }

    private fun cargarYGenerarHTML() {
        Toast.makeText(this, "Preparando inventario...", Toast.LENGTH_SHORT).show()
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaProductos.clear()
                for (prodSnapshot in snapshot.children) {
                    val producto = prodSnapshot.getValue(Producto::class.java)?.copy(id = prodSnapshot.key)
                    if (producto != null && (producto.stock ?: 0) > 0) {
                        listaProductos.add(producto)
                    }
                }

                if (listaProductos.isEmpty()) {
                    Toast.makeText(this@ImprimirCodigosActivity, "No hay productos con stock", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ImprimirCodigosActivity, "Generando etiquetas QR...", Toast.LENGTH_LONG).show()
                    generarFormatoImpresion()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ImprimirCodigosActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generarFormatoImpresion() {
        val htmlContent = StringBuilder()

        htmlContent.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <link href="https://fonts.googleapis.com/css2?family=Afacad:wght@500&family=Oswald:wght@700&display=swap" rel="stylesheet">
                <script src="https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js"></script>
                <style>
                    @media print {
                        body { margin: 0; padding: 0; }
                        @page { size: auto; margin: 5mm; }
                    }
                    body { 
                        font-family: 'Afacad', sans-serif; 
                        margin: 0; 
                        padding: 5mm; 
                        background-color: white; 
                    }
                    #labels-grid {
                        display: grid;
                        grid-template-columns: repeat(3, 1fr);
                        gap: 8mm; /* Espacio entre etiquetas */
                    }
                    .label-container { 
                        width: 100%;
                        height: 62mm; /* Altura para que quepan 4 filas por página */
                        border: 1px dashed #ccc;
                        display: flex;
                        flex-direction: column; /* CORREGIDO: De vertical a column */
                        align-items: center;
                        justify-content: space-between;
                        text-align: center;
                        padding: 4mm;
                        box-sizing: border-box;
                        page-break-inside: avoid;
                    }
                    .product-name { 
                        font-family: 'Oswald', sans-serif;
                        font-size: 12pt;
                        text-transform: uppercase;
                        margin: 0;
                        height: 2.4em;
                        overflow: hidden;
                        line-height: 1.1;
                        color: #000;
                        display: flex;
                        align-items: center;
                    }
                    .qr-code-box {
                        width: 35mm;
                        height: 35mm;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    /* Asegura que el canvas del QR llene el contenedor */
                    .qr-code-box canvas, .qr-code-box img {
                        max-width: 100%;
                        max-height: 100%;
                    }
                    .product-id {
                        font-size: 8pt;
                        margin-top: 2mm;
                        color: #333;
                        font-weight: 500;
                        letter-spacing: 0.5px;
                    }
                </style>
            </head>
            <body>
                <div id="labels-grid">
        """.trimIndent())

        for (producto in listaProductos) {
            val stock = producto.stock ?: 0
            for (i in 1..stock) {
                val uniqueId = "qr_${producto.id?.filter { it.isLetterOrDigit() }}_$i"
                htmlContent.append("""
                    <div class="label-container">
                        <div class="product-name">${producto.nombre}</div>
                        <div id="$uniqueId" class="qr-code-box"></div>
                        <div class="product-id">ID: ${producto.id}</div>
                        <script>
                            new QRCode(document.getElementById("$uniqueId"), {
                                text: "${producto.id}",
                                width: 128,
                                height: 128,
                                colorDark : "#000000",
                                colorLight : "#ffffff",
                                correctLevel : QRCode.CorrectLevel.M /* Nivel medio para mayor rapidez de escaneo */
                            });
                        </script>
                    </div>
                """.trimIndent())
            }
        }

        htmlContent.append("""
                </div>
            </body>
            </html>
        """.trimIndent())

        webView.loadDataWithBaseURL("https://cdnjs.cloudflare.com", htmlContent.toString(), "text/HTML", "UTF-8", null)
    }

    private fun lanzarPrintManager() {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = webView.createPrintDocumentAdapter("Etiquetas_QR_SoundSystem")
        printManager.print("Impresión de Etiquetas QR", printAdapter, PrintAttributes.Builder().build())
    }
}
