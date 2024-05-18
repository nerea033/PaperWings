package es.dam.paperwings.model.entities

data class Book(
    val author: String,
    val category: String,
    val description: String,
    val discount: Double,
    val id: Int,
    val image: ByteArray, // Change it to bitMap
    val isbn: String,
    val language: String,
    val pages: Int,
    val price: Int,
    val publisher: String,
    val stock: Int,
    val title: String,
    val year: Int
)