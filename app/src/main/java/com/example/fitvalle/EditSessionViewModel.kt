package com.example.fitvalle

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log

class EditSessionViewModel : ViewModel() {
    private val _editedExercises = MutableStateFlow<Map<String, SessionExercise>>(emptyMap())
    val editedExercises: StateFlow<Map<String, SessionExercise>> = _editedExercises

    fun updateExercise(exercise: SessionExercise) {
        Log.d("EditSessionVM", "🔧 updateExercise called for id='${exercise.exerciseId}' name='${exercise.exerciseName}' sets=${exercise.sets}")
        _editedExercises.value = _editedExercises.value.toMutableMap().apply {
            put(exercise.exerciseId, exercise)
        }
        Log.d("EditSessionVM", "🗂️ editedExercises keys now=${_editedExercises.value.keys}")
    }

    fun getEditedExercise(exerciseId: String): SessionExercise? {
        return _editedExercises.value[exerciseId]
    }

    fun clearEdited() {
        _editedExercises.value = emptyMap()
    }
}
