package com.example.actividadmenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.model.Aviso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AvisoAdapter(private var avisos: List<Aviso>) :
    RecyclerView.Adapter<AvisoAdapter.AvisoViewHolder>() {

    class AvisoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatarLetra: TextView = view.findViewById(R.id.tvAvatarLetra)
        val tvAutorNombre: TextView = view.findViewById(R.id.tvAutorNombre)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvContenido: TextView = view.findViewById(R.id.tvContenido)
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
    }

    override fun getItemCount() = avisos.size

    fun updateLista(nuevaLista: List<Aviso>) {
        avisos = nuevaLista
        notifyDataSetChanged()
    }
}
