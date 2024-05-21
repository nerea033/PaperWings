package es.dam.paperwings.model.api

import retrofit2.Response
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.model.entities.Structure
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiBooksServices {


    suspend fun addBook()

    @GET("books")
    suspend fun listBooks(): Response<Structure<Book>>

    @GET("books/{id}")
    suspend fun fetchBookById(@Path("id") id: Int): Response<Structure<Book>>

    suspend fun fetchBookByTitle()

    suspend fun fetchBookByAuthor()

    suspend fun fetchBookByLanguage()

    suspend fun fetchBookByCategory()

    suspend fun fetchBookByIsbn()

}