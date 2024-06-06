package es.dam.paperwings.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import es.dam.paperwings.R
import es.dam.paperwings.model.api.ApiServiceFactory
import es.dam.paperwings.model.api.UpdateUserRequest
import es.dam.paperwings.model.entities.User
import es.dam.paperwings.view.activities.LoginActivity

class ProfileFragment : Fragment() {

    private var tvUsername: TextView? = null
    private var tvMail: TextView? = null
    private var btnLogout: Button? = null

    private lateinit var auth: FirebaseAuth

    private var username: String? = null
    private var mail: String? = null
    private var uid: String? = null
    private var rol: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Inicializar los componentes de la vista
        tvUsername = view.findViewById(R.id.tvNameProfile)
        tvMail = view.findViewById(R.id.tvMailProfile)
        btnLogout = view.findViewById(R.id.btnLogoutProfile)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Recuperar datos de SharedPreferences
        val sharedPref = activity?.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        username = sharedPref?.getString("username", "N/A") // volver a obtenerlo después de actualizarlo
        mail = sharedPref?.getString("mail", "N/A")
        uid = sharedPref?.getString("uid", "N/A")
        rol = sharedPref?.getString("rol", "N/A")


        showProfile(username, mail)

        // Agregar Listener al botón de logout
        btnLogout?.setOnClickListener {
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Cerrar sesión")
                setMessage("¿Seguro que deseas cerrar sesión?")
                setPositiveButton("Sí") { _, _ ->
                    logout()
                }
                setNegativeButton("No", null)
                create()
                show()
            }
        }

        return view
    }

    private fun showProfile(username: String?, mail: String?) {
        tvUsername?.text = "Hola, ${username}"
        tvMail?.text = mail
    }

    private fun logout() {
        auth.signOut()

        // Verificar si el usuario se ha desconectado de Firebase
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // El usuario se ha desconectado correctamente de Firebase
            println("usuario se ha desconectado correctamente de Firebase")
            switchToLogin()
        } else {
            // El usuario todavía está autenticado en Firebase, el cierre de sesión no fue efectivo
        }
    }

    // Editar el username del usuario registrado
    suspend fun updateUserApi(){
        val userService = ApiServiceFactory.makeUsersService()
        val id = uid
        val idField = "uid"
        val nombre = "Usuario" // Cambiarlo por el que introduzca el user
        var user = uid?.let { rol?.let { it1 -> User(it, nombre, it1) } }

        // Create UpdateRequest instance
        val updateRequest =
                UpdateUserRequest(idField, id, user)

        try {
            val response = userService.updateUser(updateRequest)
            if (response != null) {
                if (response.isSuccessful) {
                    // Usuario editado con éxito
                    //Cambiarlo en el sharedPreferences


                } else {
                    // Fallo al agregar el lirbo, manejar error
                    val errorResponse = response.errorBody()?.string()
                    //mostrar error
                }
            }
        }  catch (e: Exception) {
            // Manejar excepciones, como problemas de red o configuración
            //mostrar error
        }
    }


    /**
     * Método para cambiar la pestaña de Login
     */
    private fun switchToLogin() {
        val loginIntent = Intent(requireActivity(), LoginActivity::class.java)
        // Comenzar la actividad.
        startActivity(loginIntent)
        // Finalizar la actividad actual.
        requireActivity().finish() // Para evitar que el usuario regrese a la actividad anterior después de pulsar el botón de retroceso.
    }
}
