import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import es.dam.paperwings.R
import es.dam.paperwings.view.Login

class ProfileFragment : Fragment() {

    private var tvUsername: TextView? = null
    private var tvMail: TextView? = null
    private var btnLogout: Button? = null

    private lateinit var auth: FirebaseAuth

    private var username: String? = null
    private var mail: String? = null

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
        val username = sharedPref?.getString("username", "N/A")
        val mail = sharedPref?.getString("mail", "N/A")

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


    /**
     * Método para cambiar la pestaña de Login
     */
    private fun switchToLogin() {
        val loginIntent = Intent(requireActivity(), Login::class.java)
        // Comenzar la actividad.
        startActivity(loginIntent)
        // Finalizar la actividad actual.
        requireActivity().finish() // Para evitar que el usuario regrese a la actividad anterior después de pulsar el botón de retroceso.
    }
}
