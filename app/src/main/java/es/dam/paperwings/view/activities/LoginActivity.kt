package es.dam.paperwings.view.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import es.dam.paperwings.R
import es.dam.paperwings.model.api.ApiServiceFactory
import es.dam.paperwings.model.constans.Constants
import es.dam.paperwings.model.repositories.RepositoryImpl
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // Declaro los elementos de mi vista
    private lateinit var tbMail: EditText
    private lateinit var tbPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var linkRegister: TextView

    // Instancia del repositorio
    private val repository = RepositoryImpl()

    // Inicializo la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializo los elementos de la vista
        tbMail = findViewById(R.id.tbMailLogin)
        tbPassword = findViewById(R.id.tbPswLogin)
        btnLogin = findViewById(R.id.btnLogin)
        linkRegister = findViewById(R.id.linkToRegister)

        loginUser()

        linkRegister.setOnClickListener {
            switchToRegister()
        }

    }

    /**
     * Permitir al usuario iniciar sesión si ya está registrado.
     */
    fun loginUser(){
        var userId: String = ""
        var usernameResult : String = ""
        var rolResult : String= ""
        btnLogin.setOnClickListener {
            val mail: String = tbMail.text.toString()
            val password: String = tbPassword.text.toString()

            if (mail.isNotEmpty() && password.isNotEmpty()){
                loginFirebase(mail, password) { uid ->
                    if (uid != null) {
                        // Registro exitoso
                        userId = uid.toString()
                        println("Éxito, usuario encontrado en firebase")
                        // Le paso el uid del Firebase Authentication a la API para que lo compruebe en la DDBB
                        lifecycleScope.launch {// Lanzar una corrutina
                            val result = loginDataBase(userId)
                            // Verifico si el resultado no es nulo
                            result?.let { (username, rol) ->
                                // Asignar los valores a las variables locales cambiadas
                                usernameResult = username
                                rolResult = rol

                                if (rolResult == "USER") {
                                    // Si el rol es "USER", cambiar a la actividad principal pasando el username
                                    repository.saveUserToSharedPreferences(this@LoginActivity, usernameResult, userId, mail, rolResult)
                                    switchToHome()
                                } else if (rolResult == "ADMIN"){
                                    // Si el rol es "admin", cambiar a la actividad principal del administrador pasando el username
                                    repository.saveUserToSharedPreferences(this@LoginActivity, usernameResult, userId, mail, rolResult)
                                    switchToHomeAdmin()
                                }
                            }
                        }

                    } else {
                        // Mostrar mensaje de error
                        repository.showAlert(this@LoginActivity, "Error","Usuario o contraseña incorrectos")
                    }
                }
            } else{
                repository.showAlert(this@LoginActivity, "Error","Por favor, completa todos los campos.")
            }
        }

    }

    /**
     * Comprobar el usuario en Firebase Authentication
     */
    fun loginFirebase(email: String, password: String, onResult: (String?) -> Unit) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Si la autenticación es exitosa, obtener el UID del usuario
                    val uid = firebaseAuth.currentUser?.uid
                    onResult(uid)  // Devolver el UID al llamador
                } else {
                    // Si la autenticación falla, manejar el error
                    onResult(null)
                    println("Authentication failed: ${task.exception?.message}")
                }
            }
    }

    /**
     * Comprobar el usuario en la base de datos.
     */
    suspend fun loginDataBase(uid: String): Pair<String, String>?  {
        val userService = ApiServiceFactory.makeUsersService()
        var username: String = ""
        var rol: String= ""
        try {

            // Busco el usuario mediante su uid a través de la API
            val response = userService.fetchUserByUid(uid)
            if (response.isSuccessful && response.body() != null) { // Si no hay error y no es null

                // Almaceno el usuario en forma de lista (por la Structure)
                val usersList = response.body()!!.data
                if (usersList != null && usersList.isNotEmpty()) {

                    // Cojo el primer y único usuario de la lista
                    val user = usersList.first()

                    // Guardo el nombre y rol de usuario que quiere iniciar sesión
                    username = user.name
                    rol = user.rol

                } else {
                    println("No se encontró el usuario en la bbdd con uid: $uid")
                }
            } else {
                println("Error al obtener los datos del usuario: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            repository.showAlert(this@LoginActivity,"Error","Error en la red o al parsear los datos: ${e.message}")
        }
        // Devuelvo una pareja (Pair) con el nombre de usuario y el rol
        return if (username.isNotEmpty() && rol.isNotEmpty()) {
            Pair(username, rol)
        } else {
            null
        }
    }


    /**
     * Método para cambiar la pestaña a la principal (Home)
     */
    private fun switchToHome() {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("FRAGMENT_TO_LOAD", "home")
        }
        // Comenzar la actividad.
        startActivity(homeIntent)
        // Finalizar la actividad actual.
        finish() // Para evitar que el usuario regrese a la actividad anterior después de pulsar el botón de retroceso.
    }


    /**
     * Método para cambiar la pestaña a la principal (HomeAdmin) del administrador
     */
    private fun switchToHomeAdmin() {
        val homeIntent: Intent = Intent(this, MainActivityAdmin::class.java).apply {
            putExtra("FRAGMENT_TO_LOAD", Constants.ADD_FRAGMENT)
        }
        // Comenzar la actividad.
        startActivity(homeIntent)
        // Finalizar la actividad actual.
        finish() // Para evitar que el usuario regrese a la actividad anterior después de pulsar el botón de retroceso.
    }

    /**
     * Método para cambiar la pestaña de Register
     */
    private fun switchToRegister() {
        val registerIntent: Intent = Intent(this, RegisterActivity::class.java).apply {}
        // Comenzar la actividad.
        startActivity(registerIntent)
        // Finalizar la actividad actual.
        finish() // Para evitar que el usuario regrese a la actividad anterior después de pulsar el botón de retroceso.
    }

}