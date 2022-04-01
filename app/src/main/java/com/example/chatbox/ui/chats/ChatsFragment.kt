package com.example.chatbox.ui.chats

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatbox.R
import com.example.chatbox.adapters.UserAdapter
import com.example.chatbox.databinding.FragmentChatsBinding
import com.example.chatbox.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatsFragment : Fragment() {

    private var _binding: FragmentChatsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    val list = ArrayList<User>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val db = Firebase.database
        val auth = FirebaseAuth.getInstance()
        val adapter = UserAdapter(list, context) // initialize in onCreateView to avoid NPE

        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        db.reference.child("Users").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (snap in snapshot.children) {
                    val user = snap.getValue(User::class.java)
                    if (user != null && auth.currentUser?.uid != snap.key) {
                        list.add(user)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        return root
    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({
            _binding?.progressBar2?.visibility = View.GONE
        }, 5000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}