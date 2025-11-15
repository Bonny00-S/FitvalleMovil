package com.example.fitvalle

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class TemplateDao {

    private val db = FirebaseDatabase
        .getInstance("https://fitvalle-fced7-default-rtdb.firebaseio.com/")
        .getReference("templates")

    private val auth = FirebaseAuth.getInstance()

    /**
     * 🔹 Guarda una nueva plantilla en Realtime Database
     */
    suspend fun saveTemplate(name: String, exercises: List<String>): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val id = db.push().key ?: return false
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val template = Template(
            id = id,
            userId = userId,
            name = name,
            exercises = exercises,
            createdAt = date
        )

        db.child(userId).child(id).setValue(template).await()
        return true
    }

    /**
     * 🔹 Obtiene todas las plantillas del usuario logueado
     */
    suspend fun getUserTemplates(): List<Template> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.child(userId).get().await()
        return snapshot.children.mapNotNull { it.getValue(Template::class.java) }
    }

    /**
     * 🔹 (opcional) Obtiene las plantillas de un user concreto
     */
    suspend fun getUserTemplates(userId: String): List<Template> {
        val snapshot = db.child(userId).get().await()
        return snapshot.children.mapNotNull { it.getValue(Template::class.java) }
    }

    /**
     * 🔹 Obtiene una plantilla específica por su ID
     */
    suspend fun getTemplateById(templateId: String): Template? {
        val userId = auth.currentUser?.uid ?: return null
        val snapshot = db.child(userId).child(templateId).get().await()
        return snapshot.getValue(Template::class.java)
    }

    /**
     * 🔹 Elimina una plantilla existente
     */
    suspend fun deleteTemplate(templateId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        db.child(userId).child(templateId).removeValue().await()
        return true
    }
}