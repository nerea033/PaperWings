package es.dam.paperwings.view.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import es.dam.paperwings.R
import es.dam.paperwings.model.api.ApiServiceFactory
import es.dam.paperwings.model.constans.Constants
import es.dam.paperwings.model.entities.User
import es.dam.paperwings.model.repositories.RepositoryImpl
import kotlinx.coroutines.launch

/**
 * The `RegisterActivity` class allows users to register by providing username, email, and password.
 * It handles registration using Firebase Authentication and saves user data to the backend server
 * via an API call. The activity includes validation checks for input fields and manages UI interactions
 * such as navigation to login or home screens.
 */
class RegisterActivity : AppCompatActivity() {

    // Declare UI elements
    private lateinit  var tbUsername: EditText
    private lateinit var tbMail: EditText
    private lateinit var tbPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var linkToLogin: TextView
    private lateinit var ivInformation: ImageView

    // Instance of the repository for data operations
    private val repository = RepositoryImpl()

    /**
     * Initializes the activity, sets up UI elements, enables edge-to-edge display,
     * and sets listeners for UI interactions such as registration button click and link to login.
     *
     * @param savedInstanceState Previously saved instance state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        tbUsername = findViewById(R.id.tbUsernameRegister)
        tbMail = findViewById(R.id.tbMailRegister)
        tbUsername = findViewById(R.id.tbUsernameRegister)
        tbPassword = findViewById(R.id.tbPswRegister)
        btnRegister = findViewById(R.id.btnRegister)
        linkToLogin = findViewById(R.id.linkToLogin)
        ivInformation = findViewById(R.id.ivInformation)

        // Set up user registration functionality
        registerUser()

        // Set up click listener for "already have an account?" link
        linkToLogin.setOnClickListener {
            switchToLogin()
        }
        // Set up click listener for information icon
        ivInformation.setOnClickListener{
            repository.showAlert(this@RegisterActivity, "Información", "Escribe tu nombre")
        }

    }

    /**
     * Registers a user by validating input fields, registering in Firebase Authentication,
     * and adding user data to the database via API call.
     */
    private fun registerUser() {

        var userId: String

        btnRegister.setOnClickListener {
            val username: String = tbUsername.text.toString()
            val mail: String = tbMail.text.toString()
            val password: String = tbPassword.text.toString()

            if (mail.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                if (password.length >= 6 && username.length <= 50) { // Check string length constraints
                    if (isValidEmail(mail)) { // Validate email format

                        registerFirebase(mail, password) { uid ->
                            if (uid != null) {
                                // Successful registration
                                userId = uid.toString()

                                // Pass Firebase Auth UID and username to API for database insertion
                                lifecycleScope.launch {
                                    addUserToDatabase(userId, username)
                                    repository.saveUserToSharedPreferences(this@RegisterActivity, username, userId, mail, "USER")
                                    switchToHome() // Navigate to home screen after user is added to database
                                    print("Registro del usuario exitoso.")
                                }

                            } else {
                                // Show error message
                                print("Error al registrar el usuario.")
                            }
                        }
                    } else {
                        // Show error message
                        repository.showAlert(this@RegisterActivity, "Error","El correo electrónico no es válido.")
                    }
                } else {
                    // Show error message
                    repository.showAlert(this@RegisterActivity, "Error","La contraseña debe tener al menos 6 caracteres.")
                }
            } else {
                // Mostrar mensaje de error
                repository.showAlert(this@RegisterActivity, "Error","Por favor, completa todos los campos.")
            }
        }
    }

    /**
     * Registers the user in Firebase Authentication.
     *
     * @param mail User's email address.
     * @param password User's chosen password.
     * @param onComplete Callback function with UID as parameter upon completion.
     */
    private fun registerFirebase(mail: String, password: String, onComplete: (String?) -> Unit) {
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(mail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    val uid = firebaseUser?.uid
                    onComplete(uid) // Callback with UID
                } else {
                    // If there is an error, callback with null
                    onComplete(null)
                    // Error message
                    print("Error al registrar el usuario en Fireabase")
                }
            }
    }

    /**
     * Adds the user to the backend database via API call.
     *
     * @param uid User's Firebase Auth UID.
     * @param username User's chosen username.
     */
    suspend fun addUserToDatabase(uid: String, username: String) {
        val user = User(uid, username, "USER")  // Create user object
        val userService = ApiServiceFactory.makeUsersService()  // Get API service instance

        try {
            val response = userService.addUser(user)  // Make API call
            if (response.isSuccessful) {
                // User added successfully
                print("Usuario creado con éxito en ddbb. Nombre del usuario: ${user.name}")

            } else {
                // Failed to add user, handle error
                val errorResponse = response.errorBody()?.string()
                print("Error al crear el usuario: $errorResponse")
            }
        } catch (e: Exception) {
            // Handle exceptions, such as network issues or configuration problems
            print("Error al conectar con la API: ${e.message}")
        }
    }


    /**
     * Validates an email address using regex.
     *
     * @param email Email address to validate.
     * @return `true` if the email is valid, `false` otherwise.
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        return email.matches(emailRegex.toRegex())
    }


    /**
     * Switches the activity to the home screen.
     */
    private fun switchToHome() {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            // Pass the fragment to be displayed to the activity
            putExtra("FRAGMENT_TO_LOAD", Constants.HOME_FRAGMENT)
        }
        // Start the activity
        startActivity(homeIntent)
        // Finish the current activity
        finish() // To prevent the user from returning to the previous activity after pressing the back button
    }

    /**
     * Switches the activity to the login screen.
     */
    private fun switchToLogin() {
        val loginIntent: Intent = Intent(this, LoginActivity::class.java).apply {}
        // Start the activity
        startActivity(loginIntent)
        // Finish the current activity
        finish() // To prevent the user from returning to the previous activity after pressing the back button
    }
}