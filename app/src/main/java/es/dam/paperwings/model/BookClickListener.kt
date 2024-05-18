package es.dam.paperwings.model

import es.dam.paperwings.model.entities.Book

interface BookClickListener {
    fun onBookClick(book: Book)
}