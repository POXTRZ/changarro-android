package com.example.actividadmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.actividadmenu.databinding.BottomSheetPublicarEventoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PublicarEventoBottomSheet(
    private val onPublicar: (titulo: String, desc: String, lugar: String, fecha: String, imagenUrl: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPublicarEventoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetPublicarEventoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPublicarEvento.setOnClickListener {
            val titulo = binding.etTituloEvento.text.toString().trim()
            val desc = binding.etDescEvento.text.toString().trim()
            val lugar = binding.etLugarEvento.text.toString().trim()
            val fecha = binding.etFechaEvento.text.toString().trim()
            val imagenUrl = binding.etImagenUrlEvento.text.toString().trim()

            if (titulo.isEmpty() || fecha.isEmpty()) {
                // Podrías agregar errores visuales aquí con TextInputLayout
                return@setOnClickListener
            }

            onPublicar(titulo, desc, lugar, fecha, imagenUrl)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
