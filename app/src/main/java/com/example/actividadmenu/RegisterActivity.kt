package com.example.actividadmenu

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.actividadmenu.databinding.ActivityRegisterBinding
import com.example.actividadmenu.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnRegister.setOnClickListener {
            ejecutarRegistro()
        }
    }

    private fun ejecutarRegistro() {
        val nombre = binding.etNombreRegister.text.toString().trim()
        val email = binding.etEmailRegister.text.toString().trim()
        val whatsapp = binding.etWhatsappRegister.text.toString().trim()
        val carrera = binding.etCarreraRegister.text.toString().trim()
        val semestre = binding.etSemestreRegister.text.toString().trim()
        val password = binding.etPasswordRegister.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || whatsapp.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            binding.etPasswordRegister.error = "Mínimo 6 caracteres"
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Las contraseñas no coinciden"
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val nuevoUsuario = Usuario(
                        uid = uid,
                        nombre = nombre,
                        email = email,
                        whatsapp = whatsapp,
                        carrera = carrera,
                        semestre = semestre
                    )
                    
                    db.collection("usuarios").document(uid).set(nuevoUsuario)
                        .addOnSuccessListener {
                            Toast.makeText(this, "¡Bienvenido a Changarro!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al crear perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
