package fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.xpynx.todolist.R
import com.xpynx.todolist.adapter.NotesAdapter
import com.xpynx.todolist.model.NotesDataClass
import java.util.Calendar


class TodoListFragment : Fragment() {

    private lateinit var dialogView: View
    private lateinit var alertDialog: AlertDialog
    private lateinit var searchView: SearchView

    private lateinit var recyclerView: RecyclerView
    private lateinit var noteList: MutableList<NotesDataClass>
    private lateinit var adapter: NotesAdapter
    private lateinit var fabAddNote: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_todo_list, container, false)

        recyclerView = view.findViewById(R.id.todoRecyclerView)
        searchView = view.findViewById(R.id.searchView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        noteList = mutableListOf()
        fetchNotesFromFirestore()

        adapter = NotesAdapter(noteList)
        recyclerView.adapter = adapter
        fabAddNote = view.findViewById(R.id.fabAddNote)
        dialogView = layoutInflater.inflate(R.layout.popup_add_note, null)

        fabAddNote.setOnClickListener {
            dialogView = layoutInflater.inflate(R.layout.popup_add_note, null)

            alertDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    addNotesToFirebase()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    alertDialog.dismiss()
                }
                .show()
        }


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()) {
                    adapter.filter(newText)
                } else {
                    fetchNotesFromFirestore()
                }
                return true
            }
        })



        return view
    }

    private fun fetchNotesFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val db = FirebaseFirestore.getInstance()
        val notesCollection = db.collection("notes")
        noteList.clear()
        val query: Query = notesCollection.document(userId)
            .collection("userNotes")

        query.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val documentId = document.id
                    val title = document.getString("title")
                    val isHighPriority = document.getBoolean("isHighPriority") ?: false
                    val description = document.getString("description")
                    val dueDate = document.getString("dueDate")

                    if (title != null && description != null) {
                        val note = dueDate?.let {
                            NotesDataClass(documentId, title, description, isHighPriority,
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
            .addOnFailureListener {
                Toast.makeText(context, "Error Loading Notes", Toast.LENGTH_SHORT).show()
            }
    }




    private fun addNotesToFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)

        val title = etTitle.text.toString()
        val description = etDescription.text.toString()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(context, "Both fields are required to add note", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedDate = "${datePicker.year}-${datePicker.month + 1}-${datePicker.dayOfMonth}"

        val noteData = hashMapOf(
            "title" to title,
            "description" to description,
            "dueDate" to selectedDate
        )

        val db = FirebaseFirestore.getInstance()
        val userNotesCollection = db.collection("notes").document(userId)
            .collection("userNotes")

        userNotesCollection.add(noteData)
            .addOnSuccessListener { documentReference ->
                fetchNotesFromFirestore()
            }
            .addOnFailureListener { e ->
Toast.makeText(context,"Unable to add Note",Toast.LENGTH_SHORT).show();
            }
    }

    private fun setHighPriority(documentId: String) {
        Toast.makeText(context, documentId, Toast.LENGTH_SHORT).show()
    }
}
