package com.example.fitvalle

import com.example.fitvalle.Routine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.example.fitvalle.Session
import com.example.fitvalle.SessionExercise
import com.example.fitvalle.SessionWithExercises

import kotlinx.coroutines.tasks.await
class RoutineDao {

    // 👉 usamos tu misma URL
    private val db = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .reference

    private val auth = FirebaseAuth.getInstance()

    /**
     * 🔹 Rutinas que me asignó el coach
     * Busca en /routine y se queda solo con las que tengan customerId == uid
     */
    suspend fun getAssignedRoutines(): List<Routine> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.child("routine").get().await()
        val result = mutableListOf<Routine>()

        for (routineSnap in snapshot.children) {
            val customerId = routineSnap.child("customerId").getValue(String::class.java)
            if (customerId == uid) {
                val routine = routineSnap.getValue(Routine::class.java)
                if (routine != null) {
                    routine.id = routineSnap.key ?: routine.id

                    // 🔹 Cargar nombre del coach
                    val coachId = routine.coachId
                    if (!coachId.isNullOrEmpty()) {
                        routine.coachName = getCoachName(coachId) ?: "Entrenador"
                    }

                    // 🔹 Cargar las sesiones de la rutina
                    val sessionsMap = mutableMapOf<String, Session>()
                    val sessionsSnap = routineSnap.child("sessions")
                    if (sessionsSnap.exists()) {
                        for (sessionSnap in sessionsSnap.children) {
                            val session = sessionSnap.getValue(Session::class.java)
                            if (session != null) {
                                val sessionId = sessionSnap.key ?: session.id

                                // 🔹 Cargar ejercicios de la sesión
                                val exSnap = db.child("sessionExercises")
                                    .child(sessionId)
                                    .get()
                                    .await()

                                val exercisesMap = mutableMapOf<String, SessionExercise>()
                                for (exChild in exSnap.children) {
                                    val exercise = exChild.getValue(SessionExercise::class.java)
                                    if (exercise != null) {
                                        exercisesMap[exChild.key ?: ""] = exercise
                                    }
                                }

                                // Agregar sesión completa con ejercicios
                                sessionsMap[sessionId] = session.copy(
                                    id = sessionId,
                                    sessionExercises = exercisesMap
                                )
                            }
                        }
                    }

                    // 🔹 Añadir las sesiones cargadas
                    result.add(routine.copy(sessions = sessionsMap))
                }
            }
        }

        return result
    }


    /**
     * 🔹 Lee las sesiones que pertenecen a una rutina
     * Ruta: /routine/{routineId}/sessions
     */
    suspend fun getSessionsByRoutine(routineId: String): List<Session> {
        return try {
            val snap = db.child("routine")
                .child(routineId)
                .child("sessions")
                .get()
                .await()

            snap.children.mapNotNull { sessionSnap ->
                val session = sessionSnap.getValue(Session::class.java)
                session?.copy(id = sessionSnap.key ?: "")
            }
        } catch (e: Exception) {
            Log.e("RoutineDao", "Error leyendo sesiones de rutina: ${e.message}")
            emptyList()
        }
    }

    /**
     * 🔹 Devuelve los ejercicios de una sesión leyendo /sessionExercises/{sessionId}
     * (esto es por si ya lo usas en ActiveSessionScreen)
     */
    suspend fun getExercisesForSession(sessionId: String): List<SessionExercise> {
        return try {
            val snap = db.child("sessionExercises")
                .child(sessionId)
                .get()
                .await()

            snap.children.mapNotNull { it.getValue(SessionExercise::class.java) }
        } catch (e: Exception) {
            Log.e("RoutineDao", "Error leyendo ejercicios de la sesión: ${e.message}")
            emptyList()
        }
    }

    /**
     * 🔹 Guarda una rutina en /routine (por si el coach o admin crean desde la app)
     */
    suspend fun saveUserRoutine(userId: String, routine: Routine) {
        try {
            val routineId = if (routine.id.isNotEmpty()) {
                routine.id
            } else {
                db.child("routine").push().key ?: return
            }

            val routineRef = db.child("routine").child(routineId)
            val routineToSave = routine.copy(
                id = routineId,
                customerId = userId,
                registerDate = routine.registerDate.ifEmpty { System.currentTimeMillis().toString() }
            )

            routineRef.setValue(routineToSave).await()
            Log.d("RoutineDao", "Rutina guardada correctamente en /routine/$routineId")
        } catch (e: Exception) {
            Log.e("RoutineDao", "Error guardando rutina: ${e.message}", e)
        }
    }

    /**
     * 🔹 Por si en algún lado quieres mezclar las rutinas base + asignadas
     */
    suspend fun getAllRoutinesForUser(): List<Routine> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val routines = mutableListOf<Routine>()

        // 1) todas las rutinas
        val allSnap = db.child("routine").get().await()
        for (rSnap in allSnap.children) {
            val routine = rSnap.getValue(Routine::class.java) ?: continue
            routine.id = rSnap.key ?: ""

            // si tiene customerId y es mío, la agrego
            val customerId = routine.customerId
            if (customerId == null || customerId == uid) {
                // intentar obtener nombre del coach
                val coachId = routine.coachId ?: ""
                if (coachId.isNotEmpty()) {
                    routine.coachName = getCoachName(coachId) ?: "Entrenador"
                }
                routines.add(routine)
            }
        }

        return routines
    }

    /**
     * 🔹 Helper para leer el nombre del coach
     * prueba en /user y luego en /users
     */
    private suspend fun getCoachName(coachId: String): String? {
        return try {
            val userNode = db.child("user").child(coachId).get().await()
            if (userNode.exists()) {
                userNode.child("name").getValue(String::class.java)
            } else {
                val usersNode = db.child("users").child(coachId).get().await()
                if (usersNode.exists()) {
                    usersNode.child("name").getValue(String::class.java)
                } else null
            }
        } catch (e: Exception) {
            Log.e("RoutineDao", "Error obteniendo nombre del coach: ${e.message}")
            null
        }
    }
}