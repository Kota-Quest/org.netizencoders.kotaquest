package org.netizencoders.kotaquest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class ActivityLogin : AppCompatActivity() {
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button = findViewById(R.id.login)
        button.setOnClickListener {
            val moveIntent = Intent(this, ActivityListQuest::class.java)
            startActivity(moveIntent)
        }
    }
}