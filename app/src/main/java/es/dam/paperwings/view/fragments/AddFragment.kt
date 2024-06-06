package es.dam.paperwings.view.fragments

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import es.dam.paperwings.R
import es.dam.paperwings.model.api.ApiServiceFactory
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.model.repositories.RepositoryImpl
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * A Fragment for adding a new book entry.
 */
class AddFragment : Fragment() {

    // Views
    private lateinit var tieTitleAdd: TextInputEditText
    private lateinit var tieAuthorAdd: TextInputEditText
    private lateinit var tiePublisherAdd: TextInputEditText
    private lateinit var tieIsbnAdd: TextInputEditText
    private lateinit var tiePagesAdd: TextInputEditText
    private lateinit var tieDateAdd: TextInputEditText
    private lateinit var tiePriceAdd: TextInputEditText
    private lateinit var tieImageAdd: TextInputEditText
    private lateinit var tieDescriptionAdd: TextInputEditText
    private lateinit var actCategoryAdd: AutoCompleteTextView
    private lateinit var actLanguage: AutoCompleteTextView
    private lateinit var btnAddBook: Button

    // Repository instance
    private val repository = RepositoryImpl()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add, container, false)

        // Initialize views
        tieTitleAdd = view.findViewById(R.id.tieTitleAdd)
        tieAuthorAdd = view.findViewById(R.id.tieAuthorAdd)
        tiePublisherAdd = view.findViewById(R.id.tiePublisherAdd)
        tieIsbnAdd = view.findViewById(R.id.tieIsbnAdd)
        tiePagesAdd = view.findViewById(R.id.tiePagesAdd)
        tieDateAdd = view.findViewById(R.id.tieDateAdd)
        tiePriceAdd = view.findViewById(R.id.tiePriceAdd)
        tieImageAdd = view.findViewById(R.id.tieImageAdd)
        tieDescriptionAdd = view.findViewById(R.id.tieDescriptionAdd)
        actCategoryAdd = view.findViewById(R.id.actCategoryAdd)
        actLanguage = view.findViewById(R.id.actLanguageAdd)
        btnAddBook = view.findViewById(R.id.btnAddBook)

        // Button click listener
        btnAddBook.setOnClickListener {
            lifecycleScope.launch {
                addBook()
            }
        }

        // Set OnClickListener to show DatePickerDialog
        tieDateAdd.setOnClickListener {
            showDatePickerDialog()
        }

        // Prevent keyboard from appearing
        tieDateAdd.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDatePickerDialog()
            }
        }

        // Setup categories and languages
        val categories = listOf(
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

        // ArrayAdapter for AutoCompleteTextViews
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        val languageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languages)

        // Set adapters
        actCategoryAdd.setAdapter(adapter)
        actLanguage.setAdapter(languageAdapter)

        return view
    }

    /**
     * Adds a book entry to the database.
     */
    fun addBook() {
        val title = tieTitleAdd.text.toString()
        val category = actCategoryAdd.text.toString()
        val price = tiePriceAdd.text.toString().toDoubleOrNull() ?: 0.0
        val pages = tiePagesAdd.text.toString().toIntOrNull() ?: 0
        val language = actLanguage.text.toString()
        val author = tieAuthorAdd.text.toString().ifEmpty { "Unknown" }
        val isbn = tieIsbnAdd.text.toString().ifEmpty { "Unknown" }
        val publisher = tiePublisherAdd.text.toString().ifEmpty { "Unknown" }
        val description = tieDescriptionAdd.text.toString().ifEmpty { "No description" }
        val date = tieDateAdd.text.toString().ifEmpty { "0000-00-00" }
        val discount = 0.0
        val stock = 0
        val image = if (tieImageAdd.text.isNullOrEmpty()) {
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
            repository.showAlert(this,"Atención", "Debe rellenar todos los campos marcados con un asterisco*")
        } else if (author.isEmpty() || isbn.isEmpty() || publisher.isEmpty() || description.isEmpty() || date.isEmpty() || image == null ) {
            repository.showAlertOkCancel(this, "Atención", "¿Seguro que quiere dejar campos vacíos?", true) { confirmed ->
                if (confirmed) {
                    val book =
                        Book(author, category, description, discount,
                            image, isbn, language, pages, price, publisher, stock, title, date)

                    if (book != null) {
                        lifecycleScope.launch {
                            addBookApi(book)
                            clearFields()
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
                    addBookApi(book)
                    clearFields()
                }
            }
        }
    }

    /**
     * Calls the API to add a book.
     *
     * @param book The book object to add.
     */
    suspend fun addBookApi(book: Book){
        val bookService = ApiServiceFactory.makeBooksService()

        try {
            val response = bookService.addBook(book)
            if (response.isSuccessful) {
                // Book added successfully
                repository.showAlert(this,"Información","Libro agregado con éxito.")

            } else {
                // Failed to add book, handle error
                val errorResponse = response.errorBody()?.string()
                repository.showAlert(this,"Error","Error al agregar el libro: $errorResponse")
            }
        }  catch (e: Exception) {
            // Handle exceptions, such as network issues or configuration problems
            repository.showAlert(this,"Error","Error al conectar con la API: ${e.message}")
        }
    }

    /**
     * Clears all input fields.
     */
    private fun clearFields() {
        tieTitleAdd.text?.clear()
        actCategoryAdd.setHint("Categoría*")
        actCategoryAdd.setText("")
        tiePriceAdd.text?.clear()
        tiePagesAdd.text?.clear()
        actLanguage.setHint("Idioma*")
        actLanguage.setText("")
        tieAuthorAdd.text?.clear()
        tieIsbnAdd.text?.clear()
        tiePublisherAdd.text?.clear()
        tieDescriptionAdd.text?.clear()
        tieDateAdd.text?.clear()
        tieImageAdd.text?.clear()
    }

    /**
     * Shows a DatePickerDialog to select a date.
     */
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Month is 0 based, so add 1 to display correct month
                val selectedDate = formatDate(year, month + 1, dayOfMonth)
                tieDateAdd.setText(selectedDate)
            },
            year,
            month,
            dayOfMonth
        )

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

    /**
     * Formats the date into a string with the format "yyyy-MM-dd".
     *
     * @param year The year.
     * @param month The month (1-12).
     * @param day The day of the month.
     * @return The formatted date string.
     */
    private fun formatDate(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day) // Month is 0 based
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

}






