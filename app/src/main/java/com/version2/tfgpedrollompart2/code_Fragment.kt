package com.version2.tfgpedrollompart2

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView


class code_Fragment : Fragment() {

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
        val root= inflater.inflate(R.layout.fragment_code_, container, false)

        val sh: SharedPreferences =requireActivity().getSharedPreferences("FORGOTTENPASSWORD", MODE_PRIVATE)
        val editor=sh.edit()


        val emailEdit: TextView =root.findViewById(R.id.gmailEdit)
        val userNameEdit: TextView =root.findViewById(R.id.usernameEdit)
        val sendbtn: Button =root.findViewById(R.id.sendbtn)

        val arrowback: ImageButton =root.findViewById(R.id.arrow_back)

        arrowback.setOnClickListener(View.OnClickListener {

            requireActivity().supportFragmentManager.popBackStack()

        })

    return root
    }

    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            code_Fragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}