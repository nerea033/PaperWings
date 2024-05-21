package es.dam.paperwings.view

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import es.dam.paperwings.R
import es.dam.paperwings.model.api.ApiServiceFactory
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.model.entities.Cart
import es.dam.paperwings.view.fragments.HomeFragment
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtengo el id_book de HomeFragment como Intent
        bookId = intent.getIntExtra("id_book", -1)

        // Obtengo el uid de sharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        uid = sharedPref.getString("uid", "N/A")

        // Inicializo los elementos de la interfaz
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


        // Obtengo los datos del libro por su id
        lifecycleScope.launch{
            bookPrice = fetchBookById(bookId)

            btnAddCart.setOnClickListener {
                lifecycleScope.launch {
                    addCartToDatabase(uid, bookId, bookPrice)
                }
            }
        }

        ibGoBack.setOnClickListener {
            switchToHome()
        }



    }

    /**
     * Obtengo los datos del libro a través de su id
     *
     */
    suspend fun fetchBookById(id: Int): Double{
        val bookservice = ApiServiceFactory.makeBooksService()

        var price: Double = 0.0
        try {

            // Busco el libro mediante su id a través de la API
            val response = bookservice.fetchBookById(id)
            if (response.isSuccessful && response.body() != null) { // Si no hay error y no es null

                // Almaceno el libro en forma de lista (por la Structure)
                val bookList = response.body()!!.data
                if (bookList != null && bookList.isNotEmpty()) {

                    // Cojo el primer y único usuario de la lista
                    val book = bookList.first()

                    // Guardo todos los atributos que le voy a pasar al carrito
                    val id_book = book.id
                    price = book.price

                    // Mostrar los atributos en la interfaz
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

    fun showBookAttributes (book: Book){
        // Imagen!!!!!!!!!!!!!!!!!!!!!!!!
        // Manejar el caso donde la imagen es nula
        ivCover?.setImageResource(R.drawable.ic_book_cover2)


        // Título
        tvTitle?.text = book.title


        // Autor
        book.author?.let {
            tvAuthor?.text = it
        } ?: run {
            // Manejar el caso donde el autor es nulo
            tvAuthor?.text = "Autor no disponible"
        }

        // Precio
        tvPrice?.text = book.price.toString()  + " €"

        // Páginas

        tvPages?.text = book.pages.toString()


        // Idioma
        book.language?.let {
            tvLanguage?.text = it
        } ?: run {
            // Manejar el caso donde el idioma es nulo
            tvLanguage?.text = "No disponible"
        }

        // Editorial
        book.publisher?.let {
            tvPublisher?.text = it
        } ?: run {
            // Manejar el caso donde la editorial es nula
            tvPublisher?.text = "No disponible"
        }

        // Fecha de publicación es de tipo LocalDate
        book.date?.let { dateString ->
            val localDate = book.getLocalDate()
            if (localDate != null) {
                val day = localDate.dayOfMonth
                val month = localDate.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
                val year = localDate.year
                val formattedDate = "$day de $month de $year"
                tvDate?.text = formattedDate
            } else {
                tvDate?.text = "Fecha no disponible"
            }
        } ?: run {
            tvDate?.text = "Fecha no disponible"
        }

        // ISBN
        book.isbn?.let {
            tvIsbn?.text = it
        } ?: run {
            // Manejar el caso donde el ISBN es nulo
            tvIsbn?.text = "No disponible"
        }


        // Categoría
        tvCategory?.text = book.category


        // Sinopsis
        book.description?.let {
            tvSinopsis?.text = it
        } ?: run {
            // Manejar el caso donde la sinopsis es nula
            tvSinopsis?.text = "Sinopsis no disponible"
        }
    }

    suspend fun addCartToDatabase(uid: String?, id_book: Int, price: Double) {
        if (uid != null && id_book != -1 && price != 0.0) {
            val cartService = ApiServiceFactory.makeCartsService()
            val id = uid
            val quantity = 0
            val cart = Cart(id, uid, id_book, price, quantity)

            try {
                val response = cartService.addCart(cart)  // Hacer la llamada API
                if (response.isSuccessful) {
                    // Registro agregado con éxito
                    println("Registro agregado con éxito al carrito")
                } else {
                    // Fallo al agregar el usuario, manejar error
                    val errorResponse = response.errorBody()?.string()
                    println("Error al agregar el registro al carrito: $errorResponse")
                }
            } catch (e: Exception) {
                // Manejar excepciones, como problemas de red o configuración
                println("Error al conectar con la API: ${e.message}")
            }
        } else {
            println("Información insuficiente para añadir al carrito")
        }
    }


    private fun switchToHome() {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("FRAGMENT_TO_LOAD", "home") // Cambiarlo dependiendo de la página de la que procedo
        }
        // Comenzar la actividad.
        startActivity(homeIntent)
        // Finalizar la actividad actual.
        finish() // Para evitar que el usuario regrese a la actividad anterior después de pulsar el botón de retroceso.
    }
}