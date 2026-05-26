package com.example.actividadmenu

import android.util.Log
import com.example.actividadmenu.model.Aviso
import com.example.actividadmenu.model.Evento
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Utilería para inyectar datos de prueba iniciales en Firebase Firestore.
 * Uso: MockDataSeeder.inyectarDatosDePrueba()
 */
object MockDataSeeder {

    private const val TAG = "MockDataSeeder"
    private val db = FirebaseFirestore.getInstance()

    fun inyectarDatosDePrueba() {
        seedAvisos()
        seedProductos()
        seedEventos()
    }

    private fun seedAvisos() {
        val avisos = listOf(
            Aviso(autorNombre = "Ian", contenido = "¡Plebes, no vino el profe de Bases de Datos!"),
            Aviso(autorNombre = "Mar", contenido = "¿Alguien sabe si la cafetería ya abrió?"),
            Aviso(autorNombre = "Ale", contenido = "Vendiendo boletos para el Foro Arpa, manden mensaje."),
            Aviso(autorNombre = "Alan", contenido = "¿Alguien para armar equipo en Genshin Impact o Minecraft saliendo?")
        )

        avisos.forEach { aviso ->
            db.collection("avisos").add(aviso)
                .addOnSuccessListener { Log.d(TAG, "Aviso agregado con éxito") }
                .addOnFailureListener { e -> Log.e(TAG, "Error al agregar aviso", e) }
        }
    }

    private fun seedProductos() {
        val productos = listOf(
            Producto(
                nombre = "Teclado Mecánico Inalámbrico",
                precio = 850.0,
                stock = 1,
                vendedorNombre = "Alan",
                categoria = "Gadgets"
            ),
            Producto(
                nombre = "Mouse Attack Shark X11",
                precio = 400.0,
                stock = 2,
                vendedorNombre = "Hugo",
                categoria = "Gadgets"
            ),
            Producto(
                nombre = "Tacos al Pastor",
                precio = 15.0,
                descripcion = "Traigo 20 órdenes, ¡lleguenle!",
                stock = 20,
                vendedorNombre = "Mar",
                categoria = "Snacks"
            ),
            Producto(
                nombre = "Esquites con Tuétano",
                precio = 35.0,
                stock = 15,
                vendedorNombre = "Ale",
                categoria = "Snacks"
            )
        )

        productos.forEach { producto ->
            db.collection("productos").add(producto)
                .addOnSuccessListener { Log.d(TAG, "Producto agregado con éxito") }
                .addOnFailureListener { e -> Log.e(TAG, "Error al agregar producto", e) }
        }
    }

    private fun seedEventos() {
        val eventos = listOf(
            Evento(
                titulo = "Asamblea de Ingeniería",
                lugar = "Auditorio",
                fechaDelEvento = "Lunes 11:00 AM",
                descripcion = "Reunión general para tratar temas académicos."
            ),
            Evento(
                titulo = "Torneo de E-Sports",
                lugar = "Centro de Cómputo",
                fechaDelEvento = "Viernes 4:00 PM",
                descripcion = "Competencia de videojuegos abierta a todos los alumnos."
            )
        )

        eventos.forEach { evento ->
            db.collection("eventos").add(evento)
                .addOnSuccessListener { Log.d(TAG, "Evento agregado con éxito") }
                .addOnFailureListener { e -> Log.e(TAG, "Error al agregar evento", e) }
        }
    }
}
