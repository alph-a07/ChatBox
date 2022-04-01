package com.example.chatbox

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.chatbox.models.User
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class WelcomeScreenActivity : AppCompatActivity() {

    val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.database
    private val myRef = db.getReference("Users")
    private val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)
        supportActionBar?.hide()

        //region SUBTITLE ANIMATION
        // Load the ImageView that will host the animation and
        // set its background to our AnimationDrawable XML resource.
        val img: ImageView = findViewById<View>(R.id.welcome_subtitle) as ImageView
        img.setBackgroundResource(R.drawable.welcome_subtitle)

        // Get the background, which has been compiled to an AnimationDrawable object.
        val frameAnimation = img.background as AnimationDrawable

        // Start the animation (looped playback by default).
        frameAnimation.start()
        //endregion

        //region BACKGROUND ANIMATION
        val bg: RelativeLayout = findViewById<View>(R.id.welcome_bg) as RelativeLayout
        bg.setBackgroundResource(R.drawable.welcome_bg)
        (bg.background as AnimationDrawable).start()
        //endregion

        //region TITLE ANIMATION
        val img2: ImageView = findViewById<View>(R.id.welcome_title) as ImageView
        img2.setBackgroundResource(R.drawable.welcome_title)
        (img2.background as AnimationDrawable).start()
        //endregion

        //region BIRD ANIMATION
        val img3: ImageView = findViewById<View>(R.id.welcome_bird) as ImageView
        img3.setBackgroundResource(R.drawable.welcome_bird)
        (img3.background as AnimationDrawable).start()
        //endregion

        findViewById<TextView>(R.id.welcome_login).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<TextView>(R.id.welcome_signup).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        //region --> GOOGLE LOGIN CODE <--

        // helps open email access popup
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) // displays app's name and logo in popup
                .requestEmail()
                .build()

        findViewById<LinearLayout>(R.id.welcome_google).setOnClickListener {
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut() // clears cookies about last login and provides all email options each time
            startActivityForResult(googleSignInClient.signInIntent, 1)
        }
        //endregion

        findViewById<LinearLayout>(R.id.welcome_phone).setOnClickListener {
            startActivity(Intent(this, SignUp2Activity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<TextView>(R.id.tv_github).setOnClickListener {
            val uri: Uri =
                Uri.parse("https://github.com/alph-a07/Android-App-Development/tree/master/ChatBox")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    // must override to capture results from googleSignInClient
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            // task = account info
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!

                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "SignIn failed,try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        // user credentials
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                // Google signIn successful
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val model = User()
                    model.email = user?.email.toString()
                    model.userName = user?.displayName.toString()
                    model.uid = user?.uid.toString()
                    myRef.child(auth.uid.toString())
                        .setValue(model)

                    updateUI(user)
                }
                // Google signIn failed
                else {
                    Toast.makeText(this, "Sign in failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        startActivity(Intent(this, ProfilePhotoActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            //reload();
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}

