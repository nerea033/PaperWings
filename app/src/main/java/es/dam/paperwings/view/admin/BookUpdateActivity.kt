package es.dam.paperwings.view.admin


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import es.dam.paperwings.R
import es.dam.paperwings.model.api.ApiServiceFactory
import es.dam.paperwings.model.api.UpdateRequest
import es.dam.paperwings.model.entities.Book
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

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
    private lateinit var actCategoryUpdate: AutoCompleteTextView
    private lateinit var actLanguage: AutoCompleteTextView
    private lateinit var btnUpdateBook: Button
    private lateinit var ibCancel: ImageButton

    private var bookId: Int = -1
    private var uid: String? = null

    private var sourceFragment: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_update)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtengo el id_book de los fragment como Intent
        bookId = intent.getIntExtra("id_book", -1)

        // Obtengo el fragment del que procedo
        sourceFragment = intent.getStringExtra("source")

        // Obtengo el uid de sharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        uid = sharedPref.getString("uid", "N/A")

        // Elementos de la vista
        tieTitleUpdate = findViewById(R.id.tieTitleUpdate)
        tieAuthorUpdate = findViewById(R.id.tieAuthorUpdate)
        tiePublisherUpdate = findViewById(R.id.tiePublisherUpdate)
        tieIsbnUpdate = findViewById(R.id.tieIsbnUpdate)
        tiePagesUpdate = findViewById(R.id.tiePagesUpdate)
        tieDateUpdate = findViewById(R.id.tieDateUpdate)
        tiePriceUpdate = findViewById(R.id.tiePriceUpdate)
        tieImageUpdate = findViewById(R.id.tieImageUpdate)
        tieDescriptionUpdate = findViewById(R.id.tieDescriptionUpdate)
        actCategoryUpdate = findViewById(R.id.actCategoryUpdate)
        actLanguage = findViewById(R.id.actLanguageUpdate)
        btnUpdateBook = findViewById(R.id.btnUpdateBook)
        ibCancel = findViewById(R.id.ibCancel)


        // Obtengo los datos del libro por su id
        lifecycleScope.launch{
            fetchBookById(bookId)
        }

        ibCancel.setOnClickListener {
            switchPrevios()
        }

        btnUpdateBook.setOnClickListener {
            lifecycleScope.launch{
                updateBook()
            }
        }
        
    }

    /**
     * Obtengo los datos del libro a través de su id
     *
     */
    suspend fun fetchBookById(id: Int){
        val bookservice = ApiServiceFactory.makeBooksService()

        try {

            // Busco el libro mediante su id a través de la API
            val response = bookservice.fetchBookById(id)
            if (response.isSuccessful && response.body() != null) { // Si no hay error y no es null

                // Almaceno el libro en forma de lista (por la Structure)
                val bookList = response.body()!!.data
                if (bookList != null && bookList.isNotEmpty()) {

                    // Cojo el primer y único usuario de la lista
                    val book = bookList.first()

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

    }


    fun updateBook() {
        val title = tieTitleUpdate.text.toString()
        val category = actCategoryUpdate.text.toString()
        val price = tiePriceUpdate.text.toString().toDoubleOrNull() ?: 0.0
        val pages = tiePagesUpdate.text.toString().toIntOrNull() ?: 0
        val language = actLanguage.text.toString()
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
            // Convert the image path or URL to ByteArray, replace this with your actual logic
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_book_cover)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
        if (title.isEmpty() || category.isEmpty() || price == 0.0|| pages == 0|| language.isEmpty()) {
            showAlert("Atención", "Debe rellenar todos los campos marcados con un asterisco*")
        } else if (author.isEmpty() || isbn.isEmpty() || publisher.isEmpty() || description.isEmpty() || date.isEmpty() || image == null ) {
            showAlertOkCancel("Atención", "¿Seguro que quiere dejar campos vacíos?", true) { confirmed ->
                if (confirmed) {
                    val book =
                        Book(author, category, description, discount,
                            image, isbn, language, pages, price, publisher, stock, title, date)

                    if (book != null) {
                        lifecycleScope.launch {
                            updateBookApi(book)
                        }
                    }
                }
            }
        } else {
            val book = image?.let {
                Book(author, category, description, discount,
                    it, isbn, language, pages, price, publisher, stock, title, date)
            }
            lifecycleScope.launch {
                if (book != null) {
                    updateBookApi(book)
                }
            }
        }
    }

    suspend fun updateBookApi(book: Book){
        val bookService = ApiServiceFactory.makeBooksService()
        val id = bookId
        val idField = "id"

        // Create UpdateRequest instance
        val updateRequest = UpdateRequest(idField, id, listOf(book))
        try {
            val response = bookService.updateBook(updateRequest)
            if (response.isSuccessful) {
                // Usuario agregado con éxito
                showAlert("Información","Libro agregado con éxito.")

            } else {
                // Fallo al agregar el lirbo, manejar error
                val errorResponse = response.errorBody()?.string()
                showAlert("Error","Error al agregar el libro: $errorResponse")
            }
        }  catch (e: Exception) {
            // Manejar excepciones, como problemas de red o configuración
            showAlert("Error","Error al conectar con la API: ${e.message}")
        }
    }


        private fun showBookAttributes(book: Book) {
            // Título
            tieTitleUpdate.setText(book.title)

            // Autor
            tieAuthorUpdate.setText(book.author ?: "")

            // Precio
            tiePriceUpdate.setText(book.price.toString())

            // Páginas
            tiePagesUpdate.setText(book.pages.toString())

            // Idioma
            actLanguage.setText(book.language ?: "")

            // Editorial
            tiePublisherUpdate.setText(book.publisher ?: "")

            // Fecha de publicación es de tipo LocalDate
            // Fecha de publicación es de tipo LocalDate
            book.date?.let { dateString ->
                val localDate = LocalDate.parse(dateString)
                val formattedDate = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                tieDateUpdate.setText(formattedDate)
            } ?: run {
                tieDateUpdate.setText("")
            }

            // ISBN
            tieIsbnUpdate.setText(book.isbn ?: "")

            // Categoría
            actCategoryUpdate.setText(book.category)

            // Sinopsis
            tieDescriptionUpdate.setText(book.description ?: "")
        }



    private fun switchPrevios() {
        // Simula la acción de presionar el botón de retroceso utilizando onBackPressedDispatcher.
        onBackPressedDispatcher.onBackPressed()
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showAlertOkCancel(title: String, message: String, showCancel: Boolean = false, onResult: ((Boolean) -> Unit)? = null) {
        // Verificar si la actividad no está en proceso de finalización
        if (!isFinishing) {
            // Usar el contexto de la actividad para construir el AlertDialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("Aceptar") { dialog, which ->
                onResult?.invoke(true)
            }
            if (showCancel) {
                builder.setNegativeButton("Cancelar") { dialog, which ->
                    onResult?.invoke(false)
                }
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
    fun showAlert(title: String, message: String) {
        if (!isFinishing) { // Verificar si la actividad no está en proceso de finalización
            val builder = AlertDialog.Builder(this)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("Aceptar", null)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
}