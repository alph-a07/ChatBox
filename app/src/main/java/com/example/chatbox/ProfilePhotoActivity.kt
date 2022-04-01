package com.example.chatbox

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.chatbox.models.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile_photo.*
import java.io.File

class ProfilePhotoActivity : AppCompatActivity() {
    @SuppressLint("UseCompatLoadingForDrawables")

    private lateinit var tempImageUri: Uri
    private var tempImagePath = ""

    // Create a storage reference from our app
    private val storage = FirebaseStorage.getInstance()
    private var storageRef = storage.reference
    private val auth = FirebaseAuth.getInstance()
    private val imageRef = storageRef.child("profileImage")
        .child(auth.currentUser?.uid.toString())
    private lateinit var progressBar:ProgressBar
    private lateinit var pleaseWait:TextView

    private val selectPictureLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            Picasso.get().load(it).into(preview)
            uploadImageUri(it)
            preview.tag = "galleryPictureSelected"
            findViewById<ImageView>(R.id.user_male).setImageResource(R.drawable.user_male)
            findViewById<ImageView>(R.id.user_female).setImageResource(R.drawable.user_female)
            progressBar.visibility = View.VISIBLE
            pleaseWait.visibility = View.VISIBLE
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Picasso.get().load(tempImageUri).into(preview)
                uploadImageUri(tempImageUri)
                preview.tag = "cameraPictureSelected"
                findViewById<ImageView>(R.id.user_male).setImageResource(R.drawable.user_male)
                findViewById<ImageView>(R.id.user_female).setImageResource(R.drawable.user_female)
                progressBar.visibility = View.VISIBLE
                pleaseWait.visibility = View.VISIBLE
            }
        }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_photo)
        supportActionBar?.hide()

        val maleUser = findViewById<ImageView>(R.id.user_male)
        val femaleUser = findViewById<ImageView>(R.id.user_female)
        progressBar = findViewById(R.id.progressBar3)
        pleaseWait = findViewById(R.id.profile_wait)

        maleUser.setOnClickListener {
            when (maleUser.tag) {
                "maleLogo" -> {
                    if (femaleUser.tag == "femaleLogo") {
                        maleUser.setImageResource(R.drawable.user_male_selected)
                        maleUser.tag = "maleLogoSelected"
                        preview.setImageResource(R.drawable.user_male)
                        preview.tag = "maleLogo"
                    }
                    if (femaleUser.tag == "femaleLogoSelected") {
                        femaleUser.setImageResource(R.drawable.user_female)
                        femaleUser.tag = "femaleLogo"
                        maleUser.setImageResource(R.drawable.user_male_selected)
                        maleUser.tag = "maleLogoSelected"
                        preview.setImageResource(R.drawable.user_male)
                        preview.tag = "maleLogo"
                    }
                }
                "maleLogoSelected" -> {
                    maleUser.setImageResource(R.drawable.user_male)
                    maleUser.tag = "maleLogo"
                    preview.setImageResource(R.drawable.basic_user)
                    preview.tag = "noPhoto"
                }
            }
        }

        femaleUser.setOnClickListener {
            when (femaleUser.tag) {
                "femaleLogo" -> {
                    if (maleUser.tag == "maleLogo") {
                        femaleUser.setImageResource(R.drawable.user_female_selected)
                        femaleUser.tag = "femaleLogoSelected"
                        preview.setImageResource(R.drawable.user_female)
                        preview.tag = "femaleLogo"
                    }
                    if (maleUser.tag == "maleLogoSelected") {
                        maleUser.setImageResource(R.drawable.user_male)
                        maleUser.tag = "maleLogo"
                        femaleUser.setImageResource(R.drawable.user_female_selected)
                        femaleUser.tag = "femaleLogoSelected"
                        preview.setImageResource(R.drawable.user_female)
                        preview.tag = "femaleLogo"
                    }
                }
                "femaleLogoSelected" -> {
                    femaleUser.setImageResource(R.drawable.user_female)
                    femaleUser.tag = "femaleLogo"
                    preview.setImageResource(R.drawable.basic_user)
                    preview.tag = "noPhoto"
                }
            }
        }

        // Layout for dialog
        val view = layoutInflater.inflate(R.layout.edt_profile_popup, null)

        // Object of Dialog
        val dialog = Dialog(this)
        // to avoid black corners
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // animation
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        // Opening dialog
        findViewById<ImageView>(R.id.floatingActionButton).setOnClickListener {
            // Assigning layout
            dialog.setContentView(view)
            // Showing bottom sheet
            dialog.show()
        }

        val camera = view.findViewById<LinearLayout>(R.id.ll1)
        val gallery = view.findViewById<LinearLayout>(R.id.ll2)

        camera?.setOnClickListener {

            tempImageUri = FileProvider.getUriForFile(
                this,
                "com.example.chatbox.provider", createImageFile().also {
                    tempImagePath = it.absolutePath
                }
            )
            cameraLauncher.launch(tempImageUri)
            dialog.dismiss()
        }

        gallery?.setOnClickListener {
            selectPictureLauncher.launch("image/*")
            dialog.dismiss()
        }

        findViewById<Button>(R.id.button).setOnClickListener {
            val intent1 = Intent(this, MainActivity::class.java)
            if (preview.tag == "cameraPictureSelected" ||
                preview.tag == "galleryPictureSelected" ||
                preview.tag == "maleLogo" ||
                preview.tag == "femaleLogo"
            ) {
                if (preview.tag == "maleLogo") {
                    val uri = Uri.parse("android.resource://com.example.chatbox/drawable/user_male")
                    uploadImageUri(uri)
                } else if (preview.tag == "femaleLogo") {
                    val uri =
                        Uri.parse("android.resource://com.example.chatbox/drawable/user_female")
                    uploadImageUri(uri)
                }
                startActivity(intent1)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                Snackbar.make(it, "Please select a profile picture!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("temp_image", ".jpg", storageDir)
    }

    private fun uploadImageUri(uri: Uri?) {
        if (uri != null) {
            imageRef.putFile(uri).addOnSuccessListener {
                imageRef?.downloadUrl.addOnSuccessListener {
                    Firebase.database.reference.child("Users")
                        .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                        .child("profile")
                        .setValue(it.toString())
                    progressBar.visibility = View.GONE
                    pleaseWait.text = "Profile successfully updated!"
                    pleaseWait.setTextColor(ContextCompat.getColor(this,R.color.green))
                    Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        Firebase.database.getReference("Users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val model = snap.getValue(User::class.java)
                    if (model != null && snap.key == FirebaseAuth.getInstance().currentUser?.uid) {
                        Picasso.get().load(Uri.parse(model.profile))
                            .into(findViewById<ImageView>(R.id.preview))
                    }
                    if (snap.key == FirebaseAuth.getInstance().currentUser?.uid && model?.profile == "") {
                        findViewById<ImageView>(R.id.preview)
                            .setImageResource(R.drawable.basic_user)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}
