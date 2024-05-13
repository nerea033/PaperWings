package es.dam.paperwings.model.api

interface ApiBooksServices {


    suspend fun addBook()
    suspend fun listBooks()

    suspend fun fetchBookById()

    suspend fun fetchBookByTitle()

    suspend fun fetchBookByAuthor()

    suspend fun fetchBookByLanguage()

    suspend fun fetchBookByCategory()

    suspend fun fetchBookByIsbn()

}