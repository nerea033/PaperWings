package es.dam.paperwings.view.user.fragments

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
import es.dam.paperwings.databinding.FragmentHomeBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.api.ApiServiceFactory
import es.dam.paperwings.model.constans.Constants
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.view.user.BookDetailActivity
import es.dam.paperwings.view.user.recicledView.CardAdapterHome
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), BookClickListener {

    // This property holds the binding object that provides access to the views in the fragment_home.xml layout.
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    // LiveData to hold the list of books
    private val _booksLiveData = MutableLiveData<List<Book>>()
    val booksLiveData: LiveData<List<Book>> get() = _booksLiveData


    private lateinit var cardAdapterHome: CardAdapterHome

    // Elementos de la vista
    private lateinit var searchView: SearchView


    private var backPressedOnce = false
    private val backPressHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment and initialize the binding object.
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize the SearchView from the layout
        searchView = view.findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(newText: String?): Boolean {
                if (newText != null && newText.length >= 3) {
                    lifecycleScope.launch {
                        searchBooks(newText)
                    }
                    return true
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {

                    if (newText.length >= 3) {
                        // Iniciar la búsqueda solo si hay al menos 3 caracteres
                        lifecycleScope.launch {
                            searchBooks(newText)
                        }
                    } else if (newText.length == 0) {
                        // Si el texto es vacío, mostrar todos los libros nuevamente
                        lifecycleScope.launch {
                            fetchBooks() // Esto recuperará todos los libros nuevamente
                        }
                    } else {
                        // Si no coincide, no muestro nada
                        _booksLiveData.postValue(emptyList())
                    }
                }
                return true
            }
        })

        // Set up the RecyclerView with a GridLayoutManager and the CardAdapter.
        cardAdapterHome = CardAdapterHome(emptyList(), this)

        binding.recycledViewUser.apply {
            layoutManager = GridLayoutManager(activity?.applicationContext, 2)
            adapter = cardAdapterHome
        }

        // Observe the books LiveData and update the adapter when the data changes
        booksLiveData.observe(viewLifecycleOwner, { books ->
            cardAdapterHome.updateBooks(books)
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
                    Toast.makeText(context, "Haga click de nuevo para salir", Toast.LENGTH_SHORT).show()
                    backPressHandler.postDelayed({ backPressedOnce = false }, 2000)
                }
            }
        })

        return view
    }

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

    // Buscar libros a través de sus atributos en el cuadro de búsqueda
    private suspend fun searchBooks(query: String) {
        val bookService = ApiServiceFactory.makeBooksService()
        try {
            val response = bookService.searchBooks(query, query, query, query, query)
            if (response.isSuccessful && response.body() != null) {
                val booksList = response.body()!!.data
                if (booksList != null && booksList.isNotEmpty()) {
                    _booksLiveData.postValue(booksList)
                } else {
                    _booksLiveData.postValue(emptyList())
                }
            } else {
                showToast("Error al buscar los libros: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            showToast("Error en la red o al parsear los datos: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(activity?.applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBookClick(book: Book) {
        val intent = Intent(activity?.applicationContext, BookDetailActivity::class.java)
        intent.putExtra("id_book", book.id)
        intent.putExtra("source", Constants.HOME_FRAGMENT) // Añado el extra para indicar que estoy en "home"
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
