package es.dam.paperwings.model.api

import retrofit2.Response
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.model.entities.StructureFetch
import es.dam.paperwings.model.entities.StructureUpdate
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiBooksServices {


    @POST("books")
    suspend fun addBook(@Body book: Book): Response<StructureUpdate>

    @GET("books")
    suspend fun listBooks(): Response<StructureFetch<Book>>

    @GET("books/{id}")
    suspend fun fetchBookById(@Path("id") id: Int): Response<StructureFetch<Book>>

    @GET("books/search")
    suspend fun searchBooks(
        @Query("title") title: String?,
        @Query("author") author: String?,
        @Query("language") language: String?,
        @Query("category") category: String?,
        @Query("isbn") isbn: String?
    ): Response<StructureFetch<Book>>

    @PUT("books/update")
    suspend fun updateBook(@Body updateRequest: UpdateRequest<Book>): Response<StructureUpdate>

    @DELETE("books/{id}")
    suspend fun deleteBook(@Path("id") id: Int): Response<StructureUpdate>

}
data class UpdateRequest<T>(
    val idField: String,
    val id: Int,
    val updateData: List<T>?, //Permite que data sea null
)