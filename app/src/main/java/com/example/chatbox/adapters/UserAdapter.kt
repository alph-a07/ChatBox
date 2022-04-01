package com.example.chatbox.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatbox.ChatActivity
import com.example.chatbox.R
import com.example.chatbox.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class UserAdapter(val list: ArrayList<User>, val context: Context?) :
    RecyclerView.Adapter<UserAdapter.viewHolder>() {

    class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profile: ImageView = itemView.findViewById(R.id.user_profile)
        var name: TextView = itemView.findViewById(R.id.user_name)
        var lastMsg: TextView = itemView.findViewById(R.id.user_lastmsg)
        var time: TextView = itemView.findViewById(R.id.user_time)
        var layout: LinearLayout = itemView.findViewById(R.id.user_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sample_users, parent, false)

        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val temp = list[position]


        if(temp.profile!=""){
            Picasso.get().load(Uri.parse(temp.profile))
                .into(holder.profile)
        }
        else
            holder.profile.setImageResource(R.drawable.basic_user)

        holder.name.text = temp.userName

        //this will update the last message in chat fragment.
        Firebase.database.reference.child("Chats")
            .child(FirebaseAuth.getInstance().uid + temp.uid)
            .orderByKey().limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children) {
                        holder.lastMsg.text = snap.child("msg").getValue(String::class.java)
                        holder.time.text = snap.child("time").getValue(String::class.java)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        holder.layout.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", temp.userName)
            intent.putExtra("profile", temp.profile)
            intent.putExtra("uid", temp.uid)

            context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}