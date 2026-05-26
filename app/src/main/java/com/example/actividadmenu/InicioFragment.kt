package com.example.actividadmenu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.model.Aviso
import com.example.actividadmenu.viewmodel.HomeViewModel
import com.example.actividadmenu.repository.ChatRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InicioFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: AvisoAdapter
    private lateinit var destacadosAdapter: ProductoMiniAdapter
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var miWhatsApp = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inicio, container, false)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        val rvAvisos = view.findViewById<RecyclerView>(R.id.rvAvisos)
        val rvDestacados = view.findViewById<RecyclerView>(R.id.rvDestacados)
        val etSearch = view.findViewById<EditText>(R.id.etSearchMuro)
        val fabNuevoAviso = view.findViewById<ExtendedFloatingActionButton>(R.id.fabNuevoAviso)
        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)

        val btnNavMercado = view.findViewById<View>(R.id.btnNavMercado)
        val btnNavEventos = view.findViewById<View>(R.id.btnNavEventos)

        auth.currentUser?.let { user ->
            db.collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre") ?: user.displayName ?: "Estudiante"
                    tvGreeting.text = "Hola, $nombre"
                    miWhatsApp = doc.getString("whatsapp") ?: ""
                }
        }

        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        btnNavMercado.setOnClickListener { bottomNav?.selectedItemId = R.id.nav_mercado }
        btnNavEventos.setOnClickListener { bottomNav?.selectedItemId = R.id.nav_calendario }

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filtrarAvisos(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // CONFIGURACIÓN DEL ADAPTADOR CON CHAT COMUNITARIO
        adapter = AvisoAdapter(
            avisos = emptyList(),
            onEliminarClick = { aviso ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar publicación")
                    .setMessage("¿Estás seguro de que quieres eliminar este aviso?")
                    .setPositiveButton("Eliminar") { _, _ -> viewModel.eliminarAviso(aviso) }
                    .setNegativeButton("Cancelar", null)
                    .show()
            },
            onChatClick = { aviso ->
                val user = auth.currentUser
                if (user != null && user.uid != aviso.autorId) {
                    val repo = ChatRepository()
                    val miNombre = user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Estudiante"
                    
                    Toast.makeText(context, "Abriendo chat con ${aviso.autorNombre}...", Toast.LENGTH_SHORT).show()
                    
                    repo.crearUObtenerChat(user.uid, miNombre, aviso.autorId, aviso.autorNombre) { chatId ->
                        val intent = Intent(requireContext(), ChatActivity::class.java)
                        intent.putExtra("chatId", chatId)
                        intent.putExtra("otroNombre", aviso.autorNombre)
                        startActivity(intent)
                    }
                }
            }
        )
        rvAvisos.layoutManager = LinearLayoutManager(context)
        rvAvisos.adapter = adapter
        rvAvisos.isNestedScrollingEnabled = false

        destacadosAdapter = ProductoMiniAdapter(emptyList()) { producto ->
            Toast.makeText(context, "Producto: ${producto.nombre}", Toast.LENGTH_SHORT).show()
        }
        rvDestacados.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvDestacados.adapter = destacadosAdapter

        viewModel.avisos.observe(viewLifecycleOwner) { lista -> adapter.updateLista(lista) }
        viewModel.productosDestacados.observe(viewLifecycleOwner) { lista -> destacadosAdapter.update(lista) }

        fabNuevoAviso.setOnClickListener {
            val bottomSheet = PublicarAvisoBottomSheet { texto ->
                val user = auth.currentUser
                if (user != null) {
                    val nuevoAviso = Aviso(
                        autorId = user.uid,
                        autorNombre = tvGreeting.text.toString().removePrefix("Hola, "),
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

        return view
    }
}
