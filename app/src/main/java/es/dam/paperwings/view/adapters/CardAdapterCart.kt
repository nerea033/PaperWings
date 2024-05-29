package es.dam.paperwings.view.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import es.dam.paperwings.R
import es.dam.paperwings.databinding.CardCellCartBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.CartClickListener
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.view.holders.CardViewHolderCart

class CardAdapterCart (

    private var books: List<Book>,
    private var quantities: List<Int>,
    private var clickListener: BookClickListener,
    private var updateClickListener: CartClickListener)

    : RecyclerView.Adapter<CardViewHolderCart>() {

    private lateinit var recyclerView: RecyclerView  // Para referencia al RecyclerView
    private lateinit var emptyView: View  // Para referencia al empty_view
    private lateinit var tvDetailsCart: View
    private lateinit var cardDetails: CardView
    private val handler = Handler(Looper.getMainLooper())  // Handler para manejar el retraso
    private val emptyCheckRunnable = Runnable { checkEmptyImmediate() }  // Runnable para la comprobación inmediata


    // Actualiza la lista de libros y verifica la visibilidad del empty_view
    fun updateBooks(newBooks: List<Book>, newQuantities: List<Int>) {
        books = newBooks
        quantities = newQuantities
        notifyDataSetChanged()
        checkEmptyWithDelay()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolderCart {
        val from = LayoutInflater.from(parent.context)
        val binding = CardCellCartBinding.inflate(from, parent,false)
        return CardViewHolderCart(binding, clickListener, updateClickListener)
    }

    // Método llamado cuando se adjunta el adaptador al RecyclerView -> para mostrar cuando la lista está vacía
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        emptyView = recyclerView.rootView.findViewById(R.id.empty_view)
        tvDetailsCart = recyclerView.rootView.findViewById(R.id.tvDetailsCart)
        cardDetails = recyclerView.rootView.findViewById(R.id.cardviewDetailCart)
        checkEmptyWithDelay()
    }

    private fun checkEmptyWithDelay() {
        handler.removeCallbacks(emptyCheckRunnable)  // Eliminar cualquier llamada previa
        handler.postDelayed(emptyCheckRunnable, 0)  // Postponer la comprobación en segundos
    }

    private fun checkEmptyImmediate() {
        if (books.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvDetailsCart.visibility = View.GONE
            cardDetails.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            tvDetailsCart.visibility = View.VISIBLE
            cardDetails.visibility = View.VISIBLE
        }
    }


    override fun getItemCount(): Int {
        return books.size
    }


    override fun onBindViewHolder(holder: CardViewHolderCart, position: Int) {
        holder.findBook(books[position], quantities[position])

    }
}