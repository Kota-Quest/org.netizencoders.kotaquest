package org.netizencoders.kotaquest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class ActivitySplash : AppCompatActivity() {
    private var timeout:Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val actionBar = supportActionBar
        actionBar?.hide()

        loadSplashScreen()
    }

    private fun loadSplashScreen(){
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this,ActivityLogin::class.java)
            startActivity(intent)
            finish()
        }, timeout)
    }
}