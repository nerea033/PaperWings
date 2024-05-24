package es.dam.paperwings.model.entities

/**
 * Clase para usar el Live data en el CartFragment
 */
data class CartData(
    val books: List<Book>,
    val quantities: List<Int>
)
