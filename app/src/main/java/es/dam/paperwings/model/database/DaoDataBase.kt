package es.dam.paperwings.model.database

import es.dam.paperwings.model.entities.User

/**
 * Interfaz que contiene todos los métodos CRUD que se van a usar en la base de datos
 */
interface DaoDataBase {
    fun registerUser(user: User)
}