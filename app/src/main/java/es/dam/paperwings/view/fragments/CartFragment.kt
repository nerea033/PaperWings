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


class CartFragment : Fragment(), BookClickListener, CartClickListener {

    // This property holds the binding object that provides access to the views in the fragment_home.xml layout.
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    // LiveData to hold the list of record in CART
    private val _cartLiveData = MutableLiveData<CartData>()
    val cartLiveData: LiveData<CartData> get() = _cartLiveData


    private lateinit var cardAdapterCart: CardAdapterCart

    // Presionar hacia atrás
    private var backPressedOnce = false
    private val backPressHandler = Handler(Looper.getMainLooper())

    private var uid: String? = null

    // Elementos de la interfaz
    var tvFinalPrice: TextView? = null

    // Instancia del repositorio
    private val repository = RepositoryImpl()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment and initialize the binding object.
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val view = binding.root

        // Obtengo el uid de sharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        uid = sharedPref.getString("uid", "N/A")

        // Inicializa tvFinalPrice usando binding
        tvFinalPrice = binding.tvFinalPrice
        val ibDeleteAllCart: ImageButton =  binding.ibDeleteAllCart
        val btnBuyAll: Button =  binding.btnBuyAll


        // Set up the RecyclerView with a GridLayoutManager and the CardAdapter.
        cardAdapterCart = CardAdapterCart(emptyList(), emptyList(), this, this)

        binding.rvCart.apply {
            layoutManager = GridLayoutManager(activity?.applicationContext, 1)
            adapter = cardAdapterCart
        }


        // Observe the books LiveData and update the adapter when the data changes
        cartLiveData.observe(viewLifecycleOwner, { cartData ->
            cartData?.let {
                cardAdapterCart.updateBooks(it.books, it.quantities)
                calculateFinalPrice(it.books, it.quantities)  // Calcular el precio total cuando los datos cambian
            }
        })


        // Start loading records
        lifecycleScope.launch {
            getCartBooks()
        }

        if (ibDeleteAllCart != null) {
            ibDeleteAllCart.setOnClickListener{
                lifecycleScope.launch {
                    // Vaciar carrito del ususario
                    uid?.let { it1 -> showConfirmationDialog(it1,"¿Está seguro que desea vaciar el carrito?","Vaciar") }
                }

            }
        }

        btnBuyAll.setOnClickListener {
            lifecycleScope.launch {
                uid?.let { it1 -> deleteAllCart(it1) }
                Toast.makeText(context, "Compra ralizada con éxito", Toast.LENGTH_SHORT).show()
            }
        }



        // Función para que solo salga de la app si pulsa back dos veces seguidas
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

    // Obtiene los registros en el carrito del usuario actual
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


    // Obtiene los datos de cada libro
    suspend fun fetchBookById(id: Int): Book? {
        val bookService = ApiServiceFactory.makeBooksService()

        return try{
            // Obtener detalles del libro por su ID desde el servicio API
            val response = bookService.fetchBookById(id)

            if (response.isSuccessful && response.body() != null) {
                val bookList = response.body()!!.data

                // Verificar si se encontró el libro con el ID dado
                if (bookList != null && bookList.isNotEmpty()) {
                    //???????????????????????????????????????????????????????????????????????????
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


    // Almaceno en el LiveData los libros y las cantidades de estos que tiene el usuario en la cesta
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

    // Se ejecuta el volver a este fragmento
    override fun onResume() {
        super.onResume()
        // Recargar los datos del carrito al volver a este fragmento
        lifecycleScope.launch {
            getCartBooks()
        }
    }


    // Aumentar la cantidad de un libro en el carrito del usuario
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

    // Disminuir la cantidad de un libro en el carrito del usuario
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
                showDeleteConfirmationDialog {
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

    // Eliminar un reguistro del carrito
    suspend fun deleteCartRegister(uid: String?, id_book: Int) {
        if (uid != null && id_book != -1) {
            val cartService = ApiServiceFactory.makeCartService()

            try {
                val response = cartService.deleteCart(uid, id_book)  // Make the API call
                if (response.isSuccessful) {
                    // Successfully deleted the record
                    println("Registro del Carrito eliminado con éxito")
                    showToast("Libro eliminado del carrito")
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

    // Función para calcular el precio final (todos los libros del carrito)
    private fun calculateFinalPrice(books: List<Book>, quantities: List<Int>) {
        var finalPrice: Double = 0.0
        for ((index, book) in books.withIndex()) {
            finalPrice += book.price * quantities[index]
        }
        tvFinalPrice?.text = finalPrice.toString()
    }

    // Mostrar un mensaje temporal
    private fun showToast(message: String) {
        Toast.makeText(activity?.applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    // Mostrar un mensaje para continuar o terminar con una operación
    fun showDeleteConfirmationDialog(onConfirm: () -> Unit) {
        val activity = activity
        if (activity != null && !activity.isFinishing) { // Verificar si la actividad no está en proceso de finalización
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Confirmación")
            builder.setMessage("Está a punto de eliminar el libro del carrito, ¿está seguro que desea continuar?")
            builder.setPositiveButton("Aceptar") { _, _ ->
                onConfirm()  // Acción a realizar si el usuario acepta
            }
            builder.setNegativeButton("Cancelar", null)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    // Cuando clico un libro cambio a una actividad
    override fun onBookClick(book: Book) {
        val intent = Intent(activity?.applicationContext, BookDetailActivity::class.java)
        intent.putExtra("id_book", book.id)
        intent.putExtra("source", Constants.CART_FRAGMENT) // Añado el extra para indicar que estoy en "home"
        startActivity(intent)
    }

    // Aumentar la cantidad a través del botón de la card
    override fun onAddClick(idBook: Int, quantity: Int) {
        lifecycleScope.launch {
            addCartQuantity(uid, idBook, quantity )
            // Actualizar la vista después de agregar cantidad
            getCartBooks()
        }

    }

    // Disminuir la cantidad a través del botón de la card
    override fun onSubstractClick(idBook: Int, quantity: Int) {
        lifecycleScope.launch {
            subtractCartQuantity(uid, idBook, quantity )
            // Actualizar la vista después de agregar cantidad
            getCartBooks()
        }

    }


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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}