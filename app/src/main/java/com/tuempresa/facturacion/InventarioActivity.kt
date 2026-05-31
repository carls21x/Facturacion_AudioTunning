package com.tuempresa.facturacion

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*

class InventarioActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private val listaProductos = mutableListOf<Producto>()
    private lateinit var adapter: ProductoAdapter
    private var productoSeleccionadoId: String? = null

    private lateinit var btnAgregar: MaterialCardView
    private lateinit var btnActualizar: MaterialCardView
    private lateinit var btnEliminar: MaterialCardView
    private lateinit var btnImprimirCodigos: MaterialCardView
    private lateinit var fab: FloatingActionButton
    private lateinit var formContainer: LinearLayout
    private lateinit var mainLayout: ViewGroup
    
    private lateinit var etBuscar: TextInputEditText
    private lateinit var tilBuscar: TextInputLayout

    private lateinit var etNombre: TextInputEditText
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var etCategoria: TextInputEditText
    private lateinit var etProveedor: TextInputEditText
    private lateinit var etPrecioCosto: TextInputEditText
    private lateinit var etPrecio1: TextInputEditText
    private lateinit var etPrecio2: TextInputEditText
    private lateinit var etPrecio3: TextInputEditText
    private lateinit var etStock: TextInputEditText

    private val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedCode = result.data?.getStringExtra("SCAN_RESULT")
            if (scannedCode != null) {
                etBuscar.setText(scannedCode)
                adapter.filtrar(scannedCode)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)

        val pathProductos = PreferenciasConfig.getPathProductos(this)
        dbRef = FirebaseDatabase.getInstance().getReference(pathProductos)

        inicializarVistas()
        configurarRecyclerView()
        cargarProductos()
        configurarBuscador()

        fab.setOnClickListener { toggleFormulario() }

        btnAgregar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val stockStr = etStock.text.toString().trim()
            val precio1Str = etPrecio1.text.toString().trim()

            if (nombre.isEmpty() || stockStr.isEmpty() || precio1Str.isEmpty()) {
                Toast.makeText(this, "Nombre, Stock y Precio 1 son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = dbRef.push().key ?: return@setOnClickListener
            val producto = Producto(
                id, nombre, etDescripcion.text.toString().trim(), etCategoria.text.toString().trim(),
                etProveedor.text.toString().trim(), etPrecioCosto.text.toString().toDoubleOrNull(),
                precio1Str.toDoubleOrNull(), etPrecio2.text.toString().toDoubleOrNull(),
                etPrecio3.text.toString().toDoubleOrNull(), stockStr.toIntOrNull()
            )

            dbRef.child(id).setValue(producto)
            Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show()
            limpiarCampos()
            toggleFormulario()
        }

        btnActualizar.setOnClickListener {
            val id = productoSeleccionadoId ?: return@setOnClickListener
            val productoActualizado = Producto(
                id, etNombre.text.toString().trim(), etDescripcion.text.toString().trim(),
                etCategoria.text.toString().trim(), etProveedor.text.toString().trim(),
                etPrecioCosto.text.toString().toDoubleOrNull(), etPrecio1.text.toString().toDoubleOrNull(),
                etPrecio2.text.toString().toDoubleOrNull(), etPrecio3.text.toString().toDoubleOrNull(),
                etStock.text.toString().toIntOrNull()
            )

            dbRef.child(id).setValue(productoActualizado)
            Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
            limpiarCampos()
            toggleFormulario()
        }

        btnEliminar.setOnClickListener {
            val id = productoSeleccionadoId ?: return@setOnClickListener
            dbRef.child(id).removeValue()
            Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
            limpiarCampos()
            toggleFormulario()
        }

        btnImprimirCodigos.setOnClickListener {
            startActivity(Intent(this, ImprimirCodigosActivity::class.java))
        }
    }

    private fun inicializarVistas() {
        mainLayout = findViewById(R.id.main_content_layout)
        formContainer = findViewById(R.id.form_container)
        fab = findViewById(R.id.fab_show_form)

        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        etCategoria = findViewById(R.id.etCategoria)
        etProveedor = findViewById(R.id.etProveedor)
        etPrecioCosto = findViewById(R.id.etPrecioCosto)
        etPrecio1 = findViewById(R.id.etPrecio1)
        etPrecio2 = findViewById(R.id.etPrecio2)
        etPrecio3 = findViewById(R.id.etPrecio3)
        etStock = findViewById(R.id.etStock)

        etBuscar = findViewById(R.id.etBuscarProducto)
        tilBuscar = findViewById(R.id.tilBuscarProducto)
        btnAgregar = findViewById(R.id.btnAgregarCard)
        btnActualizar = findViewById(R.id.btnActualizarCard)
        btnEliminar = findViewById(R.id.btnEliminarCard)
        btnImprimirCodigos = findViewById(R.id.btnIrAImprimirCodigos)
        recyclerView = findViewById(R.id.recyclerViewProductos)
        
        tilBuscar.setEndIconOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            scannerLauncher.launch(intent)
        }
    }

    private fun configurarRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductoAdapter(listaProductos) { producto ->
            productoSeleccionadoId = producto.id
            etNombre.setText(producto.nombre)
            etDescripcion.setText(producto.descripcion)
            etCategoria.setText(producto.categoria)
            etProveedor.setText(producto.proveedor)
            etPrecioCosto.setText(producto.precioCosto.toString())
            etPrecio1.setText(producto.precio1.toString())
            etPrecio2.setText(producto.precio2.toString())
            etPrecio3.setText(producto.precio3.toString())
            etStock.setText(producto.stock.toString())

            if (formContainer.visibility == View.GONE) toggleFormulario()
            else actualizarEstadoBotones(true)
        }
        recyclerView.adapter = adapter
    }

    private fun configurarBuscador() {
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filtrar(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun cargarProductos() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaProductos.clear()
                for (prodSnapshot in snapshot.children) {
                    val producto = prodSnapshot.getValue(Producto::class.java)?.copy(id = prodSnapshot.key)
                    if (producto != null) listaProductos.add(producto)
                }
                adapter.actualizarLista(listaProductos)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun toggleFormulario() {
        TransitionManager.beginDelayedTransition(mainLayout)
        if (formContainer.visibility == View.GONE) {
            formContainer.visibility = View.VISIBLE
            fab.animate().rotation(45f).setDuration(300).start()
            actualizarEstadoBotones(productoSeleccionadoId != null)
        } else {
            formContainer.visibility = View.GONE
            fab.animate().rotation(0f).setDuration(300).start()
            limpiarCampos()
        }
    }

    private fun limpiarCampos() {
        etNombre.text?.clear()
        etDescripcion.text?.clear()
        etCategoria.text?.clear()
        etProveedor.text?.clear()
        etPrecioCosto.text?.clear()
        etPrecio1.text?.clear()
        etPrecio2.text?.clear()
        etPrecio3.text?.clear()
        etStock.text?.clear()
        productoSeleccionadoId = null
        actualizarEstadoBotones(false)
    }

    private fun actualizarEstadoBotones(productoSeleccionado: Boolean) {
        if (productoSeleccionado) {
            btnAgregar.visibility = View.GONE
            btnActualizar.visibility = View.VISIBLE
            btnEliminar.visibility = View.VISIBLE
        } else {
            btnAgregar.visibility = View.VISIBLE
            btnActualizar.visibility = View.GONE
            btnEliminar.visibility = View.GONE
        }
    }
}
