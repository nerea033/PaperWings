package es.dam.paperwings.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.dam.paperwings.databinding.CardCellBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.entities.Book

class CardAdapter (

    private var books: List<Book>,
    private var clickListener: BookClickListener)
    : RecyclerView.Adapter<CardViewHolder>() {

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    /**
     * This method is called when the RecyclerView needs a new view to represent
     * an item in the list.
     * @param parent The ViewGroup into which the new view will be added after it is bound to an adapter position.
     * @param viewType The view type of the new view.
     * @return A new CardViewHolder that holds a view representing an item in the list.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = CardCellBinding.inflate(from, parent,false)
        return CardViewHolder(binding, clickListener)
    }

    /**
     * This method returns the total number of items in the list of books.
     * @return The total number of items in the list of books.
     */
    override fun getItemCount(): Int = books.size

    /**
     * This method is called when specific data of a book needs to be bound to a view
     * at a particular position within the RecyclerView.
     * @param holder The CardViewHolder representing the view of an item in the list.
     * @param position The position of the item in the list of books.
     */
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.findBook(books[position])
    }
}