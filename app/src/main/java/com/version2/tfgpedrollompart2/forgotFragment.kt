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
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class forgotFragment : Fragment() {


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
        val root=inflater.inflate(R.layout.fragment_forgot, container, false)

        val sh: SharedPreferences =requireActivity().getSharedPreferences("FORGOTTENPASSWORD", MODE_PRIVATE)
        val editor=sh.edit()


        val emailEdit:TextView=root.findViewById(R.id.gmailEdit)
        val userNameEdit:TextView=root.findViewById(R.id.usernameEdit)
        val sendbtn: Button =root.findViewById(R.id.sendbtn)

        val arrowback:ImageButton=root.findViewById(R.id.arrow_back)

        arrowback.setOnClickListener(View.OnClickListener {

            requireActivity().supportFragmentManager.popBackStack()

        })

        sendbtn.setOnClickListener(View.OnClickListener {

            if(emailEdit.text.toString().isEmpty()||userNameEdit.text.toString().isEmpty()){

                Toast.makeText(requireContext(),"Rellena todos los campos.",Toast.LENGTH_SHORT).show()
                return@OnClickListener

            }

            val db = Firebase.firestore

            db.collection("usuarios")
                .whereEqualTo("email",emailEdit.text.toString().trim())
                .whereEqualTo("usuario",userNameEdit.text.toString().trim())
                .get()
                .addOnSuccessListener { result ->

                    if(result.isEmpty){

                        Toast.makeText(requireContext(),"El correo o el usuario introducidos no son correctos pruebe usted otra vez.",Toast.LENGTH_SHORT).show()

                    }else{
                       val code=generateCode()


                        for (document in result){

                            sendCodeByGmail(document.getString("email"),code)
                            editor.putString("code",code)
                            editor.putLong("time",System.currentTimeMillis())
                            editor.putString("username",userNameEdit.text.toString())
                            editor.commit()

                        }
                        val fragment=changeFragment()

                        requireActivity()
                            .supportFragmentManager.beginTransaction()
                            .replace(R.id.register_Container,fragment)
                            .addToBackStack(null)
                            .commit()

                    }

                }

                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)

                }







        })




    return root

    }

    private fun sendCodeByGmail(email: String?, code: String) {


        if (email == null) {
            Toast.makeText(requireContext(), "Correo inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", null)
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.sendgrid.com/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val service = retrofit.create(SendGridService::class.java)

        val emailRequest = SendGridEmail(
            personalizations = listOf(Personalization(listOf( Email(email) ))),
            from = Email("pedrollom@gmail.com"),
            subject = "compra online",
            content = listOf(Content("text/plain", "Tu nuevo codigo de identificación es "+code+" Por favor asegurese de enviarlo en 5 minutos.Un saludo, el equipo de Netjoy."))
        )

        service.sendEmail(emailRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(),"El producto se ha enviado correctamente",Toast.LENGTH_SHORT).show()
                    Log.d("SendGrid", "Correo enviado correctamente.")
                } else {
                    Toast.makeText(requireContext(),"Hemos tenido un problema.",Toast.LENGTH_SHORT).show()
                    Log.d("SendGrid", "Error en el envío. ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, throwable: Throwable) {
                Log.d("SendGrid", "Fallo en red. ${throwable.message}")
            }
        })



    }

    private fun generateCode():String {


        val charset = ('0'..'9')
        return (1..6).map { charset.random() }.joinToString("")

    }

    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            forgotFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}