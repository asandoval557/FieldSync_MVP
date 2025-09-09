# FieldSync

A native Android retail execution platform that streamlines field account manager workflows during client store visits.

---

## Repository

**Repo URL:** https://github.com/asandoval557/FieldSync_MVP

---

## Introduction

FieldSync solves the inefficiency facing field account managers who must track visits, verify in-store execution, capture photos, and record outcomes while moving between locations. Current tools are fragmented, slow, and produce incomplete data.

### Why FieldSync?
- One app for GPS check-in/out, merchandising/compliance, photo capture, visit notes, and visit history.
- Designed for teams that need more than spreadsheets without enterprise-level complexity/cost.
- Replaces fragmented manual processes with an integrated, mobile-first workflow.

---

## MVP Scope (Android-only)

- **User Authentication** — email/password registration & login
- **Store Management** — view assigned stores & key info
- **GPS Check-In/Out** — location-verified visits with duration
- **Photo Capture** — attach photos as visit proof
- **Visit Notes** — text notes per visit
- **Visit History** — past visits with timestamps & duration

_Planned next: offline mode + sync, admin console, reporting, iOS client._

---

## Technology Stack

### Android Client
- **IDE:** **IntelliJ IDEA** (Android Studio not required)
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Min/Target SDK:** 24 / 34
- **Tooling:** AGP **8.5.x**, Kotlin **1.9.25**, Compose Compiler **1.5.15**, **JDK 17**
- **Libraries:** Activity Compose, Lifecycle KTX, Retrofit + OkHttp, Hilt (DI), CameraX, Google Play Services Location, Coil/Glide

### Backend API
- **Framework:** FastAPI (Python 3.11+)
- **Auth:** JWT (python-jose) + passlib/bcrypt
- **DB (planned):** Postgres + SQLAlchemy 2.x + Alembic
- **Server:** Uvicorn
- **HTTP Client:** httpx

### CI/CD
- **GitHub Actions:** Android (assembleDebug) + Backend (deps + lint/compile)

---

## Repository Structure
FieldSync_MVP/
├─ android-app/ # Android project (Gradle Kotlin DSL)

│ ├─ app/

│ ├─ build.gradle.kts

│ └─ settings.gradle.kts

├─ backend/ # FastAPI app

│ ├─ app/

│ │ ├─ core/ # config, security (JWT, hashing)

│ │ ├─ models/ # (planned DB models)

│ │ ├─ routes/ # /auth endpoints

│ │ ├─ schemas/ # Pydantic models

│ │ └─ main.py

│ └─ requirements.txt

├─ .github/workflows/ # CI pipelines

│ ├─ android-ci.yml

│ └─ backend-ci.yml

├─ docs/

└─ tests/

---

## Branching Strategy
main ← stable/demo-ready (protected)
└─ development ← sprint integration (protected)
└─ feature/<task> ← short-lived branches per task/PR


**Flow**
1. `git checkout development && git pull`
2. `git checkout -b feature/<task>`
3. Commit small, focused changes
4. Open PR → **base: development**
5. Merge when CI is green → delete the feature branch
6. Promote `development → main` for demos/releases

---

## Developer Setup (IntelliJ-only)

### Prerequisites
- **IntelliJ IDEA** (Community or Ultimate)
- **JDK 17**
- **Android SDK** with:
  - **platforms;android-34**
  - **build-tools;34.0.0**
  - **platform-tools**
- **Python 3.11+**
- **Git**

> If you already have Android Studio installed, you can still use IntelliJ; just point to the same SDK folder (usually `C:/Users/<you>/AppData/Local/Android/Sdk` on Windows).

---

## Quick Start

### Clone the repository
```bash
git clone https://github.com/asandoval557/FieldSync_MVP.git
cd FieldSync_MVP


**Flow**
1. `git checkout development && git pull`
2. `git checkout -b feature/<task>`
3. Commit small, focused changes
4. Open PR → **base: development**
5. Merge when CI is green → delete the feature branch
6. Promote `development → main` for demos/releases

---

## Developer Setup (IntelliJ-only)

### Prerequisites
- **IntelliJ IDEA** (Community or Ultimate)
- **JDK 17**
- **Android SDK** with:
  - **platforms;android-34**
  - **build-tools;34.0.0**
  - **platform-tools**
- **Python 3.11+**
- **Git**

---

## Quick Start

### Clone the repository
```bash
git clone https://github.com/asandoval557/FieldSync_MVP.git
cd FieldSync_MVP

Android App (in IntelliJ)

Open android-app/ in IntelliJ (File → Open… → select android-app).

Gradle JDK: File → Settings → Build, Execution, Deployment → Gradle → Gradle JDK = 17.

SDK path: create/edit android-app/local.properties with your SDK location:
sdk.dir=C:/Users/<your-username>/AppData/Local/Android/Sdk

Build:

Windows: android-app\gradlew.bat clean assembleDebug

macOS/Linux: ./android-app/gradlew clean assembleDebug

If you see Kotlin/Compose mismatch errors, ensure Kotlin 1.9.25 and Compose Compiler 1.5.15 in app/build.gradle.kts, and jvmTarget=17.

cd backend
python -m venv .venv
# Windows PowerShell:
. .venv/Scripts/Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload
# http://127.0.0.1:8000/health

Auth (MVP, in-memory)

POST /auth/register → { email, password, full_name? } → user

POST /auth/login → { email, password } → { access_token, token_type }

Contribution Workflow

Create or pick an Issue (MVP task).

Branch from development: feature/<area>-<short-name>

Commit style:

feat(auth): add LoginScreen

fix(visits): correct duration calc

chore(ci): install Android SDK on runner

Open a PR → base development

CI must pass; review & merge; delete branch

CI Notes

Android CI installs the SDK on the runner and runs ./gradlew clean assembleDebug.

Backend CI installs Python deps and verifies the app compiles.

Roadmap (MVP – 4 Weeks)

Wk 1: Project setup, repo + CI, backend auth skeleton, IntelliJ project initialization

Wk 2: Auth (Android UI + backend wiring), start GPS + Camera

Wk 3: Store management, persistence, polish

Wk 4: Visit history, testing, demo readiness

License

MIT
