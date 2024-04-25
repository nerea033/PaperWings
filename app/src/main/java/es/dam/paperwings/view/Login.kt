package es.dam.paperwings.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import es.dam.paperwings.R

class Login : AppCompatActivity() {

    // Declaro los elementos de mi vista
    private lateinit var tbUsername: EditText
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializo los elementos de la vista
        tbUsername = findViewById(R.id.tbUsernameLogin)
        tbPassword = findViewById(R.id.tbPswLogin)
        btnLogin = findViewById(R.id.btnLogin)
        linkRegister = findViewById(R.id.linkRegister)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnFacebook = findViewById(R.id.btnFacebook)
        btnTwitter = findViewById(R.id.btnTwitter)


    }

}