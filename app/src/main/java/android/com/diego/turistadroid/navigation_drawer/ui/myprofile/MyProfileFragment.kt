package android.com.diego.turistadroid.navigation_drawer.ui.myprofile

import android.com.diego.turistadroid.R
import android.com.diego.turistadroid.login.LogInActivity
import android.com.diego.turistadroid.utilities.Utilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_gallery.*


class MyProfileFragment : Fragment() {

    private lateinit var myProfileViewModel: MyProfileViewModel
    private val user = LogInActivity.user

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myProfileViewModel =
            ViewModelProviders.of(this).get(MyProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        //val textView: TextView = root.findViewById(R.string.myProfileTitle)

        val imaProfile: ImageView = root.findViewById(R.id.imaProfile)
        val txtNameProfile: TextView = root.findViewById(R.id.txtNameProfile)
        val txtNameUserProfile: TextView = root.findViewById(R.id.txtNameUserProfile)
        val txtEmailProfile: TextView = root.findViewById(R.id.txtEmailProfile)
        val txtPassProfile: TextView = root.findViewById(R.id.txtPassProfile)

        imaProfile.setImageBitmap(Utilities.base64ToBitmap(user.foto))
        Utilities.redondearFoto(imaProfile)
        txtNameProfile.text = user.nombre
        txtNameUserProfile.text = user.nombreUser
        txtEmailProfile.text = user.email

        myProfileViewModel.text.observe(viewLifecycleOwner, Observer {
            //textView.text = it
        })
        return root
    }

}