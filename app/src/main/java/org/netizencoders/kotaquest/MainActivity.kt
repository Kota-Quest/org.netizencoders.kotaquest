package org.netizencoders.kotaquest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class   MainActivity : AppCompatActivity() {
    private lateinit var tv: TextView
    private lateinit var button: Button
    private lateinit var sp1: Button
    private lateinit var sp2: Button
    private lateinit var sp3: Button

    private val db = Firebase.firestore

    private val user = hashMapOf(
        "first" to "Elmerulia",
        "last" to "Frixell",
        "born" to 2020
    )

    private var status = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        Log.d("", "Start")

        tv = findViewById(R.id.tvstatus)

        button = findViewById(R.id.buttontest)
        button.setOnClickListener {
            Toast.makeText(this@MainActivity, "Firebase test", Toast.LENGTH_SHORT).show()
            status = "Loading"
            tv.text = status
//            addData()
            readData()
        }

        sp1 = findViewById(R.id.listquest)
        sp1.setOnClickListener {
            val moveIntent = Intent(this, ActivityListQuest::class.java)
            startActivity(moveIntent)
        }

        sp2 = findViewById(R.id.login)
        sp2.setOnClickListener {
            val moveIntent = Intent(this, ActivityLogin::class.java)
            startActivity(moveIntent)
        }

        sp3 = findViewById(R.id.newquest)
        sp3.setOnClickListener {
            val moveIntent = Intent(this, ActivityNewQuest::class.java)
            startActivity(moveIntent)
        }

    }



    private fun addData() {
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("", "Error adding document", e)
            }
    }

    private fun readData() {
        status = ""
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("", "${document.id} => ${document.data}")
                    status += "${document.id} => ${document.data} \n"
                    tv.text = status
                }
            }
            .addOnFailureListener { exception ->
                Log.w("", "Error getting documents.", exception)
            }
        Log.d("", status)
    }
}