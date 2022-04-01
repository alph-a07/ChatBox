@file:Suppress("DEPRECATION")

package com.example.chatbox

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.chatbox.models.User
import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    val auth = FirebaseAuth.getInstance()
    private val db = Firebase.database
    private val myRef = db.getReference("Users")
    private val callbackManager = create()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(application)
        supportActionBar?.hide()

        val password1 = findViewById<EditText>(R.id.signup_password)
        val password2 = findViewById<EditText>(R.id.signup_password2)
        val email = findViewById<EditText>(R.id.signup_email)
        val name = findViewById<EditText>(R.id.signup_name)

        // what happens when signUp button is clicked
        findViewById<AppCompatButton>(R.id.btnSignUp).setOnClickListener {
            // if both passwords are same
            if (password1.text.toString() == password2.text.toString()) {
                // email-password signUp
                auth.createUserWithEmailAndPassword(
                    email.text.toString(),
                    name.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        // if signUp successful
                        if (task.isSuccessful) {
                            val user = auth.currentUser

                            // push data to firebase
                            val model = User()
                            model.userName = name.text.toString()
                            model.email = email.text.toString()
                            model.passwd = password2.text.toString()
                            model.uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

                            myRef.child(auth.uid.toString()).setValue(model)

                            updateUI(user)
                        } else {
                            Toast.makeText(
                                baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Both passwords should be same.", Toast.LENGTH_SHORT).show()
            }
        }

        hideViewPassword(password1, password2)
        hideViewPassword(password2, password1)

        findViewById<TextView>(R.id.signup_to_signin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        startActivity(Intent(this, ProfilePhotoActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    @SuppressLint("ClickableViewAccessibility")
    // function to handle hide/show operations of password
    // operated on both edittext simultaneously
    private fun hideViewPassword(password1: EditText, password2: EditText) {

        // onTouch Lambda
        password1.setOnTouchListener { _, event ->

            // ACTION_DOWN is for the first finger that touches the screen. This starts the gesture. The pointer data for this finger is always at index 0 in the MotionEvent.
            // ACTION_POINTER_DOWN is for extra fingers that enter the screen beyond the first. The pointer data for this finger is at the index returned by getActionIndex().
            // ACTION_POINTER_UP is sent when a finger leaves the screen but at least one finger is still touching it. The last data sample about the finger that went up is at the index returned by getActionIndex().
            // ACTION_UP is sent when the last finger leaves the screen. The last data sample about the finger that went up is at index 0. This ends the gesture.
            // ACTION_CANCEL means the entire gesture was aborted for some reason. This ends the gesture.
            if (event!!.action == MotionEvent.ACTION_DOWN) {

                // event.x => Co-ordinate of the touch event w.r.t parent layout
                // event.rawX => Co-ordinate of the touch event w.r.t. screen
                // password.right => Co-ordinate of right end of editText
                // password1.compoundDrawables[2].bounds.width() => width of drawable right
                // password1.right - password1.compoundDrawables[2].bounds.width()) => Co-ordinate of starting point of drawable right
                // Perform action only if drawable right is touched
                if (event.rawX >= (password1.right - password1.compoundDrawables[2].bounds.width())) {

                    // CASE 1 : Password is hidden
                    if (password1.tag == "hidden" && password2.tag == "hidden") {

                        // Make password visible
                        password1.transformationMethod = null  // no transformation on text
                        password2.transformationMethod = null  // visible as it is

                        // Change icons
                        password1.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.hide,
                            0
                        )
                        password2.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.hide,
                            0
                        )

                        // Tag as visible
                        password1.tag = "visible"
                        password2.tag = "visible"
                        return@setOnTouchListener true
                    }

                    // CASE 2 : Password is visible
                    if (password1.tag == "visible" && password2.tag == "visible") {

                        // Make password hidden
                        password1.transformationMethod =
                            PasswordTransformationMethod()  // transformed text
                        password2.transformationMethod =
                            PasswordTransformationMethod()  // visible as dots

                        // Change icons
                        password1.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.view,
                            0
                        )
                        password2.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.view,
                            0
                        )

                        // Tag as hidden
                        password1.tag = "hidden"
                        password2.tag = "hidden"
                        return@setOnTouchListener true
                    }
                }
            }
            return@setOnTouchListener false
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, WelcomeScreenActivity::class.java))
    }
}