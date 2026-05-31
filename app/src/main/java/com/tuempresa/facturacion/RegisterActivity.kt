package com.tuempresa.facturacion

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etNombre: TextInputEditText
    private lateinit var spinnerRol: AutoCompleteTextView
    private lateinit var btnRegister: MaterialCardView // Cambiado de Button a MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        // FORZAR MODO CLARO
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        // Inicializar las vistas
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etNombre = findViewById(R.id.etNombre)
        spinnerRol = findViewById(R.id.spinnerRol1)
        btnRegister = findViewById(R.id.btnRegisterCard) // ID corregido según el nuevo XML
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        val roles = arrayOf("empleado", "gerente", "admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spinnerRol.setAdapter(adapter)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val nombre = etNombre.text.toString().trim()
            val rolSeleccionado = spinnerRol.text.toString()

            if (email.isEmpty() || password.isEmpty() || nombre.isEmpty() || rolSeleccionado.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                        val userMap = mapOf(
                            "uid" to uid,
                            "nombre" to nombre,
                            "email" to email,
                            "role" to rolSeleccionado
                        )

                        db.getReference("users").child(uid).setValue(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                limpiarCampos()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al guardar datos", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun limpiarCampos() {
        etNombre.text?.clear()
        etEmail.text?.clear()
        etPassword.text?.clear()
        spinnerRol.text.clear()
        etNombre.requestFocus()
    }
}
