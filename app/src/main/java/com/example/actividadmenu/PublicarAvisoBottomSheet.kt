package com.example.actividadmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Fragmento moderno para publicar avisos usando BottomSheetDialogFragment.
 * @param onPublicar Callback que se ejecuta cuando el usuario presiona "Compartir" con texto válido.
 */
class PublicarAvisoBottomSheet(
    private val onPublicar: (String) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_publicar_aviso, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tilContenido = view.findViewById<TextInputLayout>(R.id.tilContenidoAviso)
        val etContenido = view.findViewById<TextInputEditText>(R.id.etContenidoAviso)
        val btnCancelar = view.findViewById<MaterialButton>(R.id.btnCancelarAviso)
        val btnPublicar = view.findViewById<MaterialButton>(R.id.btnPublicarAviso)

        // Botón Cancelar
        btnCancelar.setOnClickListener {
            dismiss()
        }

        // Botón Compartir (Lógica de validación)
        btnPublicar.setOnClickListener {
            val texto = etContenido.text.toString().trim()

            if (texto.isEmpty()) {
                tilContenido.error = "Escribe algo para la comunidad"
            } else {
                tilContenido.error = null // Limpiar error si existe
                onPublicar(texto) // Ejecutar callback de éxito
                dismiss() // Cerrar el Bottom Sheet
            }
        }
    }


}
