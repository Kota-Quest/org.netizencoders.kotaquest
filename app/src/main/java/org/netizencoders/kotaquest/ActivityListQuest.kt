package org.netizencoders.kotaquest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore


class ActivityListQuest : AppCompatActivity() {
    private lateinit var qName: TextView
    private lateinit var qLocation: TextView
    private lateinit var qDesc: TextView
    private lateinit var qImage: ImageView
    private lateinit var buttonNew: Button

    private var listofquest = arrayListOf<Quest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listquest)

        qName = findViewById(R.id.quest_name)
        qLocation = findViewById(R.id.quest_location)
        qDesc = findViewById(R.id.quest_desc)
        qImage = findViewById(R.id.quest_img)

        buttonNew = findViewById(R.id.quest_new)
        buttonNew.setOnClickListener {
            val moveIntent = Intent(this, ActivityNewQuest::class.java)
            startActivity(moveIntent)
        }

        readData()
    }

    override fun onRestart() {
        super.onRestart()
        recreate()
    }

    private fun readData() {
        listofquest = arrayListOf()

        val db = FirebaseFirestore.getInstance()
        db.collection("quests")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val quest = Quest(document.data["Title"].toString(), document.data["Location"].toString(), document.data["Description"].toString(), document.data["ImageURL"].toString())
                    listofquest.add(quest)
                }
                setData()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: \n $exception", Toast.LENGTH_LONG).show()
            }
    }

    private fun setData() {
        if (!listofquest.isNullOrEmpty()) {
            listofquest.reverse()

            listofquest.forEach {
                Log.d("", it.toString())
            }

            qName.text = listofquest[0].Title.toString()
            qLocation.text = listofquest[0].Location.toString()
            qDesc.text = listofquest[0].Description.toString()

            val qImageUrl = listofquest[0].ImageURL.toString()
            if (qImageUrl != "null") {
                Glide.with(applicationContext)
                    .load(qImageUrl)
                    .into(qImage)
            }
        }
    }
}