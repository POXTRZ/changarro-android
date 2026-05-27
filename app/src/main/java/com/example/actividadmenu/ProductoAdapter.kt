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
    private val onClick: (Producto) -> Unit = {},
    private val onLongClick: (Producto) -> Unit = {},
    private val onFavoriteClick: ((Producto) -> Unit)? = null,
    private val onEliminarClick: ((Producto) -> Unit)? = null,
    private var favoritosIds: Set<String> = emptySet()
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProducto: ImageView = view.findViewById(R.id.ivProducto)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProducto)
        val tvVendedor: TextView = view.findViewById(R.id.tvVendedor)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionProducto)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecioProducto)
        val tvStock: TextView = view.findViewById(R.id.tvStockProducto)
        val ivFavorite: ImageView = view.findViewById(R.id.ivFavorite)
        val btnEliminar: android.widget.ImageButton = view.findViewById(R.id.btnEliminarProducto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]
        holder.tvNombre.text = producto.nombre
        holder.tvVendedor.text = "por ${if(producto.vendedorNombre.isEmpty()) "Estudiante" else producto.vendedorNombre}"
        holder.tvDescripcion.text = producto.descripcion
        holder.tvPrecio.text = "$${producto.precio}"
        holder.tvStock.text = "Stock: ${producto.stock}"

        val esFavorito = favoritosIds.contains(producto.id)
        holder.ivFavorite.setImageResource(if (esFavorito) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)

        if (producto.imagenUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(producto.imagenUrl)
                .placeholder(R.drawable.ic_product_placeholder)
                .centerCrop()
                .into(holder.ivProducto)
        } else {
            holder.ivProducto.setImageResource(R.drawable.ic_product_placeholder)
        }

        holder.itemView.setOnClickListener { onClick(producto) }
        holder.ivFavorite.setOnClickListener { onFavoriteClick?.invoke(producto) }
        
        val miId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (producto.vendedorId == miId) {
            holder.btnEliminar.visibility = View.VISIBLE
            holder.btnEliminar.setOnClickListener { onEliminarClick?.invoke(producto) }
        } else {
            holder.btnEliminar.visibility = View.GONE
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

    fun updateFavoritos(nuevosFavoritos: Set<String>) {
        favoritosIds = nuevosFavoritos
        notifyDataSetChanged()
    }
}
