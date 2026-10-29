# Animarch

## 📝 Description

**Animarch** is an anime tracking and discovery app built with modern Android technologies. It allows users to browse recent anime episodes, view series details, and explore anime content with a beautiful UI.

> ⚠️ **Important Notice**: This application is for **practice and learning purposes only**. It is not intended for production use. The codebase demonstrates Android development patterns, Compose UI, and API integration, but should not be used as a basis for a commercial application without significant modifications and proper licensing.

## ✨ Features

- Browse recent anime episodes and series details
- AI-powered chat assistance (Gemini AI integration)
- Community features for anime discussions
- Favoriting and watch history tracking
- Beautiful otaku-themed UI with animated components

## 📦 Technical Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Networking**: Retrofit + Supabase
- **Image Loading**: Coil
- **Database**: Room + Supabase
- **AI**: Gemini API

## 👤 Attribution

- **Anime Community**: This app was inspired by and designed for the global anime community. All anime content is gathered from public APIs (AniList, Anikoto) and is intended for fan consumption and discovery.

- **Artist Making Anime**: Special thanks to all the incredible artists, animators, and studios who create the anime we love. This app is a tribute to your hard work and creativity.

- **Backend Services**: Supabase for database and authentication services.

- **AI Integration**: Google's Gemini API for intelligent anime recommendations and chat assistance.

## 🚀 Getting Started

**Prerequisites**:
- Android Studio (latest version recommended)
- Gemini API key (for AI features)
- Supabase project (for backend)

**Installation**:
1. Open this project in Android Studio
2. Create a `.env` file in the project root based on `.env.example`
3. Set your `GEMINI_API_KEY` in the `.env` file
4. Remove the debug signing config from `app/build.gradle.kts` if building for release
5. Run the app on an emulator or physical device

## 📜 License

This project is for educational and practice purposes. Anime art and content remain the property of their respective creators and studios.

---

< Built with ❤️ for the anime community
