package com.version2.tfgpedrollompart2.ui.dashboard

import android.content.ContentValues.TAG
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import com.version2.tfgpedrollompart2.R
import com.version2.tfgpedrollompart2.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var filaCatalogo: LinearLayout
    //Generamos una lista para introducir los productos.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val paymentbtn:Button=root.findViewById(R.id.btnProceder)
        val scrolltxt:TextView=root.findViewById(R.id.scrollTxt)

        dashboardViewModel.text.observe(viewLifecycleOwner) {

        }

        //Llamamos al local.
        val sh: SharedPreferences =requireActivity().getSharedPreferences("MYPREFERENCES",
            MODE_PRIVATE
        )

        val userP=sh.getString("name","")


        //Primero el boton de visibilidad es invisible para que nadie pague nada, esto se queda así si no hay un usuario local.
        paymentbtn.visibility=View.INVISIBLE
        scrolltxt.text="No hay ningún usuario registrado."


        //Tomamos una cadena de ids de la coleccion de videojuegos, para eliminar los juegos.
        val idDesire= mutableListOf<String>()
        //Tomamos una cadena de ids de los videojuegos en general.El objetivo es destruirlo tanto en deseados tanto de mi carrito de deseados como del de los demás.
        val idGames= mutableListOf<Long>()

        //Si tenemos un usuario local...
        if(!userP.isNullOrEmpty()){

            paymentbtn.visibility=View.VISIBLE
            paymentbtn.setBackgroundColor(Color.YELLOW)
            scrolltxt.text=""
            filaCatalogo=binding.filasProducto
            var userArticles= mutableListOf<String>()

            //Llamamos a deseados.
            val db = Firebase.firestore

            db.collection("deseados")
                .whereEqualTo("usuario",userP)//Nos encargamos que la lista sea la del usuario
                //Que haya iniciado sesion.
                .get()
                .addOnSuccessListener { result ->

                    filaCatalogo.removeAllViews()

                    for (document in result) {

                        //Tomamos los valores del campo.
                        val title = document.getString("titulo")
                        val provider = document.getString("proveedor")
                        val price = document.getDouble("precio")
                        val idGame = document.get("id") as? Long
                        val genre = document.getString("genero")
                        val publish_date = document.getTimestamp("fecha_publicacion")
                        val url = document.getString("url")
                        val mailProveedor=document.getString("mailProveedor")
                        val idProduct=document.id

                        //Tomamos el id del producto.
                        if (idGame != null) {
                            idDesire.add(idProduct)
                            idGames.add(idGame)
                        }

                        //Introducimos todos los gmails del proveedor aquí.
                        if (mailProveedor != null) {
                            userArticles.add(mailProveedor)
                        }

                        //Los añadimos al LinearLayOut.
                        addCatalogue(inflater, title, provider, price, idGame, genre, publish_date, url,idProduct)

                        Log.d(TAG, "${document.id} => ${document.data}")

                    }
                }

                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)

                }

            //Aquí nos movemos a la ventana de pago.
            paymentbtn.setOnClickListener(View.OnClickListener {

                val editor=sh.edit()

                //Guardamos los correos en una String separados con , y lo guardamos en el contenedor local.
                val arrayTosave=userArticles.joinToString(separator = ",")
                editor.putString("emailList",arrayTosave)

                //Guardamos los ids en una String separados con , y lo guardamos en el contenedor local.
                val idtoSave=idDesire.joinToString (separator = ",")
                editor.putString("idList",idtoSave)

                val idGameToSave=idGames.joinToString (separator = ",")
                editor.putString("idGame",idGameToSave)

                //Guardamos cambios.
                editor.apply()

                //Vaciamos la lista.
                userArticles.clear()
                idDesire.clear()
                idGames.clear()



                findNavController().navigate(R.id.payment_fragment)

            })

        }

        return root
    }



    private fun addCatalogue(
        inflater: LayoutInflater,
        title: String?,
        provider: String?,
        price: Double?,
        id: Long?,
        genre: String?,
        publishDate: Timestamp?,
        url: String?,
        idProduct: String
    ) {
        //Inflamos el articulo en la vista Actual.
        val itemView=inflater.inflate(R.layout.articulo,binding.filasProducto,false)
       //Declaramos cada variable.
        val titleTV: TextView = itemView.findViewById(R.id.tituloText)
        val priceTV: TextView = itemView.findViewById(R.id.pricetext)
        val imageIV: ImageView = itemView.findViewById(R.id.fotoImage)
        val trashBtn:Button=itemView.findViewById(R.id.trashBtn)
        val shopping_cart_btn:Button=itemView.findViewById(R.id.carritobtn)

        //Asignamos valor a cada variable.
        shopping_cart_btn.visibility= View.INVISIBLE
        titleTV.text = title
        priceTV.text = price.toString()
        if (!url.isNullOrEmpty()) {
            Picasso.get()
                .load(url)
                .placeholder(R.drawable.error_not_found)
                .into(imageIV)
        } else {
            imageIV.setImageResource(R.drawable.ic_launcher_foreground)
        }

        trashBtn.setOnClickListener(View.OnClickListener {

            val db = Firebase.firestore

            db.collection("deseados")
                .document(idProduct)
                .delete()
                .addOnSuccessListener {


                        Log.d(TAG, " Documento eliminado exitosamente.")
                    Toast.makeText(requireContext(),title+" ha sido retirado de la lista.",Toast.LENGTH_SHORT).show()
                    filaCatalogo.removeView(itemView)


                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error eliminando el documento.", exception)
                }

        })

        //Añadimos el valor.
        filaCatalogo.addView(itemView)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}