package es.dam.paperwings.model.database

/**
 * Clase que se encarga de elegir qué archivo de base de datos elegir si el de prueba o el de implementación
 */
class FactoryDataBase {
    companion object { //stático
        const val MODO_TEST = 0
        const val MODO_IMPL = 1

        fun getDao(modo: Int): DaoDataBase {
            return if (modo == MODO_TEST) {
                TestDataBase()
            } else {
                ImplDataBase()
            }
        }
    }
}