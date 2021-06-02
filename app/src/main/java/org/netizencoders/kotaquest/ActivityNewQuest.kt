package org.netizencoders.kotaquest

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.*
import kotlin.collections.HashMap


var storage: FirebaseStorage? = null
var storageReference: StorageReference? = null


class ActivityNewQuest : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var btnChoose: Button
    private lateinit var btnContinue: Button
    private lateinit var qTitle: EditText
    private lateinit var qLocation: EditText
    private lateinit var qDesc: EditText
    private lateinit var qImgURL: String

    private var filePath: Uri? = null
    private val PICK_IMAGE_REQUEST = 71

    private lateinit var imagePicker: ActivityResultLauncher<Intent>
    private lateinit var data: HashMap<String, Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newquest)

        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        imageView = findViewById(R.id.imgView)
        btnChoose = findViewById(R.id.btnChoose)
        btnContinue = findViewById(R.id.newquest_post)
        qTitle = findViewById(R.id.newquest_name)
        qLocation = findViewById(R.id.newquest_location)
        qDesc = findViewById(R.id.newquest_desc)
        qTitle.setText("")
        qLocation.setText("")
        qDesc.setText("")
        qImgURL = ""

        imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(PICK_IMAGE_REQUEST, result)
        }

        btnChoose.setOnClickListener {
            chooseImage()
        }

        btnContinue.setOnClickListener {
            commit()
        }
    }

    private fun commit() {
        Toast.makeText(this, "Processing...", Toast.LENGTH_LONG).show()
        if(filePath != null) {
            uploadImage()
        } else {
            prepareQuest()
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        imagePicker.launch(Intent.createChooser(
            intent,
            "Please select..."
        ))
    }

    private fun uploadImage() {
        if(filePath != null){
            val ref = storageReference?.child("images/" + UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(filePath!!)

            uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    qImgURL = downloadUri.toString()
                    prepareQuest()
                }
            }?.addOnFailureListener{
                Toast.makeText(this, "Error: " + it.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onActivityResult(requestCode: Int, result: ActivityResult) {
        if(result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    if (intent != null) {
                        filePath = intent.data
                    }
                    val bitmap: Bitmap
                    val contentResolver = contentResolver
                    try {
                        bitmap = if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                        } else {
                            val source = ImageDecoder.createSource(contentResolver, filePath!!)
                            ImageDecoder.decodeBitmap(source)
                        }
                        imageView.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun prepareQuest() {
        val q = Quest(qTitle.text.toString(), qLocation.text.toString(), qDesc.text.toString(),
            qImgURL
        )
        data = HashMap()
        data["Title"] = q.Title.toString()
        data["Location"] = q.Location.toString()
        data["Description"] = q.Description.toString()
        data["ImageURL"] = q.ImageURL.toString()

        Log.d("", data.toString())

        postQuest()
    }

    private fun postQuest() {
        if (!data.isNullOrEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("quests")
                .add(data)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Quest posted. \nID: ${documentReference.id}", Toast.LENGTH_LONG).show()
                    val moveIntent = Intent(this, ActivityListQuest::class.java)
                    startActivity(moveIntent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: $e", Toast.LENGTH_LONG).show()
                }
        }
    }
}
