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

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var cardGestionUsuarios: MaterialCardView
    private lateinit var cardRegistrarUsuario: MaterialCardView
    private lateinit var cardGestionInventario: MaterialCardView
    private lateinit var cardVerReportes: MaterialCardView
    private lateinit var cardHistorial: MaterialCardView
    private lateinit var cardConsultarExistencia: MaterialCardView
    private lateinit var cardFacturacionDestacada: MaterialCardView
    private lateinit var cardCerrarSesion: MaterialCardView
    
    private lateinit var cardCambiarSucursal: MaterialCardView
    private lateinit var tvSucursalActual: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        auth = FirebaseAuth.getInstance()

        // Inicializar todas las tarjetas como MaterialCardView según el nuevo XML
        cardGestionUsuarios = findViewById(R.id.btnGestionUsuarios)
        cardRegistrarUsuario = findViewById(R.id.btnRegistrarUsuario)
        cardGestionInventario = findViewById(R.id.btnGestionInventario)
        cardVerReportes = findViewById(R.id.btnVerReportes)
        cardHistorial = findViewById(R.id.btnHistorialVentas)
        cardConsultarExistencia = findViewById(R.id.btnConsultarExistencia)
        cardFacturacionDestacada = findViewById(R.id.btnFacturacion)
        cardCerrarSesion = findViewById(R.id.btnCerrarAdminCard)
        
        cardCambiarSucursal = findViewById(R.id.btnCambiarSucursal)
        tvSucursalActual = findViewById(R.id.tvSucursalActual)

        // Mostrar sucursal actual al iniciar
        actualizarTextoUI()

        // Configurar acciones de clics
        cardGestionUsuarios.setOnClickListener { startActivity(Intent(this, GestionUsuariosActivity::class.java)) }
        cardRegistrarUsuario.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        cardGestionInventario.setOnClickListener { startActivity(Intent(this, InventarioActivity::class.java)) }
        cardVerReportes.setOnClickListener { startActivity(Intent(this, ReportesActivity::class.java)) }
        cardHistorial.setOnClickListener { startActivity(Intent(this, HistorialVentasActivity::class.java)) }
        cardConsultarExistencia.setOnClickListener { startActivity(Intent(this, ConsultarExistenciaActivity::class.java)) }
        cardFacturacionDestacada.setOnClickListener { startActivity(Intent(this, FacturacionActivity::class.java)) }

        // Lógica para cambiar sucursal rápidamente con diseño custom
        cardCambiarSucursal.setOnClickListener { mostrarSeleccionSucursalCustom() }

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
