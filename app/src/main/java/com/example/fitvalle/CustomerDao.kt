package com.example.fitvalle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
class CustomerDao {
    private val dbRef: DatabaseReference = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("customer")

    fun saveCustomer(customer: Customer, callback: (Boolean, String) -> Unit) {
        dbRef.child(customer.id).setValue(customer)
            .addOnSuccessListener { callback(true, "Cliente guardado") }
            .addOnFailureListener { e -> callback(false, e.message ?: "Error guardando cliente") }
    }

    fun existsCustomer(id: String, callback: (Boolean) -> Unit) {
        dbRef.child(id).get().addOnSuccessListener {
            callback(it.exists())
        }.addOnFailureListener {
            callback(false)
        }
    }
}
