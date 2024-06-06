package es.dam.paperwings.view.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import es.dam.paperwings.R
import es.dam.paperwings.databinding.FragmentUpdateBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.api.ApiServiceFactory
import es.dam.paperwings.model.constans.Constants
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.view.activities.BookUpdateActivity
import es.dam.paperwings.view.adapters.CardAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 * Use the [UpdateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdateFragment : Fragment(), BookClickListener {

    // This property holds the binding object that provides access to the views in the fragment_home.xml layout.
    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!


    // LiveData to hold the list of books
    private val _booksLiveData = MutableLiveData<List<Book>>()
    val booksLiveData: LiveData<List<Book>> get() = _booksLiveData


    private lateinit var cardAdapter: CardAdapter

    // Elementos de la vista
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment and initialize the binding object.
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        val view = binding.root


        // Initialize the SearchView from the layout
        searchView = view.findViewById(R.id.searchViewUpdate)
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
        cardAdapter = CardAdapter(emptyList(), this)

        binding.recycledViewUpdate.apply {
            layoutManager = GridLayoutManager(activity?.applicationContext, 2)
            adapter = cardAdapter
        }

        // Observe the books LiveData and update the adapter when the data changes
        booksLiveData.observe(viewLifecycleOwner) { books ->
            cardAdapter.updateBooks(books)
        }


        // Start loading books
        lifecycleScope.launch {
            fetchBooks()
        }

        return view

    }

    suspend fun fetchBooks(){
        val bookService = ApiServiceFactory.makeBooksService()
        _booksLiveData.postValue(emptyList())
        try {
            Log.d("FetchBooks", "Iniciando la solicitud de libros")
            // Get the books
            val response = bookService.listBooks()
            if (response.isSuccessful && response.body() != null) { // Si no hay error y no es null

                // Store the books in the LiveData
                val booksList = response.body()!!.data
                if (booksList != null && booksList.isNotEmpty()) {
                    Log.d("FetchBooks", "Lista de libros obtenida: $booksList")
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
                Log.d("SearchBook", "Respuesta fallida")
                val errorResponse = response.errorBody()?.string()
                withContext(Dispatchers.Main) {
                    showAlert("Error", "Error al buscar el libro: $errorResponse")
                }
            }
        } catch (e: Exception) {
            Log.e("SearchBook", "Excepción atrapada: ${e.message}")
            withContext(Dispatchers.Main) {
                showAlert("Error", "Error al conectar con la API: ${e.message}")
            }
        }
    }

    suspend fun deleteBook(book: Book) {
        val bookService = ApiServiceFactory.makeBooksService()

        try {
            val response = bookService.deleteBook(book.id)
            if (response.isSuccessful) {
                Log.d("DeleteBook", "Respuesta exitosa")
                withContext(Dispatchers.Main) {// asegurarse de que el código que actualiza la interfaz de usuario se ejecute en el hilo principal
                    Log.d("DeleteBook", "Actualizando la UI en el hilo principal")
                    // Usuario agregado con éxito
                    showAlert("Información", "Libro eliminado con éxito.")
                    // Actualizar la vista después de eliminar el libro
                    fetchBooks()
                }

            } else {
                // Fallo al agregar el lirbo, manejar error
                val errorResponse = response.errorBody()?.string()
                showAlert("Error","Error al eliminar el libro: $errorResponse")
            }
        }  catch (e: Exception) {
            // Manejar excepciones, como problemas de red o configuración
            showAlert("Error","Error al conectar con la API: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(activity?.applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBookClick(book: Book) {
        val options = arrayOf("Modificar", "Eliminar", "Cancelar")

        AlertDialog.Builder(requireContext())
            .setTitle("¿Qué desea hacer con el libro?")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Modificar
                        val intent = Intent(activity?.applicationContext, BookUpdateActivity::class.java)
                        intent.putExtra("id_book", book.id)
                        intent.putExtra("source", Constants.HOME_FRAGMENT)
                        startActivity(intent)
                    }
                    1 -> {
                        // Eliminar
                        showConfirmationDialog(book)
                    }
                    // No se hace nada si elige Cancelar, simplemente se cierra el diálogo
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Recargar los datos del recycledView al volver a este fragmento
        lifecycleScope.launch {
            fetchBooks()
        }
    }

    private fun showConfirmationDialog(book: Book) {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Está seguro que desea eliminar este libro?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    deleteBook(book)
                }

            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun showAlert(title: String, message: String) {
        if (activity != null && !requireActivity().isFinishing) { // Verificar si la actividad no está en proceso de finalización
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("Aceptar", null)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        } else {
            Log.d("ShowAlert", "Actividad no disponible para mostrar la alerta")
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


