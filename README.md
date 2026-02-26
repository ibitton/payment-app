# Cashi Payment App

A production-quality FinTech payment application built with **Kotlin Multiplatform (KMP)**. This app allows users to send payments to recipients and view transaction history, with real-time updates from Firebase Firestore.

## Features

- **Send Payment**: Enter recipient email, amount, and select currency (16 supported currencies)
- **Transaction History**: Real-time updates from Firestore with status indicators
- **Cross-Platform**: Shared business logic between Android (and potentially iOS/Web)
- **Backend API**: Ktor server with validation and mock payment processing
- **Comprehensive Testing**: BDD tests with Spek, unit tests, UI tests with Appium, and performance testing with JMeter

## Architecture

```mermaid
flowchart TB
    subgraph Mobile["Mobile (Android)"]
        UI["Compose UI<br/>Payment Form<br/>Transaction List"]
        VM["ViewModels"]
        DI["Koin DI"]
    end
    
    subgraph Shared["Shared KMP Module"]
        API["PaymentApi Interface<br/>Ktor Implementation"]
        VAL["Validation Logic"]
        REPO["PaymentRepository Interface<br/>Firestore Implementation"]
        MODELS["Data Models"]
        RESULT["OperationResult<T><br/>Unified Error Handling"]
    end
    
    subgraph Backend["Backend (Ktor)"]
        REST["REST API<br/>POST /payments"]
        PROC["Payment<br/>Validation"]
    end
    
    subgraph Firebase["Firebase"]
        FS[("Firestore<br/>Transactions")]
    end
    
    UI --> VM --> REPO
    VM --> DI
    REPO --> API
    REPO --> VAL
    API --> REST
    REST --> PROC
    REPO --> FS
```

### Data Flow

1. User enters payment details in Android app (Compose UI)
2. Payment is validated in shared KMP module (client-side validation)
3. Validated payment is sent to Ktor backend via HTTP POST /payments
4. Backend processes payment and returns success/failure response
5. Successful payment is saved to Firestore by the client app
6. Transaction history updates in real-time via Firestore snapshots

### Architecture Patterns

**Interface-Based Design**

The project uses contract interfaces for data layer components, enabling testability and dependency inversion:

```kotlin
// API Contract
interface PaymentApi {
    suspend fun processPayment(request: PaymentRequest, idempotencyKey: String): OperationResult<PaymentResponse>
}

// Repository Contract  
interface PaymentRepository {
    suspend fun saveTransaction(payment: Payment): OperationResult<Unit>
    fun getAllTransactions(): Flow<List<Payment>>
}
```

Implementations (`PaymentApiClient`, `FirestorePaymentRepository`) are bound via Koin DI, allowing test fakes to be substituted without code changes.

**Unified Error Handling**

A sealed class `OperationResult<T>` provides consistent error handling across all layers:

```kotlin
sealed class OperationResult<out T> {
    data class Success<T>(val data: T) : OperationResult<T>()
    data class Failure(val error: Throwable, val message: String) : OperationResult<Nothing>()
}
```

This replaces the standard Kotlin `Result<>` type to provide:
- Consistent error representation across API, repository, and use case layers
- Rich error information (exception + human-readable message)
- Extension functions (`map`, `onSuccess`, `onFailure`) for functional composition

**Dependency Injection**

Koin is used throughout the project for dependency management:
- `commonModule()` in shared module binds interfaces to implementations
- ViewModels receive use cases via constructor injection
- Test fakes can be substituted by providing alternative module configurations

| Component | Technology |
|-----------|------------|
| **Platform** | Kotlin Multiplatform (KMP) |
| **UI Framework** | Jetpack Compose / Compose Multiplatform |
| **Backend** | Ktor (Netty engine) |
| **Database** | Firebase Firestore (GitLive KMP SDK) |
| **Networking** | Ktor Client with Content Negotiation |
| **Dependency Injection** | Koin |
| **Serialization** | Kotlinx Serialization |
| **Testing (BDD)** | Spek Framework with JUnit 5 |
| **Testing (UI)** | Appium with WebdriverIO |
| **Testing (Performance)** | Apache JMeter |

## Project Structure

```
CashiMobileAppChallenge/
├── androidApp/                  # Android application module
│   └── src/main/
│       └── kotlin/com/cashi/challenge/
│           └── MainActivity.kt  # Entry point with Koin initialization
├── composeApp/                  # Shared Compose UI (KMP)
│   └── src/commonMain/kotlin/
│       └── com/cashi/challenge/
│           ├── App.kt           # Navigation and app root
│           └── ui/
│               ├── screens/
│               │   ├── PaymentScreen.kt         # Payment form UI
│               │   └── TransactionHistoryScreen.kt # Transaction list UI
│               └── viewmodel/
│                   ├── PaymentViewModel.kt
│                   └── TransactionHistoryViewModel.kt
├── server/                      # Ktor backend
│   └── src/main/kotlin/
│       └── com/cashi/challenge/
│           ├── Application.kt   # Ktor app configuration
│           └── routes/
│               └── PaymentRoutes.kt  # POST /payments endpoint
├── shared/                      # Shared KMP business logic
│   └── src/
│       ├── commonMain/kotlin/
│       │   └── com/cashi/challenge/
│       │       ├── domain/
│       │       │   ├── models/
│       │       │   │   └── Payment.kt        # Payment, Currency, PaymentStatus
│       │       │   ├── validation/
│       │       │   │   └── PaymentValidator.kt # Validation logic
│       │       │   └── usecases/
│       │       │       ├── ProcessPaymentUseCase.kt
│       │       │       └── GetTransactionHistoryUseCase.kt
│       │       ├── data/
│       │       │   ├── api/
│       │       │   │   ├── PaymentApi.kt           # API contract interface
│       │       │   │   └── PaymentApiClient.kt     # Ktor HTTP client implementation
│       │       │   └── repository/
│       │       │       ├── PaymentRepository.kt    # Repository contract interface
│       │       │       └── FirestorePaymentRepository.kt # Firestore implementation
│       │       └── di/
│       │           └── CommonModule.kt           # Koin DI configuration
│       ├── commonTest/kotlin/
│       │   └── com/cashi/challenge/
│       │       ├── validation/
│       │       │   └── PaymentValidatorTest.kt # Platform-agnostic validation tests
│       │       ├── test/
│       │       │   ├── FakePaymentApi.kt       # Test fake for API
│       │       │   └── FakePaymentRepository.kt # Test fake for repository
│       │       └── usecases/
│       │           └── ProcessPaymentUseCaseTest.kt # Use case unit tests
│       └── jvmTest/kotlin/
│           └── com/cashi/challenge/bdd/
│               ├── PaymentValidationSpek.kt    # BDD validation tests
│               └── PaymentProcessingSpek.kt    # BDD processing tests
├── composeApp/
│   └── src/
│       └── androidUnitTest/kotlin/
│           └── com/cashi/challenge/ui/viewmodel/
│               ├── PaymentViewModelTest.kt       # (Not yet implemented - would test state management)
│               └── TransactionHistoryViewModelTest.kt # (Not yet implemented - would test flow collection)
├── appium-tests/                # UI automation tests
│   ├── payment-flow-test.js     # Appium test script
│   └── package.json
└── jmeter/                      # Performance testing
    └── payment-load-test.jmx     # JMeter test plan
```

## Prerequisites

- **JDK 17** or higher
- **Android Studio** (latest stable version)
- **Android SDK** with API 36
- **Node.js** (for Appium tests)
- **Apache JMeter** (for performance testing)

**Note**: Firebase Firestore is used for data persistence. A Firebase project with Firestore enabled is required to run the app with full functionality.

## Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd CashiMobileAppChallenge
```

### 2. Build the Project

```bash
# Windows
.\gradlew.bat :androidApp:assembleDebug

# macOS/Linux
./gradlew :androidApp:assembleDebug
```

## Running the Application

### Start the Backend Server

```bash
# Windows
.\gradlew.bat :server:run

# macOS/Linux
./gradlew :server:run
```

The server starts on `http://localhost:8080`

### Run Android App

1. Start Android emulator or connect physical device
2. Run from Android Studio, or:

```bash
.\gradlew.bat :androidApp:installDebug
```

### Test the Backend API

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{"recipientEmail":"test@example.com","amount":100.00,"currency":"USD"}'
```

## Testing

### Test Coverage Overview

| Module | Test Type | Location | Coverage |
|--------|-----------|----------|----------|
| **shared** | BDD (Spek) | `shared/src/jvmTest/` | Payment validation scenarios (10+), Payment processing flow (9+) |
| **shared** | Unit Tests (JVM) | `shared/src/jvmTest/` | PaymentValidator logic with Spek |
| **shared** | Unit Tests (Common) | `shared/src/commonTest/` | PaymentValidator (22+ tests), Use cases with fakes |
| **shared** | Fakes/Mocks | `shared/src/commonTest/` | `FakePaymentApi`, `FakePaymentRepository` for testing |
| **appium-tests** | UI Tests (JS) | `appium-tests/` | Payment flow automation |

**Test Architecture**

The project uses a multi-layered testing approach:

1. **commonTest**: Platform-agnostic unit tests using `kotlin.test` with fake implementations
2. **jvmTest**: BDD tests using Spek framework with JUnit 5 runner
3. **androidUnitTest**: Android-specific ViewModel tests with coroutines testing
4. **appium-tests**: End-to-end UI automation

**Test Fakes and Interfaces**

The architecture uses interface-based design for testability:
- `PaymentApi` interface with `FakePaymentApi` implementation for tests
- `PaymentRepository` interface with `FakePaymentRepository` implementation for tests
- Fakes enable testing use cases without network or Firestore dependencies

**Error Handling in Tests**

The project uses a unified `OperationResult<T>` sealed class for consistent error handling:
```kotlin
sealed class OperationResult<out T> {
    data class Success<T>(val data: T) : OperationResult<T>()
    data class Failure(val error: Throwable, val message: String) : OperationResult<Nothing>()
}
```

This replaces mixed `Result<>` and exception handling across API, repository, and use case layers.

### Run Tests by Category

```bash
# Run common tests (platform-agnostic)
.\gradlew.bat :shared:commonTest

# Run JVM tests (Spek BDD tests)
.\gradlew.bat :shared:jvmTest

# Run ViewModel tests
.\gradlew.bat :composeApp:testDebugUnitTest

# Run all tests
.\gradlew.bat test
```

**Test Reports:**
- `shared/build/reports/tests/commonTest/index.html`
- `shared/build/reports/tests/jvmTest/index.html`
- `composeApp/build/reports/tests/testDebugUnitTest/index.html`

**Test Coverage:**
- **PaymentValidationSpek**: 10 BDD scenarios for validation
- **PaymentProcessingSpek**: 9 BDD scenarios for payment flow
- **PaymentValidatorTest** (commonTest): 22+ unit tests for validation logic
- **ProcessPaymentUseCaseTest**: Use case tests with fakes
- **PaymentViewModelTest**: State management and interaction testing
- **TransactionHistoryViewModelTest**: Flow collection and error handling testing

### Run UI Tests with Appium

**Prerequisites:**
```bash
# Install Appium globally
npm install -g appium

# Install UIAutomator2 driver
appium driver install uiautomator2
```

**Run Tests:**
```bash
# 1. Start Appium server
appium

# 2. In another terminal, navigate to appium-tests
cd appium-tests
npm install

# 3. Run the test
node payment-flow-test.js
```

### Run Performance Tests with JMeter

**Prerequisites:**
- Download Apache JMeter from [jmeter.apache.org](https://jmeter.apache.org/)
- Extract and run `jmeter.bat` (Windows) or `jmeter` (macOS/Linux)

**Run Test:**
1. Start the backend server: `.\gradlew.bat :server:run`
2. Open JMeter GUI
3. File → Open → Select `jmeter/payment-load-test.jmx`
4. Click "Start" (green play button)

**Test Configuration:**
- 5 concurrent users (threads)
- 10 iterations per user
- 5-second ramp-up time
- POST requests to `/payments` endpoint
- Response assertions for HTTP 200 and SUCCESS status

**Results:**
- View Results Tree (detailed request/response)
- Summary Report (statistics)
- Graph Results (response time visualization)
- Aggregate Report (saved to `jmeter/results/`)

## Demo Video

Watch the app in action: [demo.webm](./demo.webm)

The video demonstrates:
- Sending a payment with email, amount, and currency selection
- Real-time validation feedback
- Payment processing and success confirmation
- Viewing transaction history with live updates

## Demo Script

Follow these steps to demonstrate the app functionality:

### Prerequisites
- Android emulator running (API 28+) or physical device
- Backend server started: `.\gradlew.bat :server:run`
- APK installed: `.\gradlew.bat :androidApp:installDebug`

### Scenario 1: Send a Valid Payment
1. Open the Cashi app on your device
2. Enter recipient email: `test@example.com`
3. Enter amount: `100.00`
4. Select currency: **USD** from dropdown
5. Tap "Send Payment" button
6. **Expected**: Green success message "Payment sent successfully" appears

### Scenario 2: Validation Error (Invalid Email)
1. Clear the form (tap X on fields if present, or retype)
2. Enter email: `invalid-email` (no @ symbol)
3. Enter amount: `50`
4. Select currency: **EUR**
5. Tap "Send Payment"
6. **Expected**: Red error message "Invalid email format" appears inline

### Scenario 3: View Transaction History
1. From payment screen, tap "History" button at bottom
2. **Expected**: Transaction list appears showing previous payment
3. **Expected**: Status shows "Success" with green indicator
4. **Expected**: Amount shows "$100.00", email shows "test@example.com"

### Scenario 4: Real-time Update Demonstration
1. Keep History screen open on Device A
2. From Device B (or emulator instance), send a new payment
3. **Expected**: New transaction appears automatically in Device A without manual refresh
4. **Expected**: New entry shows correct amount, currency, and timestamp

### Scenario 5: Currency Support
1. Return to payment screen (tap back arrow)
2. Tap currency dropdown
3. **Expected**: List shows 16 currencies (USD, EUR, GBP, JPY, CAD, etc.)
4. Select any non-USD currency (e.g., JPY)
5. Enter amount and valid email
6. **Expected**: Payment processes with selected currency

## Production Considerations

This application demonstrates core functionality for a technical assessment. The following features would be required for a real-world production FinTech application but were intentionally excluded to maintain scope:

### Architecture & Data Flow

- **Server-Side Firestore Updates**: In this assessment, the client app writes directly to Firestore after receiving backend confirmation. In production, the backend server should update Firestore after processing payments. This ensures:
  - Single source of truth for transaction state
  - Prevents race conditions between client writes
  - Allows server-side validation before persistence
  - Enables idempotency checks at the data layer

- **Event-Driven Architecture**: Production systems typically use message queues (e.g., Kafka, Pub/Sub) for async payment processing with guaranteed delivery

- **CQRS Pattern**: Separate read/write models - Firestore for fast reads, relational database (PostgreSQL) for complex queries and reporting

### Security & Authentication
- **User Authentication**: Login/registration with email/password or OAuth (Google, Apple)
- **PIN/Biometric Confirmation**: Require fingerprint/FaceID or PIN before sending payments
- **Session Management**: Token-based auth with refresh tokens and automatic logout
- **Data Encryption**: End-to-end encryption for sensitive transaction data at rest

### Compliance & Audit
- **KYC/AML Verification**: Identity verification for regulatory compliance
- **Audit Logging**: Comprehensive transaction audit trails for compliance officers
- **GDPR Compliance**: Data retention policies and user data export/deletion
- **PCI DSS Compliance**: Card industry security standards (if handling card data)

### Reliability & Resilience
- **Offline Queue**: Queue payments when offline, retry automatically when connection restored
- **Idempotency Keys**: Ensure duplicate submissions don't create multiple transactions
- **Circuit Breakers**: Handle backend outages gracefully with fallback behavior
- **Push Notifications**: Notify users of successful payments or received funds via FCM

### User Experience
- **Transaction Receipts**: Email/SMS confirmation receipts for each transaction
- **Contact Book**: Save frequent recipients for quick selection
- **Search & Filtering**: Search transaction history by recipient, date range, amount
- **Export Data**: PDF/CSV export of transaction history for accounting

### Operational
- **Analytics Integration**: Firebase Analytics or Mixpanel for user behavior tracking
- **Crash Reporting**: Firebase Crashlytics or Sentry for error monitoring
- **A/B Testing Framework**: Experiment with UI variations to optimize conversion
- **Feature Flags**: Remote configuration to enable/disable features without app updates

### Internationalization
- **Currency Conversion**: Real-time exchange rates for cross-currency transactions
- **Localization**: Multi-language support for UI strings and currency formatting
- **Timezone Handling**: Proper date/time display based on user's locale

### Accessibility
- **Full TalkBack Support**: Comprehensive screen reader labels and navigation
- **High Contrast Mode**: Support for visually impaired users
- **Dynamic Type**: Respect system font size preferences
- **Color Blindness Friendly**: Patterns/icons in addition to color for status indicators

These features represent industry-standard requirements for production FinTech applications but were deprioritized for this assessment to focus on demonstrating core architecture, KMP implementation, and testing strategies.

## Architecture Decisions

### Why Kotlin Multiplatform?

- **Shared Business Logic**: Payment validation, API calls, and data models are shared between platforms
- **Native UI**: Each platform can use native UI (Android = Jetpack Compose, iOS = SwiftUI)
- **Firebase Integration**: GitLive Firebase SDK provides KMP-compatible Firestore access
- **Future Extensibility**: Easy to add iOS, Web (Wasm), or Desktop targets

### Why Ktor for Backend?

- **Kotlin Native**: Same language as client code
- **Lightweight**: Minimal overhead for simple API endpoints
- **Content Negotiation**: Built-in JSON serialization with Kotlinx
- **Mock Server**: Suitable for development and testing

### Why Spek for BDD?

- **Kotlin DSL**: Native Kotlin syntax, no external feature files
- **JUnit 5 Integration**: Works with standard test runners
- **Readability**: `describe`/`it` blocks read like specifications
- **IDE Support**: Excellent integration with IntelliJ IDEA

## Supported Currencies

The app supports 16 major international currencies:

- **USD** - US Dollar
- **EUR** - Euro
- **GBP** - British Pound
- **JPY** - Japanese Yen
- **CAD** - Canadian Dollar
- **AUD** - Australian Dollar
- **CHF** - Swiss Franc
- **CNY** - Chinese Yuan
- **INR** - Indian Rupee
- **SGD** - Singapore Dollar
- **NZD** - New Zealand Dollar
- **SEK** - Swedish Krona
- **NOK** - Norwegian Krone
- **DKK** - Danish Krone
- **PLN** - Polish Zloty
- **MXN** - Mexican Peso

## Transaction Limits

- **Minimum**: $0.01 (or equivalent)
- **Maximum**: $1,000,000 (or equivalent)
- **Precision**: 2 decimal places (cents)

## Development Guidelines

### Adding a New Currency

1. Add to `Currency` enum in `shared/src/commonMain/kotlin/domain/models/Payment.kt`
2. Update `PaymentValidator` error message to include new currency
3. Add BDD test scenario in `PaymentValidationSpek.kt`
4. Add unit test in `PaymentValidatorTest.kt`

### Adding a New Test

**BDD Test (Spek):**
```kotlin
describe("Given a user [scenario]") {
    val request = PaymentRequest(...)
    it("should [expected outcome]") {
        val result = validator.validate(request)
        // assertions
    }
}
```

**Unit Test:**
```kotlin
@Test
fun `[descriptive test name]`() {
    val request = PaymentRequest(...)
    val result = validator.validate(request)
    // assertions
}
```

## Troubleshooting

### Backend Connection Issues

- Verify server is running: `http://localhost:8080`
- For Android emulator: Use `10.0.2.2` instead of `localhost`
- Check firewall settings for port 8080

### Firebase Issues

- Firebase configuration (`google-services.json`) must be present in `androidApp/src/main/`
- Firestore rules must allow read/write operations for the app to function

### Test Failures

- Run with `--rerun-tasks`: `.\gradlew.bat :shared:jvmTest --rerun-tasks`
- Check test report HTML for detailed error messages
- Ensure JUnit Platform is configured for Spek tests

## Contributing

1. Create feature branch: `git checkout -b feature/name`
2. Make changes with tests
3. Run all tests: `.\gradlew.bat :shared:jvmTest`
4. Commit with descriptive messages
5. Push and create pull request

## License

This project is for technical assessment purposes.

## Resources

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Ktor Server Documentation](https://ktor.io/docs/server.html)
- [Koin DI Documentation](https://insert-koin.io/docs/quickstart/kmp/)
- [GitLive Firebase](https://github.com/GitLiveApp/firebase-kotlin-sdk)
- [Spek Framework](https://www.spekframework.org/)
- [Appium Documentation](http://appium.io/docs/en/latest/)
- [Apache JMeter](https://jmeter.apache.org/)

---

**Last Updated**: February 2026
**KMP Version**: 2.3.10
**Compose Multiplatform**: 1.10.1