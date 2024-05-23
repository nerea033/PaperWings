package es.dam.paperwings.model

import es.dam.paperwings.model.entities.Book

interface CategoryClickListener {
    fun onCategoryClick(category: String)
}