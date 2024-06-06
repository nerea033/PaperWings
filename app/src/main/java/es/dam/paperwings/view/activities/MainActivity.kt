package es.dam.paperwings.view.activities

import es.dam.paperwings.view.fragments.ProfileFragment
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
import es.dam.paperwings.model.constans.Constants
import es.dam.paperwings.model.repositories.RepositoryImpl
import es.dam.paperwings.view.fragments.HomeFragment
import es.dam.paperwings.view.fragments.CartFragment
import es.dam.paperwings.view.fragments.CategoryFragment

/**
 * MainActivity class represents the main activity of the application,
 * managing navigation between different fragments: Home, Category, Cart, and Profile.
 * It handles user authentication and role verification using Firebase Auth,
 * and allows navigation via BottomNavigationView.
 */
class MainActivity : AppCompatActivity() {

    // Initialize the BottomNavigationView
    private lateinit var bottomNavigationView: BottomNavigationView

    // Firebase Authentication instance
    private lateinit var auth: FirebaseAuth

    private var rol: String? = null

    // Repository instance for handling data operations
    private val repository = RepositoryImpl()

    /**
     * Method called when creating the activity. Initializes the user interface,
     * sets up Firebase authentication, retrieves user data from SharedPreferences,
     * checks user authentication status, initializes BottomNavigationView,
     * and handles initial fragment loading based on intent extras.
     *
     * @param savedInstanceState Previously saved instance state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle double back press
        repository.handleDoubleBackPress(this, this)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


        // Retrieve data from SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        rol = sharedPref.getString("rol", "N/A")

        // Check user authentication status
        checkUserAuthentication()

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.botton_navigation)

        // Handle fragment transitions
        bottomNavigationView.setOnItemSelectedListener {menuItem ->
            when (menuItem.itemId){
                R.id.bottom_start -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.bottom_category -> {
                    replaceFragment(CategoryFragment())
                    true
                }
                R.id.bottom_cart -> {
                    replaceFragment(CartFragment())
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

        // Determine which fragment to load initially based on intent extras
        val fragmentToLoad = intent.getStringExtra("FRAGMENT_TO_LOAD")
        when (fragmentToLoad) {
            Constants.HOME_FRAGMENT -> {
                replaceFragment(HomeFragment())
                selectBottomNavItem(R.id.bottom_start)
            }
            Constants.CATEGORY_FRAGMENT -> {
                replaceFragment(CategoryFragment())
                selectBottomNavItem(R.id.bottom_category)
            }
            Constants.CART_FRAGMENT -> {
                replaceFragment(CartFragment())
                selectBottomNavItem(R.id.bottom_cart)
            }
            Constants.PROFILE_FRAGMENT -> {
                replaceFragment(ProfileFragment())
                selectBottomNavItem(R.id.bottom_profile)
            }
            else -> {
                replaceFragment(HomeFragment())
                selectBottomNavItem(R.id.bottom_start)
            }
        }

    }

    /**
     * Checks if the user is authenticated. Redirects to LoginActivity if not authenticated.
     * Redirects to MainActivityAdmin if the user role is "ADMIN".
     */
    private fun checkUserAuthentication() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // User not authenticated, redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // User authenticated, check role
            if (rol == "ADMIN") {
                // If role is ADMIN, redirect to MainActivityAdmin
                val intent = Intent(this, MainActivityAdmin::class.java)
                startActivity(intent)
                finish()
            } else {
                // Additional logic for authenticated users
            }
        }
    }

    /**
     * Replaces the current fragment with the provided fragment.
     *
     * @param fragment Fragment to replace the current fragment with.
     */
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }

    /**
     * Sets the selected item in the BottomNavigationView.
     *
     * @param itemId ID of the menu item to select.
     */
    private fun selectBottomNavItem(itemId: Int) {
        bottomNavigationView.selectedItemId = itemId
    }
}