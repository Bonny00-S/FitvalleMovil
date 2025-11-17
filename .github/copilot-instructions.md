# Copilot Instructions for FitvalleMovil

## Project Overview
FitvalleMovil is a Kotlin-based Android fitness app using Jetpack Compose for UI and Firebase (Realtime Database, Auth) for backend services. The app manages users, workouts, templates, and training sessions, supporting both clients and trainers.

## Architecture & Key Patterns
- **UI:** Jetpack Compose screens in `app/src/main/java/com/example/fitvalle/` (e.g., `TrainingSessionScreen.kt`, `ExerciseSessionDetailScreen.kt`).
- **Data Layer:** DAOs (e.g., `UserDao`, `WorkoutDao`, `TemplateDao`) encapsulate all Firebase access. Data models are simple Kotlin data classes (`User.kt`, `Workout.kt`, `Template.kt`).
- **State Management:** ViewModels (e.g., `UserFormViewModel`) use Compose's `mutableStateOf` for UI state.
- **Navigation:** Uses Compose navigation with `NavController` passed to screens.
- **Firebase:** All user, workout, and template data is stored per-user in Realtime Database. Auth is required for most operations.

## Developer Workflows
- **Build:** Use `./gradlew assembleDebug` or build from Android Studio.
- **Run:** Deploy via Android Studio or `./gradlew installDebug`.
- **Test:** Unit tests in `app/src/test/java/` and instrumented tests in `app/src/androidTest/java/`.
- **Dependencies:** Managed in `build.gradle.kts` files. Firebase config in `google-services.json`.

## Project-Specific Conventions
- **Firebase URLs** are hardcoded in DAOs. Each user has a separate node (e.g., `workouts/{userId}/`).
- **Passwords** are hashed with SHA-256 before storage (see `UserDao.kt`).
- **Dates** are formatted as `yyyy-MM-dd HH:mm:ss` (see DAOs).
- **Roles:** `User` model supports roles (default: `client`, can be `trainer`).
- **Trainer Profiles:** Extra fields in `User` for trainers (bio, specialty, students).
- **UI Theme:** Custom Compose theme in `ui/theme/`.

## Integration Points
- **Firebase Auth:** Used for login, registration, and user-specific data access.
- **Firebase Realtime Database:** All persistent data (users, workouts, templates).
- **Coil:** For image loading in Compose.
- **ExoPlayer:** For video playback in exercise detail screens.

## Examples
- To add a new data type, create a data class, a DAO for Firebase access, and update relevant screens.
- To add a new screen, create a Composable in the main package and add it to the navigation graph.

## Key Files & Directories
- `app/src/main/java/com/example/fitvalle/` — Main app logic, screens, DAOs, models
- `app/build.gradle.kts` — App-level dependencies and config
- `google-services.json` — Firebase project config
- `ui/theme/` — Compose theming

---
For questions about project structure or conventions, review DAOs and data models for canonical patterns. When in doubt, follow the Compose + Firebase idioms used throughout the codebase.
