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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.security.MessageDigest


class changePasswordFragment : Fragment() {


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
        val root= inflater.inflate(R.layout.fragment_change_password, container, false)

        //Tomamos el contenedor local.
        val sh: SharedPreferences =requireActivity().getSharedPreferences("MYPREFERENCES",
            MODE_PRIVATE
        )

        //Llamamos userP al usuario que tengamos activo en el momento.
        val userP=sh.getString("name","")

        //Declaramos las variables.
        val currentPassword: TextView =root.findViewById(R.id.currentPassword)
        val newPassword: TextView =root.findViewById(R.id.newPassword)
        val confirmedPassword: TextView =root.findViewById(R.id.confirmPassword)
        val backtxt:TextView=root.findViewById(R.id.backtxt)

        backtxt.setOnClickListener(View.OnClickListener {

            requireActivity().supportFragmentManager.popBackStack()

        })

        val changeButton:Button=root.findViewById(R.id.changePassword)

        //Al darle al boton
        changeButton.setOnClickListener(View.OnClickListener {



            //Si alguno de los valores está vacío.
            if(currentPassword.text.toString().trim().isEmpty()||newPassword.text.toString().trim().isEmpty()||confirmedPassword.text.toString().trim().isEmpty()){

                Toast.makeText(requireContext(),"Rellena los campos",Toast.LENGTH_SHORT).show()

                return@OnClickListener

            }


            //Llamamos a una función para comprobar que la contraseña cumple todas las condiciones.
            if(!passwordCorrect(newPassword.text.toString().trim())){

                Toast.makeText(requireContext(),"No cumples los requisistos",Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            //Comprobamos que las dos contraseñas sean iguales.
            if(!newPassword.text.toString().equals(confirmedPassword.text.toString())){

                Toast.makeText(requireContext(),"Las contraseñas no coinciden.",Toast.LENGTH_SHORT).show()
                return@OnClickListener

            }


            //Hasheamos la nueva contraseña
            val hash=hashPassword(newPassword.text.toString().trim())

            //Hasheamoos la contraseña anterior para así poder comprobar que sea correcta.
            val hashCurrent=hashPassword(currentPassword.text.toString().trim())

            //Comprobamos que el usuario anterior tuviese esta contraseña.
            existPassword(hashCurrent,userP){
                isCorrect->

                if(isCorrect){

                    Log.d("DEBUG", "la contraseña es correcta.")
                    //Si la contraseña introducida es correcta se actualiza la contraseña.
                    updatepassword(hash, userP)


                }else{

                    Toast.makeText(requireContext(),"Contraseña incorrecta",Toast.LENGTH_SHORT).show()

                }


            }

        })



    return root
    }

    private fun existPassword(hashPassword: String, userP: String?, callback: (Boolean) -> Unit) {


        val db = Firebase.firestore

        db.collection("usuarios")
            .whereEqualTo("usuario", userP)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val document = result.documents[0]
                    val storedHash = document.getString("contrasenia")

                    if (storedHash == hashPassword) {
                        callback(true)  // Contraseña actual correcta
                    } else {
                        callback(false) // Contraseña incorrecta
                    }
                } else {
                    callback(false) // Usuario no encontrado
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting user", exception)
                callback(false)
            }
    }

    private fun updatepassword(hash: String, username: String?) {

        Log.d("DEBUG", "Inicio de updatepassword con hash: $hash y username: $username")

        val db = Firebase.firestore

        db.collection("usuarios")
            .whereEqualTo("usuario",username)
            .get()
            .addOnSuccessListener { result ->

                Log.d("DEBUG", "Resultado de la consulta: $result")  // Log del resultado de la consulta

                for (document in result) {

                    Log.d("DEBUG", "Usuario donde se actualizará: ${document.getString("usuario")}")


                    update(document.id,hash)

                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

    }

    private fun update(id: String, hash: String) {

        val db = Firebase.firestore

        Log.d(TAG, "La contraseña introducida $hash")

        db.collection("usuarios")
            .document(id)
            .update("contrasenia",hash)
            .addOnSuccessListener { result ->


                Toast.makeText(requireContext(),"Contraseña actualizada",Toast.LENGTH_SHORT).show()
                Log.d("DEBUG", "Contraseña cambiada.")

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

    }

    //Comprobamos que la contraseña sea correcta.
    private fun passwordCorrect(password: String): Boolean {

        val passwordPattern=Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$")

        return passwordPattern.containsMatchIn(password)

    }

    //La hasheamos
    private fun hashPassword(password: String): String {

        Log.d("DEBUG", "Generando hash para la contraseña: $password")

        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hash= digest.joinToString("") { "%02x".format(it) }

        Log.d("DEBUG", "Hash generado: $hash")  // Log del hash generado

        return hash
    }

    companion object {


        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            changePasswordFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}