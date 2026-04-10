# Expense Tracker — Design Decisions, Guidelines & Conventions

> **Companion document to the System Architecture**  
> Every major design decision explained in depth: the reasoning, the rules, the pitfalls, and the professional conventions your team must follow.  
> **Date:** 2026-04-10

---

## Table of Contents

1. [Why Spring Boot as an Embedded Backend](#1-why-spring-boot-as-an-embedded-backend)
2. [JSON File Persistence — The Right Way](#2-json-file-persistence--the-right-way)
3. [The Generic Repository Pattern](#3-the-generic-repository-pattern)
4. [Atomic File Writes & Data Integrity](#4-atomic-file-writes--data-integrity)
5. [Concurrency Control with ReadWriteLock](#5-concurrency-control-with-readwritelock)
6. [ID Generation Strategy](#6-id-generation-strategy)
7. [DTOs vs. Models — Never Leak Your Internals](#7-dtos-vs-models--never-leak-your-internals)
8. [API Design Conventions (RESTful Contract)](#8-api-design-conventions-restful-contract)
9. [The Standard API Response Wrapper](#9-the-standard-api-response-wrapper)
10. [Global Exception Handling Strategy](#10-global-exception-handling-strategy)
11. [The Debt Simplification Algorithm — In Depth](#11-the-debt-simplification-algorithm--in-depth)
12. [Split Method Validation Rules](#12-split-method-validation-rules)
13. [Cascade Delete Rules](#13-cascade-delete-rules)
14. [Electron ↔ Java Lifecycle Management](#14-electron--java-lifecycle-management)
15. [Port Negotiation Protocol](#15-port-negotiation-protocol)
16. [Frontend State Management Philosophy](#16-frontend-state-management-philosophy)
17. [API Service Layer & Axios Configuration](#17-api-service-layer--axios-configuration)
18. [React Component Architecture Conventions](#18-react-component-architecture-conventions)
19. [Tailwind CSS Conventions](#19-tailwind-css-conventions)
20. [Currency & Rounding Rules](#20-currency--rounding-rules)
21. [Date & Time Handling Conventions](#21-date--time-handling-conventions)
22. [Project Naming Conventions](#22-project-naming-conventions)
23. [Error Handling Philosophy — Full Stack](#23-error-handling-philosophy--full-stack)
24. [Testing Strategy](#24-testing-strategy)
25. [Git Workflow & Repository Conventions](#25-git-workflow--repository-conventions)
26. [Build & Distribution Conventions](#26-build--distribution-conventions)

---

## 1. Why Spring Boot as an Embedded Backend

### The Decision

Use Spring Boot with its embedded Tomcat server, packaged as a single executable JAR, spawned as a child process by Electron.

### Why Not Alternatives?

| Alternative | Why It Was Rejected |
|---|---|
| **Java plain HTTP server (com.sun.net.httpserver)** | No dependency injection, no validation framework, no JSON serialization config. You'd reinvent half of Spring poorly. |
| **Node.js backend inside Electron** | The project requirement explicitly says Java. Also, mixing Node in the main process with backend logic creates an untestable monolith. |
| **Java directly via JNI/GraalVM** | Massively overengineered for a college project. Debugging is a nightmare. |
| **Javalin / Micronaut / Quarkus** | Valid choices, but Spring Boot has the largest ecosystem, the most learning resources, and your professors will likely know it. For a college project, community support matters. |

### Critical Convention

> **The backend must be 100% runnable and testable independently of Electron.**  
> You should be able to run `mvn spring-boot:run`, open Postman, and test every endpoint without ever launching the desktop app. This separation is non-negotiable — it makes debugging twice as fast and allows frontend and backend developers to work in parallel.

### `application.properties` Essentials

```properties
# Server
server.port=8080

# Jackson (JSON serialization)
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.default-property-inclusion=non_null
spring.jackson.deserialization.fail-on-unknown-properties=false

# Custom: path to JSON data directory
app.data.directory=./data

# Logging
logging.level.com.expensetracker=DEBUG
logging.level.org.springframework.web=INFO
```

**Why `fail-on-unknown-properties=false`?** — Future-proofing. If the frontend sends a field the backend doesn't know about yet (during parallel development), the backend won't crash. It just ignores it.

**Why `write-dates-as-timestamps=false`?** — Dates serialize as `"2026-04-10T08:00:00Z"` (ISO-8601 string) instead of `1744272000000` (epoch milliseconds). Human-readable JSON files are a core benefit of your storage choice — don't throw that away.

---

## 2. JSON File Persistence — The Right Way

### The Decision

Use flat JSON files (one per entity type) as a mock database, stored in a `data/` directory.

### File Structure Rules

1. **One file per entity type.** Never mix users and trips in the same file. This mirrors how a real database has separate tables.
2. **Each file contains a JSON array at the root.** Even if empty, the file content is `[]`, never `null`, never `{}`.
3. **Files are the source of truth.** There is no in-memory cache that diverges from disk. Every read goes to disk. Every write flushes to disk immediately.
4. **Files are created on startup if missing.** The `AppInitializer` checks for each file and creates it with `[]` if absent. The app never crashes because a file doesn't exist.

### Why Not SQLite?

The project spec says JSON. But even beyond that, JSON files have a genuine advantage for a college project: **you can open the file, read the data, and manually edit it for testing.** SQLite requires a viewer tool. When debugging at 2 AM before a deadline, this matters.

### File Location Strategy

```
Development:   ./data/          (project root, relative path)
Production:    {app.getPath('userData')}/data/   (OS-specific user data folder)
```

In production, Electron passes the absolute data directory path to Java via a CLI argument:

```
java -jar backend.jar --app.data.directory=/Users/alice/Library/Application Support/ExpenseTracker/data
```

**Why?** — The packaged `.app` / `.exe` bundle is read-only. You cannot write files inside it. The OS user-data folder is the correct place for persistent mutable data.

### Seeding Default Data

The `AppInitializer` runs on startup (implements `CommandLineRunner`) and:

1. Creates any missing JSON files with `[]`.
2. Checks if `categories.json` is empty. If so, inserts the 6 default categories (Food, Transport, Accommodation, Activities, Shopping, Other). Categories are the only entity with seed data.
3. Never overwrites existing data.

```java
@Component
public class AppInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // Create files if absent
        // Seed default categories if empty
    }
}
```

---

## 3. The Generic Repository Pattern

### The Decision

Build one `JsonRepository<T extends BaseEntity>` class that handles all file I/O and CRUD, then create thin entity-specific repositories that extend it.

### Why Generic?

Without generics, you'd write nearly identical file-read/write/search code in `UserRepository`, `TripRepository`, `ExpenseRepository`, `CategoryRepository`, and `SettlementRepository`. That's 5 copies of the same logic — 5 places for bugs to hide.

With generics, the file I/O logic exists **exactly once**.

### BaseEntity Contract

```java
public abstract class BaseEntity {
    private String id;

    public abstract String getId();
    public abstract void setId(String id);
}
```

Every model extends `BaseEntity`. The repository uses `getId()` to match, update, and delete entities. This is the single contract that makes the generic pattern work.

### Entity-Specific Repository Example

```java
@Repository
public class ExpenseRepository extends JsonRepository<Expense> {

    public ExpenseRepository(@Value("${app.data.directory}") String dataDir, ObjectMapper mapper) {
        super(
            Paths.get(dataDir, "expenses.json"),
            mapper,
            new TypeReference<List<Expense>>() {}
        );
    }

    // Custom query: find all expenses for a trip
    public List<Expense> findByTripId(String tripId) {
        return findAllByField(Expense::getTripId, tripId);
    }
}
```

### Convention: Repository Layer Rules

- Repositories **only** handle data access. No business logic.
- Repositories return `Optional<T>` for single-entity lookups. Never return `null`.
- Repositories never throw business exceptions. They throw `IOException` wrappers if file I/O fails. The service layer translates these into business exceptions.
- Every custom query method name starts with `findBy...` to match Spring Data naming conventions (even though you're not using Spring Data — consistency matters).

---

## 4. Atomic File Writes & Data Integrity

### The Problem

If your app writes to `expenses.json` and crashes mid-write (power outage, force-quit, JVM crash), the file is corrupted. You lose **all** expenses, not just the one being written. This is because JSON requires the entire structure to be valid — a truncated array like `[{"id":"1"},{"id":"2` is unparseable.

### The Solution: Write-Rename Pattern

```java
private void writeFile(List<T> entities) {
    Path tmpFile = filePath.resolveSibling(filePath.getFileName() + ".tmp");
    try {
        // Step 1: Write to a TEMPORARY file
        objectMapper.writerWithDefaultPrettyPrinter()
            .writeValue(tmpFile.toFile(), entities);

        // Step 2: Atomically replace the real file
        Files.move(tmpFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException e) {
        // Step 3: Clean up temp file if move failed
        Files.deleteIfExists(tmpFile);
        throw new DataStorageException("Failed to write " + filePath.getFileName(), e);
    }
}
```

### Why This Works

- `Files.move` with `ATOMIC_MOVE` is an operating system-level atomic operation. The file either fully exists with the old content or fully exists with the new content. There is no in-between state.
- If the write to the `.tmp` file fails (crash during Step 1), the original file is untouched.
- If the move fails (Step 2), the original file is still untouched, and the `.tmp` is cleaned up.

### Convention

> **Never use `FileWriter` or `Files.write()` directly on a data file. Always use the tmp-and-rename pattern.** This rule has zero exceptions in this project.

---

## 5. Concurrency Control with ReadWriteLock

### The Problem

Electron's renderer can fire multiple HTTP requests in quick succession (e.g., user rapidly adds expenses). Spring Boot handles each request on a separate thread. Two threads writing to the same JSON file simultaneously will corrupt it.

### The Solution

```java
private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

public List<T> findAll() {
    lock.readLock().lock();
    try {
        return readFile();
    } finally {
        lock.readLock().unlock();
    }
}

public T save(T entity) {
    lock.writeLock().lock();
    try {
        List<T> entities = readFile();
        // ... insert or update logic ...
        writeFile(entities);
        return entity;
    } finally {
        lock.writeLock().unlock();
    }
}
```

### Why ReadWriteLock Instead of `synchronized`?

- **`synchronized`** blocks all threads — even readers block other readers.
- **`ReadWriteLock`** allows **multiple concurrent readers** but **exclusive writers**. Since reads vastly outnumber writes, this gives better throughput.

### Convention

> **One lock instance per repository instance, which means one lock per JSON file.** Writing to `users.json` does not block reading from `trips.json`. This is the correct granularity.

### Edge Case: Cross-File Consistency

What if deleting a trip requires also deleting its expenses and settlements (cascade delete)? You're now modifying 3 files. With per-file locks, there's a brief window where the trip is deleted but its expenses still exist.

**Pragmatic answer for a college project:** This window is milliseconds, it's a single-user desktop app, and the service layer deletes in order (expenses → settlements → trip). If a crash occurs mid-cascade, the orphaned expenses/settlements are harmless — they reference a trip ID that no longer exists and will never be displayed.

**If you wanted bulletproof consistency**, you'd implement a write-ahead log (WAL) or use a single global lock for cascade operations. That's overkill here, but mention it in your report to show awareness.

---

## 6. ID Generation Strategy

### The Decision

Use prefixed, short, random string IDs.

```
Users:       usr_a1b2c3d4
Trips:       trp_x7y8z9w0
Expenses:    exp_m3n4o5p6
Categories:  cat_food01
Settlements: stl_q7r8s9t0
```

### Why Not Auto-Increment Integers?

In a JSON file, there's no built-in auto-increment. You'd have to:
1. Read the file.
2. Find the max ID.
3. Add 1.
4. Hope no other thread did the same thing between steps 2 and 3.

This is fragile. Random string IDs eliminate the problem entirely — collisions are astronomically unlikely with 8+ alphanumeric characters.

### Why Prefixed?

When debugging, if you see `usr_a1b2c3d4` in the `expenses.json` file under `paidByUserId`, you **instantly** know it's a user ID, not a trip or category. Prefixes are a form of type safety in your data files.

### Implementation

```java
public class IdGenerator {
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(String prefix) {
        StringBuilder sb = new StringBuilder(prefix).append("_");
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
```

### Convention

> **IDs are assigned by the backend, never by the frontend.** The `CreateExpenseRequest` DTO has no `id` field. The service layer calls `IdGenerator.generate("exp")` and sets it before saving. This prevents ID conflicts and injection.

---

## 7. DTOs vs. Models — Never Leak Your Internals

### The Decision

Maintain separate classes for:
- **Models** (`model/`) — represent the stored data structure exactly as it appears in JSON.
- **Request DTOs** (`dto/request/`) — represent what the frontend sends to create/update an entity.
- **Response DTOs** (`dto/response/`) — represent what the backend sends back to the frontend.

### Why Three Layers?

Consider the `Expense` model:

```java
public class Expense extends BaseEntity {
    private String id;           // Generated by backend
    private String tripId;       // Set by backend from URL path
    private String description;
    private double amount;
    private String currency;
    private String categoryId;
    private String paidByUserId;
    private SplitMethod splitMethod;
    private List<Split> splits;
    private String date;
    private String notes;
    private String createdAt;    // Set by backend
    private String updatedAt;    // Set by backend
}
```

Now the `CreateExpenseRequest`:

```java
public class CreateExpenseRequest {
    @NotBlank
    private String description;

    @Positive
    private double amount;

    @NotBlank
    private String categoryId;

    @NotBlank
    private String paidByUserId;

    @NotNull
    private SplitMethod splitMethod;

    @NotEmpty
    private List<SplitRequest> splits;

    @NotBlank
    private String date;

    private String notes;    // Optional
}
```

Notice what's **missing** from the request: `id`, `tripId`, `currency`, `createdAt`, `updatedAt`. The frontend has no business setting these. If you used the `Expense` model directly as the request body, a malicious or buggy frontend could:
- Set its own `id` (collision/overwrite).
- Set its own `createdAt` (data falsification).
- Set `tripId` to a different trip than the URL (inconsistency).

### Convention: DTO Mapping Rules

- **Controller** receives a Request DTO, passes it to the Service.
- **Service** maps the Request DTO to a Model, fills in backend-generated fields (`id`, `createdAt`, `tripId`), validates business rules, and calls the Repository.
- **Service** maps the Model to a Response DTO when returning data.
- **Never return a Model directly from a Controller.** Even if the Model and Response DTO happen to have the same fields today, they'll diverge tomorrow.

### Validation Annotations

Use `jakarta.validation` annotations on Request DTOs:

| Annotation | Use When |
|---|---|
| `@NotBlank` | Required string field (rejects `null`, `""`, and `"   "`) |
| `@NotNull` | Required non-string field (enums, objects) |
| `@NotEmpty` | Required collection (rejects `null` and empty list) |
| `@Positive` | Numeric field must be > 0 (amounts, prices) |
| `@Size(min, max)` | String length constraints (names, descriptions) |
| `@Email` | Email format validation |

Then annotate the controller parameter with `@Valid`:

```java
@PostMapping
public ApiResponse<Expense> create(@Valid @RequestBody CreateExpenseRequest request) { ... }
```

Spring automatically returns a 400 error with validation details if any annotation fails. **You don't write a single `if` statement for basic input validation.**

---

## 8. API Design Conventions (RESTful Contract)

### URL Structure Rules

```
/api/{resource}              → Collection (GET = list, POST = create)
/api/{resource}/{id}         → Specific item (GET = read, PUT = update, DELETE = delete)
/api/{parent}/{parentId}/{child}  → Nested resource
```

### Concrete Rules for This Project

1. **Plural nouns, never verbs.** `/api/trips`, not `/api/getTrips` or `/api/trip`.
2. **Nesting reflects ownership.** Expenses belong to a trip → `/api/trips/{tripId}/expenses`. But a specific expense is accessed at `/api/expenses/{id}` (flat) because the expense ID is globally unique and you don't always know the trip ID in every context.
3. **Computed resources use descriptive sub-paths.** `/api/trips/{id}/summary` returns a computed summary. `/api/trips/{id}/balances` returns calculated balance data. These are GET-only — they compute, not store.
4. **Query parameters for filtering and sorting,** not path segments.

```
GET /api/trips/{tripId}/expenses?categoryId=cat_food01&sortBy=date&order=desc
```

### HTTP Method Semantics

| Method | Idempotent? | Safe? | Semantics |
|---|---|---|---|
| `GET` | Yes | Yes | Read data. Never modifies state. |
| `POST` | No | No | Create new resource. Returns 201. |
| `PUT` | Yes | No | Full replacement of a resource. Returns 200. |
| `DELETE` | Yes | No | Remove resource. Returns 200 or 204. |

**Idempotent** means calling it twice produces the same result. `PUT` with the same body twice → same state. `DELETE` on an already-deleted resource → still deleted (return 204 or 404, both are acceptable).

### HTTP Status Code Contract

| Status | When to Use |
|---|---|
| `200 OK` | Successful GET, PUT, DELETE |
| `201 Created` | Successful POST (new resource created) |
| `204 No Content` | Successful DELETE with no response body |
| `400 Bad Request` | Validation error, malformed JSON, split amounts don't sum correctly |
| `404 Not Found` | Entity with the given ID doesn't exist |
| `409 Conflict` | Duplicate resource (e.g., user with same email already exists) |
| `500 Internal Server Error` | Unexpected backend error (file I/O failure, bug) |

### Convention

> **Every POST response includes the full created entity in the body (with backend-generated fields like `id` and `createdAt`).** The frontend uses this to update its local state without a second GET request.

---

## 9. The Standard API Response Wrapper

### The Decision

Every single endpoint response is wrapped in a consistent `ApiResponse<T>` envelope.

### Structure

```json
{
  "success": true,
  "message": "Expense created successfully",
  "data": { ... },
  "timestamp": "2026-04-10T08:30:00Z"
}
```

```json
{
  "success": false,
  "message": "Expense not found with id: exp_nonexist",
  "data": null,
  "timestamp": "2026-04-10T08:30:00Z"
}
```

### Why a Wrapper?

1. **The frontend has one single check:** `if (response.success)`. No guessing based on HTTP status codes, no checking if the body is an object vs. an array vs. a string.
2. **Error messages are always in the same place:** `response.message`. No hunting through different error formats.
3. **The `data` field can be anything:** a single object, a list, a computed summary, `null`. The wrapper is the contract; the payload varies.

### Implementation

```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String timestamp;

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, message, data, Instant.now().toString());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, Instant.now().toString());
    }
}
```

### Convention

> **Controllers never return raw objects.** Every return statement uses `ApiResponse.ok(...)` or `ApiResponse.error(...)`. This is enforced by code review.

---

## 10. Global Exception Handling Strategy

### The Decision

Use a single `@RestControllerAdvice` class that catches all exceptions and converts them into `ApiResponse` errors with appropriate HTTP status codes.

### Implementation

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
            errors.put(err.getField(), err.getDefaultMessage())
        );
        return ApiResponse.error("Validation failed").withData(errors);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDuplicate(DuplicateResourceException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneral(Exception ex) {
        // Log the full stack trace — critical for debugging
        log.error("Unhandled exception", ex);
        return ApiResponse.error("An internal error occurred");
    }
}
```

### Convention: Exception Hierarchy

```
Exception
  └── RuntimeException
        ├── ResourceNotFoundException    → 404
        ├── DuplicateResourceException   → 409
        ├── ValidationException          → 400  (for business rule violations)
        └── DataStorageException         → 500  (file I/O failures)
```

### Rules

1. **Service layer throws business exceptions.** `throw new ResourceNotFoundException("Trip", tripId);`
2. **Controller layer never catches exceptions.** Let them bubble up to the `GlobalExceptionHandler`.
3. **Never expose stack traces to the frontend.** The generic 500 handler logs the full trace server-side but returns only `"An internal error occurred"` to the client.
4. **Custom exception messages are user-readable.** `"Trip not found with id: trp_abc123"` — not `"NullPointerException at line 47"`.

---

## 11. The Debt Simplification Algorithm — In Depth

### The Problem

After a trip, everyone has paid for different things. Alice paid €120 for dinner (split 3 ways), Bob paid €90 for a taxi (split 2 ways), Carol paid €200 for the hotel (split 3 ways). Now who owes whom, and what's the minimum number of transfers to settle all debts?

### Step-by-Step Walkthrough

#### Step 1: Compute Net Balance Per Member

For every expense, the payer **gains** the full amount (they covered it), and each member in the split **loses** their share.

```
Expense: Alice paid €120, split equally among Alice, Bob, Carol (€40 each)
  Alice: +120 - 40 = +80
  Bob:          - 40 = -40
  Carol:        - 40 = -40

Expense: Bob paid €90, split between Bob and Carol (€45 each)
  Bob:   +90 - 45 = +45
  Carol:       - 45 = -45

Expense: Carol paid €200, split equally among all three (~€66.67 each)
  Carol: +200 - 66.67 = +133.33
  Alice:       - 66.67 = -66.67
  Bob:         - 66.67 = -66.67
```

**Running totals:**
```
Alice: +80 - 66.67      = +13.33  (net creditor)
Bob:   -40 + 45 - 66.67 = -61.67  (net debtor)
Carol: -40 - 45 + 133.33 = +48.33 (net creditor)
```

Sanity check: +13.33 - 61.67 + 48.33 = **0.00** ✓ (net balances always sum to zero)

#### Step 2: Subtract Existing Settlements

If Bob already sent Carol €20:
```
Bob:   -61.67 + 20 = -41.67
Carol: +48.33 - 20 = +28.33
```

#### Step 3: Separate Creditors and Debtors

```
Creditors: [Alice +13.33, Carol +28.33]
Debtors:   [Bob -41.67]
```

#### Step 4: Greedy Match

Sort creditors descending, debtors ascending (by absolute value):

```
Creditors: [Carol +28.33, Alice +13.33]
Debtors:   [Bob -41.67]
```

Match:
1. Bob → Carol: min(41.67, 28.33) = €28.33. Bob now owes 13.34. Carol is settled.
2. Bob → Alice: min(13.34, 13.33) = €13.33. Bob now owes 0.01 (rounding). Alice is settled.

**Result: 2 transfers instead of potentially 3+**

### Implementation Skeleton

```java
public class DebtSimplifier {

    public static List<Transfer> simplify(List<Expense> expenses, List<Settlement> settlements) {
        // Step 1: Build net balances
        Map<String, Double> balances = new HashMap<>();
        for (Expense exp : expenses) {
            balances.merge(exp.getPaidByUserId(), exp.getAmount(), Double::sum);
            for (Split split : exp.getSplits()) {
                balances.merge(split.getUserId(), -split.getAmount(), Double::sum);
            }
        }

        // Step 2: Apply settlements
        for (Settlement s : settlements) {
            balances.merge(s.getFromUserId(), s.getAmount(), Double::sum);
            balances.merge(s.getToUserId(), -s.getAmount(), Double::sum);
        }

        // Step 3: Separate
        PriorityQueue<Map.Entry<String, Double>> creditors = new PriorityQueue<>(
            (a, b) -> Double.compare(b.getValue(), a.getValue())  // max-heap
        );
        PriorityQueue<Map.Entry<String, Double>> debtors = new PriorityQueue<>(
            Comparator.comparingDouble(Map.Entry::getValue)       // min-heap (most negative first)
        );

        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            double val = round2(entry.getValue());
            if (val > 0.01) creditors.add(entry);
            else if (val < -0.01) debtors.add(entry);
        }

        // Step 4: Greedy matching
        List<Transfer> transfers = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            var creditor = creditors.poll();
            var debtor = debtors.poll();

            double amount = round2(Math.min(creditor.getValue(), -debtor.getValue()));
            transfers.add(new Transfer(debtor.getKey(), creditor.getKey(), amount));

            double newCredBal = round2(creditor.getValue() - amount);
            double newDebtBal = round2(debtor.getValue() + amount);

            if (newCredBal > 0.01) creditors.add(Map.entry(creditor.getKey(), newCredBal));
            if (newDebtBal < -0.01) debtors.add(Map.entry(debtor.getKey(), newDebtBal));
        }

        return transfers;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
```

### Complexity

- Time: **O(n log n)** where n = number of members (heap operations).
- This greedy approach doesn't always produce the *absolute minimum* number of transfers (that's NP-hard in general), but for trip groups of 2-15 people, it produces optimal or near-optimal results every time.

---

## 12. Split Method Validation Rules

### EQUAL Split

```
Rule: The backend computes splits. Frontend sends the list of participating user IDs.
      Backend divides amount by member count, assigns remainder cents to the payer.

amount = 100.00, 3 members → [33.34, 33.33, 33.33]
                               ↑ payer gets the extra cent

Validation: splits[].amount values are IGNORED in the request for EQUAL splits.
            The backend recalculates them.
```

### EXACT Split

```
Rule: Frontend sends exact amounts per person.
      Backend validates: sum(splits[].amount) == expense.amount (±0.01)

Validation:
  - Every split.amount >= 0
  - At least one split.amount > 0
  - |sum(splits) - expense.amount| <= 0.01
  - Every split.userId must be a member of the trip
```

### PERCENTAGE Split

```
Rule: Frontend sends percentage per person.
      Backend validates: sum(percentages) == 100 (±0.1)
      Backend converts percentages to amounts.

percentage → amount: (percentage / 100) * expense.amount, rounded to 2 decimals
Remainder adjustment applied to payer.

Validation:
  - Every percentage >= 0 and <= 100
  - sum(percentages) between 99.9 and 100.1 (floating point tolerance)
  - Every split.userId must be a member of the trip
```

### Convention

> **The backend is the single source of truth for split amounts.** Even if the frontend pre-calculates and sends amounts, the backend ALWAYS re-validates. Never trust the client.

---

## 13. Cascade Delete Rules

When a parent entity is deleted, its children must be cleaned up. Here are the rules:

| Deleted Entity | Cascade Action |
|---|---|
| **User** | Remove user ID from all `trip.memberIds[]`. Delete all expenses where `paidByUserId` == user. Remove user from all `expense.splits[]`. Delete all settlements involving user. **Warning: destructive. Prompt user for confirmation on the frontend.** |
| **Trip** | Delete all expenses with `tripId` == trip. Delete all settlements with `tripId` == trip. |
| **Expense** | No cascades. Settlements are independent (they're based on computed balances). |
| **Category** | Set `categoryId` to `"cat_othe01"` (Other) on all expenses using the deleted category. Never leave orphan references. |

### Implementation Convention

```java
// In TripService.java
public void deleteTrip(String tripId) {
    // 1. Verify trip exists
    Trip trip = tripRepository.findById(tripId)
        .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId));

    // 2. Cascade: delete child data FIRST
    expenseRepository.deleteByTripId(tripId);
    settlementRepository.deleteByTripId(tripId);

    // 3. Delete the trip itself LAST
    tripRepository.deleteById(tripId);
}
```

**Delete children before the parent.** If you delete the parent first and crash before deleting children, you have orphaned data. If you delete children first and crash before deleting the parent, you have a trip with no expenses — which is a valid state (empty trip). Always prefer the safer failure mode.

---

## 14. Electron ↔ Java Lifecycle Management

### The Core Challenge

Electron and Java are separate processes. The Electron main process must:
1. Start the Java backend.
2. Wait for it to be ready.
3. Keep it alive during the app's lifetime.
4. Kill it cleanly when the app closes.

### Implementation: `javaProcess.js`

```javascript
const { spawn } = require('child_process');
const path = require('path');
const http = require('http');

let javaProcess = null;

function getJarPath() {
    if (app.isPackaged) {
        return path.join(process.resourcesPath, 'backend.jar');
    }
    return path.join(__dirname, '..', '..', 'backend', 'target', 'expense-tracker-backend.jar');
}

function startBackend(port, dataDir) {
    const jarPath = getJarPath();
    javaProcess = spawn('java', [
        '-jar', jarPath,
        `--server.port=${port}`,
        `--app.data.directory=${dataDir}`
    ], {
        stdio: ['pipe', 'pipe', 'pipe']  // Capture stdout/stderr
    });

    javaProcess.stdout.on('data', (data) => console.log(`[Java] ${data}`));
    javaProcess.stderr.on('data', (data) => console.error(`[Java ERROR] ${data}`));
    javaProcess.on('exit', (code) => console.log(`[Java] Process exited: ${code}`));
}

function waitForBackend(port, retries = 15, interval = 1000) {
    return new Promise((resolve, reject) => {
        let attempts = 0;
        const check = () => {
            attempts++;
            http.get(`http://localhost:${port}/api/categories`, (res) => {
                if (res.statusCode === 200) resolve();
                else retry();
            }).on('error', retry);
        };
        const retry = () => {
            if (attempts >= retries) reject(new Error('Backend failed to start'));
            else setTimeout(check, interval);
        };
        check();
    });
}

function stopBackend() {
    if (javaProcess && !javaProcess.killed) {
        javaProcess.kill('SIGTERM');       // Graceful shutdown
        setTimeout(() => {
            if (javaProcess && !javaProcess.killed) {
                javaProcess.kill('SIGKILL');  // Force kill after 5s
            }
        }, 5000);
    }
}
```

### Convention: Startup Sequence in `main.js`

```javascript
app.on('ready', async () => {
    const port = await findFreePort();       // 1. Find available port
    const dataDir = getDataDirectory();      // 2. Resolve data path
    startBackend(port, dataDir);             // 3. Launch Java
    await waitForBackend(port);              // 4. Poll until ready
    createWindow(port);                      // 5. Create Electron window
});

app.on('before-quit', () => {
    stopBackend();                           // 6. Clean shutdown
});

// Safety net: if the window closes but the app doesn't quit
app.on('window-all-closed', () => {
    stopBackend();
    app.quit();
});
```

### Critical Rules

1. **Never assume Java starts instantly.** Spring Boot takes 2-8 seconds to start. The `waitForBackend` polling loop is mandatory.
2. **Always capture Java's stderr.** If the JAR fails to start (missing JRE, port conflict, corrupted JAR), you need to see the error in Electron's console.
3. **Always kill Java on quit.** If Electron closes without killing the child process, Java keeps running as an orphan, holding the port.
4. **Use `SIGTERM` first, `SIGKILL` as fallback.** `SIGTERM` lets Spring Boot execute its shutdown hooks (flushing any pending writes). `SIGKILL` is the nuclear option after a timeout.

---

## 15. Port Negotiation Protocol

### The Problem

You can't hardcode port `8080`. If the user has another app on that port (a local web server, another instance of your app), the Java backend fails to start.

### The Solution

```javascript
// In Electron main process
const net = require('net');

function findFreePort() {
    return new Promise((resolve, reject) => {
        const server = net.createServer();
        server.listen(0, () => {              // 0 = OS assigns a random free port
            const port = server.address().port;
            server.close(() => resolve(port));
        });
        server.on('error', reject);
    });
}
```

### Passing the Port to React

**Via the preload script (Context Bridge):**

```javascript
// preload.js
const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
    getBackendPort: () => ipcRenderer.sendSync('get-backend-port')
});
```

```javascript
// main.js
ipcMain.on('get-backend-port', (event) => {
    event.returnValue = backendPort;
});
```

```javascript
// React: services/api.js
const port = window.electronAPI?.getBackendPort() || 8080;  // 8080 fallback for dev
```

### Convention

> **The port is determined once at app startup and never changes during the session.** It flows: Electron main → Java CLI arg, Electron main → preload → React. There is no other path.

---

## 16. Frontend State Management Philosophy

### The Decision

Use **React Context + Custom Hooks** for state management. No Redux, no Zustand, no MobX.

### Why Not Redux?

Redux solves a problem this app doesn't have: **complex state shared across many unrelated components with frequent, interdependent updates.** In this app:

- There are ~5 entity types.
- At most 2-3 components share the same data (e.g., expense list + balance summary both need expenses).
- State updates are simple CRUD operations, not complex transformations.

Redux adds 3 files per feature (actions, reducers, selectors), a store configuration, provider setup, and middleware for async operations. For this project, that's **pure overhead** with zero benefit.

### State Architecture

```
AppContext (global)
  ├── currentUser: User
  ├── setCurrentUser: function
  └── backendPort: number

Per-page state (via custom hooks)
  ├── useTrips()       → { trips, loading, error, createTrip, updateTrip, deleteTrip }
  ├── useExpenses(tripId) → { expenses, loading, addExpense, deleteExpense }
  └── useSettlements(tripId) → { settlements, balances, settleDebt }
```

### Custom Hook Convention

Every hook follows this exact pattern:

```javascript
function useExpenses(tripId) {
    const [expenses, setExpenses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Fetch on mount / tripId change
    useEffect(() => {
        if (!tripId) return;
        setLoading(true);
        expenseService.getByTrip(tripId)
            .then(setExpenses)
            .catch(setError)
            .finally(() => setLoading(false));
    }, [tripId]);

    // Mutation: add expense (optimistic update pattern)
    const addExpense = async (data) => {
        const created = await expenseService.create(tripId, data);
        setExpenses(prev => [...prev, created]);   // Append to local state
        return created;
    };

    // Mutation: delete expense
    const deleteExpense = async (expenseId) => {
        await expenseService.remove(expenseId);
        setExpenses(prev => prev.filter(e => e.id !== expenseId));
    };

    return { expenses, loading, error, addExpense, deleteExpense };
}
```

### Rules

1. **Hooks own the fetch lifecycle.** The page component never calls `fetch` or `axios` directly.
2. **Hooks update local state after successful mutation.** This avoids re-fetching the entire list after every change.
3. **Hooks expose `loading` and `error` states.** The page component uses these to show spinners and error messages.
4. **Hooks are scoped.** `useExpenses(tripId)` only fetches expenses for that trip. Changing the trip ID triggers a re-fetch.

---

## 17. API Service Layer & Axios Configuration

### Architecture

```
Component → Hook → Service → Axios Instance → HTTP → Java Backend
```

### Axios Instance

```javascript
// services/api.js
import axios from 'axios';

const api = axios.create({
    baseURL: `http://localhost:${window.electronAPI?.getBackendPort() || 8080}/api`,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Response interceptor: unwrap ApiResponse envelope
api.interceptors.response.use(
    (response) => {
        const body = response.data;
        if (body.success) return body.data;
        return Promise.reject(new Error(body.message));
    },
    (error) => {
        if (error.response) {
            // Server responded with error status
            const message = error.response.data?.message || `Server error: ${error.response.status}`;
            return Promise.reject(new Error(message));
        }
        if (error.code === 'ECONNREFUSED') {
            return Promise.reject(new Error('Cannot connect to backend. Is the server running?'));
        }
        return Promise.reject(new Error('Network error. Please try again.'));
    }
);

export default api;
```

### Service Example

```javascript
// services/expenseService.js
import api from './api';

const expenseService = {
    getByTrip: (tripId) => api.get(`/trips/${tripId}/expenses`),
    getById:   (id) => api.get(`/expenses/${id}`),
    create:    (tripId, data) => api.post(`/trips/${tripId}/expenses`, data),
    update:    (id, data) => api.put(`/expenses/${id}`, data),
    remove:    (id) => api.delete(`/expenses/${id}`),
};

export default expenseService;
```

### Convention

> **Services are plain objects with methods, not classes.** No need for `new ExpenseService()`. They're stateless wrappers around Axios calls. Keep them simple.

> **The Axios interceptor handles the ApiResponse unwrapping globally.** Service methods return `data` directly (a trip object, an array of expenses), not the full `{ success, message, data }` envelope.

---

## 18. React Component Architecture Conventions

### Component Categories

| Category | Location | Role | Examples |
|---|---|---|---|
| **Pages** | `pages/` | Full-screen views, route targets. Compose layout from smaller components. Own hooks. | `TripDetailPage`, `DashboardPage` |
| **Feature Components** | `components/{feature}/` | Domain-specific UI. Receive data via props. May have local state for UI interactions. | `ExpenseForm`, `TripCard`, `BalanceBoard` |
| **Shared Components** | `components/shared/` | Reusable, domain-agnostic UI primitives. Fully controlled via props. | `Button`, `Modal`, `Input`, `LoadingSpinner` |
| **Layout Components** | `components/layout/` | App shell: sidebar, top bar, main content area. | `MainLayout`, `Sidebar`, `TopBar` |

### Naming Conventions

```
File name:         PascalCase.jsx        → ExpenseForm.jsx
Component name:    PascalCase            → export default function ExpenseForm()
Hook file:         camelCase.js          → useExpenses.js
Service file:      camelCase.js          → expenseService.js
Utility file:      camelCase.js          → formatCurrency.js
```

### Props Convention

```jsx
// ✅ Correct: destructure props, provide defaults
function ExpenseItem({ expense, onEdit, onDelete, showCategory = true }) { ... }

// ❌ Wrong: opaque props object
function ExpenseItem(props) { ... }
```

### File Structure Convention for Components

```jsx
// 1. Imports (external first, then internal, then styles)
import { useState } from 'react';
import { formatCurrency } from '../../utils/formatCurrency';

// 2. Component definition
export default function ExpenseItem({ expense, onEdit, onDelete }) {
    // 3. Hooks (useState, useEffect, custom hooks)
    const [isExpanded, setIsExpanded] = useState(false);

    // 4. Derived values / computations
    const formattedAmount = formatCurrency(expense.amount, expense.currency);

    // 5. Event handlers
    const handleDelete = () => {
        if (window.confirm('Delete this expense?')) onDelete(expense.id);
    };

    // 6. Render
    return (
        <div className="...">
            {/* JSX */}
        </div>
    );
}
```

---

## 19. Tailwind CSS Conventions

### Design Token Strategy

Define a consistent design system in `tailwind.config.js`:

```javascript
module.exports = {
    content: ['./src/**/*.{js,jsx}'],
    theme: {
        extend: {
            colors: {
                primary:   { 50: '#EEF2FF', 500: '#6366F1', 600: '#4F46E5', 700: '#4338CA' },
                success:   { 50: '#F0FDF4', 500: '#22C55E', 600: '#16A34A' },
                danger:    { 50: '#FEF2F2', 500: '#EF4444', 600: '#DC2626' },
                warning:   { 50: '#FFFBEB', 500: '#F59E0B', 600: '#D97706' },
                neutral:   { 50: '#F9FAFB', 100: '#F3F4F6', 200: '#E5E7EB',
                             700: '#374151', 800: '#1F2937', 900: '#111827' },
            },
            fontFamily: {
                sans: ['Inter', 'system-ui', 'sans-serif'],
                mono: ['JetBrains Mono', 'monospace'],
            },
        },
    },
    plugins: [],
};
```

### Class Ordering Convention

Follow the **"Outside In"** ordering:

```
Layout → Sizing → Spacing → Borders → Background → Typography → Effects → States
```

```jsx
// ✅ Correct order
<div className="flex items-center w-full p-4 border border-neutral-200 rounded-lg bg-white text-sm text-neutral-700 shadow-sm hover:shadow-md transition-shadow">

// ❌ Chaotic order
<div className="text-sm shadow-sm border flex hover:shadow-md bg-white p-4 items-center rounded-lg w-full">
```

### Reusable Component Pattern for Consistency

```jsx
// components/shared/Button.jsx
const variants = {
    primary: 'bg-primary-600 text-white hover:bg-primary-700 focus:ring-primary-500',
    secondary: 'bg-white text-neutral-700 border border-neutral-300 hover:bg-neutral-50',
    danger: 'bg-danger-600 text-white hover:bg-danger-700 focus:ring-danger-500',
};

const sizes = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-3 text-base',
};

export default function Button({ variant = 'primary', size = 'md', children, ...props }) {
    return (
        <button
            className={`inline-flex items-center justify-center font-medium rounded-lg
                        focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors
                        disabled:opacity-50 disabled:cursor-not-allowed
                        ${variants[variant]} ${sizes[size]}`}
            {...props}
        >
            {children}
        </button>
    );
}
```

### Convention

> **Never use raw color values (`bg-indigo-600`) in page/feature components.** Always use semantic names (`bg-primary-600`) or shared components (`<Button variant="primary">`). This makes theme changes a one-line edit instead of a 50-file search-and-replace.

---

## 20. Currency & Rounding Rules

### The Core Rule

> **All monetary values are stored as `double` (Java) / `number` (JSON/JS) with exactly 2 decimal places. All arithmetic is followed by rounding to 2 decimals.**

### Why Not `BigDecimal`?

For a college project with JSON storage, `BigDecimal` serialization adds complexity (custom serializers/deserializers, string-vs-number debates in JSON). The floating-point errors on `double` are sub-cent for the amounts involved (trip expenses are rarely above $10,000). Rounding after every operation eliminates visible errors.

### Rounding Convention

```java
// Java: always use this helper
public static double round2(double value) {
    return Math.round(value * 100.0) / 100.0;
}
```

```javascript
// JavaScript: always use this helper
export const round2 = (value) => Math.round(value * 100) / 100;
```

### Display Convention

```javascript
// utils/formatCurrency.js
const symbols = { USD: '$', EUR: '€', GBP: '£', BRL: 'R$', JPY: '¥' };

export function formatCurrency(amount, currency = 'USD') {
    const symbol = symbols[currency] || currency;
    return `${symbol}${amount.toFixed(2)}`;
}
// formatCurrency(120.5, 'EUR') → "€120.50"
```

### The Penny Problem

When splitting €100 three ways: 100 / 3 = 33.3333...

```
Round each share: 33.33 × 3 = 99.99  → Missing 1 cent!
```

**Solution:** The payer absorbs the remainder.

```java
double baseShare = round2(amount / memberCount);
double remainder = round2(amount - (baseShare * memberCount));
// Payer's share = baseShare + remainder
```

For €100 / 3: base = 33.33, remainder = 0.01, payer gets 33.34.

---

## 21. Date & Time Handling Conventions

### Storage Format

| Field Type | Format | Example |
|---|---|---|
| **Timestamps** (createdAt, updatedAt, settledAt) | ISO-8601 with UTC | `"2026-04-10T08:30:00Z"` |
| **Calendar dates** (startDate, endDate, expense date) | ISO-8601 date only | `"2026-04-10"` |

### Why UTC for Timestamps?

The app runs locally, so timezone isn't critical. But storing in UTC is the universally accepted convention. If you ever sync data between devices or export it, UTC is unambiguous.

### Why Date-Only for Calendar Dates?

A trip's start date is "June 15th" — no specific time. An expense date is "the day it happened." Storing these as full timestamps invites bugs: "Why does my June 15th expense show as June 14th?" (Because the timestamp was `2026-06-15T02:00:00Z` and the user's local timezone is UTC-5.)

### Java Implementation

```java
// For timestamps
String now = Instant.now().toString();  // "2026-04-10T08:30:00.123Z"

// For dates
String today = LocalDate.now().toString();  // "2026-04-10"
```

### JavaScript Implementation

```javascript
// For timestamps
const now = new Date().toISOString();  // "2026-04-10T08:30:00.123Z"

// For display
const displayDate = (isoString) => {
    return new Date(isoString).toLocaleDateString('en-US', {
        year: 'numeric', month: 'short', day: 'numeric'
    });
};
// displayDate("2026-06-16") → "Jun 16, 2026"
```

### Convention

> **The backend generates all timestamps.** The frontend only sends calendar dates (for expense date, trip dates). Never trust the client's clock for audit-trail fields.

---

## 22. Project Naming Conventions

### Java (Backend)

| Element | Convention | Example |
|---|---|---|
| Package | lowercase, dot-separated | `com.expensetracker.service` |
| Class | PascalCase | `ExpenseService`, `CreateTripRequest` |
| Interface | PascalCase (no `I` prefix) | `Serializable`, not `ISerializable` |
| Method | camelCase, verb-first | `findById()`, `createExpense()`, `deleteByTripId()` |
| Variable | camelCase | `tripId`, `paidByUserId` |
| Constant | SCREAMING_SNAKE_CASE | `MAX_RETRIES`, `DEFAULT_CURRENCY` |
| Enum value | SCREAMING_SNAKE_CASE | `SplitMethod.EQUAL`, `TripStatus.ACTIVE` |
| JSON file | lowercase, plural | `expenses.json`, `users.json` |

### JavaScript / React (Frontend)

| Element | Convention | Example |
|---|---|---|
| Component file | PascalCase.jsx | `ExpenseForm.jsx` |
| Component name | PascalCase | `export default function ExpenseForm` |
| Hook file | camelCase.js | `useExpenses.js` |
| Service file | camelCase.js | `expenseService.js` |
| Utility file | camelCase.js | `formatCurrency.js` |
| Variable / function | camelCase | `handleSubmit`, `isLoading` |
| Constant | SCREAMING_SNAKE_CASE | `API_TIMEOUT`, `MAX_FILE_SIZE` |
| CSS class (Tailwind) | kebab-case (Tailwind default) | `bg-primary-600`, `text-sm` |
| Folder | lowercase | `components/`, `services/`, `hooks/` |

### JSON Data Fields

All field names are **camelCase** in JSON:

```json
✅  { "paidByUserId": "usr_abc", "splitMethod": "EQUAL", "createdAt": "..." }
❌  { "paid_by_user_id": "usr_abc", "split_method": "EQUAL", "created_at": "..." }
```

This matches Java's default Jackson serialization and JavaScript's native style. Zero transformation needed.

---

## 23. Error Handling Philosophy — Full Stack

### Backend Error Flow

```
Controller receives request
  → @Valid fails? → Spring throws MethodArgumentNotValidException
                    → GlobalExceptionHandler catches → 400 + field errors
  → Service logic fails? → Service throws custom exception
                           → GlobalExceptionHandler catches → 4xx + message
  → Repository I/O fails? → Repository throws DataStorageException
                             → GlobalExceptionHandler catches → 500 + generic message
  → Unexpected bug? → GlobalExceptionHandler catch-all → 500 + "An internal error occurred"
```

### Frontend Error Flow

```
User action (click button)
  → Hook calls service method
    → Axios sends request
      → Success? → Interceptor unwraps data → Hook updates state → UI re-renders
      → Error?   → Interceptor extracts message → Hook sets error state → UI shows toast/alert
```

### Convention: Never Swallow Errors

```javascript
// ❌ NEVER do this
try {
    await expenseService.create(tripId, data);
} catch (e) {
    // silently ignored — user sees nothing, data isn't saved
}

// ✅ ALWAYS handle visibly
try {
    await expenseService.create(tripId, data);
    showToast('Expense added!', 'success');
} catch (e) {
    showToast(e.message, 'error');  // User sees what went wrong
}
```

### Convention: Error Messages Are for Humans

```java
// ✅ Good
throw new ResourceNotFoundException("Trip not found with id: " + tripId);
throw new ValidationException("Split amounts must sum to the expense total. Expected: 120.50, Got: 115.00");

// ❌ Bad
throw new RuntimeException("404");
throw new Exception("error");
throw new ValidationException("SPLIT_SUM_MISMATCH");
```

---

## 24. Testing Strategy

### What to Test

| Layer | What | Tool | Priority |
|---|---|---|---|
| **Repository** | File read/write, CRUD operations, atomic writes, concurrent access | JUnit 5 + temp directories | **HIGH** — this is your custom persistence layer, bugs here corrupt data |
| **Service** | Business logic, split validation, debt simplification, cascade deletes | JUnit 5 + Mockito (mock repositories) | **HIGH** — this is where the math lives |
| **Controller** | Endpoint routing, status codes, request validation | Spring MockMvc | MEDIUM |
| **DebtSimplifier** | Algorithm correctness with known inputs/outputs | JUnit 5 (pure unit tests) | **CRITICAL** — this is the hardest logic |
| **Frontend hooks** | Data fetching, state updates | React Testing Library | LOW (for college project) |

### Test Conventions

1. **Test file naming:** `{ClassName}Test.java` in the corresponding test package.
2. **Test method naming:** `should{ExpectedBehavior}_when{Condition}`

```java
@Test
void shouldReturnEmptyList_whenNoExpensesExist() { ... }

@Test
void shouldThrowValidationException_whenSplitsDontSumToTotal() { ... }

@Test
void shouldSimplifyToTwoTransfers_whenThreeMembersWithDebts() { ... }
```

3. **Use `@TempDir` for repository tests** — don't pollute real data files.

```java
@Test
void shouldPersistExpense(@TempDir Path tempDir) {
    Path file = tempDir.resolve("expenses.json");
    Files.writeString(file, "[]");
    ExpenseRepository repo = new ExpenseRepository(file, new ObjectMapper());

    Expense expense = new Expense();
    expense.setId("exp_test01");
    expense.setDescription("Test");
    repo.save(expense);

    Optional<Expense> found = repo.findById("exp_test01");
    assertTrue(found.isPresent());
    assertEquals("Test", found.get().getDescription());
}
```

4. **DebtSimplifier tests should use hand-computed expected values.**

```java
@Test
void shouldComputeCorrectBalances_threeWayDinnerSplit() {
    // Alice pays 120, split equally among Alice, Bob, Carol
    // Expected: Bob owes Alice 40, Carol owes Alice 40
    Expense dinner = expense("Alice", 120.00, equalSplit("Alice", "Bob", "Carol"));

    List<Transfer> result = DebtSimplifier.simplify(List.of(dinner), List.of());

    assertEquals(2, result.size());
    assertTransfer(result, "Bob", "Alice", 40.00);
    assertTransfer(result, "Carol", "Alice", 40.00);
}
```

---

## 25. Git Workflow & Repository Conventions

### Branch Strategy

```
main                 ← Production-ready code. Protected. Merge via PR only.
  └── develop        ← Integration branch. All feature branches merge here.
        ├── feature/backend-crud        ← Individual features
        ├── feature/frontend-trip-page
        ├── feature/debt-algorithm
        └── fix/split-rounding-error    ← Bug fixes
```

### Commit Message Convention

Use **Conventional Commits**:

```
<type>(<scope>): <short description>

Types:
  feat     → New feature
  fix      → Bug fix
  refactor → Code restructuring (no behavior change)
  docs     → Documentation only
  test     → Adding or fixing tests
  chore    → Build config, dependencies, tooling

Examples:
  feat(backend): add expense CRUD endpoints
  fix(algorithm): correct penny rounding in equal split
  refactor(frontend): extract ExpenseForm into separate component
  docs: add API endpoint table to architecture doc
  test(service): add debt simplifier edge case tests
  chore(build): configure electron-builder for Windows
```

### `.gitignore` Essentials

```gitignore
# Java
backend/target/
*.class
*.jar
!backend/src/main/resources/data/*.json

# Node
frontend/node_modules/
frontend/dist/

# IDE
.idea/
.vscode/
*.iml

# OS
.DS_Store
Thumbs.db

# Runtime data (production JSON files are NOT committed)
data/

# Environment
.env
.env.local
```

### Convention

> **Seed data files (initial `categories.json`, empty `users.json`, etc.) in `src/main/resources/data/` ARE committed.** Runtime data files in the working `data/` directory are NOT committed. This distinction prevents committing personal test data while preserving the app's default state.

---

## 26. Build & Distribution Conventions

### Java Build

```xml
<!-- pom.xml key configuration -->
<build>
    <finalName>expense-tracker-backend</finalName>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <executable>true</executable>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Command:** `mvn clean package -DskipTests`  
**Output:** `target/expense-tracker-backend.jar`

### Electron Build

```json
// electron-builder.json
{
    "appId": "com.expensetracker.app",
    "productName": "Trip Expense Tracker",
    "directories": {
        "output": "release"
    },
    "files": [
        "dist/**/*",
        "electron/**/*"
    ],
    "extraResources": [
        {
            "from": "../backend/target/expense-tracker-backend.jar",
            "to": "backend.jar"
        }
    ],
    "win": {
        "target": "nsis",
        "icon": "src/assets/icons/icon.ico"
    },
    "mac": {
        "target": "dmg",
        "icon": "src/assets/icons/icon.icns"
    },
    "linux": {
        "target": "AppImage",
        "icon": "src/assets/icons/icon.png"
    }
}
```

### JRE Bundling Decision

**Option A: Require Java installed on the user's machine.**
- Simpler build. Smaller bundle size.
- Risk: user doesn't have Java, or has wrong version.
- Acceptable for a college demo where you control the demo machine.

**Option B: Bundle a JRE with the app.**
- Use `jlink` to create a minimal custom JRE (~40-80 MB).
- The app works on any machine without pre-installed Java.
- Better for distribution, but more complex build pipeline.

**Recommendation for college project:** Use Option A for development and demo. Document Option B in your report as the production-ready approach. This shows awareness without the build complexity.

### Convention: Version Tagging

```
Format: v{major}.{minor}.{patch}

v0.1.0  → Initial working prototype (CRUD only)
v0.2.0  → Debt simplification algorithm added
v0.3.0  → Full UI implementation
v1.0.0  → Feature-complete, tested, ready for submission
```

Tag releases in Git:

```bash
git tag -a v1.0.0 -m "Feature-complete release for project submission"
git push origin v1.0.0
```

---

> **This document is your team's engineering bible.**  
> Every decision here has a reason. Every convention exists to prevent a specific class of bugs or confusion.  
> When in doubt, check this document first. When you find something not covered, add it here.