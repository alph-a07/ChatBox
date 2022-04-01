package com.example.chatbox

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.facebook.CallbackManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {
    val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.database
    private val myRef = db.getReference("Users")
    private val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        // hide/view password function
        hideViewPassword(findViewById(R.id.login_password))

        // what happens when login button is clicked
        findViewById<AppCompatButton>(R.id.btnLogin).setOnClickListener {

            // email-password login
            auth.signInWithEmailAndPassword(
                findViewById<EditText>(R.id.login_email).text.toString(),
                findViewById<EditText>(R.id.login_password).text!!.toString()
            )
                .addOnCompleteListener(this) { task ->
                    // login successful
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        updateUI(user)
                    }
                    // login failed
                    else {

                        // if email is not valid
                        if (((task.exception as FirebaseAuthException?)!!.errorCode == "ERROR_MISSING_EMAIL")) {
                            Snackbar.make(
                                it,
                                "Enter a valid e-mail address.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }

                        // if invalid credentials are entered
                        else if (((task.exception as FirebaseAuthException?)!!.errorCode == "ERROR_WRONG_PASSWORD") ||
                            ((task.exception as FirebaseAuthException?)!!.errorCode == "ERROR_USER_NOT_FOUND") ||
                            ((task.exception as FirebaseAuthException?)!!.errorCode == "ERROR_USER_TOKEN_EXPIRED") ||
                            ((task.exception as FirebaseAuthException?)!!.errorCode == "ERROR_USER_DISABLED") ||
                            ((task.exception as FirebaseAuthException?)!!.errorCode == "ERROR_INVALID_EMAIL") ||
                            ((task.exception as FirebaseAuthException?)!!.errorCode == "ERROR_INVALID_CREDENTIAL") ||
                            ((task.exception as FirebaseAuthException?)!!.errorCode == "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL")
                        ) {
                            Snackbar.make(it, "Invalid credentials.", Snackbar.LENGTH_SHORT).show()
                        }

                        // unexpected error
                        else {
                            Snackbar.make(it, "Unexpected error occurred.", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
        }

        // Intent to signup activity
        findViewById<TextView>(R.id.login_to_signup).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
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
    private fun hideViewPassword(password1: EditText) {

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
                    if (password1.tag == "hidden") {

                        // Make password visible
                        password1.transformationMethod = null  // no transformation on text

                        // Change icons
                        password1.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.hide,
                            0
                        )

                        // Tag as visible
                        password1.tag = "visible"
                        return@setOnTouchListener true
                    }

                    // CASE 2 : Password is visible
                    if (password1.tag == "visible") {

                        // Make password hidden
                        password1.transformationMethod =
                            PasswordTransformationMethod()  // transformed text

                        // Change icons
                        password1.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.view,
                            0
                        )

                        // Tag as hidden
                        password1.tag = "hidden"
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