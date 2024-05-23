package es.dam.paperwings.view.recicledView

import androidx.recyclerview.widget.RecyclerView
import es.dam.paperwings.databinding.CardCellCategoryBinding
import es.dam.paperwings.model.CategoryClickListener

/**
 * ViewHolder class for holding views of a card in a RecyclerView.
 * This class binds the views using data binding to display information about a book.
 */
class CardViewHolderCategory(
    private val cardCellBinding: CardCellCategoryBinding,
    private val clickListener: CategoryClickListener
) : RecyclerView.ViewHolder(cardCellBinding.root) {

    /**
     * Updates the views with information about the provided book.
     * @param book The book object containing information to display.
     */
    fun findBook(category: String){

        cardCellBinding.tvCategoryCategory.text = category

        cardCellBinding.cardviewCategory.setOnClickListener{
            clickListener.onCategoryClick(category)
        }


    }
}