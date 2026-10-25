# TaskApp

TaskApp is a small Android app for creating, reading, updating, and deleting tasks on the device. The task screens and a local Room database are the playground. Gradle feature modules are used here to show a conventional Android split, not because the app is large enough to need them.

App Functions work is in progress. `:core` has `BaseTaskAppFunctionService` (`getTasks` and `getTask`). A concrete service, App Function XML, and the manifest entry are not wired yet.

## Modules

```
:app  →  :feature:tasklist
      →  :feature:taskdetail
      →  :feature:taskform
      →  :core

:feature:tasklist   →  :core
:feature:taskdetail →  :core
:feature:taskform   →  :core
```

Dependencies point toward `:core`. Features do not depend on each other. `:app` is the shell: `MainActivity`, Hilt application, navigation graph, and destinations.

| Module | Role |
| --- | --- |
| `:app` | Application, activity, `TaskNavHost`, type-safe destinations |
| `:core` | Room, `TaskRepository`, clock, date formatting, Hilt data modules, theme, App Functions base |
| `:feature:tasklist` | List, search, creation-date range filter, bulk delete |
| `:feature:taskdetail` | One task by id, delete |
| `:feature:taskform` | Create, or load by id and update |

Room, the task table, and `TaskRepository` live in `:core` because every feature stores the same task. Putting the repository in one feature would force the other features to depend on it.

## Architecture

```
Presentation  ->  feature use cases  ->  TaskRepository  ->  Room
```

`TaskRepositoryImpl` takes an IO `CoroutineDispatcher` instead of calling `Dispatchers.IO`. `CoroutineModule` provides that dispatcher and the default dispatcher (used by App Functions). Tests pass a test dispatcher so writes do not depend on the real IO pool.

A screen composable takes state and callbacks. It does not take a `ViewModel`, a `NavController`, or a DAO. A route composable is the only place that reads the `ViewModel`. `TaskNavHost` is the only place that knows the routes.

Navigation passes a task id. The next screen loads that row from Room. Task objects are not navigation arguments.

Rendered state uses `StateFlow`. Failures and one-time navigation use a `SharedFlow` with no replay and a sealed event class, so a snackbar or a pop does not run again after rotation. Empty lists, a missing task, and invalid form fields stay on the screen state so those screens can be previewed.

List search text is written into UI state on each keystroke. The database query is debounced so typing does not wait on Room.

## Layout

```
app/            shell, navigation
core/           data, domain, DI, theme, App Functions base
feature/
  tasklist/
  taskdetail/
  taskform/
```

Each feature module contains `domain` and `presentation`. Strings for a feature live in that feature's resources. `:core` publishes test fixtures (`FakeTaskRepository`, `MainDispatcherRule`, sample tasks) for feature unit tests.

## Libraries

- Kotlin and Jetpack Compose with Material 3
- Navigation Compose, with a task id as the route argument
- Room and Kotlin Flow
- Hilt
- Kotlin coroutines
- Android App Functions (base service only)
- JUnit 4, kotlinx-coroutines-test, and Turbine for unit tests
- Compose UI test, Espresso (Compose idle), and an in-memory Room database for instrumentation tests

## Setup

1. Install Android Studio with compile SDK 37. The app targets API 36 and runs on API 27 or higher.
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
./gradlew :core:testDebugUnitTest \
  :feature:tasklist:testDebugUnitTest \
  :feature:taskdetail:testDebugUnitTest \
  :feature:taskform:testDebugUnitTest
```

Instrumentation tests need a started emulator or a connected device:

```bash
./gradlew :core:connectedDebugAndroidTest \
  :feature:tasklist:connectedDebugAndroidTest \
  :feature:taskdetail:connectedDebugAndroidTest \
  :feature:taskform:connectedDebugAndroidTest \
  :app:connectedDebugAndroidTest
```

Unit tests cover use cases, ViewModels, date formatting, and repository writes that must run on the injected dispatcher, including one-shot errors that must not replay. The Room instrumentation tests construct the repository with a test dispatcher and cover insert, update, search, the creation-time window, and bulk delete. Compose tests in each feature open that screen with state passed in, without a ViewModel. One Hilt test in `:app` creates a task through the real graph and checks that the list shows it.
