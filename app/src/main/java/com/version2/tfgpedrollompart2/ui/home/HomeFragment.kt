package com.version2.tfgpedrollompart2.ui.home

import android.annotation.SuppressLint
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
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import com.version2.tfgpedrollompart2.R
import com.version2.tfgpedrollompart2.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var filaCatalogo: LinearLayout
    private val productlist = mutableListOf<View>()  // Lista para las vistas de productos

    @SuppressLint("CommitPrefEdits")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.text.observe(viewLifecycleOwner) { }

        //val sh: SharedPreferences = requireActivity().getSharedPreferences("MYPURCHASE", MODE_PRIVATE)
        val sh: SharedPreferences = requireActivity().getSharedPreferences("MYPREFERENCES", MODE_PRIVATE)
        val userP = sh.getString("name", "")

        val editorButton = sh.edit()

        //El objetivo principal aquí es llamar a un contenedor local que guarde el nombre de un proveedor cuyo producto se haya comprado.


        val progressbar:ProgressBar=root.findViewById(R.id.progressBar)

        progressbar.visibility=View.VISIBLE


        filaCatalogo = binding.FilasCatalogo

        // Configuración del SearchView
        val searchView = root.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText ?: "")
                return true
            }
        })

        //Llamamos a la FireStore de videojuegos.
        val db = Firebase.firestore
        db.collection("videojuegos")
            .get()
            .addOnSuccessListener { result ->
                progressbar.visibility=View.GONE
                filaCatalogo.removeAllViews()
                productlist.clear()  // Limpiar lista antes de rellenar

                //Tomamos los datos del juego de turno
                for (document in result) {
                    val title = document.getString("titulo")
                    val provider = document.getString("proveedor")
                    val price = document.getDouble("precio")
                    val id = document.get("id") as? Long //Convertimos el Id global a Long.
                    val genre = document.getString("genero")
                    val publish_date = document.getTimestamp("fecha_publicacion")
                    val url = document.getString("url")
                    val mail=document.getString("mailProveedor")

                    //Siempre y cuando userP no sea nulo.
                    if (!userP.isNullOrBlank()) {
                        addCatalogue(inflater, title, provider, price, id, genre, publish_date, url, userP, mail)
                    }

                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                progressbar.visibility=View.GONE
                Log.w(TAG, "Error getting documents.", exception)
            }

        return root
    }

    //Filtramos los productos en la barra de navegación.
    private fun filterProducts(text: String) {
        for (item in productlist) {
            val titleTextView = item.findViewById<TextView>(R.id.tituloText)
            val title = titleTextView.text.toString().lowercase()

            item.visibility = if (title.contains(text.lowercase())) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun addCatalogue(
        inflater: LayoutInflater,
        title: String?,
        provider: String?,
        price: Double?,
        id: Long?,
        genre: String?,
        publishdate: Timestamp?,
        url: String?,
        userP: String,
        mail: String?
    ) {
        //Inflamos la vista del Item.
        val itemView = inflater.inflate(R.layout.articulo, binding.FilasCatalogo, false)

        //Tomamos los campos de la vista del Item.
        val titleTV: TextView = itemView.findViewById(R.id.tituloText)
        val priceTV: TextView = itemView.findViewById(R.id.pricetext)
        val imageIV: ImageView = itemView.findViewById(R.id.fotoImage)
        val shopping_cart_btn: Button = itemView.findViewById(R.id.carritobtn)
        val trashbtn: Button = itemView.findViewById(R.id.trashBtn)

        //Ponemos a cada campo su valor.
        trashbtn.visibility = View.INVISIBLE
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
        //En esta parte, vamos a meter la funcionalidad del añadir.
        //Hacemos una busqueda en deseados.
        val db = Firebase.firestore

        //Miramos si en deseados existe un id que sea el mismo que el de este producto, y si hay un usuario que sea el mismo al actual.
       //De esta manera comprobamos si el usuario actual tiene el producto en cuestión en su carrito o no.
        db.collection("deseados")
            .whereEqualTo("id",id)
            //.whereEqualTo("usuario",userP)
            .get()
            .addOnSuccessListener { result ->


                if(result.isEmpty) {

                    //Si no lo tiene en el carrito:
                    //Boton activado
                    //Al hacer la busqueda y no encontrar el id, automáticamente se activa y se queda de color amarillo.

                    shopping_cart_btn.isEnabled=true
                    shopping_cart_btn.setBackgroundColor(Color.YELLOW)

                    //La funcionalidad del boton:
                    shopping_cart_btn.setOnClickListener(View.OnClickListener {


                        //Se añade el Articulo a la lista de deseados
                        addArticle(title,provider,price,id,genre,publishdate,url,userP,mail)

                        //La funcion comprueba si existe un chat o no, si no existe crea uno(el chat se mantiene incluso si lo sacas de la lista de deseados.)
                        generateChat(userP,provider,id)
                        //Desactivamos el botón después de que ejecute la acción, para que no se añada dos veces.

                        shopping_cart_btn.isEnabled=false
                        shopping_cart_btn.setBackgroundColor(Color.GRAY)

                    })

                }else{

                    //Si si que está.
                    //Boton desactivado.
                    //En caso de cargar la ventana otra vez, se generará otra búsqueda, y como si que
                    //encontrará el id del artículo en deseados, se bloqueará automáticamente al cargar el ScrollView.
                    shopping_cart_btn.isEnabled=false
                    shopping_cart_btn.setBackgroundColor(Color.GRAY)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }



        filaCatalogo.addView(itemView)
        productlist.add(itemView)  // Añadir a la lista para el filtro
    }

    private fun generateChat(userP: String, provider: String?, id: Long?) {

        val db = Firebase.firestore
        //Si el chat ya existe, retiramos la función
        chatExists(userP, provider) { exists ->
            if (!exists) {
                val article = hashMapOf(
                    "usuario1" to userP,
                    "usuario2" to provider,

                    )

                db.collection("chatRoom")
                    .add(article)
                    .addOnSuccessListener { Log.d(TAG, "Chat creado") }
                    .addOnFailureListener { Log.w(TAG, "Error", it) }
            }
        }


    }

    private fun chatExists(userP: String, provider: String?, onResult: (Boolean) -> Unit) {
        val db = Firebase.firestore

        db.collection("chatRoom")
            .whereEqualTo("usuario1", userP)
            .whereEqualTo("usuario2", provider)
            .get()
            .addOnSuccessListener { result1 ->
                if (!result1.isEmpty) {
                    onResult(true)
                } else {
                    db.collection("chatRoom")
                        .whereEqualTo("usuario1", provider)
                        .whereEqualTo("usuario2", userP)
                        .get()
                        .addOnSuccessListener { result2 ->
                            onResult(!result2.isEmpty)
                        }
                        .addOnFailureListener { onResult(false) }
                }
            }
            .addOnFailureListener { onResult(false) }
    }

    private fun addArticle(
        title: String?,
        provider: String?,
        price: Double?,
        id: Long?,
        genre: String?,
        publishDate: Timestamp?,
        url: String?,
        userP: String,
        mail: String?
    ) {
        val db = Firebase.firestore

        val collectionId=id.toString()+userP

        val article = hashMapOf(
            "fecha_publicacion" to publishDate,
            "genero" to genre,
            "id" to id,
            "precio" to price,
            "proveedor" to provider,
            "titulo" to title,
            "url" to url,
            "usuario" to userP,
            "mailProveedor" to mail
        )

        db.collection("deseados")
            .document(collectionId)
            .set(article)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${collectionId}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null

    }
}