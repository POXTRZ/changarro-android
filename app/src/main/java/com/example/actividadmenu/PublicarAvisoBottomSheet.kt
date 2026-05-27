package com.example.actividadmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.actividadmenu.databinding.BottomSheetPublicarAvisoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PublicarAvisoBottomSheet(
    private val onPublicar: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPublicarAvisoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetPublicarAvisoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancelarAviso.setOnClickListener {
            dismiss()
        }

        binding.btnPublicarAviso.setOnClickListener {
            val texto = binding.etContenidoAviso.text.toString().trim()

            if (texto.isEmpty()) {
                binding.tilContenidoAviso.error = "Escribe algo para la comunidad"
            } else {
                binding.tilContenidoAviso.error = null
                onPublicar(texto)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
