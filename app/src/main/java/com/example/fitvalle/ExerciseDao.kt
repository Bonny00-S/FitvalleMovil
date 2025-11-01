package com.example.fitvalle

import android.util.Log
import com.google.firebase.database.*

class ExerciseDao {

    private val dbRoot = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")

    private val dbExercises = dbRoot.getReference("exercise")
    private val dbTypes = dbRoot.getReference("exerciseTypes")
    private val dbMuscles = dbRoot.getReference("targetMuscles")

    private val dbRef: DatabaseReference = dbRoot.getReference("exercise")

    // 🔹 Obtener ejercicios con su tipo y músculo
    fun getAllExercises(callback: (List<Triple<Exercise, ExerciseType?, TargetMuscle?>>) -> Unit) {
        dbTypes.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(typesSnapshot: DataSnapshot) {
                val typesMap = mutableMapOf<String, ExerciseType>()
                for (typeSnap in typesSnapshot.children) {
                    val type = typeSnap.getValue(ExerciseType::class.java)
                    if (type != null) typesMap[type.id] = type
                }

                dbMuscles.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(muscleSnapshot: DataSnapshot) {
                        val muscleMap = mutableMapOf<String, TargetMuscle>()
                        for (mSnap in muscleSnapshot.children) {
                            val muscle = mSnap.getValue(TargetMuscle::class.java)
                            if (muscle != null) muscleMap[muscle.id] = muscle
                        }

                        dbExercises.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(exSnapshot: DataSnapshot) {
                                val result = mutableListOf<Triple<Exercise, ExerciseType?, TargetMuscle?>>()
                                for (exSnap in exSnapshot.children) {
                                    val ex = exSnap.getValue(Exercise::class.java)
                                    if (ex != null) {
                                        val type = typesMap[ex.typeID]
                                        val muscle = muscleMap[ex.muscleID]
                                        result.add(Triple(ex, type, muscle))
                                    }
                                }
                                callback(result.sortedBy { it.first.name })
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FIREBASE", "Error cargando ejercicios: ${error.message}")
                                callback(emptyList())
                            }
                        })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FIREBASE", "Error cargando músculos: ${error.message}")
                        callback(emptyList())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE", "Error cargando tipos: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun getExerciseById(id: String, callback: (Exercise?) -> Unit) {
        dbRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val exercise = snapshot.getValue(Exercise::class.java)
                callback(exercise)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }
}
