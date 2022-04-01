package com.example.chatbox

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.chatbox.models.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hbb20.CountryCodePicker
import java.util.concurrent.TimeUnit


class SignUp2Activity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    private val db = Firebase.database
    private val myRef = db.getReference("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_up2)
        supportActionBar?.hide()

        lateinit var storedVerificationId: String
        lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")

            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(baseContext, "Invalid request", Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(
                        baseContext,
                        "Too many attempts for this number.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                progressBar.visibility = View.GONE
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
                findViewById<ImageView>(R.id.btnVerify).setImageResource(R.drawable.otp_sent)
                findViewById<ImageView>(R.id.btnVerify).tag = "gotOTP"
                findViewById<RelativeLayout>(R.id.relative2).visibility = VISIBLE
                progressBar.visibility = View.GONE
            }
        }

        // signUp2 to signIn
        findViewById<TextView>(R.id.signup2_to_signin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        val edtPhone = findViewById<EditText>(R.id.editTextPhone)
        val btnOTP = findViewById<AppCompatButton>(R.id.btn_getOTP)
        val edtOTP = findViewById<EditText>(R.id.editTextOTP)
        val ccp = findViewById<CountryCodePicker>(R.id.ccp)
        val name = findViewById<EditText>(R.id.signup2_name)
        val signUp2 = findViewById<AppCompatButton>(R.id.btnSignUp2)

        ccp.registerCarrierNumberEditText(edtPhone) // register number with country code picker

        // button to receive OTP
        findViewById<ImageView>(R.id.btnVerify).setOnClickListener {
            progressBar.visibility = View.VISIBLE
            if (btnOTP.tag == "verifyOTP") {
                // Check if phone number is already logged in before
                myRef.orderByChild("phone").equalTo(ccp.fullNumberWithPlus)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            // number already exists
                            if (snapshot.exists()) {
                                Toast.makeText(
                                    baseContext,
                                    "Number already exists, Verifying..Please wait.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val options = PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber(ccp.fullNumberWithPlus)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(this@SignUp2Activity)                 // Activity (for callback binding)
                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                    .build()
                                PhoneAuthProvider.verifyPhoneNumber(options)
                                for (snap in snapshot.children) {
                                    val model = snap.getValue(User::class.java)
                                    if (model?.phone == ccp.fullNumberWithPlus) {
                                        name.setText(model?.userName)
                                        signUp2.text = "Login"
                                    }
                                }
                            }

                            // new number login
                            else {
                                if (ccp.isValidFullNumber) {
                                    val options = PhoneAuthOptions.newBuilder(auth)
                                        .setPhoneNumber(ccp.fullNumberWithPlus)       // Phone number to verify
                                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                        .setActivity(this@SignUp2Activity)                 // Activity (for callback binding)
                                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                        .build()
                                    PhoneAuthProvider.verifyPhoneNumber(options)
                                } else {
                                    progressBar.visibility = View.GONE
                                    Snackbar.make(
                                        it,
                                        "Enter a valid Phone Number!",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w(TAG, "Failed to read value.", error.toException())
                        }
                    })
            }
        }

        btnOTP.setOnClickListener {
            if (btnOTP.tag == "verifyOTP") {
                val otp = edtOTP.text.trim().toString()
                if (otp.isNotEmpty()) {
                    val credential: PhoneAuthCredential =
                        PhoneAuthProvider.getCredential(storedVerificationId, otp)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithCredential:success")
                                val user = task.result?.user
                                btnOTP.text = "Verified"
                                btnOTP.tag = "verified"
                                btnOTP.setTextColor(ContextCompat.getColor(this, R.color.green))

                                name.visibility = VISIBLE
                                signUp2.visibility = VISIBLE
                            } else {
                                // Sign in failed, display a message and update the UI
                                Log.w(TAG, "signInWithCredential:failure", task.exception)
                                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(
                                        this,
                                        task.exception.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                Toast.makeText(this, "Invalid OTP!", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Invalid OTP!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<AppCompatButton>(R.id.btnSignUp2).setOnClickListener {
            if (findViewById<CountryCodePicker>(R.id.ccp).isValidFullNumber &&
                findViewById<EditText>(R.id.signup2_name).text.toString().isNotEmpty()
            ) {
                val model = User()
                model.phone = ccp.fullNumberWithPlus
                model.userName = name.text.toString()
                model.uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                myRef.child(auth.uid.toString()).setValue(model)

                val intent = Intent(this@SignUp2Activity, ProfilePhotoActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(baseContext, "All fields are required.", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<TextView>(R.id.reset).setOnClickListener {
            edtPhone.text = SpannableStringBuilder("") // convert string to editable
            findViewById<RelativeLayout>(R.id.relative2).visibility = View.GONE
            name.visibility = View.GONE
            signUp2.visibility = View.GONE
            progressBar.visibility = View.GONE
            findViewById<ImageView>(R.id.btnVerify).setImageResource(R.drawable.send_otp)
        }
    }

    override fun onStart() {
        super.onStart()
        findViewById<EditText>(R.id.editTextPhone).requestFocus()
    }
}