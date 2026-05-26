package com.example.actividadmenu

import android.util.Log
import com.example.actividadmenu.model.Aviso
import com.example.actividadmenu.model.Evento
import com.example.actividadmenu.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore

object MockDataSeeder {
    private val db = FirebaseFirestore.getInstance()

    fun inyectarDatosDePrueba() {
        seedUsuarios()
        seedAvisos()
        seedProductos()
        seedEventos()
    }

    private fun seedUsuarios() {
        val usuarios = listOf(
            Usuario("mock_user_1", "Ian", "ian@uni.mx", carrera = "Ing. Software", semestre = "8"),
            Usuario("mock_user_2", "Mar", "mar@uni.mx", carrera = "Diseño", semestre = "6"),
            Usuario("mock_user_3", "Ale", "ale@uni.mx", carrera = "Derecho", semestre = "4"),
            Usuario("mock_user_4", "Alan", "alan@uni.mx", carrera = "Ing. Civil", semestre = "9")
        )
        usuarios.forEach { db.collection("usuarios").document(it.uid).set(it) }
    }

    private fun seedAvisos() {
        val avisos = listOf(
            Aviso(autorId = "mock_user_1", autorNombre = "Ian", contenido = "¡No vino el profe de Redes! Room 402 libre."),
            Aviso(autorId = "mock_user_2", autorNombre = "Mar", contenido = "¿Alguien vende café cerca de la entrada?"),
            Aviso(autorId = "mock_user_4", autorNombre = "Alan", contenido = "Busco equipo para el torneo de FIFA.")
        )
        avisos.forEach { db.collection("avisos").add(it) }
    }

    private fun seedProductos() {
        val productos = listOf(
            Producto(nombre = "Calculadora TI-Nspire", precio = 1200.0, stock = 1, vendedorId = "mock_user_1", vendedorNombre = "Ian", categoria = "Gadgets"),
            Producto(nombre = "Brownies Fantasmonas", precio = 45.0, stock = 20, vendedorId = "mock_user_2", vendedorNombre = "Mar", categoria = "Snacks")
        )
        productos.forEach { db.collection("productos").add(it) }
    }

    private fun seedEventos() {
        val eventos = listOf(
            Evento(titulo = "Hackathon Changarro", lugar = "Laboratorio A", fechaDelEvento = "25 Nov"),
            Evento(titulo = "Peda masiva (After Exam)", lugar = "Depa de Alan", fechaDelEvento = "Viernes 9PM", descripcion = "Traigan su propio pomo.")
        )
        eventos.forEach { db.collection("eventos").add(it) }
    }
}
