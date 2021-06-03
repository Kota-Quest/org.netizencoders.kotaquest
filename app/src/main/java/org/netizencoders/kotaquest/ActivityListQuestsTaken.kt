package org.netizencoders.kotaquest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import org.netizencoders.kotaquest.helpers.ListDataAdapter2
import org.netizencoders.kotaquest.models.Quest

class ActivityListQuestsTaken : AppCompatActivity() {
    private lateinit var progressbar: ProgressBar
    private lateinit var noDataLabel: TextView
    private lateinit var quests: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private var data: ArrayList<Quest> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listqueststaken)

        supportActionBar?.hide()

        quests = findViewById(R.id.quest_list_taken_items)
        quests.setHasFixedSize(true)
        progressbar = findViewById(R.id.quest_list_taken_progressbar)
        noDataLabel = findViewById(R.id.quest_list_taken_no_data)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.h2
        bottomNavigationView.setOnNavigationItemSelectedListener {
            Log.d("YAW", bottomNavigationView.selectedItemId.toString())
            when (it.itemId) {
                R.id.h1 -> move("1")
                R.id.h2 -> move("2")
                R.id.h3 -> move("3")
            }
            true
        }

        getData()
    }

    override fun onRestart() {
        super.onRestart()
        recreate()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun move(nav: String) {
        when (nav) {
            "1" -> {
                val intent = Intent(this,ActivityListQuests::class.java)
                startActivity(intent)
                finish()
            }
            "2" -> {
                val intent = Intent(this,ActivityListQuestsTaken::class.java)
                startActivity(intent)
                finish()
            }
            "3" -> {
                val intent = Intent(this,ActivityNewQuest::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun getData() {
        data.clear()

        noDataLabel.visibility = View.INVISIBLE
        quests.visibility = View.INVISIBLE
        progressbar.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection(ActivityLogin.uid+"-quests-taken")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.data["Status"].toString()!="Completed") {
                        Log.d("", document.toString())
                        val quest = Quest(
                            document.reference.id,
                            document.data["Title"].toString(),
                            document.data["Location"].toString(),
                            document.data["Description"].toString(),
                            document.data["ImageURL"].toString(),
                            document.data["Status"].toString(),
                            document.data["Poster"].toString(),
                            document.data["DatePosted"].toString(),
                            document.data["DateCompleted"].toString()
                        )
                        data.add(quest)
                    }
                }
                if (data.isNotEmpty()) {
                    noDataLabel.visibility = View.INVISIBLE
                    showRecyclerList(data)
                    quests.visibility = View.VISIBLE
                    progressbar.visibility = View.INVISIBLE
                } else {
                    noDataLabel.visibility = View.VISIBLE
                    progressbar.visibility = View.INVISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Log.d("", "Error: \n $exception")
                recreate()
            }
    }

    private fun showRecyclerList(data: ArrayList<Quest>) {
        quests.setHasFixedSize(true)
        quests.layoutManager = LinearLayoutManager(this)
        val listItemAdapter = ListDataAdapter2(data)
        quests.adapter = listItemAdapter

        listItemAdapter.notifyDataSetChanged()

        listItemAdapter.setOnItemBtnClickCallback(object : ListDataAdapter2.OnItemBtnClickCallback {
            override fun onItemBtnClicked(data: Quest, button: String) {
                when (button) {
                    "btnR" -> {
                        finishQuest(data)
                    }

                    "btnL" -> {
                        dropQuest(data)
                    }
                }
            }
        })
    }

    private fun finishQuest(quest: Quest) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(quest.Title)
        builder.setMessage("Report and finish Quest?")

        builder.setPositiveButton(
            "Yes") { _, _ ->
            removeQuest(quest.ID.toString(), ActivityLogin.uid+"-quests-taken")
            Toast.makeText(this, "Quest finished", Toast.LENGTH_SHORT).show()
            val moveIntent = Intent(this, ActivityListQuestsTaken::class.java)
            startActivity(moveIntent)
        }

        builder.setNegativeButton(
            "No") { _, _ ->
        }

        builder.show()
    }

    private fun dropQuest(quest: Quest) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(quest.Title)
        builder.setMessage("Are you sure you want to drop this Quest?")

        builder.setPositiveButton(
            "Yes") { _, _ ->
            prepareQuest(quest)
        }

        builder.setNegativeButton(
            "No") { _, _ ->
        }

        builder.show()
    }

    private fun prepareQuest(quest: Quest) {
        val data: HashMap<String, Any> = HashMap()
        data["ID"] = quest.ID.toString()
        data["Title"] = quest.Title.toString()
        data["Location"] = quest.Location.toString()
        data["Description"] = quest.Description.toString()
        data["ImageURL"] = quest.ImageURL.toString()
        data["Status"] = "Posted"
        data["Poster"] = quest.Poster.toString()
        data["DatePosted"] = quest.DatePosted.toString()
        data["DateCompleted"] = quest.DateCompleted.toString()

        removeQuest(quest.ID.toString(), ActivityLogin.uid+"-quests-taken")
        postQuest(data, "quests")
    }

    private fun removeQuest(questID: String, path: String) {
        if (questID.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection(path).document(questID)
                .delete()
                .addOnSuccessListener { Log.d("", "DocumentSnapshot successfully deleted!") }
                .addOnFailureListener { e -> Log.w("", "Error deleting document", e) }
        }
    }

    private fun postQuest(data: HashMap<String, Any>, path: String) {
        if (!data.isNullOrEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection(path)
                .add(data)
                .addOnSuccessListener {
                    Log.d("", "Success")
                    Toast.makeText(this, "Successfully dropped Quest", Toast.LENGTH_SHORT).show()
                    val moveIntent = Intent(this, ActivityListQuestsTaken::class.java)
                    startActivity(moveIntent)
                }
                .addOnFailureListener { e ->
                    Log.d("", "Error $e")
                    Toast.makeText(this, "Error: $e", Toast.LENGTH_LONG).show()
                }
        }
    }
}