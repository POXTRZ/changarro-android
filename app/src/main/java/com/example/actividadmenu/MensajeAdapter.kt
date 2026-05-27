package com.example.actividadmenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.model.Mensaje
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MensajeAdapter(private var mensajes: List<Mensaje>, private val miId: String) :
    RecyclerView.Adapter<MensajeAdapter.MensajeViewHolder>() {

    class MensajeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardIzquierda: MaterialCardView = view.findViewById(R.id.cardMensajeIzquierdo)
        val tvIzquierda: TextView = view.findViewById(R.id.tvMensajeIzquierdo)
        val tvHoraIzquierda: TextView = view.findViewById(R.id.tvHoraIzquierda)
        
        val cardDerecha: MaterialCardView = view.findViewById(R.id.cardMensajeDerecho)
        val tvDerecha: TextView = view.findViewById(R.id.tvMensajeDerecho)
        val tvHoraDerecha: TextView = view.findViewById(R.id.tvHoraDerecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mensaje, parent, false)
        return MensajeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MensajeViewHolder, position: Int) {
        val mensaje = mensajes[position]
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val horaStr = sdf.format(Date(mensaje.timestamp))

        if (mensaje.senderId == miId) {
            // Enviado por mí
            holder.cardIzquierda.visibility = View.GONE
            holder.cardDerecha.visibility = View.VISIBLE
            holder.tvDerecha.text = mensaje.texto
            holder.tvHoraDerecha.text = horaStr
        } else {
            // Recibido
            holder.cardIzquierda.visibility = View.VISIBLE
            holder.cardDerecha.visibility = View.GONE
            holder.tvIzquierda.text = mensaje.texto
            holder.tvHoraIzquierda.text = horaStr
        }
    }

    override fun getItemCount() = mensajes.size

    fun updateLista(nuevaLista: List<Mensaje>) {
        mensajes = nuevaLista
        notifyDataSetChanged()
    }
}
