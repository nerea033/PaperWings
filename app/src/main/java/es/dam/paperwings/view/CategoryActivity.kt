package es.dam.paperwings.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import es.dam.paperwings.R
import es.dam.paperwings.databinding.ActivityCategoryBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.api.ApiServiceFactory
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.view.recicledView.CardAdapterCategoryTwo
import kotlinx.coroutines.launch

/**
 * Clase para mostrar los libros de la categoría seleccionada
 */
class CategoryActivity : AppCompatActivity(), BookClickListener {

    // This property holds the binding object that provides access to the views in the fragment_home.xml layout.
    private var _binding: ActivityCategoryBinding? = null
    private val binding get() = _binding!!


    // LiveData to hold the list of books
    private val _booksLiveData = MutableLiveData<List<Book>>()
    val booksLiveData: LiveData<List<Book>> get() = _booksLiveData


    private lateinit var cardAdapterCategoryTwo: CardAdapterCategoryTwo

    private var category: String? = null
    private var sourceFragment: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCategoryBinding.inflate((layoutInflater))
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtengo la información pasada desde CategoryFragment
        category = intent.getStringExtra("category")
        sourceFragment = intent.getStringExtra("source")

        // Set up the RecyclerView with a GridLayoutManager and the CardAdapter.
        cardAdapterCategoryTwo = CardAdapterCategoryTwo(emptyList(), this)

        binding.rvCategoryBookUser.apply {
            layoutManager = GridLayoutManager(this@CategoryActivity, 2)
            adapter = this@CategoryActivity.cardAdapterCategoryTwo
        }

        // Observe the books LiveData and update the adapter when the data changes
        booksLiveData.observe(this, { books ->
            cardAdapterCategoryTwo.updateBooks(books)
        })

        // Start loading books
        lifecycleScope.launch {
            fetchBooks()
        }
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

                    // Filtra los libros por la categoría seleccionada
                    val filteredBooks = booksList.filter { it.category == category }
                    _booksLiveData.postValue(filteredBooks)

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

    override fun onBookClick(book: Book) {
        val intent = Intent(this, BookDetailActivity::class.java)
        intent.putExtra("id_book", book.id)
        intent.putExtra("source", "category") // Añado el extra para indicar que estoy en "home"
        startActivity(intent)
    }
}