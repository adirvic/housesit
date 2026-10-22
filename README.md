# 💑 Housesit / CoupleFinance - Couple Expense Sharing Platform

A modern private financial platform designed specifically for couples to track, share living costs, manage household budgets, and maintain transparent "who-owes-who" debt settlements.

---

## 🌟 Key Features

1. **Couple Household Model & Invite Links**:
   - Create a shared household (e.g., "Our Cozy Home").
   - Share a 6-8 character invite code (e.g. `LOVE2026`) with your partner to link accounts.

2. **Smart Split Calculator & Flexible Expense Sharing**:
   - **50% / 50% Equal Split** (auto-balances common living expenses like rent, groceries, dining).
   - **Custom Percentage Split** (e.g. 60%/40% according to income ratios).
   - **Paid 100% for Partner** (e.g. buying personal items or medicines on behalf of your spouse).
   - **100% Personal Expense** (tracked under household visibility with 0% partner debt).
   - **Exact Custom Split Amounts**.

3. **Real-time Settlement Balance ("Who Owes Who")**:
   - Mathematical settlement engine that computes net balance between both partners:
     - Out-of-pocket spending per partner.
     - Expected share per partner.
     - Net balance statement (e.g. *"Ana owes Junior $30.00"*).
     - Direct debt settlement transfers (Pix, Bank Transfer, Venmo, Cash) to reset or reduce debt back to *"All settled up!"*.

4. **Monthly Categorized Analytics**:
   - Spending breakdown across 12 standard categories: Groceries 🛒, Housing 🏠, Utilities 💡, Dining 🍽️, Transport 🚗, Health 💊, Entertainment 🍿, Shopping 🛍️, Travel ✈️, Pets 🐾, Education 📚, Other 📦.

5. **Authentication & Mobile Integration**:
   - **Google Sign-In**: Validates Google OAuth2 ID tokens via Google TokenInfo API.
   - **JWT Stateless Security**: Issues Bearer JWT tokens for mobile apps and web clients.
   - **Dev / Demo Switcher**: Instant switch between Husband and Wife profiles to test couple transactions without needing external Google accounts set up first.

6. **Frontends**:
   - **Responsive Mobile Web PWA**: Served directly by the backend at `http://localhost:8080/`.
   - **React Native / Expo Starter**: Ready-to-run mobile app in `mobile/`.

---

## 🏗️ Architecture & Tech Stack

- **Backend**: Spring Boot 4 / Java 17+, Spring Security 6 (JWT + Google OAuth2), Spring Data JPA, Hibernate, H2 Database (or PostgreSQL).
- **Frontend (Web)**: Responsive Mobile-First HTML5 / Tailwind CSS / Vanilla JS.
- **Frontend (Mobile)**: React Native / Expo with Google Sign-In support.
- **Database**: H2 In-Memory, with the web console at `/h2-console` in the `dev` profile only.

---

## 🚀 Running the Project

### 1. Start the Backend Server
Run locally with the `dev` profile, which turns on the development conveniences (demo data, dev login, mock Google tokens, H2 console) and uses a throwaway JWT key:
```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
# Or on Windows (PowerShell):
$env:SPRING_PROFILES_ACTIVE="dev"; .\mvnw.cmd spring-boot:run
```

Once running:
- **Mobile Web App UI**: Open [http://localhost:8080/](http://localhost:8080/) in any mobile browser or desktop.
- **H2 Database Console** (dev only, localhost only): Open [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:housesitdb`, user: `sa`, password: empty).

### Configuration & Security
The default configuration is secure by default: outside the `dev` profile the app refuses to start without a real JWT key, and every development shortcut is off.

| Environment variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | Yes (outside `dev`) | HMAC key for signing JWTs, at least 32 bytes. Generate with `openssl rand -base64 48`. |
| `GOOGLE_CLIENT_ID` | For Google Sign-In | OAuth client ID. Google ID tokens issued for any other audience are rejected. |
| `CORS_ALLOWED_ORIGINS` | No | Comma-separated origins allowed to call the API from a browser (e.g. `https://app.example.com`). Empty means same-origin only. Credentials are never allowed, since the API uses Bearer tokens. |
| `JWT_EXPIRATION_MS` | No | Token lifetime, default 24h. |

What only exists in the `dev` profile:
- `POST /api/auth/dev-login` (passwordless login as any email); outside `dev` the endpoint is not mapped and returns 404.
- `mock-google:<email>:<name>` tokens on `/api/auth/google`.
- The H2 web console, restricted to localhost.
- The pre-seeded demo couple and expenses.

### 2. Pre-Seeded Demo Data
In the `dev` profile, the application starts with a pre-configured couple:
- **Husband**: `Junior` (`junior@example.com`)
- **Wife**: `Ana` (`ana@example.com`)
- **Household**: `Our Cozy Home` (Invite Code: `LOVE2026`)
- Includes sample expenses (groceries, high-speed fiber internet, electricity, date night dinner, pharmacy vitamins).

---

## 📡 REST API Reference

### 🔐 Authentication (`/api/auth`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/google` | Exchange Google ID Token (`{ "idToken": "..." }`) for JWT |
| `POST` | `/api/auth/dev-login` | Instant dev login, `dev` profile only (`{ "email": "junior@example.com", "name": "Junior" }`) |
| `GET` | `/api/auth/me` | Fetch current user and household details |

### 🏠 Household (`/api/household`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/household` | Get current user's household and partner info |
| `POST` | `/api/household` | Create a new household (`{ "name": "...", "currency": "USD" }`) |
| `POST` | `/api/household/join` | Join household using invite code (`{ "inviteCode": "LOVE2026" }`) |
| `POST` | `/api/household/regenerate-invite` | Generate a new invite code |

### 💰 Expenses (`/api/expenses`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/expenses` | List expenses (optional query params: `startDate`, `endDate`, `category`, `payerId`) |
| `POST` | `/api/expenses` | Record a new expense with split strategy |
| `GET` | `/api/expenses/{id}` | Get single expense details |
| `PUT` | `/api/expenses/{id}` | Update existing expense |
| `DELETE` | `/api/expenses/{id}` | Delete expense |

### 📊 Dashboard & Settlement (`/api/dashboard`, `/api/settlements`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard/summary` | Get monthly spend, category breakdown, partner stats & net debt balance |
| `GET` | `/api/settlements` | List balance settlement payments |
| `POST` | `/api/settlements` | Record a settlement payment (`{ "toUserId": 2, "amount": 30.00, "note": "Pix" }`) |
| `GET` | `/api/categories` | List all 12 expense categories with emoji metadata |

---

## 📱 Google OAuth Setup for Production Mobile Apps

1. Go to [Google Cloud Console Credentials](https://console.cloud.google.com/apis/credentials).
2. Create an **OAuth 2.0 Client ID** (Web application).
3. Set your client ID through the `GOOGLE_CLIENT_ID` environment variable:
   ```bash
   export GOOGLE_CLIENT_ID=YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com
   ```
4. On your mobile app (React Native / Flutter / Android / iOS), sign in the user via Google Sign-in SDK, take the `idToken` from the result, and POST it to `/api/auth/google`.
