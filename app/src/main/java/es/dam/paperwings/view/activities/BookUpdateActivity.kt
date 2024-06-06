package es.dam.paperwings.view.activities


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import es.dam.paperwings.R
import es.dam.paperwings.model.api.ApiServiceFactory
import es.dam.paperwings.model.api.UpdateRequest
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.model.repositories.RepositoryImpl
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter


/**
 * `BookUpdateActivity` allows users to update information of an existing book.
 * This activity extends `AppCompatActivity`.
 */
class BookUpdateActivity : AppCompatActivity() {

    private lateinit var tieTitleUpdate: TextInputEditText
    private lateinit var tieAuthorUpdate: TextInputEditText
    private lateinit var tiePublisherUpdate: TextInputEditText
    private lateinit var tieIsbnUpdate: TextInputEditText
    private lateinit var tiePagesUpdate: TextInputEditText
    private lateinit var tieDateUpdate: TextInputEditText
    private lateinit var tiePriceUpdate: TextInputEditText
    private lateinit var tieImageUpdate: TextInputEditText
    private lateinit var tieDescriptionUpdate: TextInputEditText
    private lateinit var actCategory: AutoCompleteTextView
    private lateinit var actLanguage: AutoCompleteTextView
    private lateinit var btnUpdateBook: Button
    private lateinit var ibCancel: ImageButton

    private var bookId: Int = -1
    private var uid: String? = null

    // Repository instance
    private val repository = RepositoryImpl()

    /**
     * Method called when creating the activity. Initializes the user interface
     * and loads data of the existing book by its ID.
     *
     * @param savedInstanceState Previously saved instance state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_update)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get id_book from Intent
        bookId = intent.getIntExtra("id_book", -1)


        // Get UID from sharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        uid = sharedPref.getString("uid", "N/A")

        // Initialize view elements
        tieTitleUpdate = findViewById(R.id.tieTitleUpdate)
        tieAuthorUpdate = findViewById(R.id.tieAuthorUpdate)
        tiePublisherUpdate = findViewById(R.id.tiePublisherUpdate)
        tieIsbnUpdate = findViewById(R.id.tieIsbnUpdate)
        tiePagesUpdate = findViewById(R.id.tiePagesUpdate)
        tieDateUpdate = findViewById(R.id.tieDateUpdate)
        tiePriceUpdate = findViewById(R.id.tiePriceUpdate)
        tieImageUpdate = findViewById(R.id.tieImageUpdate)
        tieDescriptionUpdate = findViewById(R.id.tieDescriptionUpdate)
        actCategory = findViewById(R.id.actCategoryUpdate)
        actLanguage = findViewById(R.id.actLanguageUpdate)
        btnUpdateBook = findViewById(R.id.btnUpdateBook)
        ibCancel = findViewById(R.id.ibCancel)


        // Load book data by ID
        lifecycleScope.launch{
            fetchBookById(bookId)
        }

        // Configure Cancel button action
        ibCancel.setOnClickListener {
            repository.switchToPrevious(onBackPressedDispatcher)
        }

        // Configure Update button action
        btnUpdateBook.setOnClickListener {
            lifecycleScope.launch{
                updateBook()
            }
        }

        // Define categories and languages
        val categories = arrayListOf(
            "Ficción", "No Ficción", "Misterio", "Biografía", "Ciencia",
            "Fantasía", "Historia", "Romance", "Terror", "Aventura",
            "Ciencia Ficción", "Autoayuda", "Poesía", "Infantil",
            "Juvenil", "Ensayo", "Drama", "Viajes",
            "Cómics y Novelas Gráficas", "Humor"
        )

        val languages = listOf(
            "Español", "Inglés", "Francés", "Alemán",
            "Italiano", "Portugués", "Ruso", "Chino",
            "Japonés", "Árabe", "Coreano", "Holandés",
            "Sueco", "Hindi", "Griego", "Danés",
            "Finlandés", "Noruego", "Polaco", "Turco"
        )

        // Create ArrayAdapter using categories and default list layout
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        val languageAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, languages)


        // Set ArrayAdapter to AutoCompleteTextView
        actCategory.setAdapter(categoryAdapter)
        actLanguage.setAdapter(languageAdapter)
        
    }

    /**
     * Method to fetch data of an existing book from the API.
     *
     * @param id ID of the book to fetch.
     */
    suspend fun fetchBookById(id: Int){
        val bookservice = ApiServiceFactory.makeBooksService()

        try {

            // Fetch book by its ID from API
            val response = bookservice.fetchBookById(id)
            if (response.isSuccessful && response.body() != null) {

                // Retrieve book as a list (due to Structure)
                val bookList = response.body()!!.data
                if (bookList != null && bookList.isNotEmpty()) {

                    // Get the first and only book from the list
                    val book = bookList.first()

                    // Display attributes in the UI
                    showBookAttributes(book)

                } else {
                    println("No se encontró el libro con id: $id")
                }
            } else {
                println("Error al obtener los datos del usuario: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            println("Error en la red o al parsear los datos: ${e.message}")
        }

    }

    /**
     * Method to update book information using user-entered data.
     */
    fun updateBook() {
        // Get values from text fields
        val title = tieTitleUpdate.text.toString()
        val category = actCategory.text.toString()
        val price = tiePriceUpdate.text.toString().toDoubleOrNull() ?: 0.0
        val pages = tiePagesUpdate.text.toString().toIntOrNull() ?: 0
        val language = actLanguage.text.toString().ifEmpty { actLanguage.hint.toString() }
        val author = tieAuthorUpdate.text.toString().ifEmpty { "Unknown" }
        val isbn = tieIsbnUpdate.text.toString().ifEmpty { "Unknown" }
        val publisher = tiePublisherUpdate.text.toString().ifEmpty { "Unknown" }
        val description = tieDescriptionUpdate.text.toString().ifEmpty { "No description" }
        val date = tieDateUpdate.text.toString().ifEmpty { "0000-00-00" }
        val discount = 0.0
        val stock = 0
        val image = if (tieImageUpdate.text.isNullOrEmpty()) {
            null
        } else {
            // Convert the image path or URL to ByteArray
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_book_cover)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }

        // Validate required fields
        if (title.isEmpty() || category.isEmpty() || price == 0.0|| pages == 0|| language.isEmpty()) {
            repository.showAlert(this@BookUpdateActivity,"Atención", "Debe rellenar todos los campos marcados con un asterisco*")
        } else if (author.isEmpty() || isbn.isEmpty() || publisher.isEmpty() || description.isEmpty() || date.isEmpty() || image == null ) {
            repository.showAlertOkCancel(this@BookUpdateActivity, "Atención", "¿Seguro que quiere dejar campos vacíos?", true) { confirmed ->
                if (confirmed) {
                    val book =
                        Book(author, category, description, discount, bookId,
                            image, isbn, language, pages, price, publisher, stock, title, date)

                    lifecycleScope.launch {
                        updateBookApi(book)
                    }
                }
            }
        } else {
            val book = image.let {
                Book(author, category, description, discount, bookId,
                    image, isbn, language, pages, price, publisher, stock, title, date)
            }
            lifecycleScope.launch {
                updateBookApi(book)
            }
        }
    }

    /**
     * Method to update book information via API.
     *
     * @param book Book object containing updated information.
     */
    suspend fun updateBookApi(book: Book){
        val bookService = ApiServiceFactory.makeBooksService()
        val id = bookId
        val idField = "id"

        // Create UpdateRequest instance
        val updateRequest = UpdateRequest(idField, id, book)
        try {
            val response = bookService.updateBook(updateRequest)
            if (response.isSuccessful) {
                // Successfully updated book
                repository.showAlert(this@BookUpdateActivity,"Información","Libro modificado con éxito.", true)
            } else {
                // Failed to update book, handle error
                val errorResponse = response.errorBody()?.string()
                repository.showAlert(this@BookUpdateActivity,"Error","Error al modificar el libro: $errorResponse")
            }
        }  catch (e: Exception) {
            // Handle exceptions, such as network problems or configuration
            repository.showAlert(this@BookUpdateActivity,"Error","Error al conectar con la API: ${e.message}")
        }
    }

    /**
     * Method to populate UI fields with attributes of a given book.
     *
     * @param book Book object containing attributes to display.
     */
    private fun showBookAttributes(book: Book) {
        // Title
        tieTitleUpdate.setText(book.title)

        // Author
        tieAuthorUpdate.setText(book.author ?: "")

        // Price
        tiePriceUpdate.setText(book.price.toString())

        // Pages
        tiePagesUpdate.setText(book.pages.toString())

        // Language
        actLanguage.setText(book.language ?: "")
        actLanguage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                actLanguage.setText("")
                actLanguage.setHint(book.language)
            }
        }

        // Publisher
        tiePublisherUpdate.setText(book.publisher ?: "")

        // Publication Date, which is of type LocalDate
        book.date.let { dateString ->
            val localDate = LocalDate.parse(dateString)
            val formattedDate = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            tieDateUpdate.setText(formattedDate)
        } ?: run {
            tieDateUpdate.setText("")
        }

        // ISBN
        tieIsbnUpdate.setText(book.isbn ?: "")

        // Category
        actCategory.setText(book.category ?: "")
        actCategory.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                actCategory.setText("")
                actCategory.setHint(book.category)
            }
        }

        // Synopsis
        tieDescriptionUpdate.setText(book.description ?: "")
    }

}