package com.xpynx.todolist.fragment

import adapter.HighPriority
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.xpynx.todolist.R
import com.xpynx.todolist.model.NotesDataClass


class HighPriorityFragment : Fragment() {
    private lateinit var adapter: HighPriority
    private lateinit var noteList: MutableList<NotesDataClass>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_high_priority, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.highPriorityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        noteList = mutableListOf()

        loadHighPriorityNotes()
        adapter = HighPriority(noteList)

        recyclerView.adapter = adapter



        return view
    }

    private fun loadHighPriorityNotes() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val collectionReference = db.collection("notes")
            .document(userId!!)
            .collection("userNotes")

        val query = collectionReference.whereEqualTo("isHighPriority", true)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                val highPriorityNotes = mutableListOf<NotesDataClass>()

                for (document in querySnapshot) {
                    val documentId = document.id
                    val title = document.getString("title")
                    val isHighPriority = document.getBoolean("isHighPriority") ?: false
                    val description = document.getString("description")
                    val dueDate = document.getString("dueDate")

                    if (title != null && description != null) {
                        val note = dueDate?.let {
                            NotesDataClass(documentId, title, description,isHighPriority,
                                it
                            )
                        }
                        if (note != null) {
                            noteList.add(note)
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
             Toast.makeText(context,"unable to load",Toast.LENGTH_SHORT).show();
            }
    }
}
