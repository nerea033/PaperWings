package es.dam.paperwings.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.dam.paperwings.databinding.CardCellCategoryBinding
import es.dam.paperwings.model.CategoryClickListener
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.view.holders.CardViewHolderCategory

/**
 * RecyclerView adapter for displaying a list of categories.
 *
 * @property categories List of category names to display.
 * @property clickListener Listener for handling clicks on each category item.
 */
class CardAdapterCategory (
    private var categories: List<String>,
    private var clickListener: CategoryClickListener
) : RecyclerView.Adapter<CardViewHolderCategory>() {


    /**
     * Updates the list of categories based on the provided list of books.
     * Extracts unique categories from the books and updates the adapter's dataset.
     *
     * @param newBooks The new list of books to extract categories from.
     */
    fun updateBooks(newBooks: List<Book>) {
        val uniqueCategories = newBooks.map { it.category ?: "No Category" }.distinct()
        categories = uniqueCategories
        notifyDataSetChanged()
    }


    /**
     * Called when RecyclerView needs a new [CardViewHolderCategory] to represent an item.
     *
     * @param parent The ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view.
     * @return A new [CardViewHolderCategory] that holds a view representing an item in the list.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolderCategory {
        val from = LayoutInflater.from(parent.context)
        val binding = CardCellCategoryBinding.inflate(from, parent,false)
        return CardViewHolderCategory(binding, clickListener)
    }

    /**
     * Returns the total number of items in the list of categories.
     *
     * @return The total number of items in the list of categories.
     */
    override fun getItemCount(): Int = categories.size

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The [CardViewHolderCategory] that represents the view of an item.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: CardViewHolderCategory, position: Int) {
        holder.findBook(categories[position])
    }
}