# YESMIN AI – Personal AI Companion & Android Smart Assistant

**Application ID:** com.yesmin.ai
**Version:** 1.0.0 (Material 3 Native Android)
**Primary Brain:** Groq Cloud API (Llama 3.3 70B Versatile)
**Secondary Brain:** Google Gemini 2.5 Flash (Backup AI)

---

## Key Features

1. Dual AI Brain with Auto-Failover (Groq Primary + Gemini Backup)
2. Female Voice Interaction (Sweet Hindi, Hinglish, Bengali, English)
3. Android Action Engine (YouTube Music, WhatsApp, Camera, Settings, Screenshot, Accessibility gestures)
4. Coding Assistant & Termux Studio
5. Keystore-backed secure storage

---

## How to Build on Android Using Termux

1. Open Termux and update packages:
   pkg update && pkg upgrade -y
   pkg install -y openjdk-17 gradle git wget unzip

2. Run the build script:
   chmod +x build-termux.sh
   bash build-termux.sh

3. Install the APK:
   termux-open app/build/outputs/apk/debug/app-debug.apk
