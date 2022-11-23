package edu.uca.innovatech.cookbook.ui.view.main.recipe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.uca.innovatech.cookbook.CookBookApp
import edu.uca.innovatech.cookbook.R
import edu.uca.innovatech.cookbook.data.database.entities.Paso
import edu.uca.innovatech.cookbook.data.database.entities.RecetasConPasos
import edu.uca.innovatech.cookbook.databinding.FragmentAddRecipeDataBinding
import edu.uca.innovatech.cookbook.databinding.FragmentSeeRecipeBinding
import edu.uca.innovatech.cookbook.ui.view.adapter.StepsDetailsCardAdapter
import edu.uca.innovatech.cookbook.ui.viewmodel.RecipesViewModel
import edu.uca.innovatech.cookbook.ui.viewmodel.RecipesViewModelFactory

class SeeRecipeFragment : Fragment() {

    lateinit var receta: RecetasConPasos
    lateinit var pasos: List<Paso>

    private val viewModel: RecipesViewModel by viewModels {
        RecipesViewModelFactory(
            (activity?.application as CookBookApp).database.RecetaDao()
        )
    }

    private var _binding: FragmentSeeRecipeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSeeRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        val id = activity?.intent?.getIntExtra("id_receta", 0)

        val adapter = StepsDetailsCardAdapter {}

        binding.rcvPasos.layoutManager = LinearLayoutManager(this.context)
        binding.rcvPasos.adapter = adapter

        if (id != null) {
            viewModel.agarrarReceta(id)
                .observe(this.viewLifecycleOwner) { selectedReceta ->
                    selectedReceta?.let {
                        pasos = it.pasos
                        adapter.submitList(pasos)
                    }
                }
        }
    }

    private fun bind(receta: RecetasConPasos) {
        binding.apply {
            ivFotoReceta.setImageBitmap(receta.receta.bitmapImagen)
            topAppBar.title = receta.receta.nombre
            topAppBar.subtitle = receta.receta.autor

            topAppBar.setNavigationOnClickListener { activity?.onBackPressed() }
            topAppBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.editar_receta -> {
                        editarReceta()
                        true
                    }
                    R.id.eliminar_receta -> {
                        mostrarDialogConfirmacion()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun mostrarDialogConfirmacion() {
        this.context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(getString(android.R.string.dialog_alert_title))
                .setMessage(getString(R.string.delete_recipe_dialog_msg))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.no)) { _, _ -> }
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    eliminarReceta()
                }
                .show()
        }
    }

    private fun editarReceta() {
        val id = activity?.intent?.getIntExtra("id_receta", 0)

        if (id != null) {
            val action = SeeRecipeFragmentDirections.actionSeeRecipeFragmentToNavGraphNewRecipes(id)
            findNavController().navigate(action)
        }
    }

    private fun eliminarReceta() {
        viewModel.deleteReceta(receta)
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    /**
     * Called when fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}