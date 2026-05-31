package com.tuempresa.facturacion

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.card.MaterialCardView

class ForgottenPassword : AppCompatActivity() {

    private lateinit var cardCorreo: MaterialCardView
    private lateinit var btnVolver: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        // FORZAR MODO CLARO
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotten_password)

        cardCorreo = findViewById(R.id.btnCorreoCard)
        btnVolver = findViewById(R.id.btnVolverLogin)

        cardCorreo.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("carlosvelasquezc288@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Solicitud de recuperación de contraseña")
                setPackage("com.google.android.gm") // Intenta abrir Gmail directamente
            }
            startActivity(intent)
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }
}
