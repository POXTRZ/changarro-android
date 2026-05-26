package com.example.actividadmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistorialFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: TransaccionAdapter
    private var listaTransacciones = mutableListOf<Transaccion>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historial, container, false)

        db = FirebaseFirestore.getInstance()
        val rvHistorial = view.findViewById<RecyclerView>(R.id.rvHistorial)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutEmptyHistorial)

        adapter = TransaccionAdapter(listaTransacciones)
        rvHistorial.layoutManager = LinearLayoutManager(context)
        rvHistorial.adapter = adapter

        escucharTransacciones(layoutEmpty)

        return view
    }

    private fun escucharTransacciones(layoutEmpty: View) {
        db.collection("transacciones")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                listaTransacciones.clear()
                for (doc in snapshot) {
                    val trans = doc.toObject(Transaccion::class.java).copy(id = doc.id)
                    listaTransacciones.add(trans)
                }
                adapter.updateLista(listaTransacciones)
                layoutEmpty.visibility = if (listaTransacciones.isEmpty()) View.VISIBLE else View.GONE
            }
    }
}