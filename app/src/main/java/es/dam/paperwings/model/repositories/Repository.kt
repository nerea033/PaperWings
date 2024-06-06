package es.dam.paperwings.model.repositories

import android.app.Activity
import android.content.Context
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

interface Repository {
    fun saveUserToSharedPreferences(context: Context, username: String, uid: String, mail: String, rol: String)
    fun showAlert(activity: Activity, title: String, message: String)
    fun showAlert(fragment: Fragment, title: String, message: String)
    fun showAlert(activity: Activity, title: String, message: String, switchToPrevious: Boolean = false)
    fun showAlertOkCancel(activity: Activity, title: String, message: String, showCancel: Boolean = false, onResult: ((Boolean) -> Unit)? = null)
    fun showAlertOkCancel(fragment: Fragment, title: String, message: String, showCancel: Boolean = false, onResult: ((Boolean) -> Unit)? = null)
    fun showToast(context: Context, message: String)
    fun switchToPrevious(dispatcher: OnBackPressedDispatcher)
    fun handleDoubleBackPress(activity: Activity, lifecycleOwner: LifecycleOwner)
}