package com.version2.tfgpedrollompart2

import android.content.ContentValues.TAG
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import com.version2.tfgpedrollompart2.databinding.FragmentDashboardBinding
import com.version2.tfgpedrollompart2.databinding.FragmentSellBinding


class sellFragment : Fragment() {

    private var _binding: FragmentSellBinding? = null
    private val binding get() = _binding!!

    private lateinit var filaCatalogo: LinearLayout
    private val productlist = mutableListOf<View>()  // Lista para las vistas de productos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Aquí puedes hacer cualquier inicialización si es necesario
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Asignar el binding
        _binding = FragmentSellBinding.inflate(inflater, container, false)
        val root = binding.root // Usamos root como referencia

        val sh: SharedPreferences = requireActivity().getSharedPreferences("MYPREFERENCES", MODE_PRIVATE)
        val userP = sh.getString("name", "")
        val editor = sh.edit()

        val arrowback: ImageButton =root.findViewById(R.id.arrow_back)

        arrowback.setOnClickListener(View.OnClickListener {

            requireActivity().supportFragmentManager.popBackStack()

        })



        if (userP.isNullOrEmpty()) {
            Log.e(TAG, "El nombre del usuario es nulo o vacío.")
            return root  // Detener la ejecución si no hay usuario
        }


        filaCatalogo = binding.FilasCatalogo
        val sellbtn: Button = root.findViewById(R.id.sellBtn)

        sellbtn.setOnClickListener {
            // Acción para navegar a otro fragmento
            findNavController().navigate(R.id.vender_producto_fragment)
        }

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

        // Cargar productos desde Firebase
        val db = Firebase.firestore
        db.collection("videojuegos")
            .whereEqualTo("proveedor",userP)
            .get()
            .addOnSuccessListener { result ->
                filaCatalogo.removeAllViews()
                productlist.clear()  // Limpiar lista antes de rellenar

                for (document in result) {
                    val title = document.getString("titulo")
                    val provider = document.getString("proveedor")
                    val price = document.getDouble("precio")
                    val id = document.getString("id_producto")
                    val genre = document.getString("genero")
                    val publish_date = document.getTimestamp("fecha_publicacion")
                    val url = document.getString("url")
                    val mail = document.getString("mailProveedor")

                    if (userP != null) {
                        anadirCatalogo(inflater, title, provider, price, id, genre, publish_date, url, userP, editor, document.id, sh, mail)
                    }

                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        return root
    }

    // Método para añadir un producto al catálogo
    private fun anadirCatalogo(
        inflater: LayoutInflater,
        title: String?,
        provider: String?,
        price: Double?,
        id: String?,
        genre: String?,
        publishdate: Timestamp?,
        url: String?,
        userP: String,
        editorButton: SharedPreferences.Editor,
        articleId: String,
        cartbuttonsh: SharedPreferences,
        mail: String?
    ) {
        val itemView = inflater.inflate(R.layout.articulo, binding.FilasCatalogo, false)

        val titleTV: TextView = itemView.findViewById(R.id.tituloText)
        val priceTV: TextView = itemView.findViewById(R.id.pricetext)
        val imageIV: ImageView = itemView.findViewById(R.id.fotoImage)
        val trashbtn: Button = itemView.findViewById(R.id.trashBtn)
        val shopping_cart_btn:Button=itemView.findViewById(R.id.carritobtn)





        trashbtn.visibility = View.INVISIBLE
        shopping_cart_btn.visibility=View.INVISIBLE
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

        filaCatalogo.addView(itemView)
        productlist.add(itemView)  // Añadir a la lista para el filtro
    }

    // Método para filtrar los productos por el texto ingresado en el SearchView
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Liberar la referencia al binding
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            sellFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
