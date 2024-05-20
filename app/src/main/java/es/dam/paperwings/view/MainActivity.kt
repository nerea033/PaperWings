package es.dam.paperwings.view

import ProfileFragment
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import es.dam.paperwings.R
import es.dam.paperwings.view.fragments.CardFragment
import es.dam.paperwings.view.fragments.HomeFragment
import es.dam.paperwings.view.fragments.MenuFragment
import es.dam.paperwings.view.fragments.SearchFragment

class MainActivity : AppCompatActivity() {

    //Inicializar el bottom navigation view
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Verificar la autenticación del usuario
        checkUserAuthentication()


        // Recuperar datos de SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val uid = sharedPref.getString("uid", "N/A")
        val username = sharedPref.getString("username", "N/A")
        val mail = sharedPref.getString("mail", "N/A")

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
                    val profileFragment = ProfileFragment()
                    replaceFragment(profileFragment)
                    true
                }
                else -> false
            }

        }

        // Verifica el fragmento que debe ser mostrado inicialmente
        val fragmentToLoad = intent.getStringExtra("FRAGMENT_TO_LOAD")
        when (fragmentToLoad) {
            "home" -> {
                replaceFragment(HomeFragment())
                selectBottomNavItem(R.id.bottom_start)
            }
            "search" -> {
                replaceFragment(SearchFragment())
                selectBottomNavItem(R.id.bottom_category)
            }
            "menu" -> {
                replaceFragment(MenuFragment())
                selectBottomNavItem(R.id.bottom_cart)
            }
            "profile" -> {
                replaceFragment(ProfileFragment())
                selectBottomNavItem(R.id.bottom_profile)
            }
            else -> {
                replaceFragment(HomeFragment())
                selectBottomNavItem(R.id.bottom_start)
            }
        }

    }

    // Verificar si ha iniciado sesión
    private fun checkUserAuthentication() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Usuario no autenticado, redirigir a la pantalla de login
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }

    private fun selectBottomNavItem(itemId: Int) {
        bottomNavigationView.selectedItemId = itemId
    }
}