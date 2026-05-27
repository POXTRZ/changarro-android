package com.example.actividadmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.actividadmenu.databinding.FragmentEventosBinding
import com.example.actividadmenu.viewmodel.EventosViewModel
import com.google.firebase.auth.FirebaseAuth

class EventosFragment : Fragment() {

    private var _binding: FragmentEventosBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: EventosViewModel
    private lateinit var adapter: EventoAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEventosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[EventosViewModel::class.java]

        setupRecyclerView()
        setupCalendar()

        viewModel.eventos.observe(viewLifecycleOwner) { lista ->
            adapter.updateLista(lista)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.errorMostrado()
            }
        }

        binding.fabAgregarEvento.setOnClickListener { mostrarBottomSheetAgregar() }
    }

    private fun setupRecyclerView() {
        adapter = EventoAdapter(emptyList())
        binding.rvEventos.layoutManager = LinearLayoutManager(context)
        binding.rvEventos.adapter = adapter
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            viewModel.filtrarPorFecha(dayOfMonth, month + 1, year)
        }
    }

    private fun mostrarBottomSheetAgregar() {
        val bottomSheet = PublicarEventoBottomSheet { titulo, desc, lugar, fecha, imagenUrl ->
            val userId = auth.currentUser?.uid ?: ""
            viewModel.publicarEvento(titulo, desc, lugar, fecha, imagenUrl, userId)
            Toast.makeText(context, "Publicando evento...", Toast.LENGTH_SHORT).show()
        }
        bottomSheet.show(parentFragmentManager, "PublicarEventoBottomSheet")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
