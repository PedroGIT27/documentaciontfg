package com.version2.tfgpedrollompart2

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore

class venderProductoFragment : Fragment() {

    private var imageUri: Uri? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

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

        val root= inflater.inflate(R.layout.fragment_vender_producto, container, false)

        val sh: SharedPreferences =requireActivity().getSharedPreferences("MYPREFERENCES",
            MODE_PRIVATE
        )
        val userP=sh.getString("name","")


        val titletxt: TextView =root.findViewById(R.id.game_title)
        val descriptiontxt:TextView=root.findViewById(R.id.game_description)
        val checkBoxChat:CheckBox=root.findViewById(R.id.checkbox_chat)
        val stateSpinner:Spinner=root.findViewById(R.id.stateSpinner)
        val countrySpinner:Spinner=root.findViewById(R.id.countrySpinner)
        val priceEdit:TextView=root.findViewById(R.id.priceEdit)
        val coinSpinner:Spinner=root.findViewById(R.id.coinSpinner)
        val cpEdit:TextView=root.findViewById(R.id.cpEdit)
        val confirmbtn: Button =root.findViewById(R.id.confirmBtn)
        val foto:ImageView=root.findViewById(R.id.product_Image)

        val arrowback: ImageButton =root.findViewById(R.id.arrow_back)

        arrowback.setOnClickListener(View.OnClickListener {

            requireActivity().supportFragmentManager.popBackStack()

        })


        //Esta parte de aquí es la encargada de hacer que puedas introducir una imagen en la foto.
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
                foto.setImageURI(uri) // Mostrar imagen en el ImageView
            }
        }

        foto.setOnClickListener {
            pickImageLauncher.launch("image/*") // Abre la galería
        }

        //Estas partes de aquí es la encargada de dejar al usuario meter un estado, una moneda y un País.

        val states = listOf("Selecciona el estado del producto","Nuevo", "Usado", "En buenas condiciones") // Ejemplo para el estado del producto


        val countries = listOf("Selecciona un país","ES",
            "US",
            "CN",
            "MX",
            "GB",
            "DE",
            "FR",
            "IT",
            "JP",
            "BR",
            "IN",
            "AU",
            "CA",
            "AR",
            "RU"  )

        val coins = listOf("Selecciona una moneda","USD",
            "EUR",
            "CNY",
            "MXN",
            "GBP",
            "EUR",
            "EUR",
            "JPY",
            "BRL",
            "INR",
            "AUD",
            "CAD",
            "ARS",
            "RUB" )


        val stateAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, states)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        stateSpinner.adapter = stateAdapter


        val countryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, countries)
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countrySpinner.adapter = countryAdapter


        val coinAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, coins)
        coinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        coinSpinner.adapter = coinAdapter



        //El botón de confirmar venta.

        confirmbtn.setOnClickListener(View.OnClickListener {



            //Un mensaje de alerta antes de ejecutar la acción
            val builder=android.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Confirmar Publicación.")
            builder.setMessage("Esta usted a punto de subir un producto para su posterior venta en la base de datos de NetJoy, ¿desea proceder?")

            builder.setPositiveButton("Sí"){dialog,_->
                dialog.dismiss()
                //Se inicia la operación de enviar el producto.
                shareProduct(imageUri,checkBoxChat,stateSpinner,countrySpinner,coinSpinner,titletxt,descriptiontxt,priceEdit,cpEdit,userP,sh)


            }
            builder.setNegativeButton("No"){dialog,_->
                Toast.makeText(requireContext(),"Operación cancelada",Toast.LENGTH_SHORT).show()
               dialog.dismiss()


            }
            builder.create().show()




        })
    return root
    }

    fun shareProduct(
        imageUri: Uri?,
        checkBoxChat: CheckBox,
        stateSpinner: Spinner,
        countrySpinner: Spinner,
        coinSpinner: Spinner,
        titletxt: TextView,
        descriptiontxt: TextView,
        priceEdit: TextView,
        cpEdit: TextView,
        userP: String?,
        sh: SharedPreferences
    ) {
        //Dejo aquí la operación del SetOnclickListener
        //Tomamos el estado, el país y la moneda.
        val state=stateSpinner.selectedItemPosition
        val country=countrySpinner.selectedItemPosition
        val coin=coinSpinner.selectedItemPosition

        val emailP=sh.getString("email","")


        //Si los campos están vacíos te manada un mensaje y te saca de la operación.
        if(titletxt.text.toString().trim().isEmpty()||descriptiontxt.text.toString().trim().isEmpty()||priceEdit.text.toString().trim().isEmpty()||cpEdit.text.toString().trim().isEmpty()||country==0||state==0||coin==0){
            Toast.makeText(requireContext(),"Por favor rellena los campos.",Toast.LENGTH_SHORT).show()
            return
        }

        //Lo mismo para el precio.
        val price=priceEdit.text.toString().toIntOrNull()
        if(price==null||price<=0){
            Toast.makeText(requireContext(),"Introduzca usted un campo válido para el precio.",Toast.LENGTH_SHORT).show()
            return
        }

        getGlobalId { newId ->


            Log.d(TAG,"nuevo id generado: $newId")

            //A partir de aquí se tratará de generar y actualizar un nuevo Id.
            if (newId == null) {
                Toast.makeText(
                    requireContext(),
                    "Fallo al generar el ID, disculpe usted las molestias.",
                    Toast.LENGTH_SHORT
                ).show()

                return@getGlobalId
            }


            //Llamamos a firebase.
            val db = Firebase.firestore

            //Enviamos la imagen.
            if (imageUri != null) {
                val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

                imageRef.putFile(imageUri!!)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            val user = hashMapOf(
                                "titulo" to titletxt.text.toString(),
                                "descripcion" to descriptiontxt.text.toString(),
                                "precio" to price,
                                "codigo postal" to cpEdit.text.toString(),
                                "estado del producto" to stateSpinner.selectedItem.toString(),
                                "proveedor" to userP.toString(),
                                "moneda" to coinSpinner.selectedItem.toString(),
                                "pais proveedor" to countrySpinner.selectedItem.toString(),
                                "chat disponible" to checkBoxChat.isChecked,
                                "url" to downloadUrl.toString(),
                                "id" to newId,
                                "emailProveedor" to emailP
                            )
                            db.collection("videojuegos").add(user)
                                .addOnSuccessListener { Log.d(TAG, "Documento creado con imagen") }
                                .addOnFailureListener { e ->
                                    Log.w(
                                        TAG,
                                        "Error al subir documento",
                                        e
                                    )
                                }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            "Error al subir imagen",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

            } else {

                val user = hashMapOf(
                    "titulo" to titletxt.text.toString(),
                    "descripcion" to descriptiontxt.text.toString(),
                    "precio" to price,
                    "codigo postal" to cpEdit.text.toString(),
                    "estado del producto" to stateSpinner.selectedItem.toString(),
                    "proveedor" to userP.toString(),
                    "moneda" to coinSpinner.selectedItem.toString(),
                    "pais proveedor" to countrySpinner.selectedItem.toString(),
                    "chat disponible" to checkBoxChat.isChecked,
                    "id" to newId,
                    "emailProveedor" to emailP
                    // sin imagen
                )

                db.collection("videojuegos").add(user)
                    .addOnSuccessListener {

                        Toast.makeText(
                            requireContext(),
                            "Producto subido correctamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG, "Documento creado sin imagen")
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error al subir documento", e) }
            }
        }

    }

    private fun getGlobalId(callback:(Int?) -> Unit) {
// Write a message to the database

        val database = Firebase.database("https://examen-8b578-default-rtdb.europe-west1.firebasedatabase.app")

        val myRef = database.getReference("contadorGlobal")

        myRef.runTransaction(object : com.google.firebase.database.Transaction.Handler{

            override fun doTransaction(currentData: MutableData): Transaction.Result {


                var currentValue=currentData.getValue(Int::class.java)?:0
                currentData.value=currentValue+1

                return Transaction.success(currentData)

            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {

                if(committed){
                    val newId=currentData?.getValue(Int::class.java)
                    callback(newId)
                }else{
                    callback(null)
                }
            }

        })

    }

    companion object {

        private const val TAG = "VenderProductoFragment"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            venderProductoFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}