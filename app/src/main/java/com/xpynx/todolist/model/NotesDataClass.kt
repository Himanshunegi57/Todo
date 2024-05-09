package com.xpynx.todolist.model


data class NotesDataClass(
    val documentId: String,
    var title: String,
    var description: String,
    var isHighPriority: Boolean,
    var dueDate: String,
)


