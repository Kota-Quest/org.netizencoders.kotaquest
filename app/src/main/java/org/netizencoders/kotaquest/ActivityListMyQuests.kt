package org.netizencoders.kotaquest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore


class ActivityListMyQuests : AppCompatActivity() {
    private lateinit var qName: TextView
    private lateinit var qLocation: TextView
    private lateinit var qDesc: TextView
    private lateinit var qImage: ImageView
    private lateinit var qImageUrl: String
    private lateinit var qID: String
    private lateinit var data: HashMap<String, Any>
    private lateinit var buttonQL: Button
    private lateinit var buttonQM: Button
    private lateinit var buttonFinish: Button
    private lateinit var buttonDrop: Button

    private var listofquest = arrayListOf<Quest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listmyquests)

        qName = findViewById(R.id.myquest_name)
        qLocation = findViewById(R.id.myquest_location)
        qDesc = findViewById(R.id.myquest_desc)
        qImage = findViewById(R.id.myquest_img)

        buttonQL = findViewById(R.id.myquest_list)
        buttonQL.setOnClickListener {
            val moveIntent = Intent(this, ActivityListQuest::class.java)
            startActivity(moveIntent)
        }

        buttonQM = findViewById(R.id.myquest_my)
        buttonQM.setOnClickListener {
            val moveIntent = Intent(this, ActivityListMyQuests::class.java)
            startActivity(moveIntent)
        }

        buttonDrop = findViewById(R.id.myquest_drop)
        buttonDrop.setOnClickListener {
            onAlertDialogDrop(it)
        }

        buttonFinish = findViewById(R.id.myquest_finish)
        buttonFinish.setOnClickListener {
            onAlertDialogFinish(it)
        }

        readData()
    }

    override fun onRestart() {
        super.onRestart()
        recreate()
    }

    private fun readData() {
        qName.text = "Loading..."

        listofquest = arrayListOf()

        val db = FirebaseFirestore.getInstance()
        db.collection("myquests")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("", document.toString())
                    val quest = Quest(document.reference.id, document.data["Title"].toString(), document.data["Location"].toString(), document.data["Description"].toString(), document.data["ImageURL"].toString())
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
//                removeQuest(it.ID.toString())
            }

            qID = listofquest[0].ID.toString()
            qName.text = listofquest[0].Title.toString()
            qLocation.text = listofquest[0].Location.toString()
            qDesc.text = listofquest[0].Description.toString()

            qImageUrl = listofquest[0].ImageURL.toString()
            if (qImageUrl != "null") {
                Glide.with(applicationContext)
                    .load(qImageUrl)
                    .into(qImage)
            }

            buttonDrop.visibility = View.VISIBLE
            buttonFinish.visibility = View.VISIBLE
        } else {
            qName.text = "No Quests"
        }
    }

    private fun onAlertDialogDrop(view: View) {
        val builder = AlertDialog.Builder(view.context)

        builder.setTitle(qName.text)
        builder.setMessage("Are you sure you want to drop this Quest?")

        builder.setPositiveButton(
            "Yes") { _, _ ->
            prepareQuest()
        }

        builder.setNegativeButton(
            "No") { _, _ ->
        }

        builder.show()
    }

    private fun onAlertDialogFinish(view: View) {
        val builder = AlertDialog.Builder(view.context)

        builder.setTitle(qName.text)
        builder.setMessage("Finish and report this Quest?")

        builder.setPositiveButton(
            "Yes") { _, _ ->
            removeQuest(qID)
            Toast.makeText(this, "Quest finished",Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton(
            "No") { _, _ ->
        }

        builder.show()
    }

    private fun prepareQuest() {
        val q = Quest(qID, qName.text.toString(), qLocation.text.toString(), qDesc.text.toString(),
            qImageUrl
        )
        data = HashMap()
        data["ID"] = q.ID.toString()
        data["Title"] = q.Title.toString()
        data["Location"] = q.Location.toString()
        data["Description"] = q.Description.toString()
        data["ImageURL"] = q.ImageURL.toString()

        Log.d("", data.toString())
        removeQuest(data["ID"].toString())
        postQuest()
    }

    private fun removeQuest(qID: String) {
        buttonDrop.visibility = View.INVISIBLE
        buttonFinish.visibility = View.INVISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection("myquests").document(qID)
            .delete()
            .addOnSuccessListener { Log.d("", "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w("", "Error deleting document", e) }
    }

    private fun postQuest() {
        if (!data.isNullOrEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("quests")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Quest dropped",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: $e", Toast.LENGTH_LONG).show()
                }
        }
        val moveIntent = Intent(this, ActivityListMyQuests::class.java)
        startActivity(moveIntent)
    }
}