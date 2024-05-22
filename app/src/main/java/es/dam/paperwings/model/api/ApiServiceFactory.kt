package es.dam.paperwings.model.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiServiceFactory {

    // Configuración inicial del objeto Retrofit que será reutilizado para crear servicios API.
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:4000/api/") // URL base de la API. Es localhost, pero al ser un emulador hay que usar esa dirección IP.
        .addConverterFactory(GsonConverterFactory.create()) // Utiliza Gson, si tenemos un fichero Json lo convierte a objetos y viceversa.
        .build()

    // Método para crear una instancia del servicio de usuarios.
    fun makeUsersService(): ApiUsersServices {
        return retrofit.create(ApiUsersServices::class.java)
    }

    // Método para crear una instancia del servicio de libros.
    fun makeBooksService(): ApiBooksServices {
        return retrofit.create(ApiBooksServices::class.java)
    }

    // Método para crear una instancia del servicio de carrito
    fun makeCartService(): ApiCartServices {
        return  retrofit.create(ApiCartServices::class.java)
    }


}