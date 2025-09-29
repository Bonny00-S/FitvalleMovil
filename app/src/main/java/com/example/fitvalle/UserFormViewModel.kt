package com.example.fitvalle

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf

class UserFormViewModel : ViewModel() {
    // InfoFormScreen
    var exerciseType = mutableStateOf("")
    var experienceLevel = mutableStateOf("")
    var trainingDays = mutableStateOf("")

    var gender = mutableStateOf("")
    var height = mutableStateOf("")
    var weight = mutableStateOf("")

    // BirthdateScreen
    var birthdate = mutableStateOf("")

    // GoalFormScreen
    var goalWeight = mutableStateOf("")
    var activityLevel = mutableStateOf("")
    var gainSpeed = mutableStateOf("")

    // Request data
    var description = mutableStateOf("") // aquí se van concatenando preferencias
}

