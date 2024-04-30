package es.dam.paperwings.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import es.dam.paperwings.R

class Home : AppCompatActivity() {

    private lateinit var borrarBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homeActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        borrarBtn = findViewById(R.id.button)
        borrarBtn.setOnClickListener {
            // Abre Activity2 al hacer clic en el botón
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }


    }
}