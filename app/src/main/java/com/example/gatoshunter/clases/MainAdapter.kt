package com.example.gatoshunter.clases

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.R

class MainAdapter(private var listaGatos: List<Gato>) :
    RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    // Variable que guarda el ID del comprador seleccionado
    private var selectedItemId: Int? = null

    // ViewHolder que mantiene las vistas de cada ítem (comprador) en el RecyclerView
    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.nombreGato) // Nombre del gato
        val peso: TextView = itemView.findViewById(R.id.pesoGato) // Peso del Gato
        val emocion: TextView = itemView.findViewById(R.id.emocionGato) // Emocion del gato
        val descripcion: TextView =
            itemView.findViewById(R.id.descripcionGato) // Descripcion del gato
        val localidad: TextView = itemView.findViewById(R.id.localidadGato) // Localidad del gato
//        val imagen: ImageView = itemView.findViewById(R.id.imagenGato) // Imagen del gato
    }

    // Método para crear y devolver un ViewHolder que contiene el layout de cada ítem del RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gato, parent, false)
        return MainViewHolder(view)
    }

    // Método que vincula los datos de un gato a las vistas en el ViewHolder
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val gato = listaGatos[position] // Obtener el gato en la posición actual

        // Configurar el texto de cada campo en el ViewHolder
        holder.nombre.text = gato.nombre
        holder.localidad.text = gato.localidad
        holder.peso.text = gato.peso.toString()
        holder.descripcion.text = gato.descripcion
        holder.emocion.text = gato.emocion
//        val imagenPath = gato.img
//
//        if (imagenPath.isNullOrEmpty()) {
//            // Si no hay ruta de imagen, establece una imagen predeterminada
//            holder.imagen.setImageResource(R.drawable.gato1) // Reemplaza con tu drawable predeterminado
//        } else {
//            // Verifica si la ruta es para un recurso de drawable (avatar)
//            if (imagenPath.startsWith("drawable/")) {
//                val resourceName = imagenPath.substring("drawable/".length)
//                // Obtiene el ID del recurso a partir del nombre
//                val resourceId = holder.itemView.context.resources.getIdentifier(
//                    resourceName,
//                    "drawable",
//                    holder.itemView.context.packageName
//                )
//
//                if (resourceId != 0) {
//                    // Si se encontró el recurso, establece la imagen usando el ID del recurso
//                    holder.imagen.setImageResource(resourceId)
//                } else {
//                    // Si el recurso no se encontró, establece una imagen predeterminada
//                    holder.imagen.setImageResource(R.drawable.gato1)
//                }
//            }
//        }


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
            val previosPosition =
                selectedItemId // Guardamos la posición anterior del gato seleccionado
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

    //Metodo para devolver la lista
    fun getListaGatos(): List<Gato> {
        return listaGatos
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

    // Método que devuelve el número de ítems en la lista de gatos (requerido por RecyclerView.Adapter)
    override fun getItemCount(): Int = listaGatos.size

}