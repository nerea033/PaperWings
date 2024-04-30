package es.dam.paperwings.controller

import es.dam.paperwings.model.database.DaoDataBase
import es.dam.paperwings.model.database.FactoryDataBase

class ControllerPaperWings {

    // Inicializar el DAO
    private val daoDataBase: DaoDataBase

    // Constructor
    init {
        daoDataBase = FactoryDataBase.getDao(FactoryDataBase.MODO_TEST)
    }



}