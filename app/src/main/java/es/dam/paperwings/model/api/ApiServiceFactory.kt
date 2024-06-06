package es.dam.paperwings.model.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Factory object for creating instances of Retrofit services for API communication.
 */
object ApiServiceFactory {

    // Initial configuration of the Retrofit object that will be reused to create API services.
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:4000/api/") // Base URL of the API. Use localhost IP for emulator.
        .addConverterFactory(GsonConverterFactory.create()) // Use Gson converter to serialize and deserialize JSON data.
        .build()

    /**
     * Creates and returns an instance of the user service API.
     *
     * @return An instance of [ApiUsersServices].
     */
    fun makeUsersService(): ApiUsersServices {
        return retrofit.create(ApiUsersServices::class.java)
    }

    /**
     * Creates and returns an instance of the book service API.
     *
     * @return An instance of [ApiBooksServices].
     */
    fun makeBooksService(): ApiBooksServices {
        return retrofit.create(ApiBooksServices::class.java)
    }

    /**
     * Creates and returns an instance of the cart service API.
     *
     * @return An instance of [ApiCartServices].
     */
    fun makeCartService(): ApiCartServices {
        return  retrofit.create(ApiCartServices::class.java)
    }


}