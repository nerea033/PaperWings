package es.dam.paperwings.view.holders

import android.graphics.BitmapFactory
import androidx.recyclerview.widget.RecyclerView
import es.dam.paperwings.R
import es.dam.paperwings.databinding.CardCellCartBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.CartClickListener
import es.dam.paperwings.model.entities.Book

/**
 * ViewHolder class for holding views of a card in a RecyclerView.
 * This class binds the views using data binding to display information about a book.
 *
 * @property cardCellBinding Binding object containing references to the views inside the cart card layout.
 * @property clickListener Listener for handling clicks on the book card.
 * @property updateClickListener Listener for handling update actions like adding or subtracting books from the cart.
 */
class CardViewHolderCart(
    private val cardCellBinding: CardCellCartBinding,
    private val clickListener: BookClickListener,
    private val updateClickListener: CartClickListener

) : RecyclerView.ViewHolder(cardCellBinding.root) {

    /**
     * Updates the views with information about the provided book and its quantity in the cart.
     *
     * @param book The book object containing information to display.
     * @param quantity The quantity of the book in the cart.
     */
    fun findBook(book: Book, quantity: Int){
        // Set book cover image if available; otherwise, set a default image
        book.image?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            cardCellBinding.ivCoverCart.setImageBitmap(bitmap)
        } ?: run {
            cardCellBinding.ivCoverCart.setImageResource(R.drawable.ic_book_cover)
        }

        // Set book title, author, price, and quantity
        cardCellBinding.tvTitleCart.text = book.title ?: "No Title"
        cardCellBinding.tvAuthorCart.text = book.author ?: "No Author"
        cardCellBinding.tvPriceCart.text = book.price.toString() + " €" ?: "No Price"
        cardCellBinding.tvQuantityCart.text = quantity.toString()

        // Set click listeners for add and subtract buttons
        cardCellBinding.btnAddBookCart.setOnClickListener {
            updateClickListener.onAddClick(book.id, quantity)
        }
        cardCellBinding.btnSubstractBookCart.setOnClickListener {
            updateClickListener.onSubstractClick(book.id, quantity)
        }

        // Set click listener for the whole card view
        cardCellBinding.cardviewCart.setOnClickListener{
            clickListener.onBookClick(book)
        }


    }
}