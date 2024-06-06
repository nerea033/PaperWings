package es.dam.paperwings.view.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import es.dam.paperwings.R
import es.dam.paperwings.databinding.CardCellCartBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.CartClickListener
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.view.holders.CardViewHolderCart

/**
 * RecyclerView adapter for displaying a list of books in a shopping cart format.
 *
 * @property books List of books to display in the cart.
 * @property quantities List of quantities corresponding to each book in the cart.
 * @property clickListener Listener for handling clicks on each book card.
 * @property updateClickListener Listener for handling updates like quantity changes or removals from the cart.
 */
class CardAdapterCart (

    private var books: List<Book>,
    private var quantities: List<Int>,
    private var clickListener: BookClickListener,
    private var updateClickListener: CartClickListener)

    : RecyclerView.Adapter<CardViewHolderCart>() {

    private lateinit var recyclerView: RecyclerView  // Reference to the RecyclerView
    private lateinit var emptyView: View  // Reference to the empty_view
    private lateinit var tvDetailsCart: View  // Reference to the cart details TextView
    private lateinit var cardDetails: CardView  // Reference to the card view containing cart details
    private lateinit var btnBuyAll: Button  // Reference to the Buy All button
    private val handler = Handler(Looper.getMainLooper())  // Handler to manage delays
    private val emptyCheckRunnable = Runnable { checkEmptyImmediate() }  // Runnable for immediate empty check


    /**
     * Updates the list of books in the cart and their quantities, then notifies any registered observers
     * that the data set has changed. Also checks and updates the visibility of empty view with a delay.
     *
     * @param newBooks The new list of books to display in the cart.
     * @param newQuantities The new list of quantities corresponding to the books.
     */
    fun updateBooks(newBooks: List<Book>, newQuantities: List<Int>) {
        books = newBooks
        quantities = newQuantities
        notifyDataSetChanged()
        checkEmptyWithDelay()
    }

    /**
     * Called when RecyclerView needs a new [CardViewHolderCart] to represent an item.
     *
     * @param parent The ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view.
     * @return A new [CardViewHolderCart] that holds a view representing an item in the cart.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolderCart {
        val from = LayoutInflater.from(parent.context)
        val binding = CardCellCartBinding.inflate(from, parent,false)
        return CardViewHolderCart(binding, clickListener, updateClickListener)
    }

    /**
     * Called when the adapter is attached to a RecyclerView. Checks and updates the visibility of empty view
     * and other UI elements related to the cart immediately.
     *
     * @param recyclerView The RecyclerView to which the adapter is attached.
     */
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        emptyView = recyclerView.rootView.findViewById(R.id.empty_view)
        tvDetailsCart = recyclerView.rootView.findViewById(R.id.tvDetailsCart)
        cardDetails = recyclerView.rootView.findViewById(R.id.cardviewDetailCart)
        btnBuyAll = recyclerView.rootView.findViewById(R.id.btnBuyAll)
        checkEmptyWithDelay()
    }

    /**
     * Checks and updates the visibility of empty view and other UI elements related to the cart immediately.
     * Called after a delay to allow time for other UI updates to settle.
     */
    private fun checkEmptyWithDelay() {
        handler.removeCallbacks(emptyCheckRunnable)  // Remove any previous callbacks
        handler.postDelayed(emptyCheckRunnable, 0)  // Post the check with no delay
    }

    /**
     * Checks and updates the visibility of empty view and other UI elements related to the cart immediately.
     * This method is called directly for immediate checks.
     */
    private fun checkEmptyImmediate() {
        if (books.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvDetailsCart.visibility = View.GONE
            cardDetails.visibility = View.GONE
            btnBuyAll.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            tvDetailsCart.visibility = View.VISIBLE
            btnBuyAll.visibility = View.VISIBLE
            cardDetails.visibility = View.VISIBLE
        }
    }

    /**
     * Returns the total number of items in the cart.
     *
     * @return The total number of items in the cart.
     */
    override fun getItemCount(): Int {
        return books.size
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The [CardViewHolderCart] that represents the view of an item.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: CardViewHolderCart, position: Int) {
        holder.findBook(books[position], quantities[position])

    }
}