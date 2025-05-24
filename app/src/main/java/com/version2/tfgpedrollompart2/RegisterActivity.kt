package com.version2.tfgpedrollompart2

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.security.MessageDigest

class RegisterActivity : AppCompatActivity() {
    lateinit var editbtn:Button
    lateinit var passwordEdit:TextView
    lateinit var username:TextView
    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        username=findViewById(R.id.usernameEdit)
        editbtn=findViewById(R.id.loginbtn)
        passwordEdit=findViewById(R.id.passwordEdit)
        val registerTxt=findViewById<TextView>(R.id.registertxt)
        val forgotpasswordtxt:TextView=findViewById(R.id.forgottenpasswordtxt)
        registerTxt.paintFlags=registerTxt.paintFlags or Paint.UNDERLINE_TEXT_FLAG



        forgotpasswordtxt.setOnClickListener(View.OnClickListener {

            val fragment=forgotFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.register_Container,fragment)
                .addToBackStack(null)
                .commit()



        })

        registerTxt.setOnClickListener(View.OnClickListener {


            val fragment=registerFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.register_Container,fragment)
                .addToBackStack(null)
                .commit()
        })

        editbtn.setOnClickListener(View.OnClickListener {
            if(username.text.isNotEmpty()&&passwordEdit.text.isNotEmpty()){



                val sh:SharedPreferences=getSharedPreferences("MYPREFERENCES", MODE_PRIVATE)
                val userP=sh.getString("name","")
                //Si ya hay un valor registrado en el contenedor...
                if(!userP.isNullOrEmpty()){
                    //Iniciamos la actividad principal
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                val db = Firebase.firestore
                db.collection("usuarios")
                    .whereEqualTo("usuario",username.text.toString())
                    .whereEqualTo("contrasenia",hashPassword(passwordEdit.text.toString()))
                    .get()
                    .addOnSuccessListener { result ->

                        if(result.isEmpty){
                            Toast.makeText(this,"Usuario o contraseña incorrectos",Toast.LENGTH_SHORT).show()
                        }
                        for (document in result) {
                            //Si la operación tiene éxito, introducimos el nombre y la foto en el contenedor.
                            val editor=sh.edit()
                            editor.putString("name", document.getString("usuario"))
                            editor.putString("profile", document.getString("foto"))
                            editor.putString("email",document.getString("email"))
                            editor.apply()
                            //Iniciamos la actividad principal
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                            Log.d(TAG, "${document.id} => ${document.data}")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents.", exception)

                    }
            }else{
                Toast.makeText(this, "No se han completado todos los campos.", Toast.LENGTH_SHORT).show()
            }
        })
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_Container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }


}