package com.example.fitvalle.data.dao

import com.example.fitvalle.Workout
//import com.example.fitvalle.data.model.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WorkoutDao {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 🔹 Guardar nuevo entrenamiento
    suspend fun addWorkout(workout: Workout) {
        val userId = auth.currentUser?.uid ?: return
        val workoutId = db.collection("workouts").document().id

        val newWorkout = workout.copy(
            id = workoutId,
            userId = userId
        )

        db.collection("workouts").document(workoutId).set(newWorkout).await()
    }

    // 🔹 Obtener entrenamientos del usuario (para historial)
    suspend fun getWorkoutsForUser(): List<Workout> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("workouts")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        return snapshot.toObjects(Workout::class.java)
    }
}
