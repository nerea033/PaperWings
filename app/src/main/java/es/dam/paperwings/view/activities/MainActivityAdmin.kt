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
    import es.dam.paperwings.model.repositories.RepositoryImpl
    import es.dam.paperwings.view.fragments.AddFragment
    import es.dam.paperwings.view.fragments.UpdateFragment


    class MainActivityAdmin : AppCompatActivity() {

        // Initialize the BottomNavigationView for admin
        private lateinit var bottomNavigationView: BottomNavigationView

        // Firebase Authentication instance
        private lateinit var auth: FirebaseAuth

        // User role obtained from SharedPreferences
        private var rol: String? = null

        // Repository instance for handling data operations
        private val repository = RepositoryImpl()

        /**
         * The `MainActivityAdmin` class represents the main activity for administrators,
         * managing navigation between different fragments: Add, Update, and Profile.
         * It handles user authentication and role verification using Firebase Auth,
         * and allows navigation via BottomNavigationView.
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_main_admin)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_admin)) { v, insets ->
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

            // Initialize BottomNavigationView for admin
            bottomNavigationView = findViewById(R.id.botton_navigation_admin)

            // Handle fragment transitions
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

            // Determine which fragment to load initially based on intent extras
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

        /**
         * Checks if the user is authenticated. Redirects to LoginActivity if not authenticated.
         * Redirects to MainActivity if the user role is "USER".
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
                if (rol == "USER") {
                    // If role is USER, redirect to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Additional logic for authenticated admin users
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