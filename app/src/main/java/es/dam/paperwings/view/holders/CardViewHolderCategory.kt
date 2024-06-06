package es.dam.paperwings.view.holders

import androidx.recyclerview.widget.RecyclerView
import es.dam.paperwings.databinding.CardCellCategoryBinding
import es.dam.paperwings.model.CategoryClickListener

/**
 * ViewHolder class for holding views of a card in a RecyclerView.
 * This class binds the views using data binding to display information about a category.
 *
 * @property cardCellBinding Binding object containing references to the views inside the category card layout.
 * @property clickListener Listener for handling clicks on the category card.
 */
class CardViewHolderCategory(
    private val cardCellBinding: CardCellCategoryBinding,
    private val clickListener: CategoryClickListener
) : RecyclerView.ViewHolder(cardCellBinding.root) {

    /**
     * Updates the views with information about the provided category.
     *
     * @param category The name of the category to display.
     */
    fun findBook(category: String){

        cardCellBinding.tvCategoryCategory.text = category

        // Set click listener for the whole card view
        cardCellBinding.cardviewCategory.setOnClickListener{
            clickListener.onCategoryClick(category)
        }


    }
}