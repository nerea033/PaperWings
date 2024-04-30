package es.dam.paperwings.model.entities

/**
 * Clase que contiene los atributos de los usuarios
 */
class User(private var uid: String, private var name: String, private var rol: String) {

    // Constructor secundario que establece un valor por defecto para el rol
    constructor(uid: String, name: String) : this(uid, name, "USER")

    // Getes y setes
    fun getUid(): String{
        return uid
    }
    fun setUid(uid: String){
        this.uid = uid
    }

    fun getName(): String {
        return name
    }

    fun setName(name: String){
        this.name = name
    }

    fun getRol(): String {
        return rol
    }
    fun setRol(rol: String) {
        this.rol = rol
    }
}
