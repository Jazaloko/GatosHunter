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

class CompradorAdapter(
    private var listaCompradores: List<CompradorConGato>,
    private val onDoubleClick: (CompradorConGato) -> Unit // 👈 Nuevo parámetro para doble clic
) : RecyclerView.Adapter<CompradorAdapter.CompradorViewHolder>() {

    internal var selectedItemId: Int? = null

    inner class CompradorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.nombreComprador)
        val localidad: TextView = itemView.findViewById(R.id.localidadComprador)
        val imagen: ImageView = itemView.findViewById(R.id.imagenComprador)
        val gato: TextView = itemView.findViewById(R.id.gatoComprador)

        private var lastClickTime: Long = 0

        fun bind(compradorConGato: CompradorConGato) {
            nombre.text = "Nombre: " + compradorConGato.comprador.nombre
            localidad.text = "Localidad: " + compradorConGato.comprador.localidad
            gato.text = "Gato a comprar: " + compradorConGato.nombreGatoInteres.toString()

            // Cargar imagen
            val imagenName = compradorConGato.comprador.img
            val resourceId = itemView.context.resources.getIdentifier(
                imagenName,
                "drawable",
                itemView.context.packageName
            )

            imagen.setImageResource(
                if (resourceId != 0) resourceId else R.drawable.character1
            )

            // Cambiar fondo si está seleccionado
            itemView.setBackgroundColor(
                if (compradorConGato.comprador.id == selectedItemId) Color.LTGRAY else Color.WHITE
            )

            // Doble clic y selección
            itemView.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 300) {
                    // 👇 Llama al callback del doble clic
                    onDoubleClick(compradorConGato)
                }
                lastClickTime = currentTime

                val previousId = selectedItemId
                selectedItemId = compradorConGato.comprador.id
                notifyItemChanged(listaCompradores.indexOfFirst { it.comprador.id == previousId })
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompradorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comprador, parent, false)
        return CompradorViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompradorViewHolder, position: Int) {
        holder.bind(listaCompradores[position])
    }

    override fun getItemCount(): Int = listaCompradores.size

    fun actualizarLista(nuevaLista: List<CompradorConGato>) {
        listaCompradores = nuevaLista
        notifyDataSetChanged()
    }

    fun eliminarComprador(id: Int) {
        val index = listaCompradores.indexOfFirst { it.comprador.id == id }
        if (index != -1) {
            listaCompradores = listaCompradores.toMutableList().apply { removeAt(index) }
            notifyItemRemoved(index)
        }
    }
}
