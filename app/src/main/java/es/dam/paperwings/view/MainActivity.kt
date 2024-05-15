package es.dam.paperwings.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import es.dam.paperwings.R
import es.dam.paperwings.view.fragments.CardFragment
import es.dam.paperwings.view.fragments.HomeFragment
import es.dam.paperwings.view.fragments.MenuFragment
import es.dam.paperwings.view.fragments.ProfileFragment
import es.dam.paperwings.view.fragments.SearchFragment

class MainActivity : AppCompatActivity() {

    //Inicializar el bottom navigation view
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recuperar el username del intent (del login o register)
        val username = intent.getStringExtra("username")

        if (username != null) {
            // Mostrar el nombre del usuario
        }

        bottomNavigationView = findViewById(R.id.botton_navigation)

        // Transiciones entre fragments
        bottomNavigationView.setOnItemSelectedListener {menuItem ->
            when (menuItem.itemId){
                R.id.bottom_start -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.bottom_category -> {
                    replaceFragment(SearchFragment())
                    true
                }
                R.id.bottom_cart -> {
                    replaceFragment(MenuFragment())
                    true
                }
                R.id.bottom_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }

        }

        // Fragmento inicial
        replaceFragment(HomeFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }
}