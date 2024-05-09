package adapter

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xpynx.todolist.R
import com.xpynx.todolist.model.NotesDataClass


class HighPriority(
    private val noteList: MutableList<NotesDataClass>
) : RecyclerView.Adapter<HighPriority.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.tvDescription)

        init {
            // Register the itemView for the context menu
            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            view: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            // Here, we can directly use the adapter position as there is no direct equivalent to AdapterView.AdapterContextMenuInfo
            val position = adapterPosition

            menu?.setHeaderTitle("Context Menu")
            menu?.add(Menu.NONE, R.id.menu_high_priority, Menu.NONE, "Remove Priority")
            menu?.add(Menu.NONE, R.id.menu_delete, Menu.NONE, "Delete")

            val note = noteList[position]

            menu?.getItem(1)?.setOnMenuItemClickListener {
                deleteDocumentFromFirebase(note.documentId)
                true
            }

            menu?.getItem(0)?.setOnMenuItemClickListener {
                removeFromToPriorityList(note.documentId)
                true
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.notes_adapter, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]
        holder.titleTextView.text = note.title
        holder.descriptionTextView.text = note.description


    }

    override fun getItemCount(): Int {
        return noteList.size
    }
    fun removeFromToPriorityList(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // Replace with the actual user ID

        // Reference to the document you want to update
        val documentReference = db.collection("notes")
            .document(userId)
            .collection("userNotes")
            .document(documentId)

        // Create a data map to update the priority
        val data = hashMapOf("isHighPriority" to false)

        documentReference.update(data as Map<String, Any>)
            .addOnSuccessListener {
                val position = findItemPosition(documentId)
                if (position != -1) {
                    noteList.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
            .addOnFailureListener { e ->
                // Handle the error if the document couldn't be updated
                // ...
            }
    }

    fun deleteDocumentFromFirebase(documentId: String) {
        val db = FirebaseFirestore.getInstance()

        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // Replace with the actual user ID

        // Reference to the document you want to delete
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
                }
            }
            .addOnFailureListener { e ->

            }
    }
    private fun findItemPosition(documentId: String): Int {
        // Find the index of the item by comparing documentId
        for (index in noteList.indices) {
            if (noteList[index].documentId == documentId) {
                return index
            }
        }
        return -1  // Item not found
    }

}
