package com.xpynx.todolist.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xpynx.todolist.R
import com.xpynx.todolist.auth.Login
import com.xpynx.todolist.model.NotesDataClass


class NotesAdapter(
    private val noteList: MutableList<NotesDataClass>
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {


    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {

        val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.tvDescription)
        val priority: TextView = itemView.findViewById(R.id.tvPriority)
        val dueDate: TextView = itemView.findViewById(R.id.tvDueDate)

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            view: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            val position = adapterPosition

            menu?.add(Menu.NONE, R.id.menu_edit, Menu.NONE, "Edit")
            menu?.add(Menu.NONE, R.id.menu_high_priority, Menu.NONE, "High Priority")
            menu?.add(Menu.NONE, R.id.menu_delete, Menu.NONE, "Delete")

            menu?.add(Menu.NONE, R.id.menu_logout, Menu.NONE, "Logout")

            val note = noteList[position]


            menu?.getItem(0)?.setOnMenuItemClickListener {
                showEditDialog(noteList[position], itemView,note.documentId)
                true
            }
            menu?.getItem(1)?.setOnMenuItemClickListener {

                addToPriorityList(note.documentId,itemView)

                true
            }
            menu?.getItem(2)?.setOnMenuItemClickListener {
                deleteDocumentFromFirebase(note.documentId,itemView)
                true
            }
            menu?.getItem(3)?.setOnMenuItemClickListener {
                logOut(itemView)
                true
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.notes_adapter, parent, false)
        return NoteViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]
        holder.titleTextView.text = note.title
        holder.descriptionTextView.text = note.description
        holder.dueDate.text = "Due Date:${note.dueDate}";
        holder.priority.text = if(note.isHighPriority) "Priority: High" else "Priority: Low"

        holder.itemView.setOnClickListener {

            Toast.makeText(
                holder.itemView.context,
                "Please hold the note for more options",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int {
        return noteList.size
    }
    fun addToPriorityList(documentId: String, itemView: View) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val documentReference = db.collection("notes")
            .document(userId)
            .collection("userNotes")
            .document(documentId)

        val data = hashMapOf("isHighPriority" to true)

        documentReference.update(data as Map<String, Any>)
            .addOnSuccessListener {
showToast("Added to priority",itemView)
                    }
            .addOnFailureListener { e ->

                showToast("unable to update priority",itemView)
            }
    }

    fun deleteDocumentFromFirebase(documentId: String, itemView: View) {
        val db = FirebaseFirestore.getInstance()

        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val documentReference = db.collection("notes")
            .document(userId)
            .collection("userNotes")
            .document(documentId)

        documentReference.delete()
            .addOnSuccessListener {
                val position = findItemPosition(documentId)
                if (position != -1) {
                    noteList.removeAt(position)
                    notifyItemRemoved(position)
                    showToast("Successfully deleted",itemView)
                }


            }
            .addOnFailureListener { e ->
                showToast("Unable To delete",itemView)

            }
    }
    private fun findItemPosition(documentId: String): Int {
        for (index in noteList.indices) {
            if (noteList[index].documentId == documentId) {
                return index
            }
        }
        return -1
    }


    private fun showEditDialog(note: NotesDataClass, itemView: View,documentId: String) {
        val dialogView: View = LayoutInflater.from(itemView.context).inflate(R.layout.popup_add_note, null)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)

        etTitle.setText(note.title)
        etDescription.setText(note.description)

        val dialog = AlertDialog.Builder(itemView.context)
            .setTitle("Edit Note")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedTitle = etTitle.text.toString()
                val updatedDescription = etDescription.text.toString()

                val db=FirebaseFirestore.getInstance();
                val userId=FirebaseAuth.getInstance().currentUser?.uid.toString()
                val documentReference = db.collection("notes")
                    .document(userId)
                    .collection("userNotes")
                    .document(documentId)
                val data = hashMapOf(
                    "title" to updatedTitle,
                    "description" to updatedDescription
                )

                documentReference.update(data as Map<String, Any>)
                    .addOnSuccessListener {
                        for (index in noteList.indices) {
                            if (noteList[index].documentId == documentId) {
                                noteList[index].description=updatedDescription;
                                noteList[index].title=updatedTitle
                                showToast("Note updated successfully",itemView)
                            }

                        }

                        notifyDataSetChanged()



                    }
                    .addOnFailureListener { e ->
                        showToast("Unable to update note",itemView)
                    }


                notifyDataSetChanged()
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .create()

        dialog.show()
    }
    private fun logOut(itemView: View) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()
showToast("Logged out",itemView)
              val intent = Intent(itemView.context, Login::class.java)
        itemView.context.startActivity(intent)
        if (itemView.context is Activity) {
            (itemView.context as Activity).finish()
        }    }



    private fun showToast(message: String,itemView: View) {
        Toast.makeText(itemView.context, message, Toast.LENGTH_SHORT).show()
    }
    fun filter(query: String) {
        println("noteList: $noteList")
        println("query query"+query)
        val filteredList = mutableListOf<NotesDataClass>()

        if (query.isBlank()) {
            println("Query is blank. Adding all items from noteList to filteredList.")
            filteredList.addAll(noteList)
            notifyDataSetChanged()
        } else {
            val filterPattern = query.trim().toLowerCase()
            println("Filtering with pattern: $filterPattern")
            noteList.forEach { note ->
                if (note.title.toLowerCase().contains(filterPattern)) {
                    filteredList.add(note)
                }
            }
        }

        println("filteredList: $filteredList")

        noteList.clear()
        noteList.addAll(filteredList)
        notifyDataSetChanged()
    }

}
