package es.dam.paperwings.view.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import es.dam.paperwings.R
import es.dam.paperwings.databinding.FragmentCategoryBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.CategoryClickListener
import es.dam.paperwings.model.api.ApiServiceFactory
import es.dam.paperwings.model.constans.Constants
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.view.BookDetailActivity
import es.dam.paperwings.view.CategoryActivity
import es.dam.paperwings.view.recicledView.CardAdapterCategory
import kotlinx.coroutines.launch

/**
 * Clase para mostrar las categorías de los libros de la base de datos
 */
class CategoryFragment : Fragment(), CategoryClickListener {

    // This property holds the binding object that provides access to the views in the fragment_home.xml layout.
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!


    // LiveData to hold the list of books
    private val _booksLiveData = MutableLiveData<List<Book>>()
    val booksLiveData: LiveData<List<Book>> get() = _booksLiveData

    // Inicializo elementos de la vista
    private lateinit var cardAdapterCategory: CardAdapterCategory

    private var backPressedOnce = false
    private val backPressHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment and initialize the binding object.
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val view = binding.root


        // Set up the RecyclerView with a GridLayoutManager and the CardAdapter.
        cardAdapterCategory = CardAdapterCategory(emptyList(), this)

        binding.recycledViewCategoryUser.apply {
            layoutManager = GridLayoutManager(activity?.applicationContext, 1)
            adapter = cardAdapterCategory
        }

        // Observe the books LiveData and update the adapter when the data changes
        booksLiveData.observe(viewLifecycleOwner, { books ->
            cardAdapterCategory.updateBooks(books)
        })


        // Start loading books
        lifecycleScope.launch {
            fetchBooks()
        }

        // Función para que solo salga si pulsa back dos veces seguidas
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedOnce) {
                    activity?.finish()
                } else {
                    backPressedOnce = true
                    showToast("Haga click de nuevo para salir")
                    backPressHandler.postDelayed({ backPressedOnce = false }, 2000)
                }
            }
        })

        return view
    }

    // Obtengo todos los libros de la base de datos
    suspend fun fetchBooks(){
        val bookService = ApiServiceFactory.makeBooksService()
        try {

            // Get the books
            val response = bookService.listBooks()
            if (response.isSuccessful && response.body() != null) { // Si no hay error y no es null

                // Store the books in the LiveData
                val booksList = response.body()!!.data
                if (booksList != null && booksList.isNotEmpty()) {

                    // Se almacenan todos los atributos de cada libro
                    _booksLiveData.postValue(booksList)

                } else {
                    println("Lista vacía")
                }
            } else {
                println("Error al obtener los libros: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            println("Error en la red o al parsear los datos: ${e.message}")
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onCategoryClick(category: String) {
        val intent = Intent(activity?.applicationContext, CategoryActivity::class.java)
        intent.putExtra("category", category)
        intent.putExtra("source", Constants.CATEGORY_FRAGMENT) // Añado el extra para indicar que estoy en "home"
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
