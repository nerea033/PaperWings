package es.dam.paperwings.model.entities

import com.google.gson.annotations.SerializedName

data class Cart(
    val uid: String,
    @SerializedName("id_book")
    val idBook: Int,
    val price: Double,
    val quantity: Int,
)