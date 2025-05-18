package com.example.gatoshunter.adaptes

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.R
import com.example.gatoshunter.clases.Gato

// Interfaz para manejar el doble clic
interface OnGatoDoubleClickListener {
    fun onGatoDoubleClick(gato: Gato)
}

class GatoAdapter(private var listaGatos: List<Gato>, private val listener: OnGatoDoubleClickListener?) :
    RecyclerView.Adapter<GatoAdapter.GatoViewHolder>() {

    // Variable que guarda el ID del gato seleccionado
    internal var selectedItemId: Int? = null

    // ViewHolder que mantiene las vistas de cada ítem (gato) en el RecyclerView
    class GatoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.nombreGato) // Nombre del gato
        val peso: TextView = itemView.findViewById(R.id.pesoGato) // Peso del gato
        val localidad: TextView = itemView.findViewById(R.id.localidadGato) // Localidad del gato
        val descripcion: TextView = itemView.findViewById(R.id.descripcionGato) // Descripción del gato
        val emocion: TextView = itemView.findViewById(R.id.emocionGato) // Emocion del gato
        val imagen: ImageView = itemView.findViewById(R.id.imagenGato) // Imagen del gato
    }

    // Método para crear y devolver un ViewHolder que contiene el layout de cada ítem del RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GatoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gato, parent, false)
        return GatoViewHolder(view)
    }

    // Método que vincula los datos de un gato a las vistas en el ViewHolder
    override fun onBindViewHolder(holder: GatoViewHolder, position: Int) {
        val gato = listaGatos[position] // Obtener el gato en la posición actual

        // Configurar el texto de cada campo en el ViewHolder
        holder.nombre.text = "Nombre: " + gato.nombre
        holder.peso.text = "Peso: " + gato.peso.toString()
        holder.localidad.text = "Localidad: " + gato.localidad
        holder.descripcion.text = "Descripcion: " + gato.descripcion
        holder.emocion.text = "Emocion: " + gato.emocion

        // Configurar la imagen del gato
        val imagenName = gato.img
        val resourceId = holder.itemView.context.resources.getIdentifier(
            imagenName,
            "drawable",
            holder.itemView.context.packageName
        )

        if (resourceId != 0) {
            holder.imagen.setImageResource(resourceId)
        } else {
            holder.imagen.setImageResource(R.drawable.gato1)
        }

        // Cambiar el color de fondo del ítem seleccionado
        holder.itemView.setBackgroundColor(
            if (gato.id == selectedItemId) { // Si este gato es el seleccionado, cambiar el color
                Color.LTGRAY
            } else {
                Color.WHITE // Si no es el seleccionado, mantener blanco
            }
        )

        // Cuando el ítem es tocado, cambia el estado de selección
        var lastClickTime = 0L
        val DOUBLE_CLICK_TIME_DELTA = 300 // milisegundos

        holder.itemView.setOnClickListener {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                // Doble clic detectado
                listener?.onGatoDoubleClick(gato)
            }
            lastClickTime = clickTime

            // Mantener funcionalidad de selección
            val previosPosition = selectedItemId
            selectedItemId = gato.id
            notifyItemChanged(listaGatos.indexOfFirst { it.id == previosPosition })
            notifyItemChanged(position)
        }
    }

    // Método para actualizar la lista de gatos en el adaptador
    fun actualizarLista(nuevaLista: List<Gato>) {
        listaGatos = nuevaLista
        notifyDataSetChanged() // Notificamos que la lista ha cambiado y se debe redibujar el RecyclerView
    }

    // Método para eliminar un gato de la lista basado en su ID
    fun eliminarGato(id: Int) {
        val index =
            listaGatos.indexOfFirst { it.id == id } // Encontrar la posición del gato en la lista
        if (index != -1) {
            listaGatos = listaGatos.toMutableList()
                .apply { removeAt(index) } // Eliminamos el gato de la lista mutable
            notifyItemRemoved(index) // Notificamos que un ítem ha sido eliminado para que el RecyclerView se actualice
        }
    }

    //Metodo que devuelve el gato seleccionado
    fun getGatoSeleccionado(): Gato? {
        return listaGatos.find { it.id == selectedItemId }
    }

    // Método que devuelve el número de ítems en la lista de gatos (requerido por RecyclerView.Adapter)
    override fun getItemCount(): Int = listaGatos.size
}
