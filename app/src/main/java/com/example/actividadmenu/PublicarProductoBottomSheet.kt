package com.example.actividadmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.actividadmenu.databinding.BottomSheetPublicarProductoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * BottomSheet para publicar productos. 
 * Se ha removido la lógica de Firebase Storage para mantener el plan gratuito,
 * regresando al uso de URLs de imagen.
 */
class PublicarProductoBottomSheet(
    private val onPublicar: (String, Double, Int, String, String, String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPublicarProductoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetPublicarProductoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar Spinner de categorías
        val categorias = arrayOf("Comida", "Dulces", "Apuntes", "Libros", "Electrónicos", "Accesorios", "Otros")
        binding.spCategoriaProd.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categorias)

        binding.btnPublicarProd.setOnClickListener {
            val nombre = binding.etNombreProd.text.toString().trim()
            val imagenUrl = binding.etImagenUrl.text.toString().trim()
            val precio = binding.etPrecioProd.text.toString().toDoubleOrNull() ?: 0.0
            val stock = binding.etStockProd.text.toString().toIntOrNull() ?: 0
            val desc = binding.etDescProd.text.toString().trim()
            val cat = binding.spCategoriaProd.selectedItem.toString()

            if (validarCampos(nombre, precio, stock)) {
                onPublicar(nombre, precio, stock, desc, cat, imagenUrl)
                dismiss()
            }
        }
    }

    private fun validarCampos(n: String, p: Double, s: Int): Boolean {
        var esValido = true
        if (n.isEmpty()) { 
            binding.tilNombre.error = "El nombre es obligatorio"
            esValido = false 
        } else {
            binding.tilNombre.error = null
        }
        
        if (p <= 0) { 
            binding.tilPrecio.error = "Ingresa un precio válido"
            esValido = false 
        } else {
            binding.tilPrecio.error = null
        }
        
        if (s <= 0) { 
            binding.tilStock.error = "El stock debe ser mayor a 0"
            esValido = false 
        } else {
            binding.tilStock.error = null
        }
        
        return esValido
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
