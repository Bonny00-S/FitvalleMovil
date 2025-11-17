package com.example.fitvalle

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import android.util.Log

class SessionDao {

    private val dbRoot = FirebaseDatabase.getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/").reference
    private val sessionExercisesRef = dbRoot.child("sessionExercises")
    private val exercisesRef = dbRoot.child("exercise")

    // Devuelve la lista de SessionExercise con exerciseName cargado cuando sea posible
    suspend fun getSessionExercises(sessionId: String): List<SessionExercise> {
        return try {
            val list = mutableListOf<SessionExercise>()
            val nodeSnap = sessionExercisesRef.child(sessionId).get().await()

            if (nodeSnap.exists()) {
                for (exChild in nodeSnap.children) {
                    val exerciseId = exChild.child("exerciseId").getValue(String::class.java) ?: continue

                    val exercise = SessionExercise(
                        sessionId = sessionId,
                        exerciseId = exerciseId,
                        sets = exChild.child("sets").getValue(Int::class.java) ?: 0,
                        reps = exChild.child("reps").getValue(Int::class.java) ?: 0,
                        weight = exChild.child("weight").getValue(Int::class.java) ?: 0,
                        speed = exChild.child("speed").getValue(Int::class.java) ?: 0,
                        duration = exChild.child("duration").getValue(Int::class.java) ?: 0
                    ).apply {
                        exerciseName = getExerciseName(exerciseId)
                    }

                    list.add(exercise)
                }
            }

            list
        } catch (e: Exception) {
            Log.e("SessionDao", "Error getSessionExercises: ${e.message}", e)
            emptyList()
        }
    }




    private suspend fun getExerciseName(exerciseId: String?): String? {
        if (exerciseId.isNullOrEmpty()) return null
        return try {
            val snap = exercisesRef.child(exerciseId).get().await()
            snap.child("name").getValue(String::class.java)
        } catch (e: Exception) {
            Log.w("SessionDao", "No pude leer exercise name para $exerciseId: ${e.message}")
            null
        }
    }

    suspend fun saveSessionExercise(exercise: SessionExercise) {
        try {
            val key = sessionExercisesRef.push().key ?: return
            sessionExercisesRef.child(key).setValue(exercise).await()
        } catch (e: Exception) {
            Log.e("SessionDao", "Error saveSessionExercise: ${e.message}", e)
        }
    }
    suspend fun saveCompletedSession(
        customerId: String,
        routineId: String,
        sessionId: String,
        exercisesDone: List<SessionExercise>
    ): Boolean {
        return try {
            Log.d("SessionDao", "🔹 Guardando sesión completada con ${exercisesDone.size} ejercicios")
            exercisesDone.forEachIndexed { index, exercise ->
                Log.d("SessionDao", "  [$index] ${exercise.exerciseName}: Sets=${exercise.sets}, Reps=${exercise.reps}, Weight=${exercise.weight}, Speed=${exercise.speed}, Duration=${exercise.duration}")
            }
            
            val completedSessionRef = dbRoot.child("completedSessions").push()
            val completedData = mapOf(
                "id" to completedSessionRef.key,
                "customerId" to customerId,
                "routineId" to routineId,
                "sessionId" to sessionId,
                "dateFinished" to java.time.Instant.now().toString(),
                "exercisesDone" to exercisesDone.map {
                    mapOf(
                        "exerciseId" to it.exerciseId,
                        "exerciseName" to it.exerciseName,
                        "sets" to it.sets,
                        "reps" to it.reps,
                        "weight" to it.weight,
                        "speed" to it.speed,
                        "duration" to it.duration
                    )
                }
            )
            completedSessionRef.setValue(completedData).await()
            Log.d("SessionDao", "✅ Sesión guardada correctamente con ID: ${completedSessionRef.key}")
            true
        } catch (e: Exception) {
            Log.e("SessionDao", "Error guardando sesión completada: ${e.message}", e)
            false
        }
    }
    suspend fun updateLastSessionTrained(customerId: String, sessionId: String) {
        try {
            dbRoot.child("userProgress")
                .child(customerId)
                .child("lastSessionTrained")
                .setValue(sessionId)
                .await()
            Log.d("SessionDao", "Progreso actualizado a sesión $sessionId")
        } catch (e: Exception) {
            Log.e("SessionDao", "Error actualizando progreso: ${e.message}", e)
        }
    }

    suspend fun getLastSessionTrained(customerId: String): String? {
        return try {
            val snap = dbRoot.child("userProgress")
                .child(customerId)
                .child("lastSessionTrained")
                .get()
                .await()
            snap.getValue(String::class.java)
        } catch (e: Exception) {
            Log.e("SessionDao", "Error obteniendo progreso: ${e.message}", e)
            null
        }
    }

}
