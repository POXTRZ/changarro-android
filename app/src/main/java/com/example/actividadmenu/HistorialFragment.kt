package com.example.actividadmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.actividadmenu.databinding.FragmentHistorialBinding
import com.example.actividadmenu.viewmodel.MisProductosViewModel
import com.google.firebase.auth.FirebaseAuth

class HistorialFragment : Fragment() {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MisProductosViewModel
    private lateinit var adapter: TransaccionAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Usamos el ViewModel de MisProductos ya que maneja las transacciones del usuario
        viewModel = ViewModelProvider(this)[MisProductosViewModel::class.java]

        setupRecyclerView()

        viewModel.listaTransacciones.observe(viewLifecycleOwner) { lista ->
            adapter.updateLista(lista)
            binding.layoutEmptyHistorial.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
        }

        auth.currentUser?.let {
            viewModel.cargarTransacciones(it.uid)
        }
    }

    private fun setupRecyclerView() {
        adapter = TransaccionAdapter(emptyList())
        binding.rvHistorial.layoutManager = LinearLayoutManager(context)
        binding.rvHistorial.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
