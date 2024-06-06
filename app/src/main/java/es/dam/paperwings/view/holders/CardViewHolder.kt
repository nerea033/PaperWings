package es.dam.paperwings.view.holders

import android.graphics.BitmapFactory
import androidx.recyclerview.widget.RecyclerView
import es.dam.paperwings.R
import es.dam.paperwings.databinding.CardCellHomeBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.entities.Book

/**
 * ViewHolder class for holding views of a card in a RecyclerView.
 * This class binds the views using data binding to display information about a book.
 *
 * @property cardCellBinding Binding object containing references to the views inside the card layout.
 * @property clickListener Listener for handling clicks on the book card.
 */
class CardViewHolder(
    private val cardCellBinding: CardCellHomeBinding,
    private val clickListener: BookClickListener
) : RecyclerView.ViewHolder(cardCellBinding.root) {

    /**
     * Updates the views with information about the provided book.
     *
     * @param book The book object containing information to display.
     */
    fun findBook(book: Book){
        // Set book cover image if available; otherwise, set a default image
        book.image?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            cardCellBinding.ivCover.setImageBitmap(bitmap)
        } ?: run {
            cardCellBinding.ivCover.setImageResource(R.drawable.ic_book_cover)
        }

        // Set book title, author, and price
        cardCellBinding.tvTitle.text = book.title ?: "No Title"
        cardCellBinding.tvAuthor.text = book.author ?: "No Author"
        cardCellBinding.tvPrice.text = book.price.toString() + " €" ?: "No Price"

        // Set click listener for the whole card view
        cardCellBinding.cardview.setOnClickListener{
            clickListener.onBookClick(book)
        }


    }
}