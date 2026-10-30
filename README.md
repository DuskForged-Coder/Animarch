# ANIMARCH
## Anime Tracking and Discovery Application
### Android Application Development Project Report

**Project Name:** Animarch  
**Project Category:** Android Application Development  
**Application Type:** Anime Tracking and Discovery Platform  
**Development Language:** Kotlin  
**UI Framework:** Jetpack Compose  
**Architecture:** MVVM with Clean Architecture  
**Development Purpose:** Educational and Practice Project  

---

## 1. Executive Summary

Animarch is an Android-based anime tracking and discovery application developed using modern Android development technologies. The application is designed to provide anime enthusiasts with a unified platform to discover anime series, browse recently released episodes, explore series information, and manage their personal anime-watching activities.

The application incorporates modern Android development practices, including Kotlin programming, Jetpack Compose for UI development, and the MVVM architectural pattern combined with Clean Architecture principles.

Animarch also integrates external services such as Supabase for backend functionality and Google's Gemini API for AI-assisted interactions. Additional features include community discussions, favourite management, and watch-history tracking.

The primary objective of this project is to demonstrate practical implementation of Android application development concepts, API integration, database management, and modern user interface design.

## 2. Project Overview

### 2.1 Introduction

Anime has become a globally popular entertainment medium, creating demand for applications that simplify anime discovery and personal watchlist management.

Animarch addresses this requirement by providing an interface through which users can explore anime-related content and manage their viewing interests.

The application focuses on combining content discovery, personal tracking, and AI-assisted interactions within a single Android application.

### 2.2 Project Objectives

The major objectives of Animarch are:

1. To develop an Android application using Kotlin and Jetpack Compose.
2. To implement an organized and maintainable application architecture.
3. To integrate external APIs for anime-related information.
4. To implement backend connectivity using Supabase.
5. To explore AI integration through Google's Gemini API.
6. To provide personal features such as favourites and watch history.
7. To create an engaging and responsive user interface.
8. To gain practical experience in full-stack Android application development.

## 3. Functional Features

### 3.1 Anime Discovery

The application allows users to explore anime-related content through integrated public APIs, including AniList and Anikoto.

Users can browse available anime information and discover recently released episodes.

### 3.2 Anime Information

The application provides access to anime series details, allowing users to explore information associated with different anime titles.

### 3.3 AI-Powered Chat Assistance

Animarch incorporates Google's Gemini API to provide AI-assisted interactions.

This functionality is intended to support anime-related conversations, recommendations, and user assistance.

### 3.4 Community Features

The application includes community-oriented functionality intended to support discussions and interactions among anime enthusiasts.

### 3.5 Favourite Management

Users can maintain a collection of their preferred anime titles using the favourite-management functionality.

### 3.6 Watch History

The application includes watch-history tracking to help users keep track of their anime-watching activities.

### 3.7 User Interface and Experience

Animarch uses an anime-inspired visual design with animated interface components. Jetpack Compose is used to build the application interface and support modern Android UI development practices.

## 4. Technology Stack

The project uses the following technologies and frameworks:

| Component | Technology | Purpose |
|---|---|---|
| Programming Language | Kotlin | Application development |
| UI Framework | Jetpack Compose | Modern Android UI |
| Architecture | MVVM | Separation of application responsibilities |
| Design Approach | Clean Architecture | Modular and maintainable code organization |
| Networking | Retrofit | API communication |
| Backend | Supabase | Backend services and authentication |
| Image Loading | Coil | Loading and displaying images |
| Local Database | Room | Local data persistence |
| AI Integration | Gemini API | AI-assisted interactions |

## 5. System Architecture

Animarch follows the MVVM architectural pattern alongside Clean Architecture principles.

The architecture is intended to separate application responsibilities into independent components, improving maintainability, testing, and code organization.

### 5.1 Presentation Layer

The presentation layer handles the application's user interface and user interactions.

Jetpack Compose is used to create the interface, while ViewModels manage UI-related state and application logic.

### 5.2 Domain Layer

The domain layer represents the application's core business logic.

It is intended to contain reusable application rules and use cases independent of Android-specific UI implementation.

### 5.3 Data Layer

The data layer handles data retrieval, storage, and communication with external services.

It uses Retrofit for network requests, Room for local persistence, and Supabase for backend connectivity.

### 5.4 External Service Integration

The application integrates external services to provide anime information and AI-assisted functionality.

These include:

- AniList API
- Anikoto API
- Supabase
- Google Gemini API

## 6. Data Management and Backend Integration

Animarch uses a combination of local and remote data-management technologies.

**Room Database:** Intended for storing application data locally on the device.

**Supabase:** Provides backend infrastructure, including database and authentication services.

**Retrofit:** Facilitates communication with external APIs and backend endpoints.

This combination demonstrates the implementation of local persistence and remote data access within an Android application.

## 7. Artificial Intelligence Integration

One of the notable components of Animarch is its integration with Google's Gemini API.

The AI functionality is intended to provide conversational assistance and anime-related recommendations.

This integration demonstrates how generative AI services can be incorporated into Android applications to enhance user interaction.

The availability and quality of AI functionality depend on API configuration, connectivity, and service availability.

## 8. Development Environment and Setup

### 8.1 Prerequisites

The following requirements are necessary to configure and run the project:

- Android Studio (latest recommended version).
- Android SDK and compatible build tools.
- Kotlin development environment.
- Google Gemini API key.
- Supabase project and configuration.
- Android emulator or compatible physical Android device.

### 8.2 Installation Procedure

1. Clone or download the Animarch project repository.
2. Open the project in Android Studio.
3. Create a `.env` file in the project root using `.env.example` as a reference.
4. Configure the required `GEMINI_API_KEY`.
5. Configure the required Supabase project credentials and environment settings.
6. Review the application's Gradle configuration.
7. Remove the debug signing configuration before preparing a release build.
8. Synchronize the Gradle project.
9. Build and execute the application using an emulator or physical Android device.

**Important:** API credentials must be handled securely. Sensitive keys should not be committed to public repositories or embedded in production application binaries.

## 9. Testing and Validation

The project provides an opportunity to explore and validate several important Android development components.

Recommended testing areas include:

- Application installation and startup.
- Navigation between application screens.
- Anime API connectivity and response handling.
- Anime information retrieval.
- Favourite-management functionality.
- Watch-history persistence.
- Supabase connectivity and authentication.
- Gemini API communication.
- Image loading and UI rendering.
- Application behaviour under network interruptions.

Formal test results, performance measurements, and production-readiness validation have not been provided as part of this project description. Therefore, no claim of complete testing or production readiness is made.

## 10. Security and Limitations

Animarch is an educational application and is not intended for commercial or production deployment in its current form.

Important considerations include:

1. API credentials must be protected against unauthorized access.
2. Backend authentication and database access policies require proper configuration.
3. Network communication should use secure HTTPS connections.
4. User-generated content requires appropriate validation and moderation.
5. External API availability and usage restrictions may affect application functionality.
6. Anime images, artwork, characters, and other intellectual property remain the property of their respective copyright holders.
7. Production deployment would require additional security reviews, testing, licensing verification, and operational improvements.

## 11. Future Scope

The application provides a foundation for exploring additional Android development capabilities.

Potential future improvements include:

- Enhanced anime recommendation algorithms.
- Improved anime search and filtering.
- Advanced watchlist and episode-progress management.
- Push notifications for new episode releases.
- Expanded community discussion functionality.
- Improved offline access through local caching.
- Enhanced accessibility and UI responsiveness.
- Better backend security and performance optimization.
- Comprehensive automated testing.
- Production-grade deployment and monitoring.

These improvements represent possible future development directions and are not claims of existing functionality.

## 12. Attribution and Acknowledgements

Animarch is inspired by the global anime community and the creativity of the artists, animators, writers, and production studios responsible for creating anime.

The project acknowledges the following services and technologies:

- **AniList:** Anime-related information and discovery.
- **Anikoto:** Anime-related content and information.
- **Google Gemini:** AI integration and conversational assistance.
- **Supabase:** Backend database and authentication infrastructure.
- **Jetpack Compose:** Modern Android user interface development.
- **Android and Kotlin communities:** Documentation, learning resources, and development support.

All anime-related intellectual property, artwork, characters, and original content remain the property of their respective owners.

## 13. Conclusion

Animarch demonstrates the practical application of modern Android development technologies through the development of an anime tracking and discovery application.

By combining Kotlin, Jetpack Compose, MVVM architecture, Retrofit, Room, Supabase, and Gemini API integration, the project explores several important concepts in contemporary mobile application development.

The application provides practical exposure to user interface development, API integration, local and remote data management, and AI-assisted application functionality.

Although Animarch is not intended for production or commercial deployment, it serves as a valuable educational project and demonstrates the development process involved in building a feature-oriented Android application.

---

## 14. Project Disclaimer and License

**Educational and Practice Use Only**

Animarch has been developed exclusively for educational, experimental, and personal learning purposes.

It is not intended for commercial distribution or production use in its current form. The application demonstrates Android development concepts and integrations but requires additional security assessments, licensing verification, testing, and modifications before any commercial application.

All third-party services, APIs, anime artwork, characters, and other intellectual property remain subject to their respective owners' terms, licenses, and policies.

---

**Project:** Animarch  
**Category:** Android Application Development  
**Purpose:** Educational and Practice Project  

*Developed for learning and experimentation with modern Android technologies.*
