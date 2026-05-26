package com.example.actividadmenu.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

/**
 * Fragmento de Perfil que actúa como Dashboard Estadístico.
 */
class PerfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: ProfileViewModel
    private lateinit var stockAdapter: ProductoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)
        
        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // 1. Referencias UI Perfil
        val tvProfileName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val tvUserEmailDetail = view.findViewById<TextView>(R.id.tvUserEmailDetail)
        val tvAvatarInitials = view.findViewById<TextView>(R.id.tvAvatarInitials)
        val tvCarrera = view.findViewById<TextView>(R.id.tvCarreraPerfil)
        val tvSemestre = view.findViewById<TextView>(R.id.tvSemestrePerfil)
        val btnSeedData = view.findViewById<MaterialButton>(R.id.btnSeedData)
        
        // 2. Referencias UI Dashboard
        val tvGanadoHoy = view.findViewById<TextView>(R.id.tvGanadoHoy)
        val tvMisVentas = view.findViewById<TextView>(R.id.tvMisVentas)
        val rvAlertasStock = view.findViewById<RecyclerView>(R.id.rvAlertasStock)
        val tvLabelAlertas = view.findViewById<TextView>(R.id.tvLabelAlertas)
        
        // 3. Botones
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnChangeEmail = view.findViewById<LinearLayout>(R.id.btnChangeEmail)
        val btnChangePassword = view.findViewById<LinearLayout>(R.id.btnChangePassword)
        val switchTheme = view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchTheme)

        // Configurar Switch de Tema
        val isDarkMode = androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
        switchTheme.isChecked = isDarkMode

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Configurar RecyclerView de Alertas
        stockAdapter = ProductoAdapter(emptyList(), onClick = { producto ->
            Toast.makeText(context, "Stock bajo: ${producto.stock} unidades", Toast.LENGTH_SHORT).show()
        }, onLongClick = {})
        
        rvAlertasStock.layoutManager = LinearLayoutManager(context)
        rvAlertasStock.adapter = stockAdapter

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val email = currentUser.email ?: "Sin correo"
            val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            
            tvProfileName.text = name
            tvProfileEmail.text = email
            tvUserEmailDetail.text = email
            tvAvatarInitials.text = name.take(1).uppercase()

            // Cargar datos financieros del dashboard
            viewModel.cargarDatosPerfil(currentUser.uid)
        }

        // 4. Observar LiveData del ViewModel
        viewModel.perfilUsuario.observe(viewLifecycleOwner) { usuario ->
            tvProfileName.text = usuario.nombre
            tvProfileEmail.text = usuario.email
            tvUserEmailDetail.text = usuario.email
            tvAvatarInitials.text = usuario.nombre.take(1).uppercase()
            tvCarrera.text = if (usuario.carrera.isNotEmpty()) usuario.carrera else "Carrera no def."
            tvSemestre.text = if (usuario.semestre.isNotEmpty()) "${usuario.semestre}° Semestre" else "Sem. no def."
        }

        // --- BOTÓN SECRETO PARA INYECTAR DATOS ---
        btnSeedData.setOnClickListener {
            MockDataSeeder.inyectarDatosDePrueba()
            Toast.makeText(requireContext(), "🚀 Inyectando datos de prueba...", Toast.LENGTH_SHORT).show()
        }

        // 4. Observar LiveData del ViewModel
        viewModel.ventasHoy.observe(viewLifecycleOwner) { monto ->
            tvGanadoHoy.text = monto
        }

        viewModel.totalProductosVendidos.observe(viewLifecycleOwner) { cantidad ->
            tvMisVentas.text = "$cantidad items"
        }

        viewModel.alertasStock.observe(viewLifecycleOwner) { productosBajos ->
            if (productosBajos.isNotEmpty()) {
                tvLabelAlertas.visibility = View.VISIBLE
                rvAlertasStock.visibility = View.VISIBLE
                stockAdapter.updateLista(productosBajos)
            } else {
                tvLabelAlertas.visibility = View.GONE
                rvAlertasStock.visibility = View.GONE
            }
        }

        // Lógica de botones de Auth (Logout)
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        btnChangeEmail.setOnClickListener {
            showUpdateDialog("Cambiar Correo", "Nuevo correo") { newValue ->
                currentUser?.verifyBeforeUpdateEmail(newValue)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Se ha enviado un correo de verificación", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnChangePassword.setOnClickListener {
            showUpdateDialog("Cambiar Contraseña", "Nueva contraseña", true) { newValue ->
                if (newValue.length >= 6) {
                    currentUser?.updatePassword(newValue)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    private fun showUpdateDialog(title: String, hint: String, isPassword: Boolean = false, onConfirm: (String) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        val input = EditText(requireContext())
        input.hint = hint
        if (isPassword) {
            input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        builder.setView(input)
        builder.setPositiveButton("Actualizar") { _, _ ->
            val text = input.text.toString()
            if (text.isNotEmpty()) onConfirm(text)
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}
