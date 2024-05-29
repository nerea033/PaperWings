package es.dam.paperwings.model.api

import es.dam.paperwings.model.entities.StructureFetch
import es.dam.paperwings.model.entities.StructureUpdate
import es.dam.paperwings.model.entities.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiUsersServices {

    @POST("users") // Parte estática de la ruta -> para obtener los usuarios
    suspend fun addUser(@Body user: User):Response<StructureFetch<User>>

    @GET("users") // La función 'suspend' permite trabajar con corrutinas directamente.
    suspend fun listUsers(): Response<StructureFetch<User>>

    @GET("users/{uid}") // Ruta para obtener los usuarios por uid
    suspend fun fetchUserByUid(@Path("uid") uid: String): Response<StructureFetch<User>>

    @PUT("users/update")
    suspend fun updateUser(@Body updateRequest: UpdateUserRequest): Response<StructureUpdate>

}
data class UpdateUserRequest(
    val idField: String,
    val id: String?,
    val updateData: User?, // nombre o rol
)

