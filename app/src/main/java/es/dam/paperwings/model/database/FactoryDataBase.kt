package es.dam.paperwings.model.dataBase

/**
 * Clase que se encarga de elegir qué archivo de base de datos elegir si el de prueba o el de implementación
 */
class FactoryDataBase {
    companion object { //estático
        const val MODO_TEST = 0
        const val MODO_IMPL = 1

        fun getDao(modo: Int): DaoDataBase {
            return when (modo) {
                MODO_TEST -> TestDataBase()
                MODO_IMPL -> ImplDataBase()
                else -> throw IllegalArgumentException("Modo de implementación no soportado")
            }
        }
    }
}