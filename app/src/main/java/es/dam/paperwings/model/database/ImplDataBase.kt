package es.dam.paperwings.model.database

import es.dam.paperwings.model.entities.User
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * Clase de Implementación, desarrolla los métodos CRUD de la BBDD indicados en el DAO
 */
class ImplDataBase : DaoDataBase{

    private val URL = "jdbc:mysql://tu_servidor:puerto/tu_base_de_datos" //tu_servidor:puerto/tu_base_de_datos
    private val USER = "usuario"
    private val PASSWORD = "contraseña"
    /**
     * Conexión a la base de datos
     *
     */
    fun connect(): Connection {
        return try {
            Class.forName("com.mysql.cj.jdbc.Driver") //Cargar el controlador JDBC
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
        } catch (e: SQLException) {
            println("Error al realizar el alta en la base de datos: ${e.message}")
        }
    }
}