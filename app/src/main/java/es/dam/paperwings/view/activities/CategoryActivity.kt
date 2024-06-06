package es.dam.paperwings.view.activities

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
import es.dam.paperwings.view.adapters.CardAdapter
import kotlinx.coroutines.launch

/**
 * Activity class to display books of the selected category.
 * Implements BookClickListener to handle clicks on books.
 */
class CategoryActivity : AppCompatActivity(), BookClickListener {

    // Binding object for this activity's layout
    private var _binding: ActivityCategoryBinding? = null
    private val binding get() = _binding!!


    // LiveData to hold the list of books
    private val _booksLiveData = MutableLiveData<List<Book>>()
    val booksLiveData: LiveData<List<Book>> get() = _booksLiveData

    // Adapter for the RecyclerView
    private lateinit var cardAdapter: CardAdapter

    private var category: String? = null
    private var sourceFragment: String? = null

    /**
     * Called when the activity is starting. Sets up UI components,
     * retrieves intent extras, initializes RecyclerView, and starts
     * loading books.
     *
     * @param savedInstanceState Bundle containing saved state information.
     */
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

        // Retrieve information passed from CategoryFragment
        category = intent.getStringExtra("category")
        sourceFragment = intent.getStringExtra("source")

        // Set up the RecyclerView with a GridLayoutManager and the CardAdapter.
        cardAdapter = CardAdapter(emptyList(), this)

        binding.rvCategoryBookUser.apply {
            layoutManager = GridLayoutManager(this@CategoryActivity, 2)
            adapter = this@CategoryActivity.cardAdapter
        }

        // Observe the books LiveData and update the adapter when the data changes
        booksLiveData.observe(this) { books ->
            cardAdapter.updateBooks(books)
        }

        // Start loading books
        lifecycleScope.launch {
            fetchBooks()
        }
    }

    /**
     * Fetches the list of books from the API based on the selected category.
     * Filters the retrieved books by the selected category and updates the
     * books LiveData accordingly.
     */
    suspend fun fetchBooks(){
        val bookService = ApiServiceFactory.makeBooksService()
        try {
            // Get the books
            val response = bookService.listBooks()
            if (response.isSuccessful && response.body() != null) { // Si no hay error y no es null

                // Store the books in the LiveData
                val booksList = response.body()!!.data
                if (booksList != null && booksList.isNotEmpty()) {

                    // Filter books by the selected category
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

    /**
     * Handles click events on the books displayed in the RecyclerView.
     * Opens the BookDetailActivity with details of the clicked book.
     *
     * @param book Book object that was clicked.
     */
    override fun onBookClick(book: Book) {
        val intent = Intent(this, BookDetailActivity::class.java)
        intent.putExtra("id_book", book.id)
        intent.putExtra("source", "category") // Add extra to indicate source as "category"
        startActivity(intent)
    }
}