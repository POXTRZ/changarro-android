package com.example.actividadmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class PublicarProductoBottomSheet(
    private val onPublicar: (String, Double, Int, String, String) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_publicar_producto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etNombre = view.findViewById<TextInputEditText>(R.id.etNombreProd)
        val etPrecio = view.findViewById<TextInputEditText>(R.id.etPrecioProd)
        val etStock = view.findViewById<TextInputEditText>(R.id.etStockProd)
        val etDesc = view.findViewById<TextInputEditText>(R.id.etDescProd)
        val spCat = view.findViewById<Spinner>(R.id.spCategoriaProd)
        val btnPublicar = view.findViewById<MaterialButton>(R.id.btnPublicarProd)

        val categorias = arrayOf("Comida", "Dulces", "Apuntes", "Libros", "Electrónicos", "Accesorios", "Otros")
        spCat.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categorias)

        btnPublicar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
            val stock = etStock.text.toString().toIntOrNull() ?: 0
            val desc = etDesc.text.toString().trim()
            val cat = spCat.selectedItem.toString()

            if (nombre.isNotEmpty() && precio > 0) {
                onPublicar(nombre, precio, stock, desc, cat)
                dismiss()
            } else {
                Toast.makeText(context, "Nombre y precio son obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
