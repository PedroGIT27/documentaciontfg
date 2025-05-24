package com.version2.tfgpedrollompart2

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class splashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*Dicen que el enable edgetoedge() debería estar aquí, sin embargo, esperare tranquilamente a ver si lo
        *pongo o no.
         */
        setContentView(R.layout.activity_splash) // Asegúrate de tener este layout
        // Usa el Looper principal para manejar el retraso
        Handler(Looper.getMainLooper()).postDelayed({
            val sh:SharedPreferences=getSharedPreferences("MYPREFERENCES", MODE_PRIVATE)
            val userP=sh.getString("name","")
            //Si no se ha iniciado Sesion localmente..
            if(userP.isNullOrEmpty()){
                //Metemos el registro.
                startActivity(Intent(this, RegisterActivity::class.java))
            }
            //Si se ha iniciado, iniciamos la aplicación.
            else{
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish() // Cierra SplashActivity para no volver atrás
        }, 3000) // 3000 ms = 3 segundos
    }
}