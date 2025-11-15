package com.example.fitvalle

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.*
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class UserDao {

    private val dbRef: DatabaseReference = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("user")

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // 🔐 Método para cifrar contraseñas con SHA-256
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // 📌 Registrar con FirebaseAuth + guardar en Realtime Database
    fun registerUser(
        name: String,
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener

                // Enviar correo de verificación
                result.user?.sendEmailVerification()

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val registerDate = sdf.format(Date())

                val newUser = User(
                    id = userId.hashCode(),
                    name = name,
                    email = email,
                    password = hashPassword(password), // opcional
                    role = "client",
                    userId = null,
                    state = 1,
                    registerDate = registerDate
                )

                dbRef.child(userId).setValue(newUser)
                    .addOnSuccessListener { callback(true, "Usuario registrado. Verifica tu correo.") }
                    .addOnFailureListener { e -> callback(false, translateAuthException(e)) }
            }
            .addOnFailureListener { e ->
                callback(false, translateAuthException(e)) // ✅ Ahora traduce aquí
            }
    }

    // 📌 Iniciar sesión (usando FirebaseAuth + validación de email verificado)
    fun loginUser(email: String, password: String, callback: (Boolean, String, User?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                if (firebaseUser != null && firebaseUser.isEmailVerified) {
                    dbRef.child(firebaseUser.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val user = snapshot.getValue(User::class.java)
                                if (user != null) {
                                    callback(true, "Login exitoso", user)
                                } else {
                                    callback(false, "Usuario no encontrado en DB", null)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                callback(false, "Error: ${error.message}", null)
                            }
                        })
                } else {
                    callback(false, "Debes verificar tu correo antes de iniciar sesión", null)
                }
            }
            .addOnFailureListener { e ->
                callback(false, translateAuthException(e), null) // ✅ Aquí también traduce
            }
    }

    // 📌 Obtener todos los usuarios
    fun getAllUsers(callback: (List<User>) -> Unit) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<User>()
                for (dato in snapshot.children) {
                    val usuario = dato.getValue(User::class.java)
                    if (usuario != null) lista.add(usuario)
                }
                callback(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE", "Error: ${error.message}")
                callback(emptyList())
            }
        })
    }

    // 📌 Obtener todos los entrenadores (role = "coach")
    fun getAllCoaches(callback: (List<User>) -> Unit) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val coaches = mutableListOf<User>()
                for (dato in snapshot.children) {
                    val usuario = dato.getValue(User::class.java)
                    if (usuario != null && usuario.role == "coach") {
                        coaches.add(usuario)
                    }
                }
                callback(coaches)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE", "Error al obtener entrenadores: ${error.message}")
                callback(emptyList())
            }
        })
    }


    // 📌 Actualizar usuario
    fun updateUser(key: String, user: User, callback: (Boolean, String) -> Unit) {
        dbRef.child(key).setValue(user)
            .addOnSuccessListener { callback(true, "Usuario actualizado") }
            .addOnFailureListener { e -> callback(false, translateAuthException(e)) }
    }

    // 📌 Eliminar usuario
    fun deleteUser(key: String, callback: (Boolean, String) -> Unit) {
        dbRef.child(key).removeValue()
            .addOnSuccessListener { callback(true, "Usuario eliminado") }
            .addOnFailureListener { e -> callback(false, translateAuthException(e)) }
    }

    // 📌 Traductor de errores de Firebase Auth
    private fun translateAuthException(e: Exception): String {
        return if (e is FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "El correo ya está registrado"
                "ERROR_INVALID_EMAIL" -> "El correo no es válido"
                "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta"
                "ERROR_USER_NOT_FOUND" -> "Usuario no encontrado"
                "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil"
                "ERROR_NETWORK_REQUEST_FAILED" -> "Error de red, revisa tu conexión"
                "ERROR_INVALID_CREDENTIAL" -> "La credencial es inválida o la contraseña es incorrecta"
                else -> "Ocurrió un error: ${e.message}"
            }
        } else {
            "Ocurrió un error: ${e.message}"
        }
    }
}
