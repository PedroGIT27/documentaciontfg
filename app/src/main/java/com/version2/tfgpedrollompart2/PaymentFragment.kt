package com.version2.tfgpedrollompart2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.Manifest


class PaymentFragment : Fragment() {

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
        val root= inflater.inflate(R.layout.fragment_payment, container, false)

        val payBtn: Button =root.findViewById(R.id.payment_button)

        val arrowBack: ImageButton =root.findViewById(R.id.arrow_back)

        arrowBack.setOnClickListener(View.OnClickListener {

            requireActivity().supportFragmentManager.popBackStack()

        })

        //Para pagar, boton para pagar.
        payBtn.setOnClickListener(View.OnClickListener {

            Log.d("MiBoton", "El botón fue pulsado")

            //Llamamos al contenedor local
            val sh: SharedPreferences =requireActivity().getSharedPreferences("MYPREFERENCES",
                MODE_PRIVATE
            )
            //Tomamos la lista de emails, la lista de ids y el id de los juegos.
            val editor=sh.edit()
            val emailList=sh.getString("emailList","")
            val idList=sh.getString("idList","")
            val idGameList=sh.getString("idGame","")

            if(emailList.isNullOrEmpty()){
                Log.d("Paso 2", "el email List está vacío $emailList")

                return@OnClickListener

            }
            Log.d("Paso 2", "$emailList")
            Log.d("Paso 2", "$idList")
            Log.d("Paso 2", "$idGameList")

            //Lo pasamos todos a arrays.
            val emails=emailList?.split(",")
            val documentIds=idList?.split(",")
            val idGames=idGameList?.split(",")

            //Generamos un contador.
            var i=0

            //Del ScrollerView de los deseados hemos tomado el gmail y el id por lo que el tamaño de los tres Arrays es el mismo
            if (emails != null) {
                for (email in emails){

                    //Enviamos un email.
                    sendEmail(email)

                    //Tomamos el id del juego
                    val documentId=documentIds?.get(i)

                    //Enviamos una notificacion.
                    findName(documentId,i)

                    //Eliminamos el deseo
                    deleteDesires(documentId)

                    val idGame=idGames?.get(i)

                    //Buscamos el juego en el
                    if (idGame != null) {
                        findGameInCatalogue(idGame,"videojuegos")
                        //Quitamos el juego de los deseados de otros usuarios.
                        findGameInCatalogue(idGame,"deseados")

                    }

                    i++
                }
            }

            //Vaciamos las listas.
            editor.putString("emailList","")
            editor.putString("idList","")
            editor.putString("idGame","")
            editor.commit()

        })

    return root
    }

    private fun findGameInCatalogue(id: String, collection: String) {

        val db = Firebase.firestore

        db.collection(collection)
            .whereEqualTo("id",id.toInt())
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    deleteFromCatalogue(document.id,collection)

                    Log.d(TAG, "documento ${document.getString("titulo")} eliminado.")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    private fun deleteFromCatalogue(id: String, collection: String) {

        val db = Firebase.firestore

        db.collection(collection)
            .document(id)
            .delete()
            .addOnSuccessListener { result ->

                Log.d(TAG, "Documento destruido.")

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

    }


    private fun findName(id: String?, i: Int) {



        Log.d(TAG, "Paso 3 El id enviado es $id.")

        if(id==null) {
            Log.d(TAG, "El id está vacío.")
        return
        }

        val db = Firebase.firestore

        db.collection("deseados")

            .document(id)
            .get()
            .addOnSuccessListener { document ->

                        val name=document.getString("titulo")
                        val user=document.getString("proveedor")

                        sendNotification(i,name,user)
                }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

    }

    private fun sendNotification(i: Int, name: String?, user: String?) {


        Log.d("NotifDebug", "Creando canal de notificación")
        createNotificationChannel()
        Log.d("NotifDebug", "Canal de notificación creado con éxito")

        val CHANNEL_ID="1"
        val textTitle="notificacion de netjoy"
        val textContent="la transacción del producto "+name+" al usuario "+user+" ha sido realizada con éxito, gracias por su cooperación.s"

        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Ícono mínimo necesario
            .setContentTitle(textTitle)
            .setContentText(textContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(textContent))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(requireContext())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Solicita el permiso si no se ha otorgado
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
                return@with
            }

            val NOTIFICATION_ID = 1
            notify(NOTIFICATION_ID, builder.build())
        }



    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("1", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun deleteDesires(id: String?) {

        val db = Firebase.firestore

        if (id != null) {
            db.collection("deseados")
                .document(id)
                .delete()
                .addOnSuccessListener { result ->

                    Log.w(TAG, "Success deleting document.")

                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error deleting documents.", exception)
                }
        }

    }

    private fun sendEmail(email: String) {

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
            personalizations = listOf(Personalization(listOf(Email(email)))),
            from = Email("pedrollom@gmail.com"),
            subject = "compra online",
            content = listOf(Content("text/plain", "Buenas tardes, se acaba de realizar la transacción para su artículo. Debe enviar dicho artículo en un plazo de 10 días. Un saludo, el equipo de Netjoy."))
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

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PaymentFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}