package com.example.actividadmenu.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.LoginActivity
import com.example.actividadmenu.MockDataSeeder
import com.example.actividadmenu.ProductoAdapter
import com.example.actividadmenu.R
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatDelegate

class PerfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: ProfileViewModel
    private lateinit var stockAdapter: ProductoAdapter
    private lateinit var favoritesAdapter: ProductoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)
        
        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // 1. Referencias Perfil
        val tvProfileName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val tvUserEmailDetail = view.findViewById<TextView>(R.id.tvUserEmailDetail)
        val tvAvatarInitials = view.findViewById<TextView>(R.id.tvAvatarInitials)
        val tvCarrera = view.findViewById<TextView>(R.id.tvCarreraPerfil)
        val tvSemestre = view.findViewById<TextView>(R.id.tvSemestrePerfil)
        val btnSeedData = view.findViewById<MaterialButton>(R.id.btnSeedData)
        
        // 2. Dashboard
        val tvGanadoHoy = view.findViewById<TextView>(R.id.tvGanadoHoy)
        val tvMisVentas = view.findViewById<TextView>(R.id.tvMisVentas)
        val rvAlertasStock = view.findViewById<RecyclerView>(R.id.rvAlertasStock)
        val tvLabelAlertas = view.findViewById<TextView>(R.id.tvLabelAlertas)
        val rvFavoritos = view.findViewById<RecyclerView>(R.id.rvFavoritos)
        val tvLabelFavoritos = view.findViewById<TextView>(R.id.tvLabelFavoritos)
        
        // 3. Botones y Settings
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        val btnChangeEmail = view.findViewById<LinearLayout>(R.id.btnChangeEmail)
        val btnChangePassword = view.findViewById<LinearLayout>(R.id.btnChangePassword)
        val switchTheme = view.findViewById<SwitchMaterial>(R.id.switchTheme)

        // Configuración de Tema
        switchTheme.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        // Setup Adapters
        stockAdapter = ProductoAdapter(emptyList(), onClick = { p ->
            Toast.makeText(context, "Quedan ${p.stock} de ${p.nombre}", Toast.LENGTH_SHORT).show()
        }, onLongClick = {})
        
        favoritesAdapter = ProductoAdapter(emptyList(), onClick = { /* Detalle */ }, onLongClick = {})

        rvAlertasStock.layoutManager = LinearLayoutManager(context)
        rvAlertasStock.adapter = stockAdapter
        
        rvFavoritos.layoutManager = LinearLayoutManager(context)
        rvFavoritos.adapter = favoritesAdapter

        // Cargar Datos
        auth.currentUser?.let { user ->
            viewModel.cargarDatosPerfil(user.uid)
            tvProfileEmail.text = user.email
            tvUserEmailDetail.text = user.email
        }

        // Observadores Senior
        viewModel.perfilUsuario.observe(viewLifecycleOwner) { usuario ->
            tvProfileName.text = usuario.nombre
            tvAvatarInitials.text = usuario.nombre.take(1).uppercase()
            tvCarrera.text = usuario.carrera.ifEmpty { "Carrera no def." }
            tvSemestre.text = if (usuario.semestre.isNotEmpty()) "${usuario.semestre}° Semestre" else "Sin semestre"
        }

        viewModel.ventasHoy.observe(viewLifecycleOwner) { tvGanadoHoy.text = it }
        viewModel.totalProductosVendidos.observe(viewLifecycleOwner) { tvMisVentas.text = "$it ventas" }

        viewModel.alertasStock.observe(viewLifecycleOwner) { lista ->
            val show = lista.isNotEmpty()
            tvLabelAlertas.visibility = if (show) View.VISIBLE else View.GONE
            rvAlertasStock.visibility = if (show) View.VISIBLE else View.GONE
            stockAdapter.updateLista(lista)
        }

        viewModel.misFavoritos.observe(viewLifecycleOwner) { lista ->
            val show = lista.isNotEmpty()
            tvLabelFavoritos.visibility = if (show) View.VISIBLE else View.GONE
            rvFavoritos.visibility = if (show) View.VISIBLE else View.GONE
            favoritesAdapter.updateLista(lista)
        }

        // Botón de Inyección (Mejorado)
        btnSeedData.setOnClickListener {
            MockDataSeeder.inyectarDatosDePrueba()
            Toast.makeText(context, "🚀 Datos de prueba inyectados (Re-abre la app)", Toast.LENGTH_LONG).show()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }

        return view
    }
}
