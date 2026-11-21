# Building DHUNLO from Source

This document contains instructions to compile and assemble the **DHUNLO** Android application from this corresponding source repository.

---

## 1. Prerequisites

### Java Development Kit (JDK)
* **JDK Version:** JDK 17 or JDK 21 (Eclipse Temurin, Amazon Corretto, or Android Studio JetBrains Runtime recommended).
* Verify installation:
  ```bash
  java -version
  ```

### Android SDK & Tooling
* **Android SDK Command-Line Tools & Platform:**
  * Compile SDK: `37` (Android 17 developer preview) / `34`–`36`
  * Minimum SDK: `24` (Android 7.0)
  * Target SDK: `35` (Android 15)
  * Build-Tools: `36.0.0`
* **Android NDK & CMake:**
  * Required to compile native Opus audio decoders (`opusDecoder`).
  * NDK Version: `30.0.16248370` (or Android NDK r27b+)
  * CMake: `3.22.1` or newer

### Environment Configuration
Ensure `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) environment variable points to your installed Android SDK:
* **Windows:** `%LOCALAPPDATA%\Android\Sdk`
* **Linux:** `$HOME/Android/Sdk`
* **macOS:** `$HOME/Library/Android/sdk`

---

## 2. Clone & Submodule Initialization

DHUNLO requires native Opus decoder sources and protobuf schemas tracked as Git submodules:

```bash
# Clone the repository
git clone https://github.com/avirallchaudhary/Dhunlo-Source.git
cd Dhunlo-Source

# Initialize and update submodules
git submodule update --init --recursive
```

---

## 3. Assembling the Android APK

### Debug APK Build
To assemble an installable debug package:
```bash
# Linux / macOS
./gradlew :androidApp:assembleDebug

# Windows PowerShell / CMD
.\gradlew.bat :androidApp:assembleDebug
```
Output: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

### Release APK Build
To assemble the official installable release APK:
```bash
# Linux / macOS
./gradlew :androidApp:assembleRelease

# Windows PowerShell / CMD
.\gradlew.bat :androidApp:assembleRelease
```
Output: `androidApp/build/outputs/apk/release/androidApp-release.apk`

---

## 4. Signing Configuration

By default, in the absence of external keystore environment variables, Gradle uses the local Android debug keystore (`~/.android/debug.keystore`) to sign the Release APK using APK Signature Scheme v2. This reproduces the official distributed certificate fingerprint (`33b85da983e857e90511fffaeef596993d8ffaff1e62941cfb48412e7fd2bbaa`).

To sign with your own custom release certificate, supply the following environment variables prior to running `assembleRelease`:
```bash
export KEYSTORE_PATH="/path/to/your/release.keystore"
export KEYSTORE_PASSWORD="your-keystore-password"
export KEY_ALIAS="your-key-alias"
export KEY_PASSWORD="your-key-password"
```

---

## 5. Verification

Inspect the compiled APK metadata using `aapt2` and verify the signature using `apksigner`:

```bash
# Inspect package, version, and labels
aapt2 dump badging androidApp/build/outputs/apk/release/androidApp-release.apk

# Verify signature scheme and certificate fingerprint
apksigner verify --verbose --print-certs androidApp/build/outputs/apk/release/androidApp-release.apk
```
