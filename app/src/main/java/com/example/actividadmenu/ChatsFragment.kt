package com.example.actividadmenu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.actividadmenu.databinding.FragmentChatsBinding
import com.example.actividadmenu.viewmodel.BandejaViewModel
import com.google.firebase.auth.FirebaseAuth

class ChatsFragment : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: BandejaViewModel
    private lateinit var adapter: ChatAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[BandejaViewModel::class.java]
        val miId = auth.currentUser?.uid ?: ""

        setupRecyclerView(miId)

        viewModel.listaChats.observe(viewLifecycleOwner) { chats ->
            adapter.updateLista(chats)
            binding.layoutEmptyChats.visibility = if (chats.isEmpty()) View.VISIBLE else View.GONE
        }

        if (miId.isNotEmpty()) {
            viewModel.cargarMisChats(miId)
        }
    }

    private fun setupRecyclerView(miId: String) {
        adapter = ChatAdapter(emptyList(), miId) { chat ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("chatId", chat.id)
            val otroId = chat.usuarios.find { it != miId } ?: ""
            intent.putExtra("otroNombre", chat.nombresUsuarios[otroId])
            startActivity(intent)
        }
        binding.rvChats.layoutManager = LinearLayoutManager(context)
        binding.rvChats.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
