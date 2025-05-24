package com.version2.tfgpedrollompart2

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.security.MessageDigest


class registerFragment : Fragment() {


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
        val root= inflater.inflate(R.layout.fragment_register, container, false)

    val arrowBack:ImageButton=root.findViewById(R.id.arrow_back)
        val nameEdit:TextView=root.findViewById(R.id.register_editText_name)
        val userNameEdit:TextView=root.findViewById(R.id.register_editText_nick)
        val mailEdit:TextView=root.findViewById(R.id.register_editText_email)
        val passwordEdt:EditText=root.findViewById(R.id.register_editText_password)
        val passwordEdit2:EditText=root.findViewById(R.id.register_editText_password_confirm)
        val switch:Switch=root.findViewById(R.id.conditionsSwitch)
        val registerBtn:Button=root.findViewById(R.id.register_btn_enter)



        arrowBack.setOnClickListener(View.OnClickListener {

            requireActivity().supportFragmentManager.popBackStack()

        })

        registerBtn.setOnClickListener(View.OnClickListener {

            if(nameEdit.text.toString().isEmpty()||userNameEdit.text.toString().isEmpty()||mailEdit.text.toString().isEmpty()||passwordEdt.text.toString().isEmpty()||passwordEdit2.text.toString().isEmpty()){
                Toast.makeText(requireContext(),"Rellene todos los campos.",Toast.LENGTH_SHORT).show()
            }else{

                if(!switch.isChecked){
                    Toast.makeText(requireContext(),"Acepte usted los terminos y servicios.",Toast.LENGTH_SHORT).show()
                }else{
                    //Aqui introduzco el campo en el firebase una función.
                    if(passwordCorrect(passwordEdt.text.toString())) {


                        if(!passwordEdit2.text.toString().equals(passwordEdt.text.toString())){

                            Toast.makeText(requireContext(),"La contraseña no es la misma.",Toast.LENGTH_SHORT).show()

                            return@OnClickListener
                        }
                        introduceFirebase(
                            nameEdit.text.toString(),
                            userNameEdit.text.toString(),
                            mailEdit.text.toString(),
                            passwordEdt.text.toString()
                        )




                    }else{
                        Toast.makeText(requireContext(),"La contraseña debe tener al menos 7 caracteres, un caracter especial un numero,una minúscula y una mayuscula",Toast.LENGTH_SHORT).show()
                    }}
            }

        })

        return root
    }

    private fun passwordCorrect(password: String): Boolean {

        val passwordPattern=Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$")

        return passwordPattern.containsMatchIn(password)

    }

    private fun introduceFirebase(name: String, userName: String, mail: String, password: String) {


        val db = Firebase.firestore

        val user = hashMapOf(
            "nombre" to name,
            "usuario" to userName,
            "email" to mail,
            "contrasenia" to hashPassword(password),
            "fechaRegistro" to Timestamp.now(),
            "foto" to "https://arangoya.net/recursos/eusko3d/perfil2.png"
        )


        userNamexists(userName, db) { exists ->
            if (exists) {
                Toast.makeText(requireContext(), "El usuario ya existe.", Toast.LENGTH_SHORT).show()
            } else {
                emailExists(mail, db) { exists ->
                    if (exists) {
                        Toast.makeText(requireContext(), "El correo introducido ya se encuentra en uso.", Toast.LENGTH_SHORT).show()
                    } else {
                        db.collection("usuarios")
                            .add(user)
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(requireContext(), "El usuario ha sido introducido correctamente.", Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                            }
                    }
                }
            }
        }
    }
    private fun userNamexists(userName: String, db: FirebaseFirestore, callback: (Boolean) -> Unit) {

        db.collection("usuarios")
            .whereEqualTo("usuario", userName)

            .get()
            .addOnSuccessListener { result ->
                callback(!result.isEmpty) // true si ya existe
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error checking user", exception)
                callback(false) // asumimos que no existe si falla
            }
    }
    private fun emailExists(mail: String, db: FirebaseFirestore, callback: (Boolean) -> Unit) {

        db.collection("usuarios")
            .whereEqualTo("email", mail)
            .get()
            .addOnSuccessListener { result ->
                callback(!result.isEmpty) // true si ya existe
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error checking user", exception)
                callback(false) // asumimos que no existe si falla
            }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            registerFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}