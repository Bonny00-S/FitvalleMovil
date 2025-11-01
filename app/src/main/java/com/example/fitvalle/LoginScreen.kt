package com.example.fitvalle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

fun saveFcmToken(userId: String) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val token = task.result
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("fcmToken", token)
        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val userDao = UserDao()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    val fondoPrincipal = Color(0xFF0D1525)
    val fondoSecundario = Color(0xFF182235)
    val primario = Color(0xFFB1163A)
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
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo Fitvalle",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 16.dp)
                )

                Text(
                    "FITVALLE",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = primario,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(30.dp))


                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = textoSecundario) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    textStyle = TextStyle(color = textoPrincipal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(fondoSecundario, shape = RoundedCornerShape(50.dp)), // 🔥 borde circular
                    shape = RoundedCornerShape(50.dp), // 🔥 borde circular
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primario,
                        unfocusedBorderColor = textoSecundario,
                        cursorColor = primario,
                        focusedTextColor = textoPrincipal,
                        unfocusedTextColor = textoPrincipal,
                        focusedLabelColor = primario,
                        unfocusedLabelColor = textoSecundario
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))


                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña", color = textoSecundario) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(color = textoPrincipal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(fondoSecundario, shape = RoundedCornerShape(50.dp)),
                    shape = RoundedCornerShape(50.dp), // 🔥 borde circular
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primario,
                        unfocusedBorderColor = textoSecundario,
                        cursorColor = primario,
                        focusedTextColor = textoPrincipal,
                        unfocusedTextColor = textoPrincipal,
                        focusedLabelColor = primario,
                        unfocusedLabelColor = textoSecundario
                    )
                )


                Spacer(modifier = Modifier.height(20.dp))

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
                                    saveFcmToken(uid)
                                    val customerDao = CustomerDao()
                                    customerDao.existsCustomer(uid) { exists ->
                                        if (exists) {
                                            navController.navigate("mainClientNav")
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
                    colors = ButtonDefaults.buttonColors(containerColor = primario),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = if (loading) "ENTRANDO..." else "INICIAR SESIÓN",
                        color = textoPrincipal,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { navController.navigate("registro") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primario),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 2.dp,
                        brush = SolidColor(primario)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("REGÍSTRATE", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Política de Privacidad",
                    color = textoSecundario,
                    fontSize = 12.sp
                )
            }
        }
    }
}
