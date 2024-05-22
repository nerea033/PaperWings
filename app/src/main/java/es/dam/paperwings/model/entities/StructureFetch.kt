package es.dam.paperwings.model.entities

import com.google.gson.annotations.SerializedName

data class StructureFetch<T>(
    @SerializedName("body")
    val data: List<T>?, //Permite que data sea null // val userResponse: ApiResponse<User> = api.getUserList()
    @SerializedName("error")
    val error: Boolean,
    @SerializedName("status")
    val status: Int
)

// Debido a val data: List<T>, a la hora de obtener los datos de un solo registro debemos asumir que obtenemos una lista y no un solo registro

