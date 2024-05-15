package es.dam.paperwings.view

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
import es.dam.paperwings.model.entities.User
import kotlinx.coroutines.launch

class Register : AppCompatActivity() {

    // Declaro los elementos de mi interfaz
    private lateinit var tbUsername: EditText
    private lateinit var tbMail: EditText
    private lateinit var tbPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var linkToLogin: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializo los elementos que he declarado al inicio de la clase
        tbUsername = findViewById(R.id.tbUsernameRegister)
        tbMail = findViewById(R.id.tbMailRegister)
        tbUsername = findViewById(R.id.tbUsernameRegister)
        tbPassword = findViewById(R.id.tbPswRegister)
        btnRegister = findViewById(R.id.btnRegister)
        linkToLogin = findViewById(R.id.linkToLogin)

        registerUser()

        linkToLogin.setOnClickListener {
            switchToLogin()
        }

    }

    /**
     * Método para registrar un usuario en Base de datos y Firebase
     */
    fun registerUser() {

        var userId: String = ""

        btnRegister.setOnClickListener {
            val username: String = tbUsername.text.toString()
            val mail: String = tbMail.text.toString()
            val password: String = tbPassword.text.toString()

            if (mail.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) { // Si están todos los campos rellenos
                if (password.length >= 6 && username.length <= 50) { // Comprobar los caracteres de los String
                    if (isValidEmail(mail)) { // Comprobar si el mail es válido

                        registerFirebase(mail, password) { uid ->
                            if (uid != null) {
                                // Registro exitoso
                                userId = uid.toString()

                                // Le paso el uid del Firebase Authentication junto al username a la API para que lo inserte en la DDBB
                                lifecycleScope.launch {
                                    addUserToDatabase(userId, username)
                                    switchToHome(username)  // Llamada a switchToHome después de agregar al usuario a la base de datos
                                }

                            } else {
                                // Mostrar mensaje de error
                                showAlert("Error","Error al registrar el usuario.")
                            }
                        }
                    } else {
                        // Mostrar mensaje de error
                        showAlert("Error","El correo electrónico no es válido.")
                    }
                } else {
                    // Mostrar mensaje de error
                    showAlert("Error","La contraseña debe tener entre 6 al menos 5 caracteres.")
                }
            } else {
                // Mostrar mensaje de error
                showAlert("Error","Por favor, completa todos los campos.")
            }
        }
    }

    /**
     * Método para registrar al usuario en Firebase Authentication
     */
    fun registerFirebase(mail: String, password: String, onComplete: (String?) -> Unit) {
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(mail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    val uid = firebaseUser?.uid
                    onComplete(uid) // Llamar al callback con el UID
                } else {
                    // Si hay un error, llamar al callback con null
                    onComplete(null)
                    // Mensaje de error
                    showAlert("Error","Error al registrar el usuario")
                }
            }
    }


    /**
     * Método para pasar el usuario a la API y de esta a la DDBB
     */
    suspend fun addUserToDatabase(uid: String, username: String) {
        val user = User(uid, username)  // Crear el objeto usuario
        val userService = ApiServiceFactory.makeUsersService()  // Obtener la instancia del servicio API

        try {
            val response = userService.addUser(user)  // Hacer la llamada API
            if (response.isSuccessful) {
                // Usuario agregado con éxito
                    showAlert("Información","Usuario creado con éxito. Nombre del usuario: ${user.name}")

            } else {
                // Fallo al agregar el usuario, manejar error
                val errorResponse = response.errorBody()?.string()
                showAlert("Error","Error al crear el usuario: $errorResponse")
            }
        } catch (e: Exception) {
            // Manejar excepciones, como problemas de red o configuración
            showAlert("Error","Error al conectar con la API: ${e.message}")
        }
    }


    /**
     * Función 'regex' para validar un correo electrónico.
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        return email.matches(emailRegex.toRegex())
    }


    /**
     * Método para mostrar mensaje
     */
    fun showAlert(title: String, message: String) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /**
     * Método para cambiar la pestaña a la principal (Home)
     */
    private fun switchToHome(username: String) {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            // Pasar el username a la nueva actividad
            putExtra("username", username)
        }
        // Comenzar la actividad.
        startActivity(homeIntent)
        // Finalizar la actividad actual.
        finish() // Para evitar que el usuario regrese a la actividad anterior después de pulsar el botón de retroceso.
    }

    /**
     * Método para cambiar la pestaña de Login
     */
    private fun switchToLogin() {
        val loginIntent: Intent = Intent(this, Login::class.java).apply {}
        // Comenzar la actividad.
        startActivity(loginIntent)
        // Finalizar la actividad actual.
        finish() // Para evitar que el usuario regrese a la actividad anterior después de pulsar el botón de retroceso.
    }


}