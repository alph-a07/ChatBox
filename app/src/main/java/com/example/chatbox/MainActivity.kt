package com.example.chatbox

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.chatbox.databinding.ActivityMainBinding
import com.example.chatbox.models.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    val currentUser = FirebaseAuth.getInstance().currentUser

    private val db = Firebase.database
    private val myRef = db.getReference("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //val value = snapshot.getValue<User>()
                //if (snapshot.key == currentUser?.uid) {

                //Log.d(TAG, "Value is: $value")

                for (snap in snapshot.children) {
                    val model = snap.getValue(User::class.java)
                    if (model != null && snap.key == currentUser?.uid) {

                        val navigationView: NavigationView = findViewById(R.id.nav_view)
                        val header: View = navigationView.getHeaderView(0)
                        header.findViewById<TextView>(R.id.profile_username).text = model.userName

                        if (model.email.isBlank()) {
                            header.findViewById<TextView>(R.id.profile_email_phone).text = model.phone
                        } else {
                            header.findViewById<TextView>(R.id.profile_email_phone).text = model.email
                        }

                        if (model.profile == "") {
                            header.findViewById<ImageView>(R.id.imageView2)
                                .setImageResource(R.drawable.basic_user)
                        } else {
                            Picasso.get().load(Uri.parse(model.profile))
                                .into(header.findViewById<ImageView>(R.id.imageView2))
                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }

        })

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Toast.makeText(this, "axas", Toast.LENGTH_SHORT).show()
            Firebase.auth.signOut()
            val logoutIntent= Intent(this,WelcomeScreenActivity::class.java)
            logoutIntent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(logoutIntent)
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_chats, R.id.nav_calls, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        startActivity(intent)
    }
}