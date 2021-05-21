package org.netizencoders.kotaquest

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class   MainActivity : AppCompatActivity() {
//    private lateinit var button: Button

//    private val db = Firebase.firestore

//    private val user = hashMapOf(
//        "first" to "Dea",
//        "last" to "Aurora",
//        "born" to 2019
//    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        button = findViewById(R.id.button)
//        button.setOnClickListener {
//            addData()
//            readData()
//        }

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
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("", "Error getting documents.", exception)
            }
    }
}