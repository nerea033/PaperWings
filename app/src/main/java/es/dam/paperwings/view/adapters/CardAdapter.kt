package es.dam.paperwings.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.dam.paperwings.databinding.CardCellHomeBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.view.holders.CardViewHolder

/**
 * RecyclerView adapter for displaying a list of books in card format.
 *
 * @property books List of books to display.
 * @property clickListener Listener for handling clicks on each book card.
 */
class CardAdapter (

    private var books: List<Book>,
    private var clickListener: BookClickListener)
    : RecyclerView.Adapter<CardViewHolder>() {

    /**
     * Updates the list of books displayed by the adapter and notifies any registered observers
     * that the data set has changed.
     *
     * @param newBooks The new list of books to display.
     */
    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    /**
     * Called when RecyclerView needs a new [CardViewHolder] to represent an item.
     *
     * @param parent The ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view.
     * @return A new [CardViewHolder] that holds a view representing an item in the list.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = CardCellHomeBinding.inflate(from, parent,false)
        return CardViewHolder(binding, clickListener)
    }

    /**
     * Returns the total number of items in the list of books.
     *
     * @return The total number of items in the list of books.
     */
    override fun getItemCount(): Int = books.size

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The [CardViewHolder] that represents the view of an item.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.findBook(books[position])
    }
}