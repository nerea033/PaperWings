package es.dam.paperwings.view

import ProfileFragment
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import es.dam.paperwings.R
import es.dam.paperwings.view.fragments.AddFragment
import es.dam.paperwings.view.fragments.DeleteFragment
import es.dam.paperwings.view.fragments.UpdateFragment

class MainActivityAdmin : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_admin)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recuperar el username del intent (del login o register)
        val username = intent.getStringExtra("username")

        if (username != null) {
            // Mostrar el nombre del usuario
        }

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
                R.id.bottom_delete -> {
                    replaceFragment(DeleteFragment())
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
            "home" -> {
                replaceFragment(AddFragment())
                selectBottomNavItem(R.id.bottom_add)
            }
            "search" -> {
                replaceFragment(UpdateFragment())
                selectBottomNavItem(R.id.bottom_update)
            }
            "menu" -> {
                replaceFragment(DeleteFragment())
                selectBottomNavItem(R.id.bottom_delete)
            }
            "profile" -> {
                replaceFragment(ProfileFragment())
                selectBottomNavItem(R.id.bottom_profile)
            }
            else -> {
                replaceFragment(AddFragment())
                selectBottomNavItem(R.id.bottom_add)
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