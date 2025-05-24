package com.version2.tfgpedrollompart2.ui.notifications

import android.content.ContentValues.TAG
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore.Images
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import com.version2.tfgpedrollompart2.R
import com.version2.tfgpedrollompart2.databinding.FragmentNotificationsBinding
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val bienvenidoText:TextView=root.findViewById(R.id.bienvenidoText)
        val profileImage:ImageView=root.findViewById(R.id.profileImage)
        val registerDate:TextView=root.findViewById(R.id.fechaRegistrotxt)
        val mailtxt:TextView=root.findViewById(R.id.mailtxt)
        val nametxt:TextView=root.findViewById(R.id.nombretxt)
        val sh:SharedPreferences=requireActivity().getSharedPreferences("MYPREFERENCES",
            MODE_PRIVATE)
        val userP=sh.getString("name","")
        val profileP=sh.getString("profile","")
        val logOutbtn=root.findViewById<Button>(R.id.logOutbtn)
        val sellbtn:Button=root.findViewById(R.id.sellbtn)
        val name2:TextView=root.findViewById(R.id.nombreValue)
        val chatbtn:Button=root.findViewById(R.id.chatbtn)
        val changepasswdBtn:Button=root.findViewById(R.id.changePasswordbtn)
        val changeIconBtn:Button=root.findViewById(R.id.changeProfilebtn)

        try {
            val copyRighttxt: TextView = root.findViewById(R.id.Copyrighttxt)

            val completeText =
                "Copyright © NetJoy. Todos los derechos \n reservados. JSC 'Helis Play', Gyneju St. 4-333,\n" +
                        "Getxo, Spain. Términos y Condiciones, Política de Privacidad, Programación de productos físicos."

            val yellowText =
                "Términos y Condiciones, Política de Privacidad, Programación de productos físicos."

            val startIndex = completeText.indexOf(yellowText)
            val endIndex = startIndex + yellowText.length

            val spannable = SpannableString(completeText)
            val yellowColor =
                ContextCompat.getColor(requireActivity(), android.R.color.holo_orange_light)

            if (startIndex != -1) {
                spannable.setSpan(
                    ForegroundColorSpan(yellowColor),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            copyRighttxt.text = spannable
        }catch (e:Exception){

            Log.d("DEBUG",e.toString())

        }


        changepasswdBtn.setOnClickListener(View.OnClickListener {

            findNavController().navigate(R.id.changePassword_fragment)

        })

        changeIconBtn.setOnClickListener(View.OnClickListener {

            findNavController().navigate(R.id.changeIcon_fragment)

        })


        chatbtn.setOnClickListener(View.OnClickListener {

            //Introduciremos el fragment ahora
            findNavController().navigate(R.id.contact_fragment)

        })

        sellbtn.setOnClickListener(View.OnClickListener {

            findNavController().navigate(R.id.sell_fragment)
        })

        //Introduciremos provisionalmente los nombres de usuario en local, más adelante, los introduciremos en la ventana de registro.
logOutbtn.setOnClickListener(View.OnClickListener {
    val editor=sh.edit()
    //Quitamos el nombre en local.
    editor.putString("name","")
    //Quitamos la foto del perfil.
    editor.putString("profile","")
    //Guardamos los cambios.
    editor.apply()
    // Quitamos todo el perfil
    mailtxt.setText("")
    nametxt.setText("")
    registerDate.setText("")
//Volvemos invisible la bienvenida.
    bienvenidoText.setText("¡Bienvenido!")
    bienvenidoText.visibility=View.INVISIBLE
    //limpiamos la imagen.
    Picasso.get().cancelRequest(profileImage)
    profileImage.setImageDrawable(null)
    //Ocultamos la imagen.
    profileImage.visibility=View.INVISIBLE

})
        if(userP.isNullOrEmpty()||profileP.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "La sesión está cerrada", Toast.LENGTH_SHORT).show()

        }else{
            //En caso de que el contenedor esté lleno,la app, rellenará los datos.
            val db = Firebase.firestore
            db.collection("usuarios")
                //En este caso, el nombre debería ser Clara.
                .whereEqualTo("usuario", userP)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        mailtxt.setText(document.getString("email"))
                        name2.setText(document.getString("nombre"))
                        nametxt.setText(document.getString("nombre"))
                        val registerDateString=timeStampToString(document.getTimestamp("fechaRegistro"))
                        registerDate.setText(registerDateString.toString())
                        bienvenidoText.visibility=View.VISIBLE
                        bienvenidoText.setText(
                            bienvenidoText.text.toString() + " " + document.getString("usuario")
                        )
                        profileImage.visibility=View.VISIBLE
                        Picasso
                            .get()
                            .load(document.getString("foto"))
                            .placeholder(R.drawable.error_not_found)
                            .error(R.drawable.profile_error)
                            .into(profileImage)

                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
        }

        return root
    }
    private fun timeStampToString(registerDate: Timestamp?): Any {
        //Primero generaremos un simpleDateFormat
        val sdf=SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date= registerDate?.toDate()
        //Aquí enviamos el timeStamp de vuelta.
       return sdf.format(date)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}