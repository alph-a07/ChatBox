package com.example.chatbox.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatbox.R
import com.example.chatbox.models.Message
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(private val list: ArrayList<Message>, val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMsg = itemView.findViewById<TextView>(R.id.sent_msg)!!
        val sentTime = itemView.findViewById<TextView>(R.id.sent_time)!!
    }

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMsg = itemView.findViewById<TextView>(R.id.received_msg)!!
        val receivedTime = itemView.findViewById<TextView>(R.id.received_time)!!
    }

    // 1 -> Use sender viewHolder
    // 2 -> Use receiver viewHolder
    override fun getItemViewType(position: Int): Int {
        return if (list[position].senderID == FirebaseAuth.getInstance().uid)
            1
        else
            0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1)
            SentViewHolder(
                LayoutInflater.from(context).inflate(R.layout.sample_sent, parent, false)
            )
        else
            ReceivedViewHolder(
                LayoutInflater.from(context).inflate(R.layout.sample_received, parent, false)
            )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val temp = list[position]

        if (holder.itemViewType == 1) {
            (holder as SentViewHolder).sentMsg.text = temp.msg
            holder.sentTime.text = temp.time
        }
        else {
            (holder as ReceivedViewHolder).receivedMsg.text = temp.msg
            holder.receivedTime.text = temp.time

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}