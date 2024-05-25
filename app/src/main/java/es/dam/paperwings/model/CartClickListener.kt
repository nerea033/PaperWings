package es.dam.paperwings.model

import es.dam.paperwings.model.entities.Book

interface CartClickListener {
    fun onAddClick(idBook: Int, quantity: Int)
    fun onSubstractClick(idBook: Int, quantity: Int)
}