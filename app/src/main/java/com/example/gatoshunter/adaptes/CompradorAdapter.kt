package com.example.gatoshunter.adaptes

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.R
import com.example.gatoshunter.VenderGato.CompradorConGato


class CompradorAdapter(private var listaCompradores: List<CompradorConGato>) :
    RecyclerView.Adapter<CompradorAdapter.CompradorViewHolder>() {

    // Variable que guarda el ID del comprador seleccionado
    internal var selectedItemId: Int? = null

    // ViewHolder que mantiene las vistas de cada ítem (comprador) en el RecyclerView
    class CompradorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.nombreComprador) // Nombre del comprador
        val localidad: TextView = itemView.findViewById(R.id.localidadComprador) // Localidad del comprador
        val imagen: ImageView = itemView.findViewById(R.id.imagenComprador) // Imagen del comprador
        val gato: TextView = itemView.findViewById(R.id.gatoComprador) // Gato a Comprar
    }

    // Método para crear y devolver un ViewHolder que contiene el layout de cada ítem del RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompradorViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_comprador, parent, false)
        return CompradorViewHolder(view)
    }

    // Método que vincula los datos de un comprador a las vistas en el ViewHolder
    override fun onBindViewHolder(holder: CompradorViewHolder, position: Int) {
        val compradorConGato = listaCompradores[position] // Obtener el comprador en la posición actual

        // Configurar el texto de cada campo en el ViewHolder
        holder.nombre.text = "Nombre: " + compradorConGato.comprador.nombre
        holder.localidad.text = "Localidad: " + compradorConGato.comprador.localidad
        holder.gato.text = "Gato a comprar: " + compradorConGato.nombreGatoInteres.toString()

        // Configurar la imagen del comprador
        val imagenName = compradorConGato.comprador.img
        val resourceId = holder.itemView.context.resources.getIdentifier(
            imagenName,
            "drawable",
            holder.itemView.context.packageName
        )

        if (resourceId != 0) {
            holder.imagen.setImageResource(resourceId)
        } else {
            holder.imagen.setImageResource(R.drawable.character1)
        }

        // Cambiar el color de fondo del ítem seleccionado
        holder.itemView.setBackgroundColor(
            if (compradorConGato.comprador.id == selectedItemId) { // Si este gato es el seleccionado, cambiar el color
                Color.LTGRAY
            } else {
                Color.WHITE // Si no es el seleccionado, mantener blanco
            }
        )

        // Cuando el ítem es tocado, cambia el estado de selección
        holder.itemView.setOnClickListener {
            val previosPosition =
                selectedItemId // Guardamos la posición anterior del gato seleccionado
            selectedItemId = compradorConGato.comprador.id // Actualizamos el gato seleccionado

            // Notificamos que los ítems han cambiado para actualizar la UI correctamente
            notifyItemChanged(listaCompradores.indexOfFirst { it.comprador.id == previosPosition }) // Notificamos que el ítem anterior debe cambiar de estado
            notifyItemChanged(position) // Notificamos que el ítem actual debe actualizarse para reflejar la selección
        }
    }

    // Método para actualizar la lista de gatos en el adaptador
    fun actualizarLista(nuevaLista: List<CompradorConGato>) {
        listaCompradores = nuevaLista
        notifyDataSetChanged() // Notificamos que la lista ha cambiado y se debe redibujar el RecyclerView
    }

    // Método para eliminar un gato de la lista basado en su ID
    fun eliminarComprador(id: Int) {
        val index =
            listaCompradores.indexOfFirst { it.comprador.id == id } // Encontrar la posición del gato en la lista
        if (index != -1) {
            listaCompradores = listaCompradores.toMutableList()
                .apply { removeAt(index) } // Eliminamos el gato de la lista mutable
            notifyItemRemoved(index) // Notificamos que un ítem ha sido eliminado para que el RecyclerView se actualice
        }
    }

    // Método que devuelve el número de ítems en la lista de gatos (requerido por RecyclerView.Adapter)
    override fun getItemCount(): Int = listaCompradores.size

}