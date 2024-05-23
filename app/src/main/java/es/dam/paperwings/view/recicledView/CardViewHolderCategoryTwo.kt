package es.dam.paperwings.view.recicledView

import android.graphics.BitmapFactory
import androidx.recyclerview.widget.RecyclerView
import es.dam.paperwings.R
import es.dam.paperwings.databinding.CardCellCategoryBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.CategoryClickListener
import es.dam.paperwings.model.entities.Book

/**
 * ViewHolder class for holding views of a card in a RecyclerView.
 * This class binds the views using data binding to display categories.
 */
class CardViewHolderCategoryTwo(
    private val cardCellBinding: CardCellCategoryBinding,
    private val clickListener: CategoryClickListener
) : RecyclerView.ViewHolder(cardCellBinding.root) {

    /**
     * Updates the views with information about the provided book.
     */
    fun findBook(category: String){

        cardCellBinding.tvCategoryCategory.text = category?: "No Category"

        // Si clico sobre una categoría
        cardCellBinding.cardviewCategory.setOnClickListener {
            clickListener.onCategoryClick(category)
        }

    }
}