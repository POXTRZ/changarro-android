package com.example.actividadmenu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.actividadmenu.databinding.ActivityChatBinding
import com.example.actividadmenu.viewmodel.SalaChatViewModel
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: SalaChatViewModel
    private lateinit var adapter: MensajeAdapter
    private val auth = FirebaseAuth.getInstance()
    private var chatId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatId = intent.getStringExtra("chatId")
        val otroNombre = intent.getStringExtra("otroNombre") ?: "Chat"

        viewModel = ViewModelProvider(this)[SalaChatViewModel::class.java]

        binding.tvNombreOtroUsuario.text = otroNombre
        binding.tvAvatarLetra.text = otroNombre.take(1).uppercase()

        val miId = auth.currentUser?.uid ?: ""
        adapter = MensajeAdapter(emptyList(), miId)
        
        binding.rvMensajes.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.rvMensajes.adapter = adapter

        chatId?.let { id ->
            viewModel.inicializarSala(id)
            viewModel.mensajes.observe(this) { lista ->
                adapter.updateLista(lista)
                if (lista.isNotEmpty()) {
                    binding.rvMensajes.smoothScrollToPosition(lista.size - 1)
                }
            }
        }

        binding.btnEnviarMensaje.setOnClickListener {
            val texto = binding.etMensaje.text.toString().trim()
            if (texto.isNotEmpty() && chatId != null) {
                viewModel.enviarMensajeTexto(chatId!!, miId, texto)
                binding.etMensaje.setText("")
            }
        }

        binding.btnBackChat.setOnClickListener { finish() }
    }
}
