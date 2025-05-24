package com.version2.tfgpedrollompart2

import android.content.ContentValues.TAG
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import com.version2.tfgpedrollompart2.databinding.FragmentChangeIconBinding


class changeIconFragment : Fragment() {

    private var _binding: FragmentChangeIconBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var basicIconLine: LinearLayout
    private lateinit var gamerIconLine: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChangeIconBinding.inflate(inflater, container, false)
        val root = binding.root

        val sh: SharedPreferences =requireActivity().getSharedPreferences("MYPREFERENCES",
            MODE_PRIVATE
        )




        val userP=sh.getString("name","")

        val profileName:TextView=root.findViewById(R.id.profileName)
        val profilePicture:ImageView=root.findViewById(R.id.profileImage)
        val profileP=sh.getString("profile","")
        val editor=sh.edit()
        val dogImage:ImageView=root.findViewById(R.id.dogImage)
        val lionImage:ImageView=root.findViewById(R.id.lionImage)
        val catImage:ImageView=root.findViewById(R.id.catImage)
        val sonyImage:ImageView=root.findViewById(R.id.sonyImage)
        val nintendoImage:ImageView=root.findViewById(R.id.nintendoImage)
        val arrowbackbtn:ImageButton=root.findViewById(R.id.arrow_back)

        profileName.text=userP
        Picasso.get()
            .load(profileP)
            .into(profilePicture)

        arrowbackbtn.setOnClickListener(View.OnClickListener {

            requireActivity().supportFragmentManager.popBackStack()

        })

        dogImage.setOnClickListener(View.OnClickListener {

            takeIcon("perro",userP,editor,profilePicture)

        })
        lionImage.setOnClickListener(View.OnClickListener {

            takeIcon("leon",userP,editor,profilePicture)

        })
        catImage.setOnClickListener(View.OnClickListener {

            takeIcon("gato",userP,editor,profilePicture)

        })
        sonyImage.setOnClickListener(View.OnClickListener {

            takeIcon("sony",userP,editor,profilePicture)

        })
        nintendoImage.setOnClickListener(View.OnClickListener {

            takeIcon("switch",userP,editor,profilePicture)

        })



    return root
    }

    private fun takeIcon(
        name: String,
        userP: String?,
        editor: SharedPreferences.Editor,
        profilePicture: ImageView
    ) {

        val db = Firebase.firestore

        db.collection("iconos")
            .whereEqualTo("nombre",name)
            .get()
            .addOnSuccessListener { result ->
                if(result.isEmpty){
                    Log.d("DEBUG", "vacio ")
                }
                for (document in result) {

                    val url=document.getString("url")
                    editor.putString("profile",url)
                    editor.commit()
                 takeUserId(userP,url)

                    Picasso.get()
                        .load(url)
                        .into(profilePicture)

                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }


    }

    private fun takeUserId(userP: String?, url: String?) {

        val db = Firebase.firestore

        db.collection("usuarios")
            .whereEqualTo("usuario",userP)
            .get()
            .addOnSuccessListener { result ->
                if(result.isEmpty){
                    Log.d("DEBUG", "vacio ")
                }
                for (document in result) {



                    updateProfile(userP,url,document.id)

                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }


    }

    private fun updateProfile(
        userP: String?,
        url: String?,
        id: String
    ) {

        val db = Firebase.firestore

        db.collection("usuarios")
            .document(id)
            .update("url",url)
            .addOnSuccessListener { result ->

                Toast.makeText(requireContext(),"Perfil actualizado",Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }


    }


    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            changeIconFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}