package es.dam.paperwings.view.activities

import android.content.Intent
import es.dam.paperwings.view.fragments.ProfileFragment
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import es.dam.paperwings.R
import es.dam.paperwings.model.constans.Constants
import es.dam.paperwings.view.fragments.AddFragment
import es.dam.paperwings.view.fragments.UpdateFragment

class MainActivityAdmin : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var auth: FirebaseAuth

    private var rol: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_admin)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()


        // Recuperar datos de SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val uid = sharedPref.getString("uid", "N/A")
        rol = sharedPref.getString("rol", "N/A")
        val username = sharedPref.getString("username", "N/A")
        val mail = sharedPref.getString("mail", "N/A")

        // Verificar la autenticación del usuario
        checkUserAuthentication()

        bottomNavigationView = findViewById(R.id.botton_navigation_admin)

        // Transiciones entre fragments
        bottomNavigationView.setOnItemSelectedListener {menuItem ->
            when (menuItem.itemId){
                R.id.bottom_add -> {
                    replaceFragment(AddFragment())
                    true
                }
                R.id.bottom_update -> {
                    replaceFragment(UpdateFragment())
                    true
                }
                R.id.bottom_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }

        }

        // Verifica el fragmento que debe ser mostrado inicialmente
        val fragmentToLoad = intent.getStringExtra("FRAGMENT_TO_LOAD")
        when (fragmentToLoad) {
            Constants.ADD_FRAGMENT -> {
                replaceFragment(AddFragment())
                selectBottomNavItem(R.id.bottom_add)
            }
            Constants.UPDATE_FRAGMENT -> {
                replaceFragment(UpdateFragment())
                selectBottomNavItem(R.id.bottom_update)
            }
            Constants.PROFILE_FRAGMENT -> {
                replaceFragment(ProfileFragment())
                selectBottomNavItem(R.id.bottom_profile)
            }
            else -> {
                replaceFragment(AddFragment())
                selectBottomNavItem(R.id.bottom_add)
            }
        }
    }

    // Verificar si ha iniciado sesión
    private fun checkUserAuthentication() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Usuario no autenticado, redirigir a la pantalla de login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Usuario autenticado, verificar el rol
            if (rol == "USER") {
                // Si es USER, redirigir a MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Lógica adicional
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container_admin, fragment).commit()
    }

    private fun selectBottomNavItem(itemId: Int) {
        bottomNavigationView.selectedItemId = itemId
    }
}