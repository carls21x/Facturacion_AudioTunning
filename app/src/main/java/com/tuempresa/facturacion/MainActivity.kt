package com.tuempresa.facturacion

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: MaterialCardView
    private lateinit var btnRegister: Button
    private lateinit var btnForgotten: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        // FORZAR MODO CLARO
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        btnForgotten = findViewById(R.id.btnForgotten)

        btnForgotten.setOnClickListener {
            startActivity(Intent(this, ForgottenPassword::class.java))
        }

        // Si ya hay sesión activa
        val currentUser = auth.currentUser
        if (currentUser != null) {
            redirigirSegunRol(currentUser.uid)
        }

        // Iniciar sesión
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            redirigirSegunRol(userId)
                        }
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Ir al registro
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun redirigirSegunRol(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val role = snapshot.child("role").value?.toString()

            if (role != null) {
                mostrarSeleccionSucursalCustom(role)
            } else {
                Toast.makeText(this, "No se encontró un rol válido", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarSeleccionSucursalCustom(role: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_sucursal, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cardJinotega = dialogView.findViewById<MaterialCardView>(R.id.cardJinotega)
        val cardEsteli = dialogView.findViewById<MaterialCardView>(R.id.cardEsteli)

        cardJinotega.setOnClickListener {
            PreferenciasConfig.setSucursal(this, "Jinotega")
            navegarAPanel(role)
            dialog.dismiss()
        }

        cardEsteli.setOnClickListener {
            PreferenciasConfig.setSucursal(this, "Esteli")
            navegarAPanel(role)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun navegarAPanel(role: String) {
        val intent = when (role) {
            "admin" -> Intent(this, AdminHomeActivity::class.java)
            "gerente" -> Intent(this, GerenteHomeActivity::class.java)
            "empleado" -> Intent(this, EmpleadoHomeActivity::class.java)
            else -> null
        }

        if (intent != null) {
            startActivity(intent)
            finish()
        }
    }
}
