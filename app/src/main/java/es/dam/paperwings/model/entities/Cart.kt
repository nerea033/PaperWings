package es.dam.paperwings.model.entities

data class Cart(
    val id: String,
    val uid: String,
    val id_book: Int,
    val price: Double,
    val quantity: Int,
)