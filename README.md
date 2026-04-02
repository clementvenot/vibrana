***

# Vibrana 🎸🎹🎙️

![Android](https://img.shields.io/badge/Android%207.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Java](https://img.shields.io/badge/Java%2017-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![AudioRecord](https://img.shields.io/badge/AudioRecord-000000?style=for-the-badge&logo=android&logoColor=white)
![SoundPool](https://img.shields.io/badge/SoundPool-1E88E5?style=for-the-badge&logo=music&logoColor=white)
![Signal Processing](https://img.shields.io/badge/Signal%20Processing-AE00FF?style=for-the-badge&logo=waveform&logoColor=white)
![Autocorrelation](https://img.shields.io/badge/Autocorrelation%20Engine-6200EA?style=for-the-badge&logo=wave&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white)
![Custom Canvas](https://img.shields.io/badge/Custom%20Canvas-FF6F00?style=for-the-badge&logo=androidstudio&logoColor=white)
![AAC Recording](https://img.shields.io/badge/AAC%2044.1kHz%20%2F%20128kbps-FF0000?style=for-the-badge&logo=soundcloud&logoColor=white)
![Low Latency](https://img.shields.io/badge/Low%20Latency-2962FF?style=for-the-badge&logo=speedtest&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![License](https://img.shields.io/badge/License-UNLICENSED-green?style=for-the-badge)

**Vibrana** is a high‑performance digital toolbox for musicians. Built to deliver professional‑grade precision in a mobile format, the app combines real‑time signal processing, high‑fidelity recording, and low‑latency sound synthesis.

***

## 🌟 Key Features

### 🎯 Smart Tuner (`TuneGuitarActivity`)

Vibrana’s tuner goes far beyond simple FFT-based implementations to offer stability and precision tailored to string instruments.

*   **Detection Engine (YIN/Autocorrelation)**: Uses time‑domain autocorrelation to identify the fundamental frequency, naturally filtering out higher harmonics that commonly confuse standard tuners.
*   **Hybrid Mode**:
    *   **Auto‑Sensing**: Detects the played string (E2 to E4) with an intelligent 15 Hz capture threshold.
    *   **Manual Lock‑On**: Focuses on a single target frequency for ultra‑accurate tuning in noisy environments.
*   **Precision UX**: Dynamic‑bias interface with exponential smoothing to prevent needle jitter. Switches to **Emerald Green** when the deviation is under 1 Hz.

  <img width="1306" height="579" alt="Screenshot 2026-04-03 at 00 31 14" src="https://github.com/user-attachments/assets/b811a8b8-861a-4e64-a5a8-2e46cbffee8f" />


### 🎙️ Recording Studio (`RecordActivity`)

A recorder designed for musicians who want to capture ideas instantly without sacrificing quality.

*   **Master Quality**: AAC encoding at **44.1 kHz / 128 kbps**, ensuring a full sound spectrum for vocals and instruments.
*   **Real‑Time Visualization**: Dynamic waveform based on peak amplitude (`getMaxAmplitude`) to monitor audio dynamics.
*   **Contextual Organization**: File naming before recording and structured storage in the app’s internal directory for secure access.

***

### 🎹 Low‑Latency Virtual Piano (`PianoActivity`)

A handy instrument for finding melodies or checking keys.

*   **SoundPool Engine**: Sounds are preloaded in RAM for instant triggering, eliminating the latency typical of `MediaPlayer`.
*   **Full Polyphony**: Multi‑touch support that allows complex chords and fast arpeggios.

***

## 🛠️ Technical Stack

### Architecture & Performance

*   **Multi‑Threading**: Heavy calculations (like autocorrelation) run on a `Background Thread` to keep the UI at a smooth 60 FPS.
*   **Memory Management**: Systematic release of resources (`AudioRecord`, `MediaRecorder`, `SoundPool`) during Android lifecycle events (`onStop`, `onDestroy`) to prevent memory leaks.
*   **Compatibility**: Optimized for Android 7.0+ (API 24) up to the latest versions (API 36).

### Key Components

| Module        | Android API / Technology                                       |
| ------------- | -------------------------------------------------------------- |
| **Audio In**  | `AudioRecord` (Raw analysis)                                   |
| **Audio Out** | `SoundPool` (Sample playback)                                  |
| **Storage**   | `ExternalFilesDir` (Secure data handling)                      |
| **UI**        | `ConstraintLayout`, `Material Design 3`, `Custom Canvas Views` |

***

## 📖 Installation & Contribution Guide

### Requirements

*   Android Studio Koala (or newer)
*   A physical Android device (strongly recommended for accurate mic/speaker latency testing)

### Quick Install

1.  Clone the repo:  
    `git clone https://github.com/votre-projet/vibrana.git`
2.  Open the project in Android Studio.
3.  Sync with Gradle.
4.  Deploy to your device.

***

## 🔒 Privacy & Security

Vibrana respects your privacy:

*   Microphone access is used **only** while the app is active.
*   No audio data is ever sent to external servers. All processing and storage occur **locally** on your device.

***

## 📜 License

There is no license; you're free to use it.

---

Feel free to contribute to this project by submitting issues or pull requests.

For any questions or support, please contact [Clément Venot](mailto:clement.venooot@gmail.com).

---
