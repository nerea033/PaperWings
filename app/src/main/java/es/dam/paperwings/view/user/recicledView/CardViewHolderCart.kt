package es.dam.paperwings.view.user.recicledView

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
 */
class CardViewHolderCart(
    private val cardCellBinding: CardCellCartBinding,
    private val clickListener: BookClickListener,
    private val updateClickListener: CartClickListener

) : RecyclerView.ViewHolder(cardCellBinding.root) {


    fun findBook(book: Book, quantity: Int){
        book.image?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            cardCellBinding.ivCoverCart.setImageBitmap(bitmap)
        } ?: run {
            // Manejar el caso donde la imagen es nula, usar una imagen por defecto
            cardCellBinding.ivCoverCart.setImageResource(R.drawable.ic_book_cover)
        }

        cardCellBinding.tvTitleCart.text = book.title ?: "No Title"
        cardCellBinding.tvAuthorCart.text = book.author ?: "No Author"
        cardCellBinding.tvPriceCart.text = book.price.toString() + " €" ?: "No Price"
        cardCellBinding.tvQuantityCart.text = quantity.toString()

        cardCellBinding.btnAddBookCart.setOnClickListener {
            updateClickListener.onAddClick(book.id, quantity)
        }
        cardCellBinding.btnSubstractBookCart.setOnClickListener {
            updateClickListener.onSubstractClick(book.id, quantity)
        }

        cardCellBinding.cardviewCart.setOnClickListener{
            clickListener.onBookClick(book)
        }


    }
}