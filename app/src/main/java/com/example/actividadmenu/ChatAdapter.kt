package com.example.actividadmenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.model.Chat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private var chats: List<Chat>,
    private val currentUserId: String,
    private val onClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar: TextView = view.findViewById(R.id.tvChatAvatar)
        val tvNombre: TextView = view.findViewById(R.id.tvChatNombre)
        val tvMensaje: TextView = view.findViewById(R.id.tvChatUltimoMensaje)
        val tvHora: TextView = view.findViewById(R.id.tvChatHora)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        
        // Determinar el nombre del otro usuario
        val otroId = chat.usuarios.find { it != currentUserId } ?: ""
        val otroNombre = chat.nombresUsuarios[otroId] ?: "Usuario"
        
        holder.tvNombre.text = otroNombre
        holder.tvAvatar.text = otroNombre.take(1).uppercase()
        holder.tvMensaje.text = chat.ultimoMensaje
        
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        holder.tvHora.text = sdf.format(Date(chat.timestamp))

        holder.itemView.setOnClickListener { onClick(chat) }
    }

    override fun getItemCount() = chats.size

    fun updateLista(nuevaLista: List<Chat>) {
        chats = nuevaLista
        notifyDataSetChanged()
    }
}
