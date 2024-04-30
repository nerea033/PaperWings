package es.dam.paperwings.view

import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import es.dam.paperwings.R
import es.dam.paperwings.controller.ControllerPaperWings
import es.dam.paperwings.model.entities.User
import java.nio.file.attribute.AclEntry.Builder

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
                                showAlert("Registro exitoso. UID del usuario: $uid")
                                userId = uid.toString()

                                // Crear un usuario con el uid de FIREBASE y su nombre
                                val user = User(uid, username) // Usar val aquí asegura que user no es mutable

                                // Instanciar el controlador
                                val controller = ControllerPaperWings()

                                // Pasar el usuario al controlador para insertarlo en la Base de Datos
                                controller.registerUser(user)

                            } else {
                                // Mostrar mensaje de error
                                showAlert("Error al registrar el usuario.")
                            }
                        }
                    } else {
                        // Mostrar mensaje de error
                        showAlert("El correo electrónico no es válido.")
                    }
                } else {
                    // Mostrar mensaje de error
                    showAlert("La contraseña debe tener entre 6 al menos 5 caracteres.")
                }
            } else {
                // Mostrar mensaje de error
                showAlert("Por favor, completa todos los campos.")
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
                    showAlert("Error al registrar el usuario")
                }
            }
    }

    /**
     * Función 'regex' para validar un correo electrónico
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        return email.matches(emailRegex.toRegex())
    }


    /**
     * Método para mostrar mensaje de error
     */
    fun showAlert(message: String) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /**
     * Método para cambiar la pestaña a la principal (Home)
     */
    private fun switchToHome() {
        val homeIntent: Intent = Intent(this, Home::class.java).apply {
            // Si quiero llevar datos hago:
            //putExtra("nombreCampo", campo) *campo es lo que le metería por parámetro al método

        }
        // Comenzar la actividad
        startActivity(homeIntent)
    }


}