package com.example.fitvalle

import java.io.Serializable

/**
 * 🏋️ Ejercicio durante una sesión de entrenamiento
 * Extiende TemplateExercise con campos para registrar lo que se realizó
 */
data class TrainingExercise(
    // Planificado (del template)
    val exerciseId: String = "",
    val exerciseName: String = "",
    val plannedSets: Int = 0,
    val plannedReps: Int = 0,
    val plannedWeight: Int = 0,
    val plannedSpeed: Int = 0,
    val plannedDuration: Int = 0,
    
    // Realizado
    val performedSets: Int = 0,
    val performedReps: Int = 0,
    val performedWeight: Int = 0,
    val performedSpeed: Int = 0,
    val performedDuration: Int = 0,
    
    // Estado
    val completed: Boolean = false
) : Serializable

fun TemplateExercise.toTrainingExercise(): TrainingExercise {
    return TrainingExercise(
        exerciseId = this.exerciseId,
        exerciseName = this.exerciseName,
        plannedSets = this.sets,
        plannedReps = this.reps,
        plannedWeight = this.weight,
        plannedSpeed = this.speed,
        plannedDuration = this.duration,
        performedSets = this.sets,
        performedReps = this.reps,
        performedWeight = this.weight,
        performedSpeed = this.speed,
        performedDuration = this.duration,
        completed = false
    )
}

fun TrainingExercise.toSessionExercise(): SessionExercise {
    return SessionExercise(
        sessionId = "",
        exerciseId = this.exerciseId,
        exerciseName = this.exerciseName,
        sets = this.performedSets,
        reps = this.performedReps,
        weight = this.performedWeight,
        speed = this.performedSpeed,
        duration = this.performedDuration,
        completed = this.completed
    )
}
