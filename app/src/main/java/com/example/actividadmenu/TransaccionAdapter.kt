package com.example.actividadmenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransaccionAdapter(private var lista: List<Transaccion>) :
    RecyclerView.Adapter<TransaccionAdapter.TransaccionViewHolder>() {

    class TransaccionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardIcon: MaterialCardView = view.findViewById(R.id.cardIconType)
        val ivType: ImageView = view.findViewById(R.id.ivTransType)
        val tvNombre: TextView = view.findViewById(R.id.tvTransNombre)
        val tvFecha: TextView = view.findViewById(R.id.tvTransFecha)
        val tvMonto: TextView = view.findViewById(R.id.tvTransMonto)
        val tvCant: TextView = view.findViewById(R.id.tvTransCant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaccionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaccion, parent, false)
        return TransaccionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransaccionViewHolder, position: Int) {
        val trans = lista[position]
        holder.tvNombre.text = trans.nombreProducto
        holder.tvCant.text = "${trans.cantidad} un."
        
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        holder.tvFecha.text = sdf.format(Date(trans.fecha))

        if (trans.tipo == "VENTA") {
            holder.cardIcon.setCardBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"))
            holder.ivType.setImageResource(android.R.drawable.ic_menu_send)
            holder.ivType.setColorFilter(android.graphics.Color.parseColor("#4CAF50"))
            holder.tvMonto.text = "+$${String.format("%.2f", trans.montoTotal)}"
            holder.tvMonto.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {
            holder.cardIcon.setCardBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"))
            holder.ivType.setImageResource(android.R.drawable.ic_input_add)
            holder.ivType.setColorFilter(android.graphics.Color.parseColor("#F44336"))
            holder.tvMonto.text = "-$${String.format("%.2f", trans.montoTotal)}"
            holder.tvMonto.setTextColor(android.graphics.Color.parseColor("#F44336"))
        }
    }

    override fun getItemCount() = lista.size

    fun updateLista(nuevaLista: List<Transaccion>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}