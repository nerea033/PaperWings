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
import com.google.firebase.auth.FirebaseAuth
import es.dam.paperwings.R
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
     * Método para registrar un usuario
     */
    fun registerUser() {
        btnRegister.setOnClickListener {

            if (tbMail.text.isNotEmpty() && tbPassword.text.isNotEmpty()){ // Si están todos los campos rellenos

                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(tbMail.text.toString(), tbPassword.text.toString())
                    .addOnCompleteListener{ // Verificar si se completa

                        if (it.isSuccessful){
                            // Ir a la pantalla principal del usuario sin privilegios
                            switchToHome()
                        } else {
                            // Mensaje de error
                            showAlert()
                        }
                    }

            }
        }
    }

    /**
     * Método para mostrar mensaje de error
     */
    fun showAlert(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Correo o contraseña no válidos")
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