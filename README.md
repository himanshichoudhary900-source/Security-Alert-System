# 🚨 Security Alert System - Android Application

Emergency response mobile application with real-time GPS tracking, camera integration, and group-based alert distribution.

## 📱 Features

- **One-Tap Emergency Alert** with confirmation dialog
- **GPS Location Tracking** using Google Play Services
- **Photo Evidence Capture** via Camera API
- **Group Management** (Family, Campus Friends, Security)
- **Alert History** with RecyclerView
- **Google Maps Integration** for location viewing
- **Offline-First Architecture** with SQLite database

## 🛠️ Tech Stack

- **Language:** Java
- **Platform:** Android (Min SDK 21)
- **Database:** SQLite (3 normalized tables)
- **APIs:** Google Location Services, Camera API, FileProvider
- **UI:** Material Design, RecyclerView, CardView
- **Architecture:** MVC Pattern

## 📊 Database Schema

### Users Table
- Stores user credentials and profile information

### Groups Table
- Manages emergency contact groups
- Supports multiple members per group

### Alerts Table
- Stores emergency alerts with location, timestamp, and photos

## 🚀 Installation

1. Clone the repository
```bash
git clone https://github.com/yourusername/Security-Alert-System-Android.git
```

2. Open in Android Studio
3. Sync Gradle dependencies
4. Run on Android device (API 21+)

## 📸 Screenshots

[Add screenshots here]

## 🎯 How It Works

1. User registers and creates emergency contact groups
2. In emergency, user presses the SOS button
3. App automatically captures GPS location and photo
4. Alert is saved to all configured groups
5. Recipients can view alerts with location and evidence

## 🔐 Permissions Required

- Location (GPS tracking)
- Camera (Photo evidence)
- Storage (Photo saving)

## 👨‍💻 Developer

**Himanshi Choudhary**
- Email: himamshichouhdary900@gmail.com
- GitHub: [himanshichoudhary900-source](https://github.com/himanshichoudhary900-source)

## 📄 License

This project is created as part of academic coursework.

## 🎓 Project Status

✅ Completed: November 2024
