# Salesforce CRUD Application

A full-stack web application for performing CRUD operations on Salesforce objects using OAuth 2.0 authentication and the Salesforce REST API.

## Features

- **OAuth 2.0 Authentication** — Secure Salesforce login with server-side token management
- **5 Salesforce Objects** — Account, Contact, Lead, Opportunity, Case
- **Full CRUD** — Create, Read, View, Update, Delete operations
- **Dynamic Forms** — Fields and forms are driven by object metadata configuration
- **Infinite Scroll** — Cursor-based pagination loading 20 records at a time using `nextRecordsUrl`
- **Input Validation** — Client-side and server-side field validation
- **Error Handling** — Global exception handling with clean API error responses
- **Security** — Backend object whitelist, field sanitization, no secrets in frontend

## Architecture

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│   React +    │────▶│  Spring Boot     │────▶│   Salesforce     │
│   Vite       │◀────│  REST API        │◀────│   REST API       │
│   (Frontend) │     │  (Backend)       │     │   (Data Store)   │
└──────────────┘     └──────────────────┘     └──────────────────┘
     :5173                :8080                  login.salesforce.com
```

### Backend Flow

```
Controller → Service → SalesforceRestClient → Salesforce REST API
```

### Frontend Flow

```
App → Pages → Components → API Service → Backend
```

## Tech Stack

| Layer        | Technology          | Version   |
|-------------|--------------------|-----------| 
| **Runtime**  | Java               | 21 LTS    |
| **Backend**  | Spring Boot        | 3.3.7     |
| **Security** | Spring Security    | 6.3.x     |
| **HTTP**     | WebClient (WebFlux)| 6.1.x     |
| **Build**    | Maven              | 3.9.9     |
| **Frontend** | React              | 18.x      |
| **Bundler**  | Vite               | 5.x / 6.x|
| **Runtime**  | Node.js            | 18+       |

## Prerequisites

- Java 21 LTS
- Node.js 18+
- Salesforce Developer Org
- Salesforce External Client App (Connected App)

---

## Salesforce Setup

### 1. Create a Developer Org

1. Go to [developer.salesforce.com/signup](https://developer.salesforce.com/signup)
2. Sign up for a free Developer Edition org
3. Verify your email and log in

### 2. Create an External Client App (Connected App)

1. In Salesforce Setup, search for **App Manager**
2. Click **New Connected App**
3. Fill in:
   - **Connected App Name**: `Salesforce CRUD App`
   - **API Name**: auto-generated
   - **Contact Email**: your email
4. Under **API (Enable OAuth Settings)**:
   - ✅ Enable OAuth Settings
   - **Callback URL**: `http://localhost:8080/api/auth/callback`
   - **Selected OAuth Scopes**:
     - `Full access (full)`
     - `Perform requests at any time (refresh_token, offline_access)`
   - ✅ Require Proof Key for Code Exchange (PKCE) — **uncheck** this
5. Click **Save** and wait 2-10 minutes for propagation
6. Under the connected app, click **Manage Consumer Details**
7. Copy the **Consumer Key** (Client ID) and **Consumer Secret** (Client Secret)

### 3. Set Trusted IP Ranges (Optional)

For development, you may need to relax IP restrictions:
1. Go to the connected app → **Manage**
2. Set **IP Relaxation** to "Relax IP restrictions"

---

## Environment Variables

Create a `.env` file in the project root (copy from `.env.example`):

```bash
cp .env.example .env
```

Fill in your values:

```env
SALESFORCE_CLIENT_ID=your_consumer_key
SALESFORCE_CLIENT_SECRET=your_consumer_secret
SALESFORCE_REDIRECT_URI=http://localhost:8080/api/auth/callback
SALESFORCE_LOGIN_URL=https://login.salesforce.com
FRONTEND_URL=http://localhost:5173
SERVER_PORT=8080
```

> ⚠️ **Never commit the `.env` file.** It is already in `.gitignore`.

---

## Running Locally

### Backend

```bash
cd backend

# Set JAVA_HOME if not already set
# Windows: set JAVA_HOME=C:\Program Files\Java\jdk-21
# Linux/Mac: export JAVA_HOME=/path/to/jdk-21

# Load env vars from root .env file
# Windows PowerShell:
Get-Content ..\.env | ForEach-Object { if ($_ -match '^([^#][^=]+)=(.*)$') { [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2]) } }

# Run
.\mvnw.cmd spring-boot:run
# or on Linux/Mac: ./mvnw spring-boot:run
```

The backend starts at `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts at `http://localhost:5173`.

### Testing the Flow

1. Open `http://localhost:5173` in your browser
2. Click **Login with Salesforce**
3. Log in with your Salesforce Developer Org credentials
4. Authorize the application
5. Select an object from the dropdown (e.g., Account)
6. View, Create, Edit, and Delete records

---

## API Documentation

### Authentication

| Method | Endpoint            | Description                    |
|--------|--------------------|---------------------------------|
| GET    | `/api/auth/login`   | Redirect to Salesforce login   |
| GET    | `/api/auth/callback` | OAuth callback handler        |
| POST   | `/api/auth/logout`  | Invalidate session             |
| GET    | `/api/auth/status`  | Check authentication status    |

### Objects

| Method | Endpoint                          | Description              |
|--------|-----------------------------------|--------------------------|
| GET    | `/api/objects`                     | List supported objects   |
| GET    | `/api/objects/{name}/metadata`     | Get field metadata       |

### Records

| Method | Endpoint                          | Description                          |
|--------|-----------------------------------|--------------------------------------|
| GET    | `/api/records/{object}`            | Query records (paginated, 20/page)  |
| POST   | `/api/records/{object}`            | Create a record                      |
| GET    | `/api/records/{object}/{id}`       | Get a single record                  |
| PATCH  | `/api/records/{object}/{id}`       | Update a record                      |
| DELETE | `/api/records/{object}/{id}`       | Delete a record                      |

**Pagination**: Pass `?cursor=<nextPageUrl>` for subsequent pages.

### Supported Objects

- `Account` — Id, Name, Phone, Website, Industry, Type, BillingCity, AnnualRevenue
- `Contact` — Id, FirstName, LastName, Email, Phone, Department, Title, MailingCity
- `Lead` — Id, FirstName, LastName, Company, Email, Phone, Status, LeadSource
- `Opportunity` — Id, Name, Amount, StageName, CloseDate, Probability, Type
- `Case` — Id, CaseNumber, Subject, Status, Priority, Origin, Description, Type

---

## Testing

### Backend Tests

```bash
cd backend
.\mvnw.cmd test
# or on Linux/Mac: ./mvnw test
```

Tests include:
- `SalesforceObjectConfigTest` — Object whitelist validation
- `RecordServiceTest` — CRUD logic, validation, sanitization
- `GlobalExceptionHandlerTest` — Error response mapping
- `OAuthServiceTest` — OAuth URL building and edge cases
- `ObjectControllerTest` — Controller endpoint tests

### Frontend Build Validation

```bash
cd frontend
npx vite build
```

### Manual Testing

1. Authenticate with a real Salesforce Developer Org
2. Test CRUD on all 5 objects
3. Test infinite scroll with > 20 records
4. Test error states (disconnect network, invalid data)
5. Test form validation (missing required fields)

---

## Deployment

### Production Build

**Backend:**
```bash
cd backend
.\mvnw.cmd clean package -DskipTests
java -jar target/salesforce-crud-0.0.1-SNAPSHOT.jar
```

**Frontend:**
```bash
cd frontend
npm run build
# Serve the dist/ folder with any static file server
```

### Docker

```bash
docker-compose up --build
```

---

## Troubleshooting

| Issue | Solution |
|-------|---------|
| `SALESFORCE_CLIENT_ID` not set | Create `.env` from `.env.example` and set the values |
| OAuth callback fails | Verify redirect URI matches exactly in Salesforce Connected App |
| CORS errors | Check `FRONTEND_URL` env var matches your frontend URL |
| 401 Unauthorized | Session may have expired. Re-login. |
| "Unsupported object" error | Only Account, Contact, Lead, Opportunity, Case are supported |
| Java not found | Set `JAVA_HOME` to your JDK 21 installation path |
| Maven not found | Use `.\mvnw.cmd` (Windows) or `./mvnw` (Linux/Mac) |

---

## Security Notes

- Salesforce Client Secret, Access Token, and Refresh Token are **never** sent to the frontend
- All tokens are stored server-side in the HTTP session
- Frontend communicates with the backend using session cookies (`credentials: 'include'`)
- Backend validates all object names against a hardcoded whitelist
- All field inputs are sanitized to only allow whitelisted editable fields
- CORS is configured to only allow the frontend origin
- No credentials or tokens are committed to Git
- Error responses never expose stack traces or internal details

---

## Project Structure

```
salesforce-crud-app/
├── backend/
│   ├── src/main/java/com/example/salesforcecrud/
│   │   ├── config/          # SalesforceConfig, SecurityConfig, ObjectConfig
│   │   ├── controller/      # AuthController, ObjectController, RecordController
│   │   ├── service/         # OAuthService, RecordService
│   │   ├── client/          # SalesforceRestClient
│   │   ├── dto/             # ErrorResponse, FieldMetadata, PagedRecordResponse
│   │   ├── exception/       # GlobalExceptionHandler, custom exceptions
│   │   ├── model/           # SalesforceToken
│   │   └── util/            # SessionUtil
│   ├── src/test/java/       # Unit tests
│   ├── pom.xml
│   └── mvnw.cmd
├── frontend/
│   ├── src/
│   │   ├── components/      # Reusable UI components
│   │   ├── pages/           # LoginPage, DashboardPage
│   │   ├── hooks/           # useAuth, useRecords
│   │   ├── services/        # API service layer
│   │   └── App.jsx
│   ├── package.json
│   └── vite.config.js
├── .env.example
├── .gitignore
├── docker-compose.yml
└── README.md
```

---

## License

This project is for educational and demonstration purposes.
