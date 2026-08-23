# CloudVanna — Salesforce CRUD Web Application

A full-stack, production-ready web application for performing CRUD operations on standard Salesforce objects using Salesforce OAuth 2.0 and the Salesforce REST API.

## Architecture

```text
GitHub
   │
   ├── frontend/
   │       ↓
   │    Vercel
   │       ↓
   │    React + Vite (SPA)
   │
   └── backend/
           ↓
        Render
           ↓
      Spring Boot (Java 21)
           ↓
     Salesforce OAuth 2.0 / REST API
```

### Flow Breakdown
- **Frontend Layer**: React 19 + Vite SPA communicating via configured environment URL (`VITE_API_URL`).
- **Backend Layer**: Spring Boot 3.3.7 providing REST APIs, session management, dynamic PORT binding (`${PORT:8080}`), and sanitized proxy calls to Salesforce.
- **Authentication**: Salesforce OAuth 2.0 (External Client App / Connected App) with server-side token storage in HTTP sessions.
- **Salesforce REST API**: Centralized client performing SOQL queries, pagination with cursors, record creation, updates, and deletes.

---

## Features

- **Salesforce Standard Objects**: Account, Contact, Lead, Opportunity, Case.
- **Central Dropdown & Dynamic Fields**: Dynamic field schemas (5 to 10 curated fields per object) driven by backend metadata.
- **Full CRUD Support**: Create, Read (View Modal), Update (Edit Modal), Delete (with confirmation dialog).
- **Cursor-Based Pagination**: 20 records per page with infinite scroll loading using Salesforce `nextRecordsUrl`.
- **OAuth 2.0 Security**: Sensitive tokens never exposed to the frontend; secure HTTP-only session cookies.
- **Health Check Endpoint**: `/api/health` for uptime monitoring and deployment verification.

---

## Tech Stack

| Component | Technology | Version |
|---|---|---|
| **Runtime** | Java LTS | 21 |
| **Backend Framework** | Spring Boot | 3.3.7 |
| **Security & Session** | Spring Security / Session | 6.3.x |
| **HTTP Client** | Spring WebClient (WebFlux) | 6.1.x |
| **Build Tool** | Apache Maven | 3.9.x |
| **Frontend Framework** | React | 19.x |
| **Build Tool / Bundler** | Vite | 8.x |
| **Hosting (Frontend)** | Vercel | Production |
| **Hosting (Backend)** | Render | Production Web Service |

---

## Local Development Setup

### 1. Prerequisites
- Java 21 LTS installed and on PATH (`java -version`)
- Node.js 18+ and npm installed (`node -v`, `npm -v`)
- Salesforce Developer Org with an External Client App / Connected App

### 2. Configure Environment Variables
Copy `.env.example` to `.env` in the root folder (or backend/frontend folders):
```bash
cp .env.example .env
```

Set your local values in `.env`:
```env
SALESFORCE_CLIENT_ID=your_salesforce_client_id
SALESFORCE_CLIENT_SECRET=your_salesforce_client_secret
SALESFORCE_REDIRECT_URI=http://localhost:8080/api/auth/callback
SALESFORCE_LOGIN_URL=https://login.salesforce.com
FRONTEND_URL=http://localhost:5173
SERVER_PORT=8080
VITE_API_URL=http://localhost:8080
```

> ⚠️ **Security Notice**: Never commit your real `.env` file or Salesforce credentials to Git. `.env` is ignored by `.gitignore`.

### 3. Start Backend
```bash
cd backend
# Windows:
.\mvnw.cmd spring-boot:run

# Linux/macOS:
./mvnw spring-boot:run
```
The backend starts at `http://localhost:8080`.

### 4. Start Frontend
```bash
cd frontend
npm install
npm run dev
```
The frontend starts at `http://localhost:5173`.

---

## Production Deployment Guide

### A. Deploy Backend to Render

1. Create a new **Web Service** on [Render](https://render.com).
2. Connect your GitHub repository.
3. Configure the service settings:
   - **Name**: `cloudvanna-backend` (or your preferred name)
   - **Root Directory**: `backend`
   - **Runtime**: `Java` (Java 21) or `Docker`
   - **Build Command**:
     ```bash
     mvn clean package -DskipTests
     ```
     *(or `./mvnw clean package -DskipTests`)*
   - **Start Command**:
     ```bash
     java -jar target/salesforce-crud-0.0.1-SNAPSHOT.jar
     ```
   - **Health Check Path**: `/api/health`
4. Add the following **Environment Variables** in Render Dashboard:
   | Variable | Value / Description |
   |---|---|
   | `SALESFORCE_CLIENT_ID` | Your Salesforce Client ID |
   | `SALESFORCE_CLIENT_SECRET` | Your Salesforce Client Secret |
   | `SALESFORCE_REDIRECT_URI` | `https://YOUR-BACKEND.onrender.com/api/auth/callback` |
   | `SALESFORCE_LOGIN_URL` | `https://login.salesforce.com` |
   | `FRONTEND_URL` | `https://YOUR-FRONTEND.vercel.app` *(add local URL `http://localhost:5173` if testing against local frontend)* |
   | `SESSION_COOKIE_SAMESITE` | `none` *(required for cross-origin HTTPS cookies between Vercel and Render)* |
   | `SESSION_COOKIE_SECURE` | `true` *(required when SameSite=None)* |

---

### B. Deploy Frontend to Vercel

1. Import your GitHub repository into [Vercel](https://vercel.com).
2. Configure the project:
   - **Framework Preset**: `Vite`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
   - **Install Command**: `npm install`
3. Add the **Environment Variable** in Vercel Project Settings:
   | Variable | Value |
   |---|---|
   | `VITE_API_URL` | `https://YOUR-BACKEND.onrender.com` |
4. Deploy! Vercel will build the SPA and use `vercel.json` rewrites for client-side routing.

---

### C. Salesforce External Client App Configuration (Post-Deployment)

Once Render assigns your live backend URL (`https://YOUR-BACKEND.onrender.com`), update your Salesforce External Client App / Connected App:

1. In Salesforce Setup, navigate to **App Manager** (or **External Client App Manager**).
2. Locate your app and click **Edit** (or **Manage OAuth Settings**).
3. Under **Callback URL**, add the production callback URL on a new line (or separated by comma/newline):
   ```text
   http://localhost:8080/api/auth/callback
   https://YOUR-BACKEND.onrender.com/api/auth/callback
   ```
4. Ensure the required OAuth Scopes are enabled:
   - `Manage user data via APIs (api)` or `Full access (full)`
   - `Perform requests at any time (refresh_token, offline_access)`
5. Save the configuration (Salesforce changes take 2-10 minutes to propagate).

---

## API Reference

### Health Check
- `GET /api/health` — Returns `{"status":"UP","service":"CloudVanna Backend"}`

### Authentication
- `GET /api/auth/login` — Initiates Salesforce OAuth 2.0 flow
- `GET /api/auth/callback` — Handles OAuth callback and session creation
- `GET /api/auth/status` — Returns `{"authenticated": true/false}`
- `POST /api/auth/logout` — Invalidates session

### Metadata & Records
- `GET /api/objects` — Lists supported Salesforce standard objects
- `GET /api/objects/{objectName}/metadata` — Retrieves field metadata
- `GET /api/records/{objectName}` — Returns first 20 records (supports `?cursor=` for next 20)
- `GET /api/records/{objectName}/{id}` — Retrieves a single record by ID
- `POST /api/records/{objectName}` — Creates a new record
- `PATCH /api/records/{objectName}/{id}` — Updates record fields
- `DELETE /api/records/{objectName}/{id}` — Deletes record

---

## Verification & Testing

### Backend Test Suite (37 Unit Tests)
```bash
cd backend
.\mvnw.cmd test
# or: ./mvnw test
```

### Frontend Build Test
```bash
cd frontend
npm run build
```

---

## Security Best Practices Implemented
- Zero credentials or tokens exposed in frontend client bundle.
- Server-side token storage in secure HTTP sessions.
- Dynamic CORS whitelisting supporting production & local environments.
- Object & field whitelist validation to prevent injection or unauthorized field mutations.
- Protected error handling preventing stack traces or sensitive Salesforce internals leakage.
