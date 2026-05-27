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
    private val onEliminarClick: ((Aviso) -> Unit)? = null,
    private val onChatClick: ((Aviso) -> Unit)? = null
) : RecyclerView.Adapter<AvisoAdapter.AvisoViewHolder>() {

    class AvisoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatarLetra: TextView = view.findViewById(R.id.tvAvatarLetra)
        val tvAutorNombre: TextView = view.findViewById(R.id.tvAutorNombre)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvContenido: TextView = view.findViewById(R.id.tvContenido)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminarAviso)
        val btnWhatsapp: ImageButton = view.findViewById(R.id.btnWhatsappAviso)
        val btnChat: ImageButton = view.findViewById(R.id.btnChatAviso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvisoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_aviso, parent, false)
        return AvisoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvisoViewHolder, position: Int) {
        val aviso = avisos[position]
        val miId = FirebaseAuth.getInstance().currentUser?.uid
        
        holder.tvAutorNombre.text = aviso.autorNombre
        holder.tvContenido.text = aviso.contenido
        holder.tvAvatarLetra.text = if (aviso.autorNombre.isNotEmpty()) aviso.autorNombre.take(1).uppercase() else "?"
        
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        holder.tvFecha.text = sdf.format(Date(aviso.timestamp))

        // Lógica de visibilidad Senior
        val esMiAviso = aviso.autorId == miId
        
        holder.btnEliminar.visibility = if (esMiAviso) View.VISIBLE else View.GONE
        holder.btnChat.visibility = if (!esMiAviso) View.VISIBLE else View.GONE
        holder.btnWhatsapp.visibility = if (!esMiAviso && aviso.autorWhatsapp.isNotEmpty()) View.VISIBLE else View.GONE

        holder.btnEliminar.setOnClickListener { onEliminarClick?.invoke(aviso) }
        holder.btnChat.setOnClickListener { onChatClick?.invoke(aviso) }
        
        holder.btnWhatsapp.setOnClickListener {
            val url = "https://api.whatsapp.com/send?phone=${aviso.autorWhatsapp}&text=Hola ${aviso.autorNombre}, vi tu aviso en Changarro..."
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = avisos.size

    fun updateLista(nuevaLista: List<Aviso>) {
        avisos = nuevaLista
        notifyDataSetChanged()
    }
}
