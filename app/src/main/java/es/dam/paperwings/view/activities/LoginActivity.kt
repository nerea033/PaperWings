package es.dam.paperwings.view.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
import es.dam.paperwings.model.repositories.RepositoryImpl
import kotlinx.coroutines.launch

/**
 * Activity class for user login functionality.
 * Implements user authentication via Firebase and validates user credentials
 * against a database API. Handles navigation based on user roles.
 */
class LoginActivity : AppCompatActivity() {

    // View elements
    private lateinit var tbMail: EditText
    private lateinit var tbPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var linkRegister: TextView

    // Repository instance
    private val repository = RepositoryImpl()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize view elements
        tbMail = findViewById(R.id.tbMailLogin)
        tbPassword = findViewById(R.id.tbPswLogin)
        btnLogin = findViewById(R.id.btnLogin)
        linkRegister = findViewById(R.id.linkToRegister)

        // Call login function when Login button is clicked
        loginUser()

        // Navigate to RegisterActivity when Register link is clicked
        linkRegister.setOnClickListener {
            switchToRegister()
        }
    }

    /**
     * Allows the user to log in if already registered.
     * Authenticates user credentials via Firebase authentication
     * and verifies user details against a database.
     */
    private fun loginUser(){
        var userId: String = ""
        var usernameResult : String = ""
        var rolResult : String= ""
        btnLogin.setOnClickListener {
            val mail: String = tbMail.text.toString()
            val password: String = tbPassword.text.toString()

            if (mail.isNotEmpty() && password.isNotEmpty()){
                loginFirebase(mail, password) { uid ->
                    if (uid != null) {
                        // Successful login
                        userId = uid.toString()
                        println("Éxito, usuario encontrado en firebase")
                        // Pass Firebase Authentication UID to API for database verification
                        lifecycleScope.launch {// Lanzar una corrutina
                            val result = loginDataBase(userId)
                            // Check if result is not null
                            result?.let { (username, rol) ->
                                // Assign values to local variables
                                usernameResult = username
                                rolResult = rol

                                if (rolResult == "USER") {
                                    // If role is USER, navigate to main activity passing username
                                    repository.saveUserToSharedPreferences(this@LoginActivity, usernameResult, userId, mail, rolResult)
                                    switchToHome()
                                } else if (rolResult == "ADMIN"){
                                    // If role is ADMIN, navigate to admin main activity passing username
                                    repository.saveUserToSharedPreferences(this@LoginActivity, usernameResult, userId, mail, rolResult)
                                    switchToHomeAdmin()
                                }
                            }
                        }

                    } else {
                        // Show error message
                        repository.showAlert(this@LoginActivity, "Error","Usuario o contraseña incorrectos")
                    }
                }
            } else{
                repository.showAlert(this@LoginActivity, "Error","Por favor, completa todos los campos.")
            }
        }

    }

    /**
     * Performs user authentication using Firebase.
     *
     * @param email User's email address for authentication.
     * @param password User's password for authentication.
     * @param onResult Callback function to return UID on successful authentication.
     */
    private fun loginFirebase(email: String, password: String, onResult: (String?) -> Unit) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // If authentication is successful, get user's UID
                    val uid = firebaseAuth.currentUser?.uid
                    onResult(uid)  // Return UID to caller
                } else {
                    // If authentication fails, handle error
                    onResult(null)
                    println("Authentication failed: ${task.exception?.message}")
                }
            }
    }

    /**
     * Verifies user details against the database using API call.
     *
     * @param uid User's Firebase Authentication UID for database lookup.
     * @return Pair containing username and role if found, null otherwise.
     */
    suspend fun loginDataBase(uid: String): Pair<String, String>?  {
        val userService = ApiServiceFactory.makeUsersService()
        var username: String = ""
        var rol: String= ""
        try {

            // Fetch user by UID via API
            val response = userService.fetchUserByUid(uid)
            if (response.isSuccessful && response.body() != null) {

                // Store user details in list (due to structure)
                val usersList = response.body()!!.data
                if (usersList != null && usersList.isNotEmpty()) {

                    // Get the first and only user from the list
                    val user = usersList.first()

                    // Store username and role of user attempting to log in
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
        // Return a Pair with username and role
        return if (username.isNotEmpty() && rol.isNotEmpty()) {
            Pair(username, rol)
        } else {
            null
        }
    }


    /**
     * Switches to the main tab (Home).
     */
    private fun switchToHome() {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("FRAGMENT_TO_LOAD", "home")
        }
        // Start the activity.
        startActivity(homeIntent)
        // Finish the current activity.
        finish() // To prevent the user from going back to the previous activity after pressing the back button.
    }


    /**
     * Switches to the Register tab.
     */
    private fun switchToHomeAdmin() {
        val homeIntent: Intent = Intent(this, MainActivityAdmin::class.java).apply {
            putExtra("FRAGMENT_TO_LOAD", Constants.ADD_FRAGMENT)
        }
        // Start the activity.
        startActivity(homeIntent)
        // Finish the current activity.
        finish() // To prevent the user from going back to the previous activity after pressing the back button.
    }

    /**
     * Method to switch to the Register tab.
     * Starts the RegisterActivity and finishes the current activity to prevent
     * the user from returning to it after pressing the back button.
     */
    private fun switchToRegister() {
        val registerIntent: Intent = Intent(this, RegisterActivity::class.java).apply {}
        // Start the activity.
        startActivity(registerIntent)
        // Finish the current activity.
        finish()
    }


}