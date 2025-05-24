package com.version2.tfgpedrollompart2

import android.content.ContentValues.TAG
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.security.MessageDigest


class changeFragment : Fragment() {


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
        val root= inflater.inflate(R.layout.fragment_change, container, false)

        val sh: SharedPreferences =requireActivity().getSharedPreferences("FORGOTTENPASSWORD", MODE_PRIVATE)
        val editor=sh.edit()
        val timeStamp=sh.getLong("time",0)
        val username=sh.getString("username","")?.trim()
        val codeP=sh.getString("code","")
        val confirmbtn: Button =root.findViewById(R.id.confirmbtn)
        val codeEdit:TextView=root.findViewById(R.id.codeEdit)

        //Hacemos que todo esto se vuelva invisible

        val codeLO=root.findViewById<LinearLayout>(R.id.codeLO)
        val presentationLO=root.findViewById<LinearLayout>(R.id.presentationLO)
        val passwordLO=root.findViewById<LinearLayout>(R.id.passwordLO)
        val conditionsLO=root.findViewById<LinearLayout>(R.id.conditionsLO)
        val sendbtn:Button=root.findViewById(R.id.sendbtn)

        val arrowback: ImageButton =root.findViewById(R.id.arrow_back)
        val registerbacktxt:TextView=root.findViewById(R.id.registerBacktxt)

        registerbacktxt.setOnClickListener(View.OnClickListener {
            val intent = Intent(requireContext(), RegisterActivity::class.java)
            startActivity(intent)
            requireActivity().finish()

        })

        arrowback.setOnClickListener(View.OnClickListener {

            requireActivity().supportFragmentManager.popBackStack()

        })


        //Visible
        codeLO.visibility=View.VISIBLE


        //Invisible

        presentationLO.visibility=View.INVISIBLE
        passwordLO.visibility=View.INVISIBLE
        conditionsLO.visibility=View.INVISIBLE
        sendbtn.visibility=View.INVISIBLE


        confirmbtn.setOnClickListener(View.OnClickListener {

            val currentTime = System.currentTimeMillis()
            val timePassed = currentTime - timeStamp

            if(codeP.equals(codeEdit.text.toString())&&timePassed<= 300000){

                //Invisible
                codeLO.visibility=View.INVISIBLE

                //Visible
                presentationLO.visibility=View.VISIBLE
                passwordLO.visibility=View.VISIBLE
                conditionsLO.visibility=View.VISIBLE
                sendbtn.visibility=View.VISIBLE

                editor.putString("code","")
                editor.putLong("time",0)
                editor.putString("user","")
                editor.commit()

            }else{
                Toast.makeText(requireContext(),"El codigo no se ha reconocido,por favor intente otra vez.",Toast.LENGTH_SHORT).show()
            }

        })

        //A partir de aqui comienza la operación de cambio de contraseña
        val passwordEdit=root.findViewById<EditText>(R.id.passwordEdit)
        val repeatPasswordEdit=root.findViewById<EditText>(R.id.repeatPasswordEdit)


        sendbtn.setOnClickListener(View.OnClickListener {

            if(passwordEdit.text.toString().isEmpty()){

                Toast.makeText(requireContext(),"Rellena el campo .",Toast.LENGTH_SHORT).show()
                return@OnClickListener

            }
            if(!passwordEdit.text.toString().equals(repeatPasswordEdit.text.toString())){

                Toast.makeText(requireContext(),"Las contraseñas no coinciden.",Toast.LENGTH_SHORT).show()
                return@OnClickListener

            }
            if(!passwordCorrect(passwordEdit.text.toString())){

                Toast.makeText(requireContext(),"Las contraseñas no cumple las condiciones.",Toast.LENGTH_SHORT).show()
                return@OnClickListener

            }

val hash=hashPassword(passwordEdit.text.toString())

            updatepassword(hash,username)

        })

    return root
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

                    val userDocRef = db.collection("usuarios").document(document.id)
                    userDocRef.update("contrasenia", hash)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

    }

    private fun passwordCorrect(password: String): Boolean {

        val passwordPattern=Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$")

        return passwordPattern.containsMatchIn(password)

    }
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
            changeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}