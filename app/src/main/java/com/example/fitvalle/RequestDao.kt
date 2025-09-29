package com.example.fitvalle

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RequestDao {
    private val dbRef: DatabaseReference = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("request")

    fun saveRequest(request: Request, callback: (Boolean, String) -> Unit) {
        dbRef.child(request.id).setValue(request)
            .addOnSuccessListener { callback(true, "Solicitud enviada") }
            .addOnFailureListener { e -> callback(false, e.message ?: "Error guardando solicitud") }
    }
}
