package com.example.gatoshunter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GatoAdapter(private var listaGatos: List<Gato>) : RecyclerView.Adapter<GatoAdapter.GatoViewHolder>() {

    // Variable que guarda el ID del gato seleccionado
    internal var selectedItemId: Int? = null

    // ViewHolder que mantiene las vistas de cada ítem (gato) en el RecyclerView
    class GatoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.nombreGato) // Nombre del gato
        val peso: TextView = itemView.findViewById(R.id.pesoGato) // Peso del gato
        val localidad: TextView = itemView.findViewById(R.id.localidadGato) // Localidad del gato
        val descripcion: TextView = itemView.findViewById(R.id.descripcionGato) // Descripción del gato
    }

    // Método para crear y devolver un ViewHolder que contiene el layout de cada ítem del RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GatoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gato, parent, false)
        return GatoViewHolder(view)
    }

    // Método que vincula los datos de un gato a las vistas en el ViewHolder
    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: GatoViewHolder, position: Int) {
        val gato = listaGatos[position] // Obtener el gato en la posición actual

        // Configurar el texto de cada campo en el ViewHolder
        holder.nombre.text = gato.nombre
        holder.peso.text = gato.peso.toString()
        holder.localidad.text = gato.localidad
        holder.descripcion.text = gato.descripcion

        // Cambiar el color de fondo del ítem seleccionado
        holder.itemView.setBackgroundColor(
            if (gato.id == selectedItemId) { // Si este gato es el seleccionado, cambiar el color
                Color.LTGRAY
            } else {
                Color.WHITE // Si no es el seleccionado, mantener blanco
            }
        )

        // Cuando el ítem es tocado, cambia el estado de selección
        holder.itemView.setOnClickListener {
            val previosPosition = selectedItemId // Guardamos la posición anterior del gato seleccionado
            selectedItemId = gato.id // Actualizamos el gato seleccionado

            // Notificamos que los ítems han cambiado para actualizar la UI correctamente
            notifyItemChanged(listaGatos.indexOfFirst { it.id == previosPosition }) // Notificamos que el ítem anterior debe cambiar de estado
            notifyItemChanged(position) // Notificamos que el ítem actual debe actualizarse para reflejar la selección
        }
    }

    // Método para actualizar la lista de gatos en el adaptador
    fun actualizarLista(nuevaLista: List<Gato>) {
        listaGatos = nuevaLista
        notifyDataSetChanged() // Notificamos que la lista ha cambiado y se debe redibujar el RecyclerView
    }

    // Método para eliminar un gato de la lista basado en su ID
    fun eliminarGato(id: Int) {
        val index = listaGatos.indexOfFirst { it.id == id } // Encontrar la posición del gato en la lista
        if (index != -1) {
            listaGatos = listaGatos.toMutableList().apply { removeAt(index) } // Eliminamos el gato de la lista mutable
            notifyItemRemoved(index) // Notificamos que un ítem ha sido eliminado para que el RecyclerView se actualice
        }
    }

    // Método que devuelve el número de ítems en la lista de gatos (requerido por RecyclerView.Adapter)
    override fun getItemCount(): Int = listaGatos.size
}
