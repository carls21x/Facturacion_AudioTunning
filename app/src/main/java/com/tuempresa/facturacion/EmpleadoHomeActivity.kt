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

class EmpleadoHomeActivity : AppCompatActivity() {

    private lateinit var cardFacturar: MaterialCardView
    private lateinit var cardExistencias: MaterialCardView
    private lateinit var cardCambiarSucursal: MaterialCardView
    private lateinit var cardCerrar: MaterialCardView
    private lateinit var tvSucursalActual: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empleado_home)

        auth = FirebaseAuth.getInstance()

        // Inicializar componentes
        cardFacturar = findViewById(R.id.btnFacturarE)
        cardExistencias = findViewById(R.id.btnConsultarExistencia)
        cardCambiarSucursal = findViewById(R.id.btnCambiarSucursal)
        cardCerrar = findViewById(R.id.btnCerrarEmpleadoCard)
        tvSucursalActual = findViewById(R.id.tvSucursalActual)

        // Mostrar sucursal actual
        actualizarTextoUI()

        // Configurar clics
        cardFacturar.setOnClickListener { startActivity(Intent(this, FacturacionActivity::class.java)) }
        cardExistencias.setOnClickListener { startActivity(Intent(this, ConsultarExistenciaActivity::class.java)) }
        
        cardCambiarSucursal.setOnClickListener { mostrarSeleccionSucursalCustom() }

        cardCerrar.setOnClickListener {
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
        // Hacer el fondo del diálogo original transparente para que se vea el diseño redondeado del card
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
