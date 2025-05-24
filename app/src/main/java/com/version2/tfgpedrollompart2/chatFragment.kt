package com.version2.tfgpedrollompart2

import MyRecyclerViewAdapter
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
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore


class chatFragment : Fragment() {


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
        val root= inflater.inflate(R.layout.fragment_chat, container, false)

        val sh: SharedPreferences =requireActivity().getSharedPreferences("MYPREFERENCES",
            MODE_PRIVATE
        )
        val userP=sh.getString("name","")

        val currentUserName=userP
        val contactUser= arguments?.getString("Contact user","")
        val messagetxt: EditText = root.findViewById(R.id.messagetxt)

        val sendbtn:Button=root.findViewById(R.id.sendbtn)

        val messageList= mutableListOf<Message>()

        val arrowback:ImageButton=root.findViewById(R.id.arrow_back)

        arrowback.setOnClickListener(View.OnClickListener {

            requireActivity().supportFragmentManager.popBackStack()

        })

        //Set up the recyclerView
        val rv:RecyclerView=root.findViewById(R.id.rvMessage)
        rv.layoutManager=LinearLayoutManager(requireContext())

            val adapter =
                currentUserName?.let {
                    MyRecyclerViewAdapter(requireContext(), messageList, it)

                }

        if (adapter != null) {
            rv.adapter = adapter
        }


        val chatId = listOf(currentUserName.toString(), contactUser.toString()).sorted().joinToString("_")

        val db = Firebase.firestore
        db.collection("mensajes")
            .whereEqualTo("id",chatId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error leyendo mensajes", error)
                    return@addSnapshotListener
                }

                messageList.clear()
                for (doc in snapshot!!) {
                    val emisor = doc.getString("emisor") ?: ""
                    val receptor = doc.getString("receptor") ?: ""
                    val contenido = doc.getString("mensaje") ?: ""

                    val isRelevant = (emisor == currentUserName && receptor == contactUser) ||
                            (emisor == contactUser && receptor == currentUserName)

                    if (isRelevant) {
                        messageList.add(Message(emisor, contenido))
                    }
                }
                adapter?.notifyDataSetChanged()
                rv.scrollToPosition(messageList.size - 1)
            }





        sendbtn.setOnClickListener(View.OnClickListener {

    if(messagetxt.text.trim().toString().isEmpty()){
        return@OnClickListener
    }
    sendMessage(currentUserName,contactUser,messagetxt.text.toString())

    messagetxt.setText("")

})

        return root
    }

    private fun sendMessage(currentUserName: String?, contactUser: String?, messagetxt: String) {

        val db = Firebase.firestore


        val chatId = listOf(currentUserName.toString(), contactUser.toString()).sorted().joinToString("_")

        val message = hashMapOf(
            "id" to chatId,
            "emisor" to currentUserName,
            "receptor" to contactUser,
            "mensaje" to messagetxt,
            "timestamp" to FieldValue.serverTimestamp()
        )
        // Add a new document with a generated ID
        db.collection("mensajes")
            .add(message)
            .addOnSuccessListener { documentReference ->


                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            chatFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}