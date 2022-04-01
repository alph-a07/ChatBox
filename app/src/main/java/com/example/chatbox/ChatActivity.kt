package com.example.chatbox

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatbox.adapters.ChatAdapter
import com.example.chatbox.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


class ChatActivity : AppCompatActivity() {
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.hide()

        val senderId = FirebaseAuth.getInstance().uid
        val receiverId = intent.getStringExtra("uid").toString()
        val receiverName = intent.getStringExtra("name")
        val receiverProfile = intent.getStringExtra("profile")

        val name = findViewById<TextView>(R.id.chat_user_name)
        val profile = findViewById<CircleImageView>(R.id.chat_profile)

        name.text = receiverName
        Picasso.get().load(Uri.parse(receiverProfile))
            .into(profile)

        val list = ArrayList<Message>()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView1)
        val adapter = ChatAdapter(list, this)

        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true            // To scroll to the latest message when chat is opened
        recyclerView.layoutManager = layoutManager

        val edtChat = findViewById<EditText>(R.id.chat_msg_edt)
        val btnSend = findViewById<ImageView>(R.id.chat_send)
        val btnAttachment = findViewById<ImageView>(R.id.chat_attachment)

        // TextWatcher to avoid empty message
        edtChat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // enable send button only when message is not empty
                if (s.toString().trim().isNotEmpty()) {
                    btnSend.setImageResource(R.drawable.send)
                    btnAttachment.visibility  = View.GONE
                } else if (s.toString().trim().isEmpty()) {
                    btnSend.setImageResource(R.drawable.microphone)
                    btnAttachment.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        btnSend.setOnClickListener {
            val msg = edtChat.text.toString()

            val model = Message()
            model.msg = msg
            model.time = SimpleDateFormat("hh:mm").format(Date()).toString()
            model.senderID = senderId.toString()

            if (edtChat.text.trim().isNotEmpty()) {
                // push the message to both sender's and receiver's database
                Firebase.database.reference.child("Chats")
                    .child(senderId + receiverId)
                    .push()
                    .setValue(model).addOnSuccessListener {
                        Firebase.database.reference.child("Chats")
                            .child(receiverId + senderId)
                            .push()
                            .setValue(model).addOnSuccessListener {
                            }
                    }
            }
            edtChat.setText(getString(R.string.empty_string))
        }

        FirebaseDatabase.getInstance().getReference("Chats").child(senderId + receiverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()
                    for (snap in snapshot.children) {
                        val model: Message = snap.getValue(Message::class.java)!!
                        list.add(model)
                    }
                    adapter.notifyDataSetChanged()
                    // Smoothly scroll to latest message when a new message is sent or received
                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        findViewById<ImageView>(R.id.chat_back).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}