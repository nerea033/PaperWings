package es.dam.paperwings.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
import kotlinx.coroutines.launch

class Login : AppCompatActivity() {

    // Declaro los elementos de mi vista
    private lateinit var tbMail: EditText
    private lateinit var tbPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var linkRegister: TextView
    private var btnGoogle: ImageButton? = null
    private var btnFacebook: ImageButton? = null
    private var btnTwitter: ImageButton? = null

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
        btnGoogle = findViewById(R.id.btnGoogle)
        btnFacebook = findViewById(R.id.btnFacebook)
        btnTwitter = findViewById(R.id.btnTwitter)

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
                                    saveUserToSharedPreferences(usernameResult, userId, mail)
                                    switchToHome()
                                } else if (rolResult == "ADMIN"){
                                    // Si el rol es "admin", cambiar a la actividad principal del administrador pasando el username
                                    saveUserToSharedPreferences(usernameResult, userId, mail)
                                    switchToHomeAdmin()
                                }
                            }
                        }

                    } else {
                        // Mostrar mensaje de error
                        showAlert("Error","Error en Firebase")
                    }
                }
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
                    println("No se encontró el usuario con uid: $uid")
                }
            } else {
                println("Error al obtener los datos del usuario: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            println("Error en la red o al parsear los datos: ${e.message}")
        }
        // Devuelvo una pareja (Pair) con el nombre de usuario y el rol
        return if (username.isNotEmpty() && rol.isNotEmpty()) {
            Pair(username, rol)
        } else {
            null
        }
    }

    /**
     * Persitir los datos localmente para poder acceder a ellos
     */
    private fun saveUserToSharedPreferences(username: String, uid: String, mail: String){
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("username", username)
            putString("uid", uid)
            putString("mail", mail)
            apply()
        }
    }


    /**
     * Método para mostrar mensaje
     */
    fun showAlert(title: String, message: String) {
        if (!isFinishing) { // Verificar si la actividad no está en proceso de finalización
            val builder = AlertDialog.Builder(this)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("Aceptar", null)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    /**
     * Método para cambiar la pestaña a la principal (Home)
     */
    private fun switchToHome() {
        val homeIntent = Intent(this, MainActivity::class.java).apply {}
        // Comenzar la actividad.
        startActivity(homeIntent)
        // Finalizar la actividad actual.
        finish() // Para evitar que el usuario regrese a la actividad anterior después de pulsar el botón de retroceso.
    }


    /**
     * Método para cambiar la pestaña a la principal (HomeAdmin) del administrador
     */
    private fun switchToHomeAdmin() {
        val homeIntent: Intent = Intent(this, MainActivityAdmin::class.java).apply {}
        // Comenzar la actividad.
        startActivity(homeIntent)
        // Finalizar la actividad actual.
        finish() // Para evitar que el usuario regrese a la actividad anterior después de pulsar el botón de retroceso.
    }

    /**
     * Método para cambiar la pestaña de Register
     */
    private fun switchToRegister() {
        val registerIntent: Intent = Intent(this, Register::class.java).apply {}
        // Comenzar la actividad.
        startActivity(registerIntent)
        // Finalizar la actividad actual.
        finish() // Para evitar que el usuario regrese a la actividad anterior después de pulsar el botón de retroceso.
    }

}