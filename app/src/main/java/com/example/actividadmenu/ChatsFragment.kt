package com.example.actividadmenu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.viewmodel.BandejaViewModel
import com.google.firebase.auth.FirebaseAuth

class ChatsFragment : Fragment() {

    private lateinit var viewModel: BandejaViewModel
    private lateinit var adapter: ChatAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        viewModel = ViewModelProvider(this)[BandejaViewModel::class.java]

        val rvChats = view.findViewById<RecyclerView>(R.id.rvChats)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutEmptyChats)

        val miId = auth.currentUser?.uid ?: ""
        
        adapter = ChatAdapter(emptyList(), miId) { chat ->
            // Al hacer click, abrir la sala de chat
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("chatId", chat.id)
            // También pasamos el nombre del otro usuario para el toolbar
            val otroId = chat.usuarios.find { it != miId } ?: ""
            intent.putExtra("otroNombre", chat.nombresUsuarios[otroId])
            startActivity(intent)
        }

        rvChats.layoutManager = LinearLayoutManager(context)
        rvChats.adapter = adapter

        viewModel.listaChats.observe(viewLifecycleOwner) { chats ->
            adapter.updateLista(chats)
            layoutEmpty.visibility = if (chats.isEmpty()) View.VISIBLE else View.GONE
        }

        if (miId.isNotEmpty()) {
            viewModel.cargarMisChats(miId)
        }

        return view
    }
}
