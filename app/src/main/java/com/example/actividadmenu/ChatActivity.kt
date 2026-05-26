package com.example.actividadmenu

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.viewmodel.SalaChatViewModel
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    private lateinit var viewModel: SalaChatViewModel
    private lateinit var adapter: MensajeAdapter
    private val auth = FirebaseAuth.getInstance()
    private var chatId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatId = intent.getStringExtra("chatId")
        val otroNombre = intent.getStringExtra("otroNombre") ?: "Chat"

        viewModel = ViewModelProvider(this)[SalaChatViewModel::class.java]

        val tvNombre = findViewById<TextView>(R.id.tvNombreOtroUsuario)
        val rvMensajes = findViewById<RecyclerView>(R.id.rvMensajes)
        val etMensaje = findViewById<EditText>(R.id.etMensaje)
        val btnEnviar = findViewById<ImageButton>(R.id.btnEnviarMensaje)
        val btnBack = findViewById<ImageButton>(R.id.btnBackChat)

        tvNombre.text = otroNombre

        val miId = auth.currentUser?.uid ?: ""
        adapter = MensajeAdapter(emptyList(), miId)
        rvMensajes.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Los mensajes nuevos aparecen abajo
        }
        rvMensajes.adapter = adapter

        chatId?.let { id ->
            viewModel.inicializarSala(id)
            
            viewModel.mensajes.observe(this) { lista ->
                adapter.updateLista(lista)
                if (lista.isNotEmpty()) {
                    rvMensajes.smoothScrollToPosition(lista.size - 1)
                }
            }
        }

        btnEnviar.setOnClickListener {
            val texto = etMensaje.text.toString()
            if (texto.isNotEmpty() && chatId != null) {
                viewModel.enviarMensajeTexto(chatId!!, miId, texto)
                etMensaje.setText("")
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
