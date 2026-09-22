# Androidxcut 🎬

> Modern offline-first multi-track video and media editor for Android, inspired by CapCut.

Androidxcut provides a complete offline video creation experience directly on device. It features multi-track timeline editing, clip trimming, dynamic transitions, real-time procedural color grading, text subtitles, animated creator stickers, offline procedural audio synthesis, and offline video project export.

---

## 📱 Download the APK

### Option 1: Download from GitHub Releases
1. Navigate to the **Releases** tab on the right side of this GitHub repository.
2. Under **Assets**, click on `androidxcut-v1.0-debug.apk` to download the APK directly to your phone or computer.
3. On your Android device, open the downloaded APK and tap **Install** (allow installation from unknown sources if prompted).

### Option 2: Download from GitHub Actions Artifacts
1. Go to the **Actions** tab in this repository.
2. Click on the latest workflow run ("Build and Publish Android APK").
3. Scroll down to the **Artifacts** section at the bottom of the page.
4. Download the `Androidxcut-APK` zip archive and extract the `.apk` file.

### Option 3: Export Directly from Google AI Studio
1. In the top navigation bar of Google AI Studio, click on **Settings** or the **Export** menu.
2. Select **Export / Build APK**.
3. Download the generated APK directly to your device.

---

## ✨ Features

- **Multi-Track Editing Timeline**:
  - Time ruler with micro-second precision scrubbing and frame stepping (±0.1s).
  - Main video track with clip thumbnails, duration tags, and dual-ended trim handles.
  - In-between clip transition selector (Fade, Dissolve, Slide, Zoom, Flash, Glitch, Wipe).
  - Dedicated secondary tracks for BGM music, SFX sound effects, text captions, and animated sticker overlays.

- **Real-Time Video Rendering Engine**:
  - Aspect ratio switcher (9:16 Reels/TikTok, 16:9 Widescreen, 1:1 Square, 4:5 Portrait, 21:9 Cinema).
  - Real-time color grading: Brightness, Contrast, Saturation, Vignette intensity.
  - Stylized filters: Cyberpunk, Vintage, Emerald, Sepia, Noir, Sunset, Glitch.
  - Procedural sample video clips and support for user media picked with Android's zero-permission Photo & Video Picker.

- **Offline Procedural Audio Engine**:
  - 100% offline audio synthesis via low-level `AudioTrack` PCM streaming (no internet connection required).
  - 6 Background Music themes: Cyber Synthwave, Lo-Fi Chill, Future Bass, Cinematic Ambient, 8-Bit Retro, Deep Focus.
  - 6 Sound Effects: Whoosh Transition, Camera Shutter, Glitch Hit, Pop Bubble, Cinematic Impact, Coin Chime.

- **Precision Editing Toolkit**:
  - **Split & Cut**: Split clips at the exact playhead timestamp.
  - **Speed Control**: Variable playback speed from 0.25x slow-motion to 3.0x fast-forward.
  - **Subtitles & Typography**: Multiple font treatments (Sans, Serif, Impact, Tech Mono, Neon Glow) with custom color palettes and badges.
  - **Graphic Stickers**: Integrated emoji and reaction badges.
  - **Clip Management**: Duplicate, rotate 90°, delete, or reorder clips.
  - **Multi-step Undo & Redo**: Safe non-destructive editing history.

- **Local Storage & Export**:
  - Room database persistence for project drafts, clip order, audio assignments, and overlay states.
  - Offline video export pipeline supporting 720p, 1080p, and 4K configurations.

---

## 🛠️ Building from Source

### Prerequisites
- JDK 21
- Android SDK 36 (targetSdk 36, minSdk 24)

### Build Commands
```bash
# Clone the repository
git clone https://github.com/<your-username>/androidxcut.git
cd androidxcut

# Make Gradle wrapper executable
chmod +x gradlew

# Build Debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🚀 Publishing a Release on GitHub

To automatically build and publish a new APK release:
```bash
# Create and push a tag
git tag v1.0.0
git push origin v1.0.0
```
The automated GitHub Actions workflow will build the APK and attach `androidxcut-v1.0-debug.apk` directly to the new GitHub Release.
