package com.example.gatoshunter.clases

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.R

// Interfaz para manejar el doble clic
interface OnGatoDoubleClickListener {
    fun onGatoDoubleClick(gato: Gato)
}

class MainAdapter(
    private var listaGatos: List<Gato>,
    private val listener: OnGatoDoubleClickListener
) : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    private var selectedItemId: Int? = null

    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.nombreGato)
        val peso: TextView = itemView.findViewById(R.id.pesoGato)
        val emocion: TextView = itemView.findViewById(R.id.emocionGato)
        val descripcion: TextView = itemView.findViewById(R.id.descripcionGato)
        val localidad: TextView = itemView.findViewById(R.id.localidadGato)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gato, parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val gato = listaGatos[position]

        holder.nombre.text = gato.nombre
        holder.localidad.text = gato.localidad
        holder.peso.text = gato.peso.toString()
        holder.descripcion.text = gato.descripcion
        holder.emocion.text = gato.emocion

        holder.itemView.setBackgroundColor(
            if (gato.id == selectedItemId) Color.LTGRAY else Color.WHITE
        )

        var lastClickTime = 0L
        holder.itemView.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 300) {
                // Doble clic detectado
                listener.onGatoDoubleClick(gato)
            } else {
                // Clic simple: seleccionar ítem
                val previousItemId = selectedItemId
                selectedItemId = gato.id
                notifyItemChanged(listaGatos.indexOfFirst { it.id == previousItemId })
                notifyItemChanged(position)
            }
            lastClickTime = currentTime
        }
    }

    override fun getItemCount(): Int = listaGatos.size

    fun actualizarLista(nuevaLista: List<Gato>) {
        listaGatos = nuevaLista
        notifyDataSetChanged()
    }

    fun getListaGatos(): List<Gato> = listaGatos

    fun eliminarGato(id: Int) {
        val index = listaGatos.indexOfFirst { it.id == id }
        if (index != -1) {
            listaGatos = listaGatos.toMutableList().apply { removeAt(index) }
            notifyItemRemoved(index)
        }
    }
}
