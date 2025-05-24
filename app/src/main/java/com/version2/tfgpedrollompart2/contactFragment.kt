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
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import com.version2.tfgpedrollompart2.databinding.FragmentContactBinding


class contactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!! // Asegúrate de que _binding no sea null

    private lateinit var filaContacto: LinearLayout
    private val contactlist = mutableListOf<View>()  // Lista para las vistas de productos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Manejo de argumentos si es necesario
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Aquí debes inicializar _binding correctamente
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        val root = binding.root  // Aquí obtienes la vista raíz inflada

        val sh: SharedPreferences = requireActivity().getSharedPreferences("MYPREFERENCES", MODE_PRIVATE)
        val userP = sh.getString("name", "")

        val arrowback: ImageButton =root.findViewById(R.id.arrow_back)

        arrowback.setOnClickListener(View.OnClickListener {

            requireActivity().supportFragmentManager.popBackStack()

        })

        // Inicializa la vista filaContacto
        filaContacto = binding.filasContacto

        val db = Firebase.firestore
        db.collection("chatRoom")
            .get()
            .addOnSuccessListener { result ->
                filaContacto.removeAllViews()
                contactlist.clear()  // Limpiar lista antes de rellenar

                for (document in result) {
                    val user1 = document.getString("usuario1")
                    val user2 = document.getString("usuario2")

                    if (userP.toString().equals(user1) || userP.toString().equals(user2)) {
                        anadirContacto(inflater, user1, user2, userP)
                    }

                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        return root
    }

    private fun anadirContacto(
        inflater: LayoutInflater,
        user1: String?,
        user2: String?,
        userP: String?
    ) {
        val itemView = inflater.inflate(R.layout.contact, binding.filasContacto, false)
        val profile: ImageView = itemView.findViewById(R.id.profile)
        val name: TextView = itemView.findViewById(R.id.contact_nametxt)
        var url = ""
        var contactUser = ""

        if (userP.toString().equals(user1)) {
            name.text = user2
            // El usuario 2 será el contacto
            contactUser = user2.toString()
        } else {
            name.text = user1
            // El usuario 1 será el contacto
            contactUser = user1.toString()
        }

        // Busca la foto de perfil
        findProfilePicture(contactUser) { url ->
            if (!url.isNullOrEmpty()) {
                Picasso.get().load(url).into(profile)
            } else {
                profile.setImageResource(R.drawable.ic_launcher_foreground) // Imagen por defecto si no tiene foto
            }
        }

        // Maneja el clic en el contacto
        itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("Contact user", contactUser)
            findNavController().navigate(R.id.chat_fragment, bundle)
        }

        // Agrega el nuevo contacto a la vista
        filaContacto.addView(itemView)
        contactlist.add(itemView)
    }

    private fun findProfilePicture(user: String?, onResult: (String?) -> Unit) {
        val db = Firebase.firestore

        db.collection("usuarios")
            .whereEqualTo("usuario", user)
            .get()
            .addOnSuccessListener { result ->
                val url = result.documents.firstOrNull()?.getString("foto")
                onResult(url)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting profile picture", exception)
                onResult(null)
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            contactFragment().apply {
                arguments = Bundle().apply {
                    // Puedes agregar parámetros a la instancia si es necesario
                }
            }
    }
}
