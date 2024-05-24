package es.dam.paperwings.view.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import es.dam.paperwings.R

/**
 * A simple [Fragment] subclass.
 * Use the [DeleteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DeleteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_delete, container, false)
    }

}