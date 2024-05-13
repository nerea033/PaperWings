package es.dam.paperwings.model.entities

data class Structure<T>(
    val data: List<T>, // val userResponse: ApiResponse<User> = api.getUserList()
    val error: Boolean,
    val status: Int
)