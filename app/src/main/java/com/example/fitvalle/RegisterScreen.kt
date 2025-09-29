package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val userDao = UserDao()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Inicia tu viaje Fitness", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(30.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("¿Cómo quieres que te llamemos?") },
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo institucional") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(Modifier.height(20.dp))

                Button(
                    enabled = !loading,
                    onClick = {
                        val error = when {
                            name.isBlank() -> "Ingresa tu nombre"
                            email.isBlank() -> "Ingresa tu correo"
                            !email.endsWith("@est.univalle.edu") -> "El correo debe terminar en @est.univalle.edu"
                            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                            else -> null
                        }
                        if (error != null) {
                            scope.launch { snackbarHostState.showSnackbar(error) }
                            return@Button
                        }

                        loading = true
                        userDao.registerUser(name, email, password) { success, msg ->
                            loading = false
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                            if (success) {
                                navController.navigate("login")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFFD50000))
                ) {
                    Text(if (loading) "CREANDO..." else "CREAR CUENTA", color = Color.White)
                }
            }
        }
    }
}
