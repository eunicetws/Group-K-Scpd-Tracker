package com.example.myapplication.ui.clubs
import com.google.firebase.firestore.DocumentId

//Firestore needs default values (a no-arg constructor) to map documents onto these.
//Field names must match the field names in Firestore exactly.
data class Club(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val logoUrl: String = ""
)
