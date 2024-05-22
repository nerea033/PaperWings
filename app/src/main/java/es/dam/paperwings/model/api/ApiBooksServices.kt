package es.dam.paperwings.model.api

import retrofit2.Response
import es.dam.paperwings.model.entities.Book
import es.dam.paperwings.model.entities.StructureFetch
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiBooksServices {


    suspend fun addBook()

    @GET("books")
    suspend fun listBooks(): Response<StructureFetch<Book>>

    @GET("books/{id}")
    suspend fun fetchBookById(@Path("id") id: Int): Response<StructureFetch<Book>>

    suspend fun fetchBookByTitle()

    suspend fun fetchBookByAuthor()

    suspend fun fetchBookByLanguage()

    suspend fun fetchBookByCategory()

    suspend fun fetchBookByIsbn()

}