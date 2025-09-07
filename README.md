#FieldSync
A native Android retail execution platform that streamlines field account manager workflows during client store visits.

#Introduction
FieldSync solves the critical inefficiency problem facing field account managers who spend excessive time on administrative tasks instead of building client relationships. Current solutions force users to switch between 3-4 different applications per store visit, manually enter duplicate data, and lack real-time access to sales performance metrics.

#Why FieldSync?
•	Consolidates GPS check-in/check-out, merchandising compliance tracking, CRM functionality, and sales data visualization into a single, intuitive native Android experience
•	Targets mid-market companies (50-1000 employees) who need more functionality than basic tools but cannot justify enterprise-level complexity and pricing
•	Replaces fragmented manual processes with an integrated digital solution

#Features
MVP Features (4-Week Sprint) - Android Only
•	User Authentication: Secure email/password registration and login
•	Store Management: View assigned retail store locations with contact information
•	GPS Check-in/Check-out: Location-verified visit tracking with automatic duration calculation
•	Photo Capture: Take and attach photos during store visits with Android camera integration
•	Visit Notes: Add text observations and notes for each visit
•	Visit History: View past visits with timestamps and duration in clean Material Design 3 UI
Planned Features (Future Releases)
•	iOS Version: Cross-platform Flutter or native iOS app
•	Offline Support: Core functions work without internet connectivity with automatic sync
•	Dynamic Surveys: Deploy location-specific questionnaires during product launches
•	Merchandising Compliance: Track adherence to brand standards with reference images
•	Sales Data Integration: 90-day sales visualization for strategic client conversations
•	Advanced CRM: Manage employee contacts, track personnel changes, set follow-up reminders
•	Route Optimization: Plan efficient store visit sequences
•	Advanced Analytics: Territory-wide performance statistics and reporting

#Technologies
Native Android Application
•	Language: Kotlin
•	IDE: Jetbrains
•	UI Framework: Material Design 3 with Jetpack Compose
•	Architecture: MVVM with Android Architecture Components
•	Database: Room (SQLite) for local data storage
•	Network: Retrofit + OkHttp for API communication
•	Location Services: Google Play Services Location API
•	Camera: CameraX library for photo capture
•	Image Loading: Glide for efficient image handling
•	Dependency Injection: Hilt (Dagger)
Backend API
•	Framework: FastAPI 0.104+ (Python 3.9+)
•	Database: PostgreSQL 15.0+ with SQLAlchemy 2.0+ ORM
•	Authentication: JWT tokens with passlib/bcrypt password hashing
•	Server: Uvicorn ASGI server
•	Migrations: Alembic for database schema management
External APIs
•	Geocoding: OpenStreetMap Nominatim API (free geocoding/reverse geocoding)
•	Weather Data: OpenWeatherMap API (5-day forecasts, 1,000 calls/day free tier)
•	Push Notifications: Firebase Cloud Messaging
Development & Deployment
•	Version Control: Git with GitFlow branching strategy
•	CI/CD: GitHub Actions for Android builds and backend deployment
•	Backend Containerization: Docker for backend deployment
•	Cloud Hosting: AWS/DigitalOcean (production deployment)

#Installation
Prerequisites
•	Android Device: Android 7.0+ (API level 24)
•	Internet connection for initial setup and data sync
•	GPS-enabled device for location tracking
•	Camera permissions for photo capture
End User Installation
Android App (Coming Soon)
•	Download from Google Play Store
•	Create account with email and password
•	Allow location and camera permissions
•	Start managing your store visits with native Android performance!
Web Dashboard (Future Release)
•	Access at https://fieldsync.app
•	Use same email/password credentials as Android app

#Development Setup
Prerequisites for Developers
•	IntelliJ IDEA 2024.2.3+
•	Kotlin 1.9+
•	Android SDK API 24+ (minimum), API 34+ (target)
•	Python 3.9+ (for backend)
•	PostgreSQL 15.0+
•	Git for version control

#Quick Start
1.	Clone the repository 
bash
git clone https://github.com/asandoval557/FieldSync_MVP.git
cd FieldSync_MVP
2.	Android App Setup 
# Open IntelliJ IDEA
# File → Open → select the android-app directory
# Wait for Gradle sync to complete

# Configure local.properties with your SDK path (Windows example)
echo "sdk.dir=C:/Users/<your-username>/AppData/Local/Android/Sdk" > android-app/local.properties

# Build the app
# Windows:
android-app\gradlew.bat clean assembleDebug
# macOS/Linux:
./android-app/gradlew clean assembleDebug
3.	Backend Setup 
bash
cd backend

# Create virtual environment
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Set up environment variables
cp .env.example .env
# Edit .env with your PostgreSQL credentials

# Run database migrations
alembic upgrade head

# Start the API server
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
4.	Database Setup 
sql
-- Create PostgreSQL database
CREATE DATABASE fieldsync_db;
CREATE USER fieldsync_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE fieldsync_db TO fieldsync_user;
Android Studio Project Structure
android-app/
 app/
src/main/
java/com/fieldsync/
MainActivity.kt
ui/
auth/          # Login/Register screens
stores/        # Store list and details
visits/        # Check-in/out and visit history
components/    # Reusable UI components
data/
local/         # Room database
remote/        # API services
repository/    # Data repository pattern
di/                # Dependency injection
utils/             # Utility classes
res/                   # Android resources
AndroidManifest.xml
build.gradle               # App-level dependencies
gradle/                        # Gradle wrapper
build.gradle                   # Project-level configuration

Development Workflow
1.	Create Feature Branch 
bash
git checkout development
git checkout -b feature/android-auth-screen
2.	Android Development 
o	Use IntelliJ IDEA’s built-in Git tools
o	Test on Android emulator or physical device
o	Follow Material Design 3 guidelines
o	Use Jetpack Compose for modern UI development
3.	Code Review Process 
o	All features must be tested on Android devices/emulators
o	Weekly team code reviews before mentor presentations
o	Minimum 1 approval required for pull requests to development

API Documentation
•	Local development: http://localhost:8000/docs
•	Interactive API testing available through FastAPI auto-generated docs

License
This project is licensed under the MIT License 

Development Team (Capstone Project - Fall 2025)
•	Adam Wuelfing - Project Lead & Backend Development - addwuelf462@gmail.com
•	Adrian Sandoval - Android Development Lead - asandoval2107@gmail.com
•	Michael Morris - Android Development - monkeygator9@gmail.com
•	Abdiel K. Arocho Medina - Backend Development - karocho@gmail.com

Maintained By: The FieldSync Development Team

Project Status
Current Phase: 🚧 Active Development (Alpha v0.1.0 - Android MVP)
Focused 4-Week Android Sprint
•	Week 1: Project setup, git repository, backend API structure, IntelliJ project initialization
•	Week 2: User authentication (backend + Android UI), database models, Material Design 3 setup
•	Week 3: GPS check-in/out functionality, camera integration, store management screens
•	Week 4: Visit history, testing, bug fixes, polished demo preparation
MVP Success Criteria
•	Functional Android app demonstrating core workflow
•	Working backend API with user management and visit tracking
•	Clean, modern UI following Material Design 3 principles
•	GPS integration for location-verified check-ins
•	Photo capture with proper Android permissions
•	Stable performance on Android 7.0+ devices

Roadmap
Version 1.0 - Android MVP (4 weeks) - Current Sprint
Goal: Native Android app with core field manager functionality
•	Project structure and development environment setup
•	Backend user authentication API
•	Android authentication screens (Login/Register)
•	Store list management with Material Design 3 UI
•	GPS-verified check-in/check-out functionality
•	Camera integration for visit photos
•	Visit notes and history tracking
•	Polish and demo preparation
Version 1.1 - Enhanced Android (Month 2)
Goal: Production-ready Android experience
•	Offline support with Room database sync
•	Advanced UI animations and interactions
•	Push notifications via Firebase
•	Enhanced photo management and gallery
•	Location-based reminders and alerts
•	Export visit summaries
Version 1.2 - Cross-Platform (Month 4)
Goal: Expand platform reach
•	iOS native app or Flutter cross-platform migration
•	Sales data visualization integration
•	Dynamic survey deployment system
•	Territory performance analytics
•	Web dashboard for managers
Version 2.0 - Enterprise Features (Month 6)
Goal: Full retail execution platform
•	Multi-user team management
•	Advanced CRM functionality
•	AI-powered compliance checking
•	Route optimization algorithms
•	Custom branding and white-label options
Technical Milestones
•	Week 2 Demo: User registration/login working end-to-end
•	Week 3 Demo: Complete store visit workflow (check-in → photo → check-out)
•	Week 4 Final: Polished Android app ready for user testing
•	Success Metrics: <3s app launch time, 95% GPS accuracy, crash-free operation
