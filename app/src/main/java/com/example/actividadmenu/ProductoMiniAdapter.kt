package com.example.actividadmenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductoMiniAdapter(
    private var lista: List<Producto>,
    private val onClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoMiniAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iv = view.findViewById<ImageView>(R.id.ivProductoMini)
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreMini)
        val tvPrecio = view.findViewById<TextView>(R.id.tvPrecioMini)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_producto_mini, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = lista[position]
        holder.tvNombre.text = p.nombre
        holder.tvPrecio.text = "$${p.precio}"
        
        Glide.with(holder.itemView.context)
            .load(p.imagenUrl)
            .placeholder(R.drawable.ic_product_placeholder)
            .into(holder.iv)
            
        holder.itemView.setOnClickListener { onClick(p) }
    }

    override fun getItemCount() = lista.size

    fun update(nueva: List<Producto>) {
        lista = nueva
        notifyDataSetChanged()
    }
}
