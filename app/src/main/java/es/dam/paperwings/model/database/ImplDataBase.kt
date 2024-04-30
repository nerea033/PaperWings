package es.dam.paperwings.model.database

import es.dam.paperwings.model.entities.User
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * Clase de Implementación, desarrolla los métodos CRUD de la BBDD indicados en el DAO
 */
class ImplDataBase : DaoDataBase{

    private val URL = "jdbc:mysql://sql.freedb.tech:3306/freedb_PaperWingsDB" //tu_servidor:puerto/tu_base_de_datos
    private val USER = "freedb_nerea"
    private val PASSWORD = "6%p!n%QneUDQV7h"
    /**
     * Conexión a la base de datos
     *
     */
    fun connect(): Connection {
        return try {
            DriverManager.getConnection(URL, USER, PASSWORD) // Obtener la conexión a la base de datos
        } catch (e: SQLException) {
            throw RuntimeException("Error al conectar a la base de datos", e)
        }
    }

    override fun registerUser(user: User) {
        val QUERY = "INSERT INTO USER(uid, name, rol) VALUES(?, ?, ?)"
        try {
            val conn = connect()
            val ps = conn.prepareStatement(QUERY)

            ps.setString(1, user.getUid())
            ps.setString(2, user.getName())
            ps.setString(3, user.getRol())

            ps.executeUpdate()

            conn.close()
            println("Inserción realizada con éxito "+user.getUid() + "\nname" + user.getName() + "\rol" + user.getRol())
        } catch (e: SQLException) {
            println("Error al realizar el alta en la base de datos: ${e.message}")
        }
    }
}