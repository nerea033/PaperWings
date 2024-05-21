package es.dam.paperwings.model.entities

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Book(
    val author: String,
    val category: String,
    val description: String,
    val discount: Double,
    val id: Int,
    val image: ByteArray, // Change it to Bitmap
    val isbn: String,
    val language: String,
    val pages: Int,
    val price: Double,
    val publisher: String,
    val stock: Int,
    val title: String,
    val date: String,
) {
    // Function to convert date string to LocalDate
    fun getLocalDate(): LocalDate? {
        return try {
            LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
        } catch (e: Exception) {
            null
        }
    }
}

