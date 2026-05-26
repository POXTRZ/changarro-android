package com.example.actividadmenu

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.actividadmenu.model.Usuario
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val etNombre = findViewById<TextInputEditText>(R.id.etNombreRegister)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmailRegister)
        val etWhatsapp = findViewById<TextInputEditText>(R.id.etWhatsappRegister)
        val etCarrera = findViewById<TextInputEditText>(R.id.etCarreraRegister)
        val etSemestre = findViewById<TextInputEditText>(R.id.etSemestreRegister)
        val etPassword = findViewById<TextInputEditText>(R.id.etPasswordRegister)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnRegister.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val whatsapp = etWhatsapp.text.toString().trim()
            val carrera = etCarrera.text.toString().trim()
            val semestre = etSemestre.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (nombre.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && whatsapp.isNotEmpty()) {
                if (password.length >= 6) {
                    if (password == confirmPassword) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    if (user != null) {
                                        val nuevoUsuario = Usuario(
                                            uid = user.uid,
                                            nombre = nombre,
                                            email = email,
                                            whatsapp = whatsapp,
                                            carrera = carrera,
                                            semestre = semestre
                                        )
                                        
                                        db.collection("usuarios").document(user.uid).set(nuevoUsuario)
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show()
                                                startActivity(Intent(this, MainActivity::class.java))
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                } else {
                                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }
}