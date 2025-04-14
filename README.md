# 📱 AuthCameraApp

This is a basic Android app that demonstrates Firebase authentication and camera functionality. It was developed as an assignment for learning Firebase integration, CameraX usage, and image storage in Firebase Storage.

---

## ✅ Features

### 🔐 Authentication
Users can sign in using three methods:
- Google Sign-In
- Facebook Login
- Email and Password Login

### 📸 Camera Access
- The app uses CameraX to show a live camera preview.
- Users can capture a photo using the device's camera.

### ☁️ Firebase Integration
- Captured photos are uploaded to Firebase Storage.
- Users can retrieve and display the photo from Firebase.

---

## 🧱 Layout Components (XML)

The main layout includes:
- `PreviewView`: to show the live camera feed.
- `Button`: to capture a photo.
- `Button`: to retrieve a photo from Firebase.
- `ImageView`: to display the retrieved photo.
- Buttons for Google, Facebook, and Email authentication.

---

## 🛠️ Technologies Used

- Kotlin
- Android Studio
- Firebase Authentication
- Firebase Storage
- CameraX
- Glide (to load images from Firebase Storage)

---

## 📷 Screenshots

<img width="335" alt="Screenshot 2025-04-14 at 12 41 47 AM" src="https://github.com/user-attachments/assets/9e1272f4-3e02-4461-b58f-2918ee75675a" />
<img width="335" alt="Screenshot 2025-04-14 at 12 46 53 AM" src="https://github.com/user-attachments/assets/614a13ce-b2bd-4fd4-b6a5-504186f9efc9" />
<img width="335" alt="Screenshot 2025-04-14 at 12 47 55 AM" src="https://github.com/user-attachments/assets/2a181847-e8f5-4344-9ca9-01200709d266" />
<img width="335" alt="Screenshot 2025-04-14 at 12 48 25 AM" src="https://github.com/user-attachments/assets/55f8a042-3579-49ef-8256-8d7d697d4ab4" />
<img width="335" alt="Screenshot 2025-04-14 at 12 49 01 AM" src="https://github.com/user-attachments/assets/d80f2855-bcd8-418f-aab3-d7a2337a82f1" />






## 🚀 How to Run

1. Clone this repository or download the source code.
2. Connect the app to your Firebase project:
   - Add `google-services.json` inside the `/app` folder.
   - Enable Authentication (Google, Facebook, Email/Password).
   - Enable Firebase Storage.
3. Update your `strings.xml` with:
   - `default_web_client_id`
   - `facebook_app_id`
   - `facebook_client_token`
4. Run the app on an emulator or physical device.

---

## 📩 Authors

- **Omar Serrano**
- **Michelle Barajas**
