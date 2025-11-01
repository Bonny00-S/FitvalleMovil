package com.example.fitvalle.data.dao

import com.example.fitvalle.Routine
//import com.example.fitvalle.data.model.Routine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.example.fitvalle.Session
import com.example.fitvalle.SessionExercise
import com.example.fitvalle.SessionWithExercises

class RoutineDao {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()


    suspend fun getAssignedRoutines(): List<Routine> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.child("assignedRoutines").child(uid).get().await()

        val routines = mutableListOf<Routine>()
        for (routineSnap in snapshot.children) {
            val routine = routineSnap.getValue(Routine::class.java)
            if (routine != null) {
                // asegurar id
                val routineWithId = routine.copy(id = (routineSnap.key ?: routine.id))

                // ---- Intento 1: leer en "user" (según tu DB mostraste 'user')
                // si en tu base de datos la colección / nodo es "users", cambia a "users"
                val coachId = routineWithId.coachId.trim()
                var coachName: String? = null

                try {
                    // 1) probar en "user"
                    val coachSnap1 = db.child("user").child(coachId).get().await()
                    if (coachSnap1.exists()) {
                        coachName = coachSnap1.child("name").getValue(String::class.java)
                        Log.d("RoutineDao", "Coach encontrado en /user: $coachName")
                    } else {
                        // 2) fallback: probar en "users"
                        val coachSnap2 = db.child("users").child(coachId).get().await()
                        if (coachSnap2.exists()) {
                            coachName = coachSnap2.child("name").getValue(String::class.java)
                            Log.d("RoutineDao", "Coach encontrado en /users: $coachName")
                        } else {
                            // 3) fallback adicional: buscar por iteración (solo para debug, quitar en prod)
                            val usersAll = db.child("user").get().await()
                            for (u in usersAll.children) {
                                val id = u.key ?: continue
                                val nm = u.child("name").getValue(String::class.java)
                                if (id == coachId || nm != null && nm.contains(coachId)) {
                                    coachName = nm
                                    Log.d("RoutineDao", "Coach heurístico encontrado: $coachName (id=$id)")
                                    break
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RoutineDao", "Error buscando coach name: ${e.message}", e)
                }

                routineWithId.coachName = coachName ?: "Entrenador desconocido"
                routines.add(routineWithId)
            }
        }

        return routines
    }

//    suspend fun getRoutineDetail(routineId: String): List<com.example.fitvalle.Exercise> {
//        val uid = auth.currentUser?.uid ?: return emptyList()
//        val routineSnap = db.child("assignedRoutines").child(uid).child(routineId).get().await()
//
//        val exercisesList = mutableListOf<com.example.fitvalle.Exercise>()
//
//        // Recorremos las sesiones
//        for (sessionSnap in routineSnap.child("sessions").children) {
//            val exercisesSnap = sessionSnap.child("exercises")
//            if (!exercisesSnap.exists()) continue
//
//            // Recorremos cada ejercicio dentro de la sesión
//            for (exSnap in exercisesSnap.children) {
//                val exerciseId = exSnap.child("exerciseId").getValue(String::class.java)
//                val sets = exSnap.child("sets").getValue(Int::class.java) ?: 0
//                val reps = exSnap.child("reps").getValue(Int::class.java) ?: 0
//
//                if (exerciseId != null) {
//                    try {
//                        val exerciseData = db.child("exercise").child(exerciseId).get().await()
//                        val ex = exerciseData.getValue(com.example.fitvalle.Exercise::class.java)
//
//                        if (ex != null) {
//                            // Buscar nombre del músculo
//                            val muscleSnap = db.child("targetMuscles").child(ex.muscleID ?: "").get().await()
//                            val muscleName = muscleSnap.child("name").getValue(String::class.java) ?: "Desconocido"
//
//                            val finalExercise = ex.copy(
//                                series = sets,
//                                repetitions = reps.toString(),
//                                muscleID = muscleName // reemplazamos ID por nombre
//                            )
//                            exercisesList.add(finalExercise)
//                        }
//                    } catch (e: Exception) {
//                        Log.e("RoutineDao", "Error leyendo ejercicio $exerciseId: ${e.message}")
//                    }
//                }
//
//            }
//        }
//
//        return exercisesList
//    }

    suspend fun getRoutineDetailBySessions(routineId: String): List<com.example.fitvalle.SessionWithExercises> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val routineSnap = db.child("assignedRoutines").child(uid).child(routineId).get().await()

        val sessionsList = mutableListOf<com.example.fitvalle.SessionWithExercises>()

        // 🔹 Recorremos las sesiones
        for (sessionSnap in routineSnap.child("sessions").children) {
            val sessionId = sessionSnap.key ?: continue
            val exercisesSnap = sessionSnap.child("exercises")
            if (!exercisesSnap.exists()) continue

            val exercisesList = mutableListOf<com.example.fitvalle.Exercise>()

            // 🔹 Recorremos cada ejercicio
            for (exSnap in exercisesSnap.children) {
                val exerciseId = exSnap.child("exerciseId").getValue(String::class.java)
                val sets = exSnap.child("sets").getValue(Int::class.java) ?: 0
                val reps = exSnap.child("reps").getValue(Int::class.java) ?: 0

                if (exerciseId != null) {
                    try {
                        val exerciseData = db.child("exercise").child(exerciseId).get().await()
                        val ex = exerciseData.getValue(com.example.fitvalle.Exercise::class.java)

                        if (ex != null) {
                            val muscleSnap = db.child("targetMuscles").child(ex.muscleID ?: "").get().await()
                            val muscleName = muscleSnap.child("name").getValue(String::class.java) ?: "Desconocido"

                            val finalExercise = ex.copy(
                                series = sets,
                                repetitions = reps.toString(),
                                muscleID = muscleName
                            )

                            exercisesList.add(finalExercise)
                        }
                    } catch (e: Exception) {
                        Log.e("RoutineDao", "Error leyendo ejercicio $exerciseId: ${e.message}")
                    }
                }
            }

            sessionsList.add(com.example.fitvalle.SessionWithExercises(sessionId, exercisesList))
        }

        return sessionsList
    }

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
    suspend fun getAllRoutinesForUser(): List<Routine> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        val database = FirebaseDatabase.getInstance().reference

        val routines = mutableListOf<Routine>()

        // 1️⃣ Plantillas por defecto (o propias)
        val defaultSnapshot = database.child("routine").get().await()
        for (routineSnap in defaultSnapshot.children) {
            val routine = routineSnap.getValue(Routine::class.java)
            if (routine != null) {
                routine.id = routineSnap.key ?: ""
                routines.add(routine)
            }
        }

        // 2️⃣ Rutinas asignadas (de coaches)
        val assignedSnapshot = database.child("assignedRoutines").child(userId).get().await()
        for (routineSnap in assignedSnapshot.children) {
            val routine = routineSnap.getValue(Routine::class.java)
            if (routine != null) {
                routine.id = routineSnap.key ?: ""
                val coachId = routine.coachId

                // 🔹 Obtener el nombre del coach
                if (!coachId.isNullOrEmpty()) {
                    val coachSnap = database.child("users").child(coachId).get().await()
                    val coachName = coachSnap.child("name").getValue(String::class.java)
                    routine.coachName = coachName ?: "Entrenador"
                }

                routines.add(routine)
            }
        }

        return routines
    }
    suspend fun getSessionsByRoutine(routineId: String): List<Session> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        return try {
            // Ruta real: assignedRoutines/{uid}/{routineId}/sessions
            val snapshot = db
                .child("assignedRoutines")
                .child(uid)
                .child(routineId)
                .child("sessions")
                .get()
                .await()

            snapshot.children.mapNotNull { sessionSnap ->
                val session = sessionSnap.getValue(Session::class.java)
                session?.copy(id = sessionSnap.key ?: "")
            }
        } catch (e: Exception) {
            Log.e("RoutineDao", "Error leyendo sesiones: ${e.message}", e)
            emptyList()
        }
    }


}
