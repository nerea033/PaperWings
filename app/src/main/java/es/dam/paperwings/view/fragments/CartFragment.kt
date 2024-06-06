package es.dam.paperwings.view.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import es.dam.paperwings.databinding.FragmentCartBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.CartClickListener
import es.dam.paperwings.model.api.ApiServiceFactory
import es.dam.paperwings.model.api.UpdateCartRequest
import es.dam.paperwings.model.constans.Constants
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.model.entities.CartData
import es.dam.paperwings.model.repositories.RepositoryImpl
import es.dam.paperwings.view.activities.BookDetailActivity
import es.dam.paperwings.view.adapters.CardAdapterCart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A Fragment that displays the user's shopping cart, allowing them to view added books,
 * manage quantities, remove books from the cart, and make purchases for all books in the cart.
 */
class CartFragment : Fragment(), BookClickListener, CartClickListener {

    // This property holds the binding object that provides access to the views in the fragment_home.xml layout.
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    // LiveData to hold the list of record in CART
    private val _cartLiveData = MutableLiveData<CartData>()
    val cartLiveData: LiveData<CartData> get() = _cartLiveData

    private lateinit var cardAdapterCart: CardAdapterCart

    private var uid: String? = null

    // Interface elements
    var tvFinalPrice: TextView? = null

    // Repository instance
    private val repository = RepositoryImpl()

    /**
     * Called when creating the view of the fragment.
     * Sets up views, initializes adapters, and sets listeners for buttons.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment and initialize the binding object.
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val view = binding.root

        // Get user's uid from SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        uid = sharedPref.getString("uid", "N/A")

        // Initialize tvFinalPrice using binding
        tvFinalPrice = binding.tvFinalPrice
        val ibDeleteAllCart: ImageButton =  binding.ibDeleteAllCart
        val btnBuyAll: Button =  binding.btnBuyAll

        // Configure RecyclerView with GridLayoutManager and CardAdapterCart adapter
        cardAdapterCart = CardAdapterCart(emptyList(), emptyList(), this, this)

        binding.rvCart.apply {
            layoutManager = GridLayoutManager(activity?.applicationContext, 1)
            adapter = cardAdapterCart
        }

        // Observe the books LiveData and update the adapter when the data changes
        cartLiveData.observe(viewLifecycleOwner) { cartData ->
            cartData?.let {
                cardAdapterCart.updateBooks(it.books, it.quantities)
                calculateFinalPrice(
                    it.books,
                    it.quantities
                )  // Calculate final price when data changes
            }
        }

        // Initial load of books in the cart
        lifecycleScope.launch {
            getCartBooks()
        }

        // Listener for clear cart button
        ibDeleteAllCart.setOnClickListener{
            lifecycleScope.launch {
                // Vaciar carrito del ususario
                uid?.let { it1 -> showConfirmationDialog(it1,"¿Está seguro que desea vaciar el carrito?","Vaciar") }
            }

        }

        // Listener for buy all button
        btnBuyAll.setOnClickListener {
            lifecycleScope.launch {
                uid?.let { it1 -> deleteAllCart(it1) }
                Toast.makeText(context, "Compra ralizada con éxito", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    /**
     * Fetches the records of books and quantities in the user's cart from the API.
     * @return Pair with the list of book IDs and the list of quantities.
     */
    suspend fun fetchCartRecords(uid: String): Pair<List<Int>, List<Int>>  {
        val cartService = ApiServiceFactory.makeCartService()
        val bookIds = mutableListOf<Int>()
        val quantities = mutableListOf<Int>()

        try {
            // Obtener los registros del carrito del servicio API
            val response = cartService.fetchUserCartRecords(uid)

            if (response.isSuccessful && response.body() != null) {
                val cartList = response.body()!!.data

                // Verificar si hay registros en el carrito
                if (cartList != null && cartList.isNotEmpty()) {
                    // Iterar sobre cada registro en el carrito y almacenar id_book y cantidad
                    for (cart in cartList) {
                        bookIds.add(cart.idBook)
                        quantities.add(cart.quantity)
                    }
                } else {
                    println("El carrito está vacío para el usuario con uid: $uid")
                }
            } else {
                println("Error al obtener los registros del carrito: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            println("Error en la red o al parsear los datos: ${e.message}")
        }

        return Pair(bookIds, quantities)
    }

    /**
     * Fetches details of a specific book by its ID from the API.
     * @return Book object if found, null if not found.
     */
    suspend fun fetchBookById(id: Int): Book? {
        val bookService = ApiServiceFactory.makeBooksService()

        return try{
            // Obtener detalles del libro por su ID desde el servicio API
            val response = bookService.fetchBookById(id)

            if (response.isSuccessful && response.body() != null) {
                val bookList = response.body()!!.data

                // Verificar si se encontró el libro con el ID dado
                if (bookList != null && bookList.isNotEmpty()) {
                    bookList[0] // Devuelve el primer libro encontrado
                } else {
                    println("No se encontró el libro con id: $id")
                    null
                }
            } else {
                println("Error al obtener los datos del libro: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            println("Error en la red o al parsear los datos: ${e.message}")
            null
        }

    }

    /**
     * Deletes all records from the cart for the current user.
     */
    suspend fun deleteAllCart(uid: String) {
        val cartService = ApiServiceFactory.makeCartService()
        getCartBooks()

        try {
            val response = cartService.deleteAllCart(uid)
            if (response.isSuccessful) {
                Log.d("DeleteAllCart", "Respuesta exitosa")
                withContext(Dispatchers.Main) {// asegurarse de que el código que actualiza la interfaz de usuario se ejecute en el hilo principal
                    Log.d("DeleteAllCart", "Actualizando la UI en el hilo principal")
                    // Usuario agregado con éxito
                    Log.i("Información", "Carrito vaciado con éxito.");
                    // Actualizar la vista después de vaciar el carrito
                    getCartBooks()
                }

            } else {
                // Fallo al agregar el lirbo, manejar error
                val errorResponse = response.errorBody()?.string()
                repository.showAlert(this,"Error","Error al vaciar el carrito: $errorResponse")
            }
        }  catch (e: Exception) {
            // Manejar excepciones, como problemas de red o configuración
            repository.showAlert(this, "Error","Error al conectar con la API: ${e.message}")
        }
    }

    /**
     * Retrieves books and quantities in the user's cart and stores them in LiveData.
     */
    suspend fun getCartBooks() {
        val results = uid?.let { fetchCartRecords(it) }
        val bookIds = results?.first ?: emptyList()
        val quantities = results?.second ?: emptyList()

        val books = mutableListOf<Book>()
        for (id in bookIds) {
            val book = fetchBookById(id)
            book?.let { books.add(it) }
        }

        if (books.size == quantities.size) {
            _cartLiveData.postValue(CartData(books, quantities))
        } else {
            println("Error: La cantidad de libros no coincide con la cantidad de registros en el carrito.")
        }
    }

    /**
     * Retrieves books and quantities in the user's cart and stores them in LiveData.
     */
    override fun onResume() {
        super.onResume()
        // Recargar los datos del carrito al volver a este fragmento
        lifecycleScope.launch {
            getCartBooks()
        }
    }

    /**
     * Increases the quantity of a book in the user's cart.
     */
    suspend fun addCartQuantity(uid: String?, id_book: Int, quantity: Int) {
        if (uid != null && id_book != -1 && quantity >= 1) {
            val cartService = ApiServiceFactory.makeCartService()
            val updatedQuantity = quantity + 1
            val updateCartRequest = UpdateCartRequest(uid, id_book, updatedQuantity)

            try {
                val response = cartService.updateCart(updateCartRequest)  // Hacer la llamada API
                if (response.isSuccessful) {
                    // Registro agregado con éxito
                    println("Registro actualizado con éxito en el carrito")
                    println("Libro agregado al carrito")
                } else {
                    // Fallo al actualizar el registro, manejar error
                    val errorResponse = response.errorBody()?.string()
                    println("Error al actualizar el registro del carrito: $errorResponse")
                }
            } catch (e: Exception) {
                // Manejar excepciones, como problemas de red o configuración
                println("Error al conectar con la API: ${e.message}")
            }
        } else {
            println("Información insuficiente para actualizar el registro del carrito")
        }
    }

    /**
     * Decreases the quantity of a book in the user's cart.
     */
    suspend fun subtractCartQuantity(uid: String?, id_book: Int, quantity: Int) {
        if (uid != null && id_book != -1 && quantity >= 1) {
            val cartService = ApiServiceFactory.makeCartService()

            if (quantity > 1) {
                val updatedQuantity = quantity - 1
                val updateCartRequest = UpdateCartRequest(uid, id_book, updatedQuantity)

                try {
                    val response = cartService.updateCart(updateCartRequest)  // Make the API call
                    if (response.isSuccessful) {
                        // Successfully updated the record
                        println("Registro del carito actualizado con éxito")
                        println("Cantidad de libros reducida")
                    } else {
                        // Failed to update the record, handle error
                        val errorResponse = response.errorBody()?.string()
                        println("Error actualizando registos: $errorResponse")
                    }
                } catch (e: Exception) {
                    // Handle exceptions, like network or configuration issues
                    println("Error connectando con la API: ${e.message}")
                }
            } else {
                repository.showAlertOkCancel(this, "Atención", "Está a punto de eliminar el libro del carrito, ¿está seguro que desea continuar?"){
                    // Call the function to delete the cart record if que user accept
                    viewLifecycleOwner.lifecycleScope.launch {
                        deleteCartRegister(uid, id_book)
                    }
                }

            }
        } else {
            println("Información insuficiente para realizar la actualización del registro del carrito")
        }
    }

    /**
     * Deletes a specific record from the user's cart.
     */
    suspend fun deleteCartRegister(uid: String?, id_book: Int) {
        if (uid != null && id_book != -1) {
            val cartService = ApiServiceFactory.makeCartService()

            try {
                val response = cartService.deleteCart(uid, id_book)  // Make the API call
                if (response.isSuccessful) {
                    // Successfully deleted the record
                    println("Registro del Carrito eliminado con éxito")
                    activity?.let { repository.showToast(it.applicationContext,"Libro eliminado del carrito") }
                    getCartBooks()
                } else {
                    // Failed to delete the record, handle error
                    val errorResponse = response.errorBody()?.string()
                    println("Error al eliminar el registro: $errorResponse")
                }
            } catch (e: Exception) {
                // Handle exceptions, like network or configuration issues
                println("Error connectando con la API: ${e.message}")
            }
        } else {
            println("Información insuficiente para realizar eliminar el registro del carrito")
        }
    }

    /**
     * Calculates the final price by summing the price of all books in the cart.
     */
    private fun calculateFinalPrice(books: List<Book>, quantities: List<Int>) {
        var finalPrice: Double = 0.0
        for ((index, book) in books.withIndex()) {
            finalPrice += book.price * quantities[index]
        }
        tvFinalPrice?.text = finalPrice.toString()
    }

    /**
     * Click listener for a book in the cart to navigate to the book details.
     */
    override fun onBookClick(book: Book) {
        val intent = Intent(activity?.applicationContext, BookDetailActivity::class.java)
        intent.putExtra("id_book", book.id)
        intent.putExtra("source", Constants.CART_FRAGMENT) // Añado el extra para indicar que estoy en "home"
        startActivity(intent)
    }

    /**
     * Click listener for add quantity button in a card of a book in the cart.
     */
    override fun onAddClick(idBook: Int, quantity: Int) {
        lifecycleScope.launch {
            addCartQuantity(uid, idBook, quantity )
            // Actualizar la vista después de agregar cantidad
            getCartBooks()
        }
    }

    /**
     * Click listener for subtract quantity button in a card of a book in the cart.
     */
    override fun onSubstractClick(idBook: Int, quantity: Int) {
        lifecycleScope.launch {
            subtractCartQuantity(uid, idBook, quantity )
            // Actualizar la vista después de agregar cantidad
            getCartBooks()
        }
    }

    /**
     * Shows a confirmation dialog for performing an action with the cart.
     */
    private fun showConfirmationDialog(uid: String, title: String, positiveButton: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setPositiveButton(positiveButton) { _, _ ->
                lifecycleScope.launch {
                     deleteAllCart(uid)
                }

            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Cleans up _binding when the fragment's view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}