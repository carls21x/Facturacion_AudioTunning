package com.tuempresa.facturacion

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*

class ConsultarExistenciaActivity : AppCompatActivity() {

    private lateinit var tvSucursal: TextView
    private lateinit var etBuscar: TextInputEditText
    private lateinit var tilBuscar: TextInputLayout
    private lateinit var rvExistencias: RecyclerView
    private lateinit var cardVolver: MaterialCardView

    private lateinit var dbRef: DatabaseReference
    private val listaProductos = mutableListOf<Producto>()
    private lateinit var adapter: ExistenciaAdapter

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
        setContentView(R.layout.activity_consultar_existencia)

        val sucursalActual = PreferenciasConfig.getSucursal(this)
        val sucursalRemota = if (sucursalActual == "Jinotega") "Esteli" else "Jinotega"
        val pathOtraSucursal = PreferenciasConfig.getPathProductosOtraSucursal(this)

        dbRef = FirebaseDatabase.getInstance().getReference(pathOtraSucursal)

        inicializarVistas(sucursalRemota)
        configurarRecyclerView()
        cargarProductosRemotos()
        configurarBuscador()

        cardVolver.setOnClickListener { finish() }
    }

    private fun inicializarVistas(sucursalRemota: String) {
        tvSucursal = findViewById(R.id.tvSucursalConsulta)
        etBuscar = findViewById(R.id.etBuscarExistencia)
        tilBuscar = findViewById(R.id.tilBuscarExistencia)
        rvExistencias = findViewById(R.id.rvExistencias)
        cardVolver = findViewById(R.id.btnVolverExistenciaCard)

        tvSucursal.text = "Consultando sucursal: $sucursalRemota"

        tilBuscar.setEndIconOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            scannerLauncher.launch(intent)
        }
    }

    private fun configurarRecyclerView() {
        rvExistencias.layoutManager = LinearLayoutManager(this)
        adapter = ExistenciaAdapter(listaProductos)
        rvExistencias.adapter = adapter
    }

    private fun cargarProductosRemotos() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaProductos.clear()
                for (prodSnapshot in snapshot.children) {
                    val producto = prodSnapshot.getValue(Producto::class.java)?.copy(id = prodSnapshot.key)
                    if (producto != null) {
                        listaProductos.add(producto)
                    }
                }
                adapter.actualizarLista(listaProductos)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ConsultarExistenciaActivity, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
}
