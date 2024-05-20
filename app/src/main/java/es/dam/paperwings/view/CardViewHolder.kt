package es.dam.paperwings.view

import android.graphics.BitmapFactory
import androidx.recyclerview.widget.RecyclerView
import es.dam.paperwings.R
import es.dam.paperwings.databinding.CardCellBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.entities.Book

/**
 * ViewHolder class for holding views of a card in a RecyclerView.
 * This class binds the views using data binding to display information about a book.
 */
class CardViewHolder(
    private val cardCellBinding: CardCellBinding,
    private val clickListener: BookClickListener
) : RecyclerView.ViewHolder(cardCellBinding.root) {

    /**
     * Updates the views with information about the provided book.
     * @param book The book object containing information to display.
     */
    fun findBook(book: Book){
        book.image?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            cardCellBinding.ivCover.setImageBitmap(bitmap)
        } ?: run {
            // Manejar el caso donde la imagen es nula, usar una imagen por defecto
            cardCellBinding.ivCover.setImageResource(R.drawable.ic_book_cover)
        }

        cardCellBinding.tvTitle.text = book.title ?: "No Title"
        cardCellBinding.tvAuthor.text = book.author ?: "No Author"

        cardCellBinding.cardview.setOnClickListener{
            clickListener.onBookClick(book)
        }


    }
}