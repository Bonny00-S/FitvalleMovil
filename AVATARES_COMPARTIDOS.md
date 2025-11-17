# Avatares Compartidos - Guía Funcional

## Overview
La funcionalidad de avatares compartidos permite que los usuarios suban imágenes de avatar que otros usuarios pueden seleccionar y usar como perfil. Las imágenes se almacenan en Firebase Storage y los metadatos en Realtime Database.

## Estructura de datos en Firebase

### Storage (`gs://fitvalle-fced7.firebasestorage.app`)
```
sharedAvatars/
├── shared_avatar_1731753600000.jpg
├── shared_avatar_1731753661234.jpg
└── ...
```

### Realtime Database (`sharedAvatars/{id}`)
```json
{
  "sharedAvatars": {
    "avatar_id_1": {
      "id": "avatar_id_1",
      "name": "Mi avatar personalizado",
      "url": "https://firebasestorage.googleapis.com/...",
      "uploadedBy": "uid_del_usuario",
      "uploadedAt": 1731753600000
    },
    "avatar_id_2": { ... }
  }
}
```

## Archivos clave

### 1. `AvatarDao.kt`
**Responsabilidad:** Subir imágenes a Firebase Storage y guardar metadatos en Realtime Database.

**Método principal:**
- `uploadSharedAvatar(context, imageUri, displayName, onSuccess, onFailure)`
  - Sube la imagen a `sharedAvatars/{filename}` en Storage.
  - Guarda metadatos en `sharedAvatars/{id}` en Realtime DB.
  - Retorna la URL pública de descarga en `onSuccess`.

### 2. `SharedAvatarDao.kt`
**Responsabilidad:** Cargar y escuchar cambios en avatares compartidos.

**Métodos:**
- `getSharedAvatars(onSuccess, onFailure)` — obtiene lista de avatares (una sola vez).
- `listenSharedAvatars(onUpdate, onFailure)` — escucha en tiempo real y actualiza cuando hay cambios.

**Data class:**
```kotlin
data class SharedAvatarItem(
    val id: String,
    val name: String,
    val url: String,
    val uploadedBy: String,
    val uploadedAt: Long
)
```

### 3. `AvatarEditScreen.kt`
**Responsabilidad:** UI para seleccionar avatares (locales o compartidos) y subir nuevos.

**Secciones:**
1. Avatares locales predefinidos (6 avatares por defecto).
2. Subir avatar desde el dispositivo (con opción de compartir).
3. Avatares compartidos (cargados dinámicamente desde Firebase).
4. Botón GUARDAR (guarda la selección en el perfil del usuario).

## Flujo de uso

### Para subir un avatar compartido
1. Usuario abre `editAvatar`.
2. Pulsa "Seleccionar imagen" y elige una foto.
3. Marca "Compartir para todos" (opcional).
4. Pulsa "SUBIR AVATAR COMPARTIDO".
5. El avatar se sube a Storage y metadatos a Database.
6. Otros usuarios ven el nuevo avatar en la sección "Avatares compartidos".

### Para seleccionar un avatar compartido
1. Usuario abre `editAvatar`.
2. En la sección "Avatares compartidos" aparecen los avatares subidos.
3. Usuario hace clic en uno.
4. Pulsa "GUARDAR".
5. El avatar se guarda en el perfil del usuario (`users/{userId}/avatar`).

## Configuración necesaria en Firebase

### Realtime Database Rules (temporal, para pruebas)
```json
{
  "rules": {
    "sharedAvatars": {
      ".read": true,
      ".write": "auth != null"
    },
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

### Storage Rules (temporal, para pruebas)
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /sharedAvatars/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

**⚠️ Nota:** Las reglas anteriores son permisivas para desarrollo. En producción, restringe lectura y escritura según necesidad.

## Notas técnicas

- **Firebase Storage:** Bucket configurado en `google-services.json` → `storage_bucket`.
- **Realtime Database URL:** `https://fitvalle-fced7-default-rtdb.firebaseio.com/`
- **Coil:** Usada para cargar imágenes remotas desde URLs (AsyncImage).
- **Async operations:** DAOs usan callbacks; en futuro se puede migrar a `Flow` o `suspend functions`.

## Próximas mejoras

- Moderación/aceptación de avatares antes de publicar.
- Permitir que solo trainers suban avatares.
- Borrar avatares propios o admin-only.
- Mostrar autor/detalles del avatar al seleccionar.
- Caché local para evitar recargas constantes.
- Mostrar avatares en ProfileScreen / otras pantallas si se seleccionó uno compartido.

## Testing

1. **Compilar desde Android Studio:**
   - `Build → Make Project`
   - `Run → Run 'app'` (con dispositivo/emulador conectado).

2. **Probar subida:**
   - Abre la app, ve a Perfil → Editar Avatar.
   - Selecciona una imagen, marca "Compartir" y pulsa "SUBIR AVATAR COMPARTIDO".
   - En Firebase Console → Storage verifica que la imagen aparece en `sharedAvatars/`.
   - En Firebase Console → Realtime Database verifica que metadatos están en `sharedAvatars/{id}`.

3. **Probar selección:**
   - Abre otra cuenta o dispositivo.
   - Abre Editar Avatar.
   - La sección "Avatares compartidos" debe mostrar los avatares subidos.
   - Selecciona uno y pulsa "GUARDAR".
   - En Realtime Database, `users/{userId}/avatar` debe contener la URL del avatar compartido.

---

**Archivos modificados/creados:**
- ✅ `AvatarDao.kt` (nuevo)
- ✅ `SharedAvatarDao.kt` (nuevo)
- ✅ `AvatarEditScreen.kt` (actualizado)
- ✅ `app/build.gradle.kts` (añadida dependencia Firebase Storage)
- ✅ `gradle.properties` (actualizado para JDK 25)
