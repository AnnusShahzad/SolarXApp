# SolarX: Real-Time Solar Inverter Monitor

**An Android app that shows live data from a SolaX solar inverter.** It shows solar output and grid (WAPDA) power as they change, and warns you when grid draw goes over a limit you set.

## Highlights

- ⚡ **Live dashboard**: polls the SolaX API about every 1.5 seconds and updates the readings immediately
- ⚠️ **Grid-power warning**: set a WAPDA threshold in watts, and the app warns you when grid draw exceeds it
- 🔐 **Login and settings screens**: store your inverter credentials and turn grid monitoring on or off
- 🏗️ **MVVM architecture**: `ViewModel`s handle state and Retrofit handles the API

## Tech Stack

Kotlin · Jetpack Compose · Material 3 · Retrofit · MVVM · Gradle (Kotlin DSL)

## Structure

```
app/src/main/java/.../
├── MainActivity.kt
├── homePage.kt / loginPage.kt / settingPage.kt   # Compose screens
├── responseViewModel.kt / loginViewModel.kt      # state + API calls
└── ui/theme/                                     # Material 3 theme
```

## Build

```bash
./gradlew assembleDebug
```

Open the project in Android Studio and run it on a device on the same network as the inverter.
