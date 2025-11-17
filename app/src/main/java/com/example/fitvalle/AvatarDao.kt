package com.example.fitvalle

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.util.*

object AvatarDao {
    private const val TAG = "AvatarDao"
    private val db = FirebaseDatabase.getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")

    /**
     * Sube un avatar convertido a Base64 a Realtime Database.
     * onSuccess recibe el ID del avatar guardado.
     */
    fun uploadSharedAvatar(
        context: Context,
        imageUri: Uri,
        displayName: String?,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                onFailure(Exception("Usuario no autenticado"))
                return
            }

            val id = UUID.randomUUID().toString()

            Log.d(TAG, "Iniciando conversión de imagen a Base64 para usuario ${user.uid}")

            // Leer la imagen desde el URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw Exception("No se pudo abrir la imagen")

            // Convertir a Bitmap
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) {
                onFailure(Exception("No se pudo decodificar la imagen"))
                return
            }

            // Comprimir y convertir a Base64
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            Log.d(TAG, "Imagen convertida a Base64: ${base64Image.length} caracteres")

            // Guardar en Database
            val meta = mapOf(
                "id" to id,
                "name" to (displayName ?: "avatar_${System.currentTimeMillis()}"),
                "imageBase64" to base64Image,
                "uploadedBy" to user.uid,
                "uploadedAt" to System.currentTimeMillis()
            )

            db.reference.child("sharedAvatars").child(id).setValue(meta)
                .addOnSuccessListener {
                    Log.d(TAG, "Avatar guardado en Database con ID: $id")
                    onSuccess(id)
                }
                .addOnFailureListener { ex ->
                    Log.e(TAG, "Error guardando en Database: ${ex.message}", ex)
                    onFailure(ex)
                }

        } catch (ex: Exception) {
            Log.e(TAG, "Excepción en uploadSharedAvatar: ${ex.message}", ex)
            onFailure(ex)
        }
    }
}
