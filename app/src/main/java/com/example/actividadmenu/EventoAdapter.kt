package com.example.actividadmenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.model.Evento

class EventoAdapter(private var eventos: List<Evento>) :
    RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    class EventoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloEvento)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaEvento)
        val tvLugar: TextView = view.findViewById(R.id.tvLugarEvento)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionEvento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.tvTitulo.text = evento.titulo
        holder.tvFecha.text = evento.fechaDelEvento
        holder.tvLugar.text = evento.lugar
        holder.tvDescripcion.text = evento.descripcion
    }

    override fun getItemCount() = eventos.size

    fun updateLista(nuevaLista: List<Evento>) {
        eventos = nuevaLista
        notifyDataSetChanged()
    }
}
