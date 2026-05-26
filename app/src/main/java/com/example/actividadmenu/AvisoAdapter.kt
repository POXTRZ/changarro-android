package com.example.actividadmenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.model.Aviso
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AvisoAdapter(
    private var avisos: List<Aviso>,
    private val onEliminarClick: ((Aviso) -> Unit)? = null
) : RecyclerView.Adapter<AvisoAdapter.AvisoViewHolder>() {

    class AvisoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatarLetra: TextView = view.findViewById(R.id.tvAvatarLetra)
        val tvAutorNombre: TextView = view.findViewById(R.id.tvAutorNombre)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvContenido: TextView = view.findViewById(R.id.tvContenido)
        val btnEliminar: android.widget.ImageButton = view.findViewById(R.id.btnEliminarAviso)
        val btnWhatsapp: android.widget.ImageButton = view.findViewById(R.id.btnWhatsappAviso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvisoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aviso, parent, false)
        return AvisoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvisoViewHolder, position: Int) {
        val aviso = avisos[position]
        
        holder.tvAutorNombre.text = aviso.autorNombre
        holder.tvContenido.text = aviso.contenido
        
        // Letra del avatar
        holder.tvAvatarLetra.text = aviso.autorNombre.take(1).uppercase()
        
        // Formato de fecha
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        holder.tvFecha.text = sdf.format(Date(aviso.timestamp))

        // Botón eliminar (solo si es el autor)
        val miId = FirebaseAuth.getInstance().currentUser?.uid
        if (aviso.autorId == miId) {
            holder.btnEliminar.visibility = View.VISIBLE
            holder.btnEliminar.setOnClickListener { onEliminarClick?.invoke(aviso) }
        } else {
            holder.btnEliminar.visibility = View.GONE
        }

        // WhatsApp
        if (aviso.autorWhatsapp.isNotEmpty() && aviso.autorId != miId) {
            holder.btnWhatsapp.visibility = View.VISIBLE
            holder.btnWhatsapp.setOnClickListener {
                val url = "https://api.whatsapp.com/send?phone=${aviso.autorWhatsapp}&text=Hola ${aviso.autorNombre}, vi tu aviso en Changarro: ${aviso.contenido.take(30)}..."
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                intent.data = android.net.Uri.parse(url)
                holder.itemView.context.startActivity(intent)
            }
        } else {
            holder.btnWhatsapp.visibility = View.GONE
        }
    }

    override fun getItemCount() = avisos.size

    fun updateLista(nuevaLista: List<Aviso>) {
        avisos = nuevaLista
        notifyDataSetChanged()
    }
}
