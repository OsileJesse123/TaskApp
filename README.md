# TaskApp

TaskApp is a small Android app for creating, reading, updating, and deleting tasks on the device. It exists so App Functions can be studied later. This project does not call the Android App Functions API. The task screens and the local database are the playground for that work.

## Architecture

The app is one Gradle module, `:app`. Code is split by feature package, not by feature module.

```
Presentation  ->  feature use cases  ->  TaskRepository  ->  Room
```

Dependencies point toward `core`. `core` does not depend on a feature, and no feature depends on another feature.

Room, the task table, and `TaskRepository` live in `core`. The list, detail, and form features all store the same task. Putting the repository in one feature would force the other features to depend on it.

`TaskRepositoryImpl` takes an IO `CoroutineDispatcher` instead of calling `Dispatchers.IO`. `CoroutineModule` provides that dispatcher, the default dispatcher, and an application `CoroutineScope`. Tests pass a test dispatcher so writes do not depend on the real IO pool.

Each feature has its own use cases and its own presentation types:

- `feature/tasklist` shows tasks, searches them, filters them by the day they were created, and deletes a selection.
- `feature/taskdetail` loads one task by id and can delete it.
- `feature/taskform` creates a task, or loads one by id and updates it.

A screen composable takes state and callbacks. It does not take a `ViewModel`, a `NavController`, or a DAO. A route composable is the only place that reads the `ViewModel`. `TaskNavHost` is the app shell and is the only place that knows the routes.

Navigation passes a task id. The next screen loads that row from Room. Task objects are not navigation arguments.

Rendered state uses `StateFlow`. Failures and one-time navigation use a `SharedFlow` with no replay and a sealed event class, so a snackbar or a pop does not run again after rotation. Empty lists, a missing task, and invalid form fields stay on the screen state so those screens can be previewed.

## Package structure

```
com.jesse.taskapp
├── MainActivity.kt
├── TaskApplication.kt
├── navigation
│   ├── TaskDestination.kt
│   └── TaskNavHost.kt
├── core
│   ├── common          clock and date formatting
│   ├── di              Hilt modules for the database, clock, and coroutine dispatchers
│   ├── domain          Task and TaskRepository
│   └── data            Room entity, DAO, database, repository
├── feature
│   ├── tasklist
│   ├── taskdetail
│   └── taskform
└── ui/theme
```

Each feature package contains `domain` and `presentation`.

## Libraries

- Kotlin and Jetpack Compose with Material 3
- Navigation Compose, with a task id as the route argument
- Room and Kotlin Flow
- Hilt
- Kotlin coroutines
- JUnit 4, kotlinx-coroutines-test, and Turbine for unit tests
- Compose UI test and an in-memory Room database for instrumentation tests

## Setup

1. Install Android Studio with a compile SDK of 36 and a device or emulator on API 27 or higher.
2. Open this directory as a project.
3. Let Gradle download the dependencies, then run the `app` configuration.

From a terminal, at the project root:

```bash
./gradlew :app:assembleDebug
```

Install the debug build from Android Studio, or with:

```bash
./gradlew :app:installDebug
```

## Tests

Unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Instrumentation tests need a started emulator or a connected device:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Unit tests cover use cases, ViewModels, and the repository writes that must run on the injected dispatcher, including one-shot errors that must not replay. The Room tests construct the repository with a test dispatcher and cover insert, update, search, the creation-day window, and bulk delete. Compose tests open the list, detail, and form screens with state passed in, without a ViewModel. One Hilt test creates a task and checks that the list shows it.
