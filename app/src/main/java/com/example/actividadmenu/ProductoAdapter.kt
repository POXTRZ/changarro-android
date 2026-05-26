package com.example.actividadmenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductoAdapter(
    private var listaProductos: List<Producto>,
    private val onClick: (Producto) -> Unit,
    private val onLongClick: (Producto) -> Unit,
    private val onFavoriteClick: ((Producto) -> Unit)? = null
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProducto: ImageView = view.findViewById(R.id.ivProducto)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProducto)
        val tvVendedor: TextView = view.findViewById(R.id.tvVendedor)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionProducto)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecioProducto)
        val tvStock: TextView = view.findViewById(R.id.tvStockProducto)
        val ivFavorite: ImageView = view.findViewById(R.id.ivFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]
        holder.tvNombre.text = producto.nombre
        holder.tvVendedor.text = "Vendedor: ${if(producto.vendedorNombre.isEmpty()) "Estudiante" else producto.vendedorNombre}"
        holder.tvDescripcion.text = producto.descripcion
        holder.tvPrecio.text = "$${producto.precio}"
        holder.tvStock.text = "Stock: ${producto.stock}"

        // Cargar imagen con Glide
        if (producto.imagenUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(producto.imagenUrl)
                .placeholder(R.drawable.ic_product_placeholder)
                .error(R.drawable.ic_product_placeholder)
                .into(holder.ivProducto)
        } else {
            holder.ivProducto.setImageResource(R.drawable.ic_product_placeholder)
        }

        holder.itemView.setOnClickListener {
            onClick(producto)
        }

        holder.ivFavorite.setOnClickListener {
            onFavoriteClick?.invoke(producto)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(producto)
            true
        }
    }

    override fun getItemCount() = listaProductos.size

    fun updateLista(nuevaLista: List<Producto>) {
        listaProductos = nuevaLista
        notifyDataSetChanged()
    }
}