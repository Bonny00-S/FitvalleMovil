package com.example.fitvalle

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class WorkoutDao {

    private val db = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("workouts")

    private val auth = FirebaseAuth.getInstance()

    /**
     * 🔹 Guarda un nuevo entrenamiento en Realtime Database
     */
    suspend fun saveWorkout(title: String, exercises: List<String>): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val id = db.push().key ?: return false
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val workout = Workout(
            id = id,
            userId = userId,
            title = title,
            exercises = exercises,
            createdAt = date
        )

        db.child(userId).child(id).setValue(workout).await()
        return true
    }

    /**
     * 🔹 Obtiene todos los entrenamientos del usuario actual
     */
    suspend fun getUserWorkouts(): List<Workout> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.child(userId).get().await()
        return snapshot.children.mapNotNull { it.getValue(Workout::class.java) }
    }

    /**
     * 🔹 Obtiene un entrenamiento específico por su ID
     */
    suspend fun getWorkoutById(workoutId: String): Workout? {
        val userId = auth.currentUser?.uid ?: return null
        val snapshot = db.child(userId).child(workoutId).get().await()
        return snapshot.getValue(Workout::class.java)
    }

    /**
     * 🔹 Elimina un entrenamiento del usuario actual
     */
    suspend fun deleteWorkout(workoutId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        db.child(userId).child(workoutId).removeValue().await()
        return true
    }
}
