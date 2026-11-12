# Active Student — Android App & Campus Hub

Native Android application (Kotlin + Jetpack Compose + Material 3) connected to Google Firebase (`activestudent-cloudolog`) for the **Anurag University Active Student Portal**.

## Modules

- **Authentication (`@anurag.edu.in`)**: Email/Password sign-in, registration with Anurag roll/batch/section parsing, password reset, and role-based routing.
- **Hub Dashboard**: Real-time overview of active elections, live Tracurrency balance, and Online Canteen status.
- **Campus Voting**: Public & private (access-code gated) election rooms, atomic Firestore vote transactions (`runTransaction`), past election results leaderboard, and Voting Admin center (`voting@anurag.edu.in`).
- **Tracurrency**: Event financial rooms, live balance tracking (`fundsGiven + credits - debits`), contribution validation, offline ZXing UPI QR + UPI app intent, batch/section analytics donut chart, and Tracurrency Admin controls (`tracurrency@anurag.edu.in`).
- **Online Canteen**: Real-time menu with Veg / Non-Veg / Essentials filters, search, cloud-synced cart, GST (5%) checkout, atomic stock-decrementing orders, offline ZXing QR order receipts, and Canteen Staff dashboard (`canteen@anurag.edu.in`).

## Project Structure

- `android-app/` — Native Kotlin + Jetpack Compose Android project
- `index.html`, `index_source.html`, `login.html`, `register.html`, `style.css` — Web hub source files

## Build & Run (Android)

1. Open `android-app/` in Android Studio or build from PowerShell:
   ```powershell
   cd android-app
   .\gradlew.bat assembleDebug
   ```
2. Install onto a connected device or emulator (`emulator-5554`):
   ```powershell
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```
