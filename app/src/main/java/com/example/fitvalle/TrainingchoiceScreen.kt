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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrainingChoiceScreen(navController: NavController, viewModel: UserFormViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val customerDao = CustomerDao() // lo creas similar al UserDao

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.training),
                    contentDescription = "Entrenamiento",
                    modifier = Modifier.size(200.dp)
                )

                Spacer(Modifier.height(20.dp))
                Text("Entrenamiento personalizado para ti", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(Modifier.height(12.dp))
                Text("Basándonos en tus objetivos y necesidades, personalizamos un programa de entrenamiento para ti.",
                    color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Text("Puedes editar o crear tus propios entrenamientos.",
                    color = Color.White, fontSize = 14.sp)

                Spacer(Modifier.height(30.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            val customer = Customer(
                                id = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                weight = viewModel.weight.value,
                                height = viewModel.height.value,
                                goalWeight = viewModel.goalWeight.value,
                                birthdate = viewModel.birthdate.value,
                                registerDate = SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss",
                                    Locale.getDefault()
                                ).format(Date())
                            )
                            customerDao.saveCustomer(customer) { success, msg ->
                                if (success) {
                                    navController.navigate("mainClient") // pantalla principal
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(Color.Gray)
                    ) {
                        Text("SEGUIR POR MI CUENTA", color = Color.White)
                    }

                    Button(
                        onClick = {
                            val customer = Customer(
                                id = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                weight = viewModel.weight.value,
                                height = viewModel.height.value,
                                goalWeight = viewModel.goalWeight.value,
                                birthdate = viewModel.birthdate.value,
                                registerDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                            )
                            customerDao.saveCustomer(customer) { success, msg ->
                                if (success) {
                                    navController.navigate("trainingPreferences") // la siguiente pantalla
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFFD50000))
                    ) {
                        Text("CONTINUAR", color = Color.White)
                    }
                }
            }
        }
    }
}