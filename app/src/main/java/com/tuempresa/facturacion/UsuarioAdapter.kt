package com.tuempresa.facturacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuarioAdapter(
    private val usuarios: List<Usuario>,
    private val onItemClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreUsuarioLista)
        val tvEmail: TextView = view.findViewById(R.id.tvEmailUsuarioLista)
        val tvRol: TextView = view.findViewById(R.id.tvRolUsuarioLista)

        fun bind(usuario: Usuario) {
            tvNombre.text = usuario.nombre
            tvEmail.text = usuario.email
            tvRol.text = usuario.role?.replaceFirstChar { it.uppercase() }
            
            itemView.setOnClickListener { onItemClick(usuario) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_usuario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(usuarios[position])
    }

    override fun getItemCount() = usuarios.size
}
