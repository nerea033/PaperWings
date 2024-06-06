package es.dam.paperwings.view.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import es.dam.paperwings.R
import es.dam.paperwings.model.api.ApiServiceFactory
import es.dam.paperwings.model.api.UpdateCartRequest
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.model.entities.Cart
import es.dam.paperwings.model.repositories.RepositoryImpl
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale

/**
 * This activity displays the details of a book and allows the user to
 * add it to their shopping cart. This class extends `AppCompatActivity`.
 */
class BookDetailActivity : AppCompatActivity() {

    private var ivCover: ImageView? = null
    private var tvTitle: TextView? = null
    private var tvAuthor: TextView? = null
    private var tvPrice: TextView? = null
    private var tvPages: TextView? = null
    private var tvLanguage: TextView? = null
    private var tvPublisher: TextView? = null
    private var tvDate: TextView? = null
    private var tvIsbn: TextView? = null
    private var tvCategory: TextView? = null
    private var tvSinopsis: TextView? = null

    private var bookId: Int = -1
    private var uid: String? = null
    private var bookPrice: Double = 0.0

    // Repository instance
    private val repository = RepositoryImpl()

    /**
    * Method called when creating the activity. Initializes the user interface
    * and retrieves the necessary data to display the book details.
    *
    * @param savedInstanceState Previously saved instance state.
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the id_book from fragments as Intent
        bookId = intent.getIntExtra("id_book", -1)

        // Get UID from sharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        uid = sharedPref.getString("uid", "N/A")

        // Initialize UI elements
        val ibGoBack: ImageButton = findViewById(R.id.ibGoBack)
        val btnAddCart: Button = findViewById(R.id.btnAddCart)
        ivCover = findViewById(R.id.ivCoverDetail)
        tvTitle = findViewById(R.id.tvTitleDetail)
        tvAuthor = findViewById(R.id.tvAuthorDetail)
        tvPrice = findViewById(R.id.tvPriceDetail)
        tvPages = findViewById(R.id.tvPages)
        tvLanguage = findViewById(R.id.tvLanguage)
        tvPublisher = findViewById(R.id.tvPublisher)
        tvDate = findViewById(R.id.tvDate)
        tvIsbn = findViewById(R.id.tvIsbn)
        tvCategory = findViewById(R.id.tvCategory)
        tvSinopsis = findViewById(R.id.tvSinopsisDetail)


        // Fetch book data by its ID
        lifecycleScope.launch{
            bookPrice = fetchBookById(bookId)

            btnAddCart.setOnClickListener {
                lifecycleScope.launch {
                    handleCart(uid, bookId, bookPrice)
                }
            }
        }

        // When the back icon is pressed, return to the previous window
        ibGoBack.setOnClickListener {
            repository.switchToPrevious(onBackPressedDispatcher)
        }

    }

    /**
     * Fetches book data by its id.
     *
     * @param id ID of the book to fetch.
     * @return Price of the book.
     */
    suspend fun fetchBookById(id: Int): Double{
        val bookservice = ApiServiceFactory.makeBooksService()

        var price = 0.0
        try {

            // Fetch the book by its id via API
            val response = bookservice.fetchBookById(id)
            if (response.isSuccessful && response.body() != null) { // Si no hay error y no es null

                // Store the book as a list (due to the Structure)
                val bookList = response.body()!!.data
                if (bookList != null && bookList.isNotEmpty()) {

                    // Get the first and only user from the list
                    val book = bookList.first()

                    // Store all attributes to pass to the cart
                    price = book.price

                    // Display attributes in the interface
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
        return price

    }

    /**
     * Displays the book attributes in the user interface.
     *
     * @param book `Book` object whose attributes will be shown.
     */
    fun showBookAttributes (book: Book){
        // Image!!!!!!!!!!!!!!!!!!!!!!!!
        // Handle case where image is null
        ivCover?.setImageResource(R.drawable.ic_book_cover2)

        // Title
        tvTitle?.text = book.title

        // Author
        book.author.let {
            tvAuthor?.text = it
        } ?: run {
            // Handle case where author is null
            tvAuthor?.text = "Autor no disponible"
        }

        // Price
        tvPrice?.text = book.price.toString()  + " €"

        // Pages
        tvPages?.text = book.pages.toString()

        // Language
        book.language.let {
            tvLanguage?.text = it
        } ?: run {
            // Manejar el caso donde el idioma es nulo
            tvLanguage?.text = "No disponible"
        }

        // Publisher
        book.publisher.let {
            tvPublisher?.text = it
        } ?: run {
            // Handle case where publisher is null
            tvPublisher?.text = "No disponible"
        }

        // Publication date is of type LocalDate
        book.date.let { dateString ->
            val localDate = book.getLocalDate()
            if (localDate != null) {
                val day = localDate.dayOfMonth
                val month = localDate.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
                val year = localDate.year
                val formattedDate = "$day de $month de $year"
                tvDate?.text = formattedDate
            } else {
                tvDate?.text = "No disponible"
            }
        } ?: run {
            tvDate?.text = "Fecha no disponible"
        }

        // ISBN
        book.isbn.let {
            tvIsbn?.text = it
        } ?: run {
            // Handle case where ISBN is null
            tvIsbn?.text = "No disponible"
        }

        // Category
        tvCategory?.text = book.category

        // Synopsis
        book.description.let {
            tvSinopsis?.text = it
        } ?: run {
            // Handle case where synopsis is null
            tvSinopsis?.text = "Sinopsis no disponible"
        }
    }

    /**
     * Handles the logic of adding or updating the cart.
     *
     * @param uid User ID.
     * @param bookId Book ID.
     * @param price Book price.
     */
    suspend fun handleCart(uid: String?, bookId: Int, price: Double) {
        if (uid != null && bookId != -1 && price != 0.0) {
            // Check if the book is already in the user's cart
            val existingCartItem = checkIfBookInCart(uid, bookId)

            if (existingCartItem != null) {
                // If the book is already in the cart, update the quantity
                updateCartRegister(uid, bookId, existingCartItem.quantity)
            } else {
                // If the book is not in the cart, add it
                addCartToDatabase(uid, bookId, price)
            }
        } else {
            println("Información insuficiente para manejar el carrito")
        }
    }

    /**
     * Adds a book to the user's cart for the first time.
     *
     * @param uid User ID.
     * @param id_book Book ID.
     * @param price Book price.
     */
    suspend fun addCartToDatabase(uid: String?, id_book: Int, price: Double) {
        if (uid != null && id_book != -1 && price != 0.0) {
            val cartService = ApiServiceFactory.makeCartService()
            val quantity = 1
            val cart = Cart(uid, id_book, price, quantity)

            try {
                val response = cartService.addCart(cart)  // Make API call
                if (response.isSuccessful) {
                    // Successfully added record
                    println("Registro agregado con éxito al carrito")
                    repository.showToast(this,"Libro agregado al carrito")
                } else {
                    // Failed to add record, handle error
                    val errorResponse = response.errorBody()?.string()
                    println("Error al agregar el registro al carrito: $errorResponse")
                }
            } catch (e: Exception) {
                // Handle exceptions, such as network problems or configuration
                println("Error al conectar con la API: ${e.message}")
            }
        } else {
            println("Información insuficiente para añadir al carrito")
        }
    }

    /**
     * Updates the quantity of a book in the cart if it already exists for that user.
     *
     * @param uid user UID.
     * @param id_book book ID.
     * @param quantity Quantity of the book in the cart.
     */
    suspend fun updateCartRegister(uid: String?, id_book: Int, quantity: Int){
        if (uid != null && id_book != -1 && quantity != -1) {
            val cartService = ApiServiceFactory.makeCartService()
            val updatedQuantity = quantity + 1

            val updateCartRequest = UpdateCartRequest(uid, id_book, updatedQuantity)
            try {
                val response = cartService.updateCart(updateCartRequest)  // Hacer la llamada API
                if (response.isSuccessful) {
                    // Successfully updated record in the cart
                    println("Registro actualizado con éxito en el carrito")
                    repository.showToast(this,"Libro agregado al carrito")
                } else {
                    // Failed to update record, handle error
                    val errorResponse = response.errorBody()?.string()
                    println("Error al actualizar el registro del carrito: $errorResponse")
                }
            } catch (e: Exception) {
                // Handle exceptions, such as network problems or configuration
                println("Error al conectar con la API: ${e.message}")
            }
        } else {
            println("Información insuficiente para actualizar el registro del carrito")
        }
    }

    /**
     * Checks if the current user has the specified book in the cart.
     *
     * @param uid User ID.
     * @param bookId Book ID.
     * @return `Cart` object if the book is in the cart, `null` otherwise.
     */
    suspend fun checkIfBookInCart(uid: String, bookId: Int): Cart? {
        val cartService = ApiServiceFactory.makeCartService()
        return try {
            val response = cartService.fetchUserCartRecords(uid)
            if (response.isSuccessful && response.body() != null) {
                val userBooks = response.body()?.data ?: emptyList()
                userBooks.find { it.idBook == bookId }
            } else {
                println("Error al obtener los libros: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            println("Error en la red o al parsear los datos: ${e.message}")
            null
        }
    }

}