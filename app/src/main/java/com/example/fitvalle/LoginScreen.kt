package com.example.fitvalle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val userDao = UserDao()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo Fitvalle",
                    modifier = Modifier.size(120.dp).padding(bottom = 16.dp)
                )

                Text("Fitvalle", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(30.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
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
                            email.isBlank() -> "Ingresa tu correo"
                            password.isBlank() -> "Ingresa tu contraseña"
                            else -> null
                        }
                        if (error != null) {
                            scope.launch { snackbarHostState.showSnackbar(error) }
                            return@Button
                        }

                        loading = true
                        userDao.loginUser(email, password) { success, msg, _ ->
                            loading = false
                            if (success) {
                                val uid = FirebaseAuth.getInstance().currentUser?.uid
                                if (uid != null) {
                                    val customerDao = CustomerDao()
                                    customerDao.existsCustomer(uid) { exists ->
                                        if (exists) {
                                            navController.navigate("mainClient")
                                        } else {
                                            navController.navigate("welcome")
                                        }
                                    }
                                }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFFD50000))
                ) {
                    Text(if (loading) "ENTRANDO..." else "INICIAR SESION", color = Color.White)
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { navController.navigate("registro") },
                    colors = ButtonDefaults.buttonColors(Color(0xFFD50000))
                ) {
                    Text("REGÍSTRATE", color = Color.White)
                }

                Spacer(Modifier.height(12.dp))
                Text("Política de Privacidad", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}
