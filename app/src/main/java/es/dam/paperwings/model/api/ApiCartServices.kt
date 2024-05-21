package es.dam.paperwings.model.api

import es.dam.paperwings.model.entities.Cart
import es.dam.paperwings.model.entities.Structure
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiCartServices {
    @POST("cart") // Parte estática de la ruta -> para obtener los registros de carrito
    suspend fun addCart(@Body cart: Cart): Response<Structure<Cart>>

    @GET("cart") // La función 'suspend' permite trabajar con corrutinas directamente.
    suspend fun listCart(): Response<Structure<Cart>>

    @GET("cart/{id}") // Ruta para obtener los registros de carrito
    suspend fun fetchCartById(@Path("id") id: String): Response<Structure<Cart>>
}