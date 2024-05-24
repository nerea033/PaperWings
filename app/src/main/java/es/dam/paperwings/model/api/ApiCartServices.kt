package es.dam.paperwings.model.api

import com.google.gson.annotations.SerializedName
import es.dam.paperwings.model.entities.Cart
import es.dam.paperwings.model.entities.StructureFetch
import es.dam.paperwings.model.entities.StructureUpdate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiCartServices {
    @POST("cart") // Parte estática de la ruta -> para obtener los registros de carrito
    suspend fun addCart(@Body cart: Cart): Response<StructureUpdate>

    @GET("cart") // La función 'suspend' permite trabajar con corrutinas directamente.
    suspend fun listCart(): Response<StructureFetch<Cart>>

    @GET("cart/{uid}") // Ruta para obtener los registros de carrito de un usario
    suspend fun fetchUserCartRecords(@Path("uid") uid: String): Response<StructureFetch<Cart>>

    @PUT("cart/update")
    suspend fun updateCart(@Body request: UpdateCartRequest): Response<StructureUpdate>

    @DELETE("cart/delete")
    suspend fun deleteCart(@Body request: DeleteCartRequest): Response<StructureUpdate>


}

// Define a data class to represent the request body
data class UpdateCartRequest(
    val uid: String,
    @SerializedName("id_book")
    val idBook: Int,
    val quantity: Int
)

data class DeleteCartRequest(
    val uid: String,
    @SerializedName("id_book")
    val idBook: Int
)