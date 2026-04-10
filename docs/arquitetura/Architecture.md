# Expense Tracker for Trip Expenses — System Architecture Document

> **Tech Stack:** Java (Spring Boot) · Electron · React · Tailwind CSS · JSON File Storage  
> **Type:** Desktop Application (College Project)  
> **Date:** 2026-04-10

---

## Table of Contents

1. [High-Level Architecture](#1-high-level-architecture)
2. [Project Directory Structure](#2-project-directory-structure)
3. [Data Models (JSON Schemas)](#3-data-models-json-schemas)
4. [Entity Relationship Diagram](#4-entity-relationship-diagram)
5. [Backend Architecture (Java — Spring Boot)](#5-backend-architecture-java--spring-boot)
   - 5.1 [Generic JSON Repository](#51-generic-json-repository-core-pattern)
   - 5.2 [REST API Endpoints](#52-rest-api-endpoints)
   - 5.3 [Balance Calculation Algorithm](#53-balance-calculation-algorithm-debtsimplifierjava)
   - 5.4 [Standard API Response Wrapper](#54-standard-api-response-wrapper)
6. [Frontend Architecture (Electron + React + Tailwind)](#6-frontend-architecture-electron--react--tailwind)
   - 6.1 [Electron Main Process Flow](#61-electron-main-process-flow)
   - 6.2 [React App Router Structure](#62-react-app-router-structure)
   - 6.3 [State Management Strategy](#63-state-management-strategy)
   - 6.4 [API Service Layer Example](#64-api-service-layer-example)
7. [Key Design Decisions & Rationale](#7-key-design-decisions--rationale)
8. [Split Method Logic](#8-split-method-logic)
9. [Build & Run Pipeline](#9-build--run-pipeline)
10. [Screen Flow Map](#10-screen-flow-map)

---

## 1. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    ELECTRON SHELL                           │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              RENDERER PROCESS                         │  │
│  │         React + Tailwind CSS (Frontend)               │  │
│  │                                                       │  │
│  │  Pages → Components → Hooks → Services (API Client)   │  │
│  └──────────────────────┬────────────────────────────────┘  │
│                         │ HTTP (localhost)                   │
│  ┌──────────────────────┴────────────────────────────────┐  │
│  │              MAIN PROCESS                             │  │
│  │  - Spawns Java backend as child process               │  │
│  │  - Manages app lifecycle                              │  │
│  │  - IPC bridge (optional)                              │  │
│  └──────────────────────┬────────────────────────────────┘  │
└─────────────────────────┼───────────────────────────────────┘
                          │ Child Process (java -jar)
┌─────────────────────────┴───────────────────────────────────┐
│                 JAVA BACKEND (Spring Boot)                   │
│                                                             │
│  Controllers → Services → Repositories → JSON File Storage  │
│                                                             │
│  /data/                                                     │
│    ├── users.json                                           │
│    ├── trips.json                                           │
│    ├── expenses.json                                        │
│    ├── categories.json                                      │
│    └── settlements.json                                     │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Project Directory Structure

```
expense-tracker/
│
├── backend/                          # Java (Spring Boot)
│   ├── pom.xml                       # Maven build config
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/expensetracker/
│   │   │   │   ├── ExpenseTrackerApplication.java
│   │   │   │   │
│   │   │   │   ├── config/
│   │   │   │   │   ├── CorsConfig.java
│   │   │   │   │   ├── JsonStorageConfig.java
│   │   │   │   │   └── AppInitializer.java
│   │   │   │   │
│   │   │   │   ├── controller/
│   │   │   │   │   ├── UserController.java
│   │   │   │   │   ├── TripController.java
│   │   │   │   │   ├── ExpenseController.java
│   │   │   │   │   ├── CategoryController.java
│   │   │   │   │   └── SettlementController.java
│   │   │   │   │
│   │   │   │   ├── service/
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   ├── TripService.java
│   │   │   │   │   ├── ExpenseService.java
│   │   │   │   │   ├── CategoryService.java
│   │   │   │   │   └── SettlementService.java
│   │   │   │   │
│   │   │   │   ├── repository/
│   │   │   │   │   ├── JsonRepository.java          # Generic base
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   ├── TripRepository.java
│   │   │   │   │   ├── ExpenseRepository.java
│   │   │   │   │   ├── CategoryRepository.java
│   │   │   │   │   └── SettlementRepository.java
│   │   │   │   │
│   │   │   │   ├── model/
│   │   │   │   │   ├── BaseEntity.java
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Trip.java
│   │   │   │   │   ├── Expense.java
│   │   │   │   │   ├── Category.java
│   │   │   │   │   ├── Settlement.java
│   │   │   │   │   └── enums/
│   │   │   │   │       ├── SplitMethod.java
│   │   │   │   │       ├── Currency.java
│   │   │   │   │       └── TripStatus.java
│   │   │   │   │
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   │   ├── CreateTripRequest.java
│   │   │   │   │   │   ├── CreateExpenseRequest.java
│   │   │   │   │   │   ├── CreateUserRequest.java
│   │   │   │   │   │   └── SettleDebtRequest.java
│   │   │   │   │   └── response/
│   │   │   │   │       ├── TripSummaryResponse.java
│   │   │   │   │       ├── BalanceResponse.java
│   │   │   │   │       ├── ExpenseBreakdownResponse.java
│   │   │   │   │       └── ApiResponse.java
│   │   │   │   │
│   │   │   │   ├── exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   │   └── ValidationException.java
│   │   │   │   │
│   │   │   │   └── util/
│   │   │   │       ├── IdGenerator.java
│   │   │   │       ├── DebtSimplifier.java
│   │   │   │       └── CurrencyConverter.java
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── data/                 # JSON "database" files
│   │   │           ├── users.json
│   │   │           ├── trips.json
│   │   │           ├── expenses.json
│   │   │           ├── categories.json
│   │   │           └── settlements.json
│   │   │
│   │   └── test/java/com/expensetracker/
│   │       ├── service/
│   │       │   ├── TripServiceTest.java
│   │       │   ├── ExpenseServiceTest.java
│   │       │   └── SettlementServiceTest.java
│   │       └── repository/
│   │           └── JsonRepositoryTest.java
│   │
│   └── target/
│       └── expense-tracker-backend.jar   # Built artifact
│
├── frontend/                             # Electron + React + Tailwind
│   ├── package.json
│   ├── tailwind.config.js
│   ├── postcss.config.js
│   ├── vite.config.js
│   ├── electron-builder.json
│   │
│   ├── electron/                         # Electron Main Process
│   │   ├── main.js                       # App entry, spawns Java
│   │   ├── preload.js                    # Context bridge
│   │   └── javaProcess.js               # Java child process manager
│   │
│   ├── src/                              # React App (Renderer)
│   │   ├── main.jsx                      # React entry point
│   │   ├── App.jsx                       # Root component + Router
│   │   │
│   │   ├── pages/
│   │   │   ├── DashboardPage.jsx
│   │   │   ├── TripsPage.jsx
│   │   │   ├── TripDetailPage.jsx
│   │   │   ├── AddExpensePage.jsx
│   │   │   ├── SettlementsPage.jsx
│   │   │   ├── CategoriesPage.jsx
│   │   │   └── ProfilePage.jsx
│   │   │
│   │   ├── components/
│   │   │   ├── layout/
│   │   │   │   ├── Sidebar.jsx
│   │   │   │   ├── TopBar.jsx
│   │   │   │   └── MainLayout.jsx
│   │   │   ├── trips/
│   │   │   │   ├── TripCard.jsx
│   │   │   │   ├── TripForm.jsx
│   │   │   │   └── TripMemberList.jsx
│   │   │   ├── expenses/
│   │   │   │   ├── ExpenseList.jsx
│   │   │   │   ├── ExpenseItem.jsx
│   │   │   │   ├── ExpenseForm.jsx
│   │   │   │   └── SplitSelector.jsx
│   │   │   ├── settlements/
│   │   │   │   ├── BalanceBoard.jsx
│   │   │   │   ├── DebtCard.jsx
│   │   │   │   └── SettleUpModal.jsx
│   │   │   ├── charts/
│   │   │   │   ├── CategoryPieChart.jsx
│   │   │   │   ├── DailySpendingChart.jsx
│   │   │   │   └── MemberSpendingBar.jsx
│   │   │   └── shared/
│   │   │       ├── Button.jsx
│   │   │       ├── Modal.jsx
│   │   │       ├── Input.jsx
│   │   │       ├── Select.jsx
│   │   │       ├── EmptyState.jsx
│   │   │       ├── LoadingSpinner.jsx
│   │   │       └── ConfirmDialog.jsx
│   │   │
│   │   ├── hooks/
│   │   │   ├── useTrips.js
│   │   │   ├── useExpenses.js
│   │   │   ├── useSettlements.js
│   │   │   └── useApi.js
│   │   │
│   │   ├── services/
│   │   │   ├── api.js                    # Axios instance
│   │   │   ├── tripService.js
│   │   │   ├── expenseService.js
│   │   │   ├── userService.js
│   │   │   └── settlementService.js
│   │   │
│   │   ├── context/
│   │   │   ├── AppContext.jsx
│   │   │   └── UserContext.jsx
│   │   │
│   │   ├── utils/
│   │   │   ├── formatCurrency.js
│   │   │   ├── formatDate.js
│   │   │   └── constants.js
│   │   │
│   │   └── assets/
│   │       ├── icons/
│   │       └── images/
│   │
│   ├── public/
│   │   └── index.html
│   │
│   └── dist/                             # Build output
│
├── scripts/
│   ├── build-all.sh                      # Builds backend + frontend
│   └── dev.sh                            # Runs both in dev mode
│
├── .gitignore
└── README.md
```

---

## 3. Data Models (JSON Schemas)

### 3.1 `users.json`

```json
[
  {
    "id": "usr_a1b2c3d4",
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "avatarColor": "#6366F1",
    "createdAt": "2026-04-10T08:00:00Z"
  }
]
```

### 3.2 `trips.json`

```json
[
  {
    "id": "trp_x7y8z9w0",
    "name": "Barcelona Summer 2026",
    "description": "Group vacation in Spain",
    "destination": "Barcelona, Spain",
    "currency": "EUR",
    "status": "ACTIVE",
    "memberIds": ["usr_a1b2c3d4", "usr_e5f6g7h8", "usr_i9j0k1l2"],
    "startDate": "2026-06-15",
    "endDate": "2026-06-22",
    "createdBy": "usr_a1b2c3d4",
    "createdAt": "2026-04-10T08:00:00Z",
    "updatedAt": "2026-04-10T08:00:00Z"
  }
]
```

### 3.3 `expenses.json`

```json
[
  {
    "id": "exp_m3n4o5p6",
    "tripId": "trp_x7y8z9w0",
    "description": "Dinner at La Boqueria",
    "amount": 120.50,
    "currency": "EUR",
    "categoryId": "cat_food01",
    "paidByUserId": "usr_a1b2c3d4",
    "splitMethod": "EQUAL",
    "splits": [
      { "userId": "usr_a1b2c3d4", "amount": 40.17 },
      { "userId": "usr_e5f6g7h8", "amount": 40.17 },
      { "userId": "usr_i9j0k1l2", "amount": 40.16 }
    ],
    "date": "2026-06-16",
    "notes": "Including wine and dessert",
    "createdAt": "2026-06-16T21:30:00Z",
    "updatedAt": "2026-06-16T21:30:00Z"
  }
]
```

### 3.4 `categories.json`

```json
[
  { "id": "cat_food01", "name": "Food & Drinks", "icon": "utensils", "color": "#EF4444" },
  { "id": "cat_tran01", "name": "Transport", "icon": "car", "color": "#3B82F6" },
  { "id": "cat_acco01", "name": "Accommodation", "icon": "bed", "color": "#8B5CF6" },
  { "id": "cat_acti01", "name": "Activities", "icon": "ticket", "color": "#F59E0B" },
  { "id": "cat_shop01", "name": "Shopping", "icon": "shopping-bag", "color": "#10B981" },
  { "id": "cat_othe01", "name": "Other", "icon": "ellipsis", "color": "#6B7280" }
]
```

### 3.5 `settlements.json`

```json
[
  {
    "id": "stl_q7r8s9t0",
    "tripId": "trp_x7y8z9w0",
    "fromUserId": "usr_e5f6g7h8",
    "toUserId": "usr_a1b2c3d4",
    "amount": 85.30,
    "currency": "EUR",
    "note": "Venmo transfer",
    "settledAt": "2026-06-23T10:00:00Z"
  }
]
```

---

## 4. Entity Relationship Diagram

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│     USER     │       │       TRIP       │       │   CATEGORY   │
├──────────────┤       ├──────────────────┤       ├──────────────┤
│ id       (PK)│◄──┐   │ id           (PK)│       │ id       (PK)│
│ name         │   │   │ name             │       │ name         │
│ email        │   │   │ description      │       │ icon         │
│ avatarColor  │   ├───┤ memberIds[]  (FK)│       │ color        │
│ createdAt    │   │   │ createdBy    (FK)│───┘   └──────┬───────┘
└──────────────┘   │   │ currency         │              │
                   │   │ status           │              │
                   │   │ destination      │              │
                   │   │ startDate        │              │
                   │   │ endDate          │              │
                   │   │ createdAt        │              │
                   │   │ updatedAt        │              │
                   │   └────────┬─────────┘              │
                   │            │                         │
                   │            │ 1:N                     │
                   │            ▼                         │
                   │   ┌──────────────────┐              │
                   │   │     EXPENSE      │              │
                   │   ├──────────────────┤              │
                   │   │ id           (PK)│              │
                   │   │ tripId       (FK)│──────────────┘
                   │   │ description      │    (categoryId FK)
                   │   │ amount           │
                   │   │ currency         │
                   │   │ categoryId   (FK)│
                   ├───┤ paidByUserId (FK)│
                   │   │ splitMethod      │
                   │   │ splits[] {       │
                   ├───┤   userId,        │
                   │   │   amount          │
                   │   │ }                │
                   │   │ date             │
                   │   │ notes            │
                   │   │ createdAt        │
                   │   │ updatedAt        │
                   │   └──────────────────┘
                   │
                   │   ┌──────────────────┐
                   │   │   SETTLEMENT     │
                   │   ├──────────────────┤
                   │   │ id           (PK)│
                   │   │ tripId       (FK)│
                   ├───┤ fromUserId   (FK)│
                   └───┤ toUserId     (FK)│
                       │ amount           │
                       │ currency         │
                       │ note             │
                       │ settledAt        │
                       └──────────────────┘
```

---

## 5. Backend Architecture (Java — Spring Boot)

### 5.1 Generic JSON Repository (Core Pattern)

This is the **heart** of the persistence layer. Every entity repository extends this.

```java
// repository/JsonRepository.java
public class JsonRepository<T extends BaseEntity> {

    private final Path filePath;
    private final ObjectMapper objectMapper;
    private final TypeReference<List<T>> typeReference;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // --- CRUD ---
    public List<T> findAll();
    public Optional<T> findById(String id);
    public T save(T entity);           // insert or update
    public void deleteById(String id);

    // --- QUERY HELPERS ---
    public List<T> findAllByField(Function<T, String> getter, String value);

    // --- INTERNAL ---
    private List<T> readFile();        // deserialize JSON array
    private void writeFile(List<T> entities); // serialize & flush
}
```

**Key design decisions:**

- **`ReentrantReadWriteLock`** prevents concurrent read/write corruption on the same JSON file.
- **Atomic writes:** Write to a `.tmp` file first, then rename — prevents data loss on crash.
- **No in-memory cache** for simplicity; file I/O per request is acceptable for a college-scale app.

---

### 5.2 REST API Endpoints

| Method   | Endpoint                          | Description                |
| -------- | --------------------------------- | -------------------------- |
| **USERS** |                                  |                            |
| `GET`    | `/api/users`                      | List all users             |
| `POST`   | `/api/users`                      | Create user                |
| `GET`    | `/api/users/{id}`                 | Get user by ID             |
| `PUT`    | `/api/users/{id}`                 | Update user                |
| `DELETE` | `/api/users/{id}`                 | Delete user                |
| **TRIPS** |                                  |                            |
| `GET`    | `/api/trips`                      | List all trips             |
| `POST`   | `/api/trips`                      | Create trip                |
| `GET`    | `/api/trips/{id}`                 | Get trip detail            |
| `PUT`    | `/api/trips/{id}`                 | Update trip                |
| `DELETE` | `/api/trips/{id}`                 | Delete trip (cascades)     |
| `GET`    | `/api/trips/{id}/summary`         | Trip summary + totals      |
| `GET`    | `/api/trips/{id}/balances`        | Who owes whom              |
| **EXPENSES** |                               |                            |
| `GET`    | `/api/trips/{tripId}/expenses`    | List expenses for trip     |
| `POST`   | `/api/trips/{tripId}/expenses`    | Add expense to trip        |
| `GET`    | `/api/expenses/{id}`              | Get expense detail         |
| `PUT`    | `/api/expenses/{id}`              | Update expense             |
| `DELETE` | `/api/expenses/{id}`              | Delete expense             |
| **CATEGORIES** |                             |                            |
| `GET`    | `/api/categories`                 | List all categories        |
| `POST`   | `/api/categories`                 | Create custom category     |
| **SETTLEMENTS** |                            |                            |
| `GET`    | `/api/trips/{tripId}/settlements` | List settlements for trip  |
| `POST`   | `/api/trips/{tripId}/settlements` | Record a settlement        |
| `DELETE` | `/api/settlements/{id}`           | Remove settlement          |

---

### 5.3 Balance Calculation Algorithm (`DebtSimplifier.java`)

This is the most critical business logic — computing **minimized debts** between members:

```
INPUT:  All expenses + settlements for a trip

STEP 1: Build net balance per member
        For each expense:
          payer gets  +amount
          each split member gets  -split.amount

STEP 2: Subtract settlements already made
          fromUser gets  +settlement.amount  (paid off debt)
          toUser   gets  -settlement.amount  (received less)

STEP 3: Separate into creditors (positive balance) and debtors (negative)

STEP 4: Greedy simplification
        Sort creditors DESC, debtors ASC (by absolute value)
        Match largest debtor to largest creditor
        Transfer min(|debt|, |credit|)
        Repeat until all balances are zero

OUTPUT: Minimal list of { from, to, amount } transfers
```

---

### 5.4 Standard API Response Wrapper

```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String timestamp;  // ISO-8601
}
```

Every endpoint returns this. The frontend always checks `success` before reading `data`.

---

## 6. Frontend Architecture (Electron + React + Tailwind)

### 6.1 Electron Main Process Flow

```
app.on('ready')
  │
  ├─► Find free port (e.g., 8080)
  ├─► Spawn: java -jar backend.jar --server.port={port}
  ├─► Poll http://localhost:{port}/actuator/health
  │     └─► Retry up to 15 times (1s interval)
  ├─► Create BrowserWindow
  │     └─► Load: http://localhost:5173 (dev) or file://dist/index.html (prod)
  └─► On window close → kill Java process → app.quit()
```

---

### 6.2 React App Router Structure

```jsx
<App>
  <AppContextProvider>
    <UserContextProvider>
      <BrowserRouter>
        <MainLayout>              {/* Sidebar + TopBar always visible */}
          <Routes>
            /                     → DashboardPage
            /trips                → TripsPage
            /trips/:id            → TripDetailPage
            /trips/:id/expenses   → (tab within TripDetailPage)
            /trips/:id/balances   → (tab within TripDetailPage)
            /trips/:id/add        → AddExpensePage
            /categories           → CategoriesPage
            /profile              → ProfilePage
          </Routes>
        </MainLayout>
      </BrowserRouter>
    </UserContextProvider>
  </AppContextProvider>
</App>
```

---

### 6.3 State Management Strategy

```
                  ┌─────────────────────┐
                  │    React Context     │
                  │  (AppContext)        │
                  │  - currentUser      │
                  │  - activeTripId     │
                  └─────────┬───────────┘
                            │ provides
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                  ▼
   useTrips()        useExpenses()      useSettlements()
   - trips[]         - expenses[]       - settlements[]
   - loading         - loading          - balances[]
   - createTrip()    - addExpense()     - settleDebt()
   - deleteTrip()    - deleteExpense()  - simplified[]
          │                 │                  │
          └─────────────────┼──────────────────┘
                            ▼
                    services/api.js
                    (Axios → localhost:{port})
```

**No Redux needed.** For a college project, `Context + custom hooks + local component state` is the right balance of simplicity and separation of concerns.

---

### 6.4 API Service Layer Example

```javascript
// services/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: `http://localhost:${window.electronAPI.getBackendPort()}/api`,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
});

// Response interceptor — unwrap ApiResponse
api.interceptors.response.use(
  (res) => res.data.data,   // extract .data from ApiResponse wrapper
  (err) => Promise.reject(err.response?.data?.message || 'Unknown error')
);

export default api;
```

---

## 7. Key Design Decisions & Rationale

| Decision                               | Rationale                                                                                                                                       |
| -------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| **Spring Boot embedded Tomcat**        | Single JAR execution — Electron spawns one `java -jar` command. No external server setup.                                                       |
| **JSON files, not SQLite**             | Explicit project requirement. Human-readable, easy to debug and seed with test data.                                                            |
| **Read/Write lock per repository**     | Prevents corrupt writes. A college app won't have high concurrency, but correctness matters.                                                    |
| **Atomic file writes (tmp + rename)**  | If the app crashes mid-write, the original file survives intact.                                                                                |
| **DTOs separate from Models**          | Request DTOs validate input. Response DTOs shape output. Models own persistence structure. Never leak internals.                                 |
| **Port negotiation**                   | Electron finds a free port → passes it to Java via CLI arg → stores it for the React app. Avoids port conflicts.                                |
| **No authentication**                  | College project, single-user desktop app. Users are "trip members" but there's no login flow. The app uses a `currentUser` selection on startup. |
| **Greedy debt simplification**         | O(n log n) and produces near-minimal transfers. A full min-cost-flow is overkill here.                                                          |
| **Vite for React bundling**            | Faster HMR than Webpack. Works seamlessly with Electron via `electron-vite` or manual config.                                                   |

---

## 8. Split Method Logic

The `SplitMethod` enum supports three modes:

```
EQUAL       → amount / memberCount (remainder pennies go to payer)
EXACT       → frontend sends exact amounts per member (must sum to total)
PERCENTAGE  → frontend sends % per member (must sum to 100%)
```

The **backend always validates** that `splits[].amount` sums to `expense.amount` (within ±0.01 tolerance for rounding).

---

## 9. Build & Run Pipeline

### Development Mode

```
Terminal 1:  cd backend && mvn spring-boot:run
Terminal 2:  cd frontend && npm run dev
             (Vite dev server + Electron)
```

### Production Build

```
1. cd backend && mvn clean package -DskipTests
   → target/expense-tracker-backend.jar

2. Copy JAR into frontend/resources/

3. cd frontend && npm run build
   → Vite builds React to dist/

4. npm run electron:build
   → electron-builder packages everything
   → Output: .exe (Windows), .dmg (macOS), .AppImage (Linux)

Final bundle contains:
  - Electron shell
  - React static files
  - Java JAR (in resources/)
  - Bundled JRE (via jlink or jpackage)
```

---

## 10. Screen Flow Map

```
   ┌─────────────────────────────────────────────────┐
   │               DASHBOARD                          │
   │  ┌─────────┐  ┌─────────────┐  ┌────────────┐  │
   │  │ Active  │  │ Total Spent │  │ Recent     │  │
   │  │ Trips: 3│  │   €2,847    │  │ Expenses   │  │
   │  └────┬────┘  └─────────────┘  └────────────┘  │
   └───────┼─────────────────────────────────────────┘
           │ click trip
           ▼
   ┌─────────────────────────────────────────────────┐
   │            TRIP DETAIL                           │
   │  ┌──────────┬────────────┬───────────────────┐  │
   │  │ Expenses │  Balances  │   Settlements     │  │
   │  │  (tab)   │   (tab)    │     (tab)         │  │
   │  └──────────┴────────────┴───────────────────┘  │
   │                                                   │
   │  [Expenses Tab]         [Balances Tab]           │
   │   - Expense list         - Who owes whom chart   │
   │   - Filter by category   - Simplified debts      │
   │   - Sort by date/amount  - "Settle Up" button    │
   │   - "+ Add Expense"                              │
   │                         [Settlements Tab]        │
   │                          - Settlement history    │
   │                          - Remaining balance     │
   └──────────────────────────────────────────────────┘
```

---

> **Architecture designed for clarity, correctness, and college-grade rigor.**  
> Every layer has a single responsibility, every data flow is explicit, and there are zero magic shortcuts that would break under real usage.