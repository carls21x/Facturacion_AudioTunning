package com.tuempresa.facturacion

import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*

class GestionUsuariosActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private val listaUsuarios = mutableListOf<Usuario>()
    private lateinit var adapter: UsuarioAdapter

    private var usuarioSeleccionadoUid: String? = null

    private lateinit var btnActualizar: MaterialCardView
    private lateinit var btnEliminar: MaterialCardView
    private lateinit var etNombre: TextInputEditText
    private lateinit var spinnerRol: AutoCompleteTextView
    private lateinit var formContainer: ViewGroup
    private lateinit var mainLayout: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_usuarios)

        dbRef = FirebaseDatabase.getInstance().getReference("users")

        // Inicializar vistas
        mainLayout = findViewById(R.id.main_content_layout)
        formContainer = findViewById(R.id.form_container_usuarios)
        etNombre = findViewById(R.id.etNombreUsuario)
        spinnerRol = findViewById(R.id.spinnerRol)
        btnActualizar = findViewById(R.id.btnActualizarUsuarioCard)
        btnEliminar = findViewById(R.id.btnEliminarUsuarioCard)
        recyclerView = findViewById(R.id.recyclerViewUsuarios)

        recyclerView.layoutManager = LinearLayoutManager(this)
        
        val roles = arrayOf("admin", "gerente", "empleado")
        val adapterRoles = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        spinnerRol.setAdapter(adapterRoles)

        actualizarEstadoBotones(false)
        cargarUsuarios()

        btnActualizar.setOnClickListener {
            val uid = usuarioSeleccionadoUid ?: return@setOnClickListener
            val nombre = etNombre.text.toString().trim()
            val rol = spinnerRol.text.toString()

            if (nombre.isEmpty() || rol.isEmpty()) {
                Toast.makeText(this, "Nombre y Rol son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dbRef.child(uid).updateChildren(mapOf("nombre" to nombre, "role" to rol))
                .addOnSuccessListener {
                    Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                    ocultarFormulario()
                }
        }

        btnEliminar.setOnClickListener {
            val uid = usuarioSeleccionadoUid ?: return@setOnClickListener
            dbRef.child(uid).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                    ocultarFormulario()
                }
        }
    }

    private fun cargarUsuarios() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaUsuarios.clear()
                for (userSnapshot in snapshot.children) {
                    val usuario = userSnapshot.getValue(Usuario::class.java)?.copy(uid = userSnapshot.key)
                    if (usuario != null) listaUsuarios.add(usuario)
                }
                
                adapter = UsuarioAdapter(listaUsuarios) { usuario ->
                    usuarioSeleccionadoUid = usuario.uid
                    etNombre.setText(usuario.nombre)
                    spinnerRol.setText(usuario.role, false)
                    mostrarFormulario()
                }
                recyclerView.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun mostrarFormulario() {
        TransitionManager.beginDelayedTransition(mainLayout)
        formContainer.visibility = View.VISIBLE
        actualizarEstadoBotones(true)
    }

    private fun ocultarFormulario() {
        TransitionManager.beginDelayedTransition(mainLayout)
        formContainer.visibility = View.GONE
        limpiarCampos()
    }

    private fun limpiarCampos() {
        etNombre.text?.clear()
        spinnerRol.text?.clear()
        usuarioSeleccionadoUid = null
        actualizarEstadoBotones(false)
    }

    private fun actualizarEstadoBotones(usuarioSeleccionado: Boolean) {
        btnActualizar.isEnabled = usuarioSeleccionado
        btnEliminar.isEnabled = usuarioSeleccionado
        btnActualizar.alpha = if (usuarioSeleccionado) 1.0f else 0.5f
        btnEliminar.alpha = if (usuarioSeleccionado) 1.0f else 0.5f
    }
}
