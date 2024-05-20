package es.dam.paperwings.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import es.dam.paperwings.R
import es.dam.paperwings.view.fragments.HomeFragment

class BookDetailActivity : AppCompatActivity() {

    private var ivCover: ImageView? = null
    private var tvTitle: TextView? = null
    private var tvAuthor: TextView? = null
    private var tvPrice: TextView? = null
    private var tvPages: TextView? = null
    private var tvLanguage: TextView? = null
    private var tvPublisher: TextView? = null
    private var tvDate: TextView? = null
    private var tvIsbn: TextView? = null
    private var tvCategory: TextView? = null
    private var tvDescription: TextView? = null
    private var btnAddCart: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtengo el id_book de HomeFragment como Intent y el uid de sharedPreferences

        val ibGoBack: ImageButton = findViewById(R.id.ibGoBack)

        ibGoBack.setOnClickListener {
            switchToHome()
        }

    }


    private fun switchToHome() {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("FRAGMENT_TO_LOAD", "profile")
        }
        // Comenzar la actividad.
        startActivity(homeIntent)
        // Finalizar la actividad actual.
        finish() // Para evitar que el usuario regrese a la actividad anterior después de pulsar el botón de retroceso.
    }
}