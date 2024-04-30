package es.dam.paperwings.controller

import es.dam.paperwings.model.database.DaoDataBase
import es.dam.paperwings.model.database.FactoryDataBase
import es.dam.paperwings.model.entities.User

class ControllerPaperWings {

    // Inicializar el DAO
    private val daoDataBase: DaoDataBase

    // Constructor
    init {
        daoDataBase = FactoryDataBase.getDao(FactoryDataBase.MODO_IMPL)
        println("DAO Type: ${daoDataBase::class.simpleName}")  // Esto imprimirá el tipo real de daoDataBase

    }

    /**
     * Método para registrar un nuevo usuario en la bbdd
     */
    fun registerUser(user: User) {
        daoDataBase.registerUser(user)
    }

}