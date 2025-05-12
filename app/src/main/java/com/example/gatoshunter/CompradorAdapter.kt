package com.example.gatoshunter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CompradorAdapter(private var listaCompradores: List<Comprador>) : RecyclerView.Adapter<CompradorAdapter.CompradorViewHolder>() {

    // Variable que guarda el ID del comprador seleccionado
    internal var selectedItemId: Int? = null

    // ViewHolder que mantiene las vistas de cada ítem (comprador) en el RecyclerView
    class CompradorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.nombreComprador) // Nombre del comprador
        val localidad: TextView = itemView.findViewById(R.id.localidadComprador) // Localidad del comprador
    }

    // Método para crear y devolver un ViewHolder que contiene el layout de cada ítem del RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompradorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comprador, parent, false)
        return CompradorViewHolder(view)
    }

    // Método que vincula los datos de un comprador a las vistas en el ViewHolder
    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CompradorViewHolder, position: Int) {
        val comprador = listaCompradores[position] // Obtener el comprador en la posición actual

        // Configurar el texto de cada campo en el ViewHolder
        holder.nombre.text = comprador.nombre
        holder.localidad.text = comprador.localidad

        // Cambiar el color de fondo del ítem seleccionado
        holder.itemView.setBackgroundColor(
            if (comprador.id == selectedItemId) { // Si este gato es el seleccionado, cambiar el color
                Color.LTGRAY
            } else {
                Color.WHITE // Si no es el seleccionado, mantener blanco
            }
        )

        // Cuando el ítem es tocado, cambia el estado de selección
        holder.itemView.setOnClickListener {
            val previosPosition = selectedItemId // Guardamos la posición anterior del gato seleccionado
            selectedItemId = comprador.id // Actualizamos el gato seleccionado

            // Notificamos que los ítems han cambiado para actualizar la UI correctamente
            notifyItemChanged(listaCompradores.indexOfFirst { it.id == previosPosition }) // Notificamos que el ítem anterior debe cambiar de estado
            notifyItemChanged(position) // Notificamos que el ítem actual debe actualizarse para reflejar la selección
        }
    }

    // Método para actualizar la lista de gatos en el adaptador
    fun actualizarLista(nuevaLista: List<Comprador>) {
        listaCompradores = nuevaLista
        notifyDataSetChanged() // Notificamos que la lista ha cambiado y se debe redibujar el RecyclerView
    }

    // Método para eliminar un gato de la lista basado en su ID
    fun eliminarComprador(id: Int) {
        val index = listaCompradores.indexOfFirst { it.id == id } // Encontrar la posición del gato en la lista
        if (index != -1) {
            listaCompradores = listaCompradores.toMutableList().apply { removeAt(index) } // Eliminamos el gato de la lista mutable
            notifyItemRemoved(index) // Notificamos que un ítem ha sido eliminado para que el RecyclerView se actualice
        }
    }

    // Método que devuelve el número de ítems en la lista de gatos (requerido por RecyclerView.Adapter)
    override fun getItemCount(): Int = listaCompradores.size
}
