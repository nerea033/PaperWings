package es.dam.paperwings.model.entities

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("uid")
    val uid: String,  // Establecer un valor predeterminado para el rol.
    @SerializedName("name")
    val name: String,
    @SerializedName("rol")
    val rol: String = "USER"

)
