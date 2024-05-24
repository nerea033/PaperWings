package es.dam.paperwings.view.user.recicledView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.dam.paperwings.databinding.CardCellHomeBinding
import es.dam.paperwings.model.BookClickListener
import es.dam.paperwings.model.entities.Book

class CardAdapterCategoryTwo (
    private var books: List<Book>,
    private var clickListener: BookClickListener)
    : RecyclerView.Adapter<CardViewHolderCategoryTwo>() {

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolderCategoryTwo {
        val from = LayoutInflater.from(parent.context)
        val binding = CardCellHomeBinding.inflate(from, parent,false)
        return CardViewHolderCategoryTwo(binding, clickListener)
    }


    override fun getItemCount(): Int = books.size


    override fun onBindViewHolder(holder: CardViewHolderCategoryTwo, position: Int) {
        holder.findBook(books[position])
    }
}