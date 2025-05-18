package com.example.gatoshunter.adaptes

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gatoshunter.R
import com.example.gatoshunter.clases.Comprador

interface OnCompradorDoubleClickListener {
    fun onCompradorDoubleClick(comprador: Comprador)
}

class CompradorAdapter(
    private var listaCompradores: List<Comprador>,
    private val listener: OnCompradorDoubleClickListener
) : RecyclerView.Adapter<CompradorAdapter.CompradorViewHolder>() {

    internal var selectedItemId: Int? = null

    inner class CompradorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.nombreComprador)
        val localidad: TextView = itemView.findViewById(R.id.localidadComprador)
        val imagen: ImageView = itemView.findViewById(R.id.imagenComprador)

        private var lastClickTime = 0L

        fun bind(comprador: Comprador) {
            nombre.text = "Nombre: " + comprador.nombre
            localidad.text = "Localidad: " + comprador.localidad

            val imagenName = comprador.img
            val resourceId = itemView.context.resources.getIdentifier(
                imagenName,
                "drawable",
                itemView.context.packageName
            )
            if (resourceId != 0) {
                imagen.setImageResource(resourceId)
            } else {
                imagen.setImageResource(R.drawable.character1)
            }

            itemView.setBackgroundColor(
                if (comprador.id == selectedItemId) Color.LTGRAY else Color.WHITE
            )

            itemView.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 400) {
                    // Doble clic detectado
                    listener.onCompradorDoubleClick(comprador)
                } else {
                    val previoId = selectedItemId
                    selectedItemId = comprador.id
                    notifyItemChanged(listaCompradores.indexOfFirst { it.id == previoId })
                    notifyItemChanged(adapterPosition)
                }
                lastClickTime = currentTime
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompradorViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_comprador, parent, false)
        return CompradorViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompradorViewHolder, position: Int) {
        holder.bind(listaCompradores[position])
    }

    override fun getItemCount(): Int = listaCompradores.size

    fun actualizarLista(nuevaLista: List<Comprador>) {
        listaCompradores = nuevaLista
        notifyDataSetChanged()
    }

    fun eliminarComprador(id: Int) {
        val index = listaCompradores.indexOfFirst { it.id == id }
        if (index != -1) {
            listaCompradores = listaCompradores.toMutableList().apply { removeAt(index) }
            notifyItemRemoved(index)
        }
    }
}
