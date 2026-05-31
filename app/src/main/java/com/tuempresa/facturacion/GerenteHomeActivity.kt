package com.tuempresa.facturacion

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth

class GerenteHomeActivity : AppCompatActivity() {

    private lateinit var cardReportes: MaterialCardView
    private lateinit var cardRegistrar: MaterialCardView
    private lateinit var cardUsuarios: MaterialCardView
    private lateinit var cardInventario: MaterialCardView
    private lateinit var cardHistorial: MaterialCardView
    private lateinit var cardConsultarExistencia: MaterialCardView
    private lateinit var cardFacturacionDestacada: MaterialCardView
    private lateinit var cardCerrarSesion: MaterialCardView
    
    private lateinit var btnCambiarSucursal: MaterialCardView
    private lateinit var tvSucursalActual: TextView
    
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerente_home)

        auth = FirebaseAuth.getInstance()

        // Inicializar todas las tarjetas como MaterialCardView
        cardReportes = findViewById(R.id.btnVerReportesG)
        cardRegistrar = findViewById(R.id.btnRegistrarUsuario)
        cardUsuarios = findViewById(R.id.btnGestionUsuarios)
        cardInventario = findViewById(R.id.btnVerInventarioG)
        cardHistorial = findViewById(R.id.btnHistorialVentas)
        cardConsultarExistencia = findViewById(R.id.btnConsultarExistencia)
        cardFacturacionDestacada = findViewById(R.id.btnFacturacion)
        cardCerrarSesion = findViewById(R.id.btnCerrarGerenteCard)
        
        btnCambiarSucursal = findViewById(R.id.btnCambiarSucursal)
        tvSucursalActual = findViewById(R.id.tvSucursalActual)

        // Mostrar sucursal actual
        actualizarTextoUI()

        // Configurar clics
        cardReportes.setOnClickListener { startActivity(Intent(this, ReportesActivity::class.java)) }
        cardRegistrar.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        cardUsuarios.setOnClickListener { startActivity(Intent(this, GestionUsuariosActivity::class.java)) }
        cardInventario.setOnClickListener { startActivity(Intent(this, InventarioActivity::class.java)) }
        cardHistorial.setOnClickListener { startActivity(Intent(this, HistorialVentasActivity::class.java)) }
        cardConsultarExistencia.setOnClickListener { startActivity(Intent(this, ConsultarExistenciaActivity::class.java)) }
        cardFacturacionDestacada.setOnClickListener { startActivity(Intent(this, FacturacionActivity::class.java)) }

        btnCambiarSucursal.setOnClickListener { mostrarSeleccionSucursalCustom() }

        cardCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun actualizarTextoUI() {
        val sucursal = PreferenciasConfig.getSucursal(this)
        tvSucursalActual.text = "Sucursal: $sucursal"
    }

    private fun mostrarSeleccionSucursalCustom() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_sucursal, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cardJinotega = dialogView.findViewById<MaterialCardView>(R.id.cardJinotega)
        val cardEsteli = dialogView.findViewById<MaterialCardView>(R.id.cardEsteli)

        cardJinotega.setOnClickListener {
            PreferenciasConfig.setSucursal(this, "Jinotega")
            actualizarTextoUI()
            Toast.makeText(this, "Sucursal cambiada a Jinotega", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        cardEsteli.setOnClickListener {
            PreferenciasConfig.setSucursal(this, "Esteli")
            actualizarTextoUI()
            Toast.makeText(this, "Sucursal cambiada a Esteli", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
