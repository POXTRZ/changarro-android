package com.example.actividadmenu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.actividadmenu.databinding.FragmentInicioBinding
import com.example.actividadmenu.model.Aviso
import com.example.actividadmenu.repository.ChatRepository
import com.example.actividadmenu.viewmodel.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: AvisoAdapter
    private lateinit var destacadosAdapter: ProductoMiniAdapter
    
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var miWhatsApp = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // 1. Cargar datos del usuario
        auth.currentUser?.let { user ->
            db.collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre") ?: user.displayName ?: "Estudiante"
                    binding.tvGreeting.text = "Hola, $nombre"
                    miWhatsApp = doc.getString("whatsapp") ?: ""
                }
        }

        setupRecyclerViews()
        setupNavigation()
        setupSearch()

        // 2. Observadores
        viewModel.avisos.observe(viewLifecycleOwner) { lista -> adapter.updateLista(lista) }
        viewModel.productosDestacados.observe(viewLifecycleOwner) { lista -> destacadosAdapter.update(lista) }

        binding.fabNuevoAviso.setOnClickListener { mostrarDialogoAviso() }
    }

    private fun setupRecyclerViews() {
        adapter = AvisoAdapter(
            avisos = emptyList(),
            onEliminarClick = { aviso ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar publicación")
                    .setMessage("¿Estás seguro?")
                    .setPositiveButton("Eliminar") { _, _ -> viewModel.eliminarAviso(aviso) }
                    .setNegativeButton("Cancelar", null).show()
            },
            onChatClick = { aviso ->
                val user = auth.currentUser
                if (user != null && user.uid != aviso.autorId) {
                    val repo = ChatRepository()
                    repo.crearUObtenerChat(user.uid, "Usuario", aviso.autorId, aviso.autorNombre) { chatId ->
                        val intent = Intent(requireContext(), ChatActivity::class.java)
                        intent.putExtra("chatId", chatId)
                        intent.putExtra("otroNombre", aviso.autorNombre)
                        startActivity(intent)
                    }
                }
            }
        )
        binding.rvAvisos.layoutManager = LinearLayoutManager(context)
        binding.rvAvisos.adapter = adapter

        destacadosAdapter = ProductoMiniAdapter(emptyList()) { p ->
            // Aquí iría el detalle del producto
        }
        binding.rvDestacados.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvDestacados.adapter = destacadosAdapter
    }

    private fun setupNavigation() {
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        binding.btnNavMercado.setOnClickListener { bottomNav?.selectedItemId = R.id.nav_mercado }
        binding.btnNavEventos.setOnClickListener { bottomNav?.selectedItemId = R.id.nav_calendario }
    }

    private fun setupSearch() {
        binding.etSearchMuro.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filtrarAvisos(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun mostrarDialogoAviso() {
        val bottomSheet = PublicarAvisoBottomSheet { texto ->
            val user = auth.currentUser
            if (user != null) {
                val nuevoAviso = Aviso(
                    autorId = user.uid,
                    autorNombre = binding.tvGreeting.text.toString().removePrefix("Hola, "),
                    autorWhatsapp = miWhatsApp,
                    contenido = texto,
                    timestamp = System.currentTimeMillis()
                )
                viewModel.publicarAviso(nuevoAviso) {
                    Toast.makeText(context, "¡Publicado!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        bottomSheet.show(parentFragmentManager, "PublicarAvisoBottomSheet")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
