# CoupleFinance Mobile Starter (React Native / Expo)

This mobile starter connects to the Spring Boot backend server for couple expense sharing and tracking.

## 🚀 Quick Start

1. **Install dependencies**:
   ```bash
   cd mobile
   npm install
   ```

2. **Configure Backend URL**:
   In `App.js`:
   - Android Emulator: `http://10.0.2.2:8080`
   - iOS Simulator / Physical Device: `http://<YOUR_LOCAL_IP>:8080` (e.g. `http://192.168.1.100:8080`)

3. **Start Expo App**:
   ```bash
   npm start
   # or
   npm run android
   # or
   npm run ios
   ```

## 🔐 Google Authentication Setup

To use Google Sign-in on mobile:
1. Configure your OAuth 2.0 Web Client ID and Android/iOS Client IDs in [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
2. Set `@react-native-google-signin/google-signin` with `GoogleSignin.configure({ webClientId: 'YOUR_WEB_CLIENT_ID' })`.
3. When user signs in, obtain `idToken` and POST it to backend `/api/auth/google`.
