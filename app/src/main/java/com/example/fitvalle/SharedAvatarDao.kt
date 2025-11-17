package com.example.fitvalle

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class SharedAvatarItem(
    val id: String = "",
    val name: String = "",
    val imageBase64: String = "",
    val uploadedBy: String = "",
    val uploadedAt: Long = 0L
)

object SharedAvatarDao {
    private val db = FirebaseDatabase.getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")

    /**
     * Obtiene la lista de avatares compartidos desde Realtime Database.
     * onSuccess recibe una lista de SharedAvatarItem.
     */
    fun getSharedAvatars(onSuccess: (List<SharedAvatarItem>) -> Unit, onFailure: (Exception) -> Unit) {
        db.reference.child("sharedAvatars").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val avatars = mutableListOf<SharedAvatarItem>()
                for (child in snapshot.children) {
                    val avatar = child.getValue(SharedAvatarItem::class.java)
                    if (avatar != null) {
                        avatars.add(avatar)
                    }
                }
                onSuccess(avatars)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.toException())
            }
        })
    }

    /**
     * Escucha cambios en tiempo real en los avatares compartidos.
     * Útil para actualizar la UI cuando nuevos avatares se suben.
     */
    fun listenSharedAvatars(onUpdate: (List<SharedAvatarItem>) -> Unit, onFailure: (Exception) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val avatars = mutableListOf<SharedAvatarItem>()
                for (child in snapshot.children) {
                    val avatar = child.getValue(SharedAvatarItem::class.java)
                    if (avatar != null) {
                        avatars.add(avatar)
                    }
                }
                onUpdate(avatars)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.toException())
            }
        }
        db.reference.child("sharedAvatars").addValueEventListener(listener)
        return listener
    }
}
