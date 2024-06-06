package es.dam.paperwings.model.repositories

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

class RepositoryImpl: Repository {
    private var backPressedOnce = false
    private val backPressHandler = Handler(Looper.getMainLooper())

    /**
     * Persitir los datos localmente para poder acceder a ellos
     */
    override fun saveUserToSharedPreferences(context: Context, username: String, uid: String, mail: String, rol: String) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("username", username)
            putString("uid", uid)
            putString("mail", mail)
            putString("rol", rol)
            apply()
        }
    }

    /**
     * Método para mostrar mensaje en actividades
     */
    override fun showAlert(activity: Activity, title: String, message: String) {
        if (!activity.isFinishing) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("Aceptar", null)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    /**
     * Método para mostrar mensaje en fragmentos
     */
    override fun showAlert(fragment: Fragment, title: String, message: String) {
        fragment.context?.let { context ->
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Aceptar", null)

            val dialog = builder.create()
            dialog.show()
        }
    }

    /**
     * Método para volver a la actividad anterior
     */
    override fun showAlert(activity: Activity, title: String, message: String, switchToPrevious: Boolean) {
        if (!activity.isFinishing) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("Aceptar") { _, _ ->
                if (switchToPrevious) {
                    // Termina la actividad actual para regresar a la anterior
                    activity.finish()
                }
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    /**
     * Método para mostrar diálogos con opción de aceptar y cancelar en actividades
     */
    override fun showAlertOkCancel(activity: Activity, title: String, message: String, showCancel: Boolean, onResult: ((Boolean) -> Unit)?) {
        // Verificar si la actividad no está en proceso de finalización
        if (!activity.isFinishing) {
            // Usar el contexto de la actividad para construir el AlertDialog
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("Aceptar") { dialog, which ->
                onResult?.invoke(true)
            }
            if (showCancel) {
                builder.setNegativeButton("Cancelar") { dialog, which ->
                    onResult?.invoke(false)
                }
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    /**
     * Método para mostrar diálogos con opción de aceptar y cancelar en fragmentos
     */
    override fun showAlertOkCancel(fragment: Fragment, title: String, message: String, showCancel: Boolean, onResult: ((Boolean) -> Unit)?) {
        fragment.context?.let { context ->
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Aceptar") { dialog, which ->
                    onResult?.invoke(true)
                }

            if (showCancel) {
                builder.setNegativeButton("Cancelar") { dialog, which ->
                    onResult?.invoke(false)
                }
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Método para volver a la actividad anterior
     */
    override fun switchToPrevious(dispatcher: OnBackPressedDispatcher) {
        // Simula la acción de presionar el botón de retroceso utilizando onBackPressedDispatcher.
        dispatcher.onBackPressed()
    }

    /**
     * Método para manejar el doble clic en el botón de retroceso
     */
    override fun handleDoubleBackPress(activity: Activity, lifecycleOwner: LifecycleOwner) {
        val onBackPressedDispatcher = (activity as? AppCompatActivity)?.onBackPressedDispatcher
        onBackPressedDispatcher?.addCallback(lifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedOnce) {
                    activity.finish()
                } else {
                    backPressedOnce = true
                    Toast.makeText(activity, "Haga click de nuevo para salir", Toast.LENGTH_SHORT).show()
                    backPressHandler.postDelayed({ backPressedOnce = false }, 2000)
                }
            }
        })
    }
}