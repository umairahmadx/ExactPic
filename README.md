# ExactPic 📸

[![Android Release Build](https://github.com/umair-ahmed/exactpic/actions/workflows/build.yml/badge.svg)](https://github.com/umair-ahmed/exactpic/actions/workflows/build.yml)
![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-orange.svg)
![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue.svg)
![Language](https://img.shields.io/badge/Kotlin-2.0-purple.svg)
![UI](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)
![Zero Secrets](https://img.shields.io/badge/Secrets%20%2F%20.env-None%20Required-success.svg)
![Offline](https://img.shields.io/badge/100%25-Offline%20%26%20Private-success.svg)

**ExactPic** is a privacy-first, zero-permission Android utility crafted for precision image file-size padding and dimension management. 

Many official portals—such as government visa portals, passport renewal systems, academic test portals, and civil service applications—enforce rigid file-size ranges (e.g. *“file size must be between 200 KB and 500 KB”* or *“minimum upload threshold is 50 KB”*). Uploading an image below the threshold results in an immediate rejection. ExactPic solves this problem by using standard-compliant binary padding techniques to expand images to any exact kilobyte target without degrading picture quality.

---

## 🔒 100% Offline & Zero Secrets Architecture

- **No `.env` or API Keys Needed**: ExactPic does not call any third-party APIs, LLMs, or cloud providers. It requires no secret tokens, credentials, or environment files.
- **Zero Internet Permissions**: `android.permission.INTERNET` is not included in the manifest. The app cannot make outbound network connections.
- **Local On-Device Engine**: All calculations, binary chunk parsers, and bitmap compressors execute locally on the device processor.

---

## ✨ Features

### 🎯 Exact File Size Padding
- **Target Kilobytes (KB)**: Set an exact file size target (e.g., 50 KB, 200 KB, 500 KB, 1024 KB).
- **Standards-Compliant Binary Techniques**:
  - **JPEG**: Non-destructive `COM` (Comment marker `0xFF 0xFE`) embedding prior to the End-Of-Image (`EOI`) marker, or safe trailing padding beyond `0xFF 0xD9`.
  - **PNG**: RFC-compliant `tEXt` ancillary metadata chunks with valid 32-bit CRC (Cyclic Redundancy Check) checksum calculations that standard image decoders safely ignore.
  - **WebP**: Safe chunk extensions adhering to RIFF container specifications.
- **Intelligent Compression**: When a source image exceeds the target size, the engine applies iterative quality compression and scaling down to meet the desired boundary, minimizing quality loss.

### 📐 Multi-Unit Dimension & Aspect Ratio Controls
- **Flexible Units**:
  - **Pixels (`px`)**: Direct pixel-perfect sizing.
  - **Percentage (`%`)**: Uniform percentage scaling from 1% to 500%.
  - **Physical Print Units (300 DPI)**: Inches (`in`), Centimeters (`cm`), and Millimeters (`mm`) computed against standard 300 DPI print resolutions.
- **Aspect Ratio Lock**: Constrain proportions automatically or freely define custom widths and heights.

### 👁️ Real-Time Visual Inspection
- **Interactive Before/After Switch**: Compare original vs. processed images instantly.
- **Side-by-Side Split View**: Evaluate visual quality differences side by side.
- **Checkerboard Background**: Inspect transparent alpha channels for PNG and WebP files.
- **Detailed File Diagnostics**: Inspect original vs. output byte counts, dimension changes, and format details.

### 💾 Export & Sharing
- **System Gallery Storage**: Save directly to public DCIM/Pictures using Android's MediaStore.
- **Zero-Permission Share Sheet**: Share processed photos directly to messaging apps, email, or drive using Android's `FileProvider`.

---

## 🏗️ Technical Architecture

- **Package & Namespace**: `com.umair.exactpic`
- **UI Toolkit**: Jetpack Compose with Material Design 3
- **Design System**: Strict Material Design 3 guidelines (centralized typography, color tokens, and 48dp accessible touch targets)
- **State Management**: Kotlin Coroutines & `StateFlow` with unidirectional data flow (UDF)
- **Binary Engine**: Kotlin-native byte stream manipulation (`ImagePadderEngine`)
- **Testing**: Robolectric JVM unit tests and Roborazzi screenshot verification

---

## 📁 Repository Structure

```
.
├── .github/
│   └── workflows/
│       └── build.yml               # Automated GitHub Actions Release build
├── app/
│   ├── build.gradle.kts            # App-level build script (ProGuard & R8 release config)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml # Zero-permission offline manifest
│       │   ├── java/com/umair/exactpic/
│       │   │   ├── MainActivity.kt
│       │   │   ├── engine/         # Binary padding & compression algorithms
│       │   │   │   └── ImagePadderEngine.kt
│       │   │   ├── model/          # Enums & metadata data models
│       │   │   │   └── ImageMetadata.kt
│       │   │   ├── ui/             # Jetpack Compose screens and components
│       │   │   │   ├── ImagePadderScreen.kt
│       │   │   │   ├── components/
│       │   │   │   └── theme/
│       │   │   ├── util/           # Bitmap, MediaStore & FileProvider utilities
│       │   │   │   └── ImageUtils.kt
│       │   │   └── viewmodel/      # ImagePadderViewModel state management
│       │   └── res/                # Vector drawables, themes, and localized strings
│       └── test/                   # Local Robolectric & JUnit test suite
├── gradle/
│   ├── libs.versions.toml          # Gradle Version Catalog
│   └── wrapper/                    # Gradle wrapper configuration
├── gradlew                         # Gradle wrapper script
├── build.gradle.kts                # Root build file
├── settings.gradle.kts             # Gradle project settings
└── README.md
```

---

## 🚀 Building the App Locally

### Prerequisites
- **Java**: JDK 21 (Eclipse Temurin or OpenJDK)
- **Android SDK**: compileSdk 36, minSdk 24
- **Android Studio**: Ladybug (2024.2.1) or newer

### Pushing to GitHub (Important)

When pushing the project to your GitHub repository, ensure the `gradle/` folder (including `gradle/libs.versions.toml` and `gradle/wrapper/`) is committed:
```bash
git add gradle/ gradlew app/ build.gradle.kts settings.gradle.kts README.md .github/
git commit -m "Add project files, Gradle Version Catalog, and CI workflow"
git push origin main
```
*(Note: If `gradle/libs.versions.toml` is omitted or ignored, Gradle will fail with `Unresolved reference 'libs'`)*

### Command Line Build

1. **Clone the repository**:
   ```bash
   git clone https://github.com/umair-ahmed/exactpic.git
   cd exactpic
   ```

2. **Grant execute permissions to the wrapper**:
   ```bash
   chmod +x gradlew
   ```

3. **Run unit and Robolectric tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

4. **Build the optimized Release APK**:
   ```bash
   # Generate a local signing key if you don't already have one
   keytool -genkeypair -v -keystore my-upload-key.jks -alias upload \
     -keypass android -storepass android -keyalg RSA -keysize 2048 \
     -validity 10000 -dname "CN=ExactPic,O=Umair,C=US"

   # Assemble the release build
   STORE_PASSWORD=android KEY_PASSWORD=android ./gradlew assembleRelease
   ```
   The minified and resource-shrunk Release APK will be created at:
   ```
   app/build/outputs/apk/release/app-release.apk
   ```

---

## ⚙️ Automated GitHub Actions CI Workflow

The workflow at `.github/workflows/build.yml` runs automatically whenever code is pushed to `main` or `master`, or via pull requests:

1. **Automatic Checkout & JDK 21 Setup**: Configures Temurin Java 21 with Gradle caching.
2. **Release Keystore Setup**:
   - **Zero Configuration Out-Of-The-Box**: If no secrets are defined, CI automatically generates a secure upload keystore on the fly and produces a signed, installable Release APK.
   - **Custom Production Signing (Optional)**: If you want to sign with your personal Google Play upload key, add these repository secrets in **Settings > Secrets and variables > Actions**:
     - `KEYSTORE_BASE64`: Your `.jks` file encoded in base64 (`base64 -w 0 my-release-key.jks`).
     - `STORE_PASSWORD`: Keystore password.
     - `KEY_PASSWORD`: Key password.
3. **Automated Verification**: Runs the complete unit and Robolectric test suite.
4. **Builds Release APK**: Executes `./gradlew assembleRelease` with R8 code minification and resource shrinking enabled.
5. **Artifact Publishing**: Uploads the production `app-release.apk` as a downloadable artifact (`ExactPic-release-apk`) in the GitHub Actions summary.

---

## 🛡️ License & Privacy

- **Privacy First**: ExactPic does not collect, record, or transmit user images. All operations happen in local volatile memory.
- Released under the open-source MIT License.
