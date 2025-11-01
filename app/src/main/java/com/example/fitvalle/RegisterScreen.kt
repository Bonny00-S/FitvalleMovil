package com.example.fitvalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
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

    // 🎨 Paleta Fitvalle
    val fondoPrincipal = Color(0xFF0D1525)
    val fondoSecundario = Color(0xFF182235)
    val primario = Color(0xFFB1163A)
    val hoverPrimario = Color(0xFFD12B56)
    val textoPrincipal = Color(0xFFFFFFFF)
    val textoSecundario = Color(0xFFAAB2C5)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = fondoPrincipal
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(fondoPrincipal),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    "Inicia tu viaje Fitness",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = primario,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(Modifier.height(30.dp))

                // Campo Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("¿Cómo quieres que te llamemos?", color = textoSecundario) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(fondoSecundario, shape = MaterialTheme.shapes.medium),
                    textStyle = LocalTextStyle.current.copy(color = textoPrincipal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primario,
                        unfocusedBorderColor = textoSecundario,
                        cursorColor = primario,
                        focusedTextColor = textoPrincipal,
                        unfocusedTextColor = textoPrincipal,
                        focusedLabelColor = primario,
                        unfocusedLabelColor = textoSecundario
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(Modifier.height(16.dp))

                // Campo Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo institucional", color = textoSecundario) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(fondoSecundario, shape = MaterialTheme.shapes.medium),
                    textStyle = LocalTextStyle.current.copy(color = textoPrincipal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primario,
                        unfocusedBorderColor = textoSecundario,
                        cursorColor = primario,
                        focusedTextColor = textoPrincipal,
                        unfocusedTextColor = textoPrincipal,
                        focusedLabelColor = primario,
                        unfocusedLabelColor = textoSecundario
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(Modifier.height(16.dp))

                // Campo Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña", color = textoSecundario) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(fondoSecundario, shape = MaterialTheme.shapes.medium),
                    textStyle = LocalTextStyle.current.copy(color = textoPrincipal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primario,
                        unfocusedBorderColor = textoSecundario,
                        cursorColor = primario,
                        focusedTextColor = textoPrincipal,
                        unfocusedTextColor = textoPrincipal,
                        focusedLabelColor = primario,
                        unfocusedLabelColor = textoSecundario
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(Modifier.height(24.dp))

                // Botón principal
                Button(
                    enabled = !loading,
                    onClick = {
                        val error = when {
                            name.isBlank() -> "Ingresa tu nombre"
                            email.isBlank() -> "Ingresa tu correo"
                            !(email.endsWith("@est.univalle.edu") || email.endsWith("@univalle.edu")) ->
                                "El correo debe terminar en @est.univalle.edu o @univalle.edu"
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
                            if (success) navController.navigate("login")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primario),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        if (loading) "CREANDO..." else "CREAR CUENTA",
                        color = textoPrincipal,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Botón secundario
                OutlinedButton(
                    onClick = { navController.navigate("login") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primario),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp, brush = SolidColor(primario)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
