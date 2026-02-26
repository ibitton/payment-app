# Demo Script

Follow these steps to demonstrate the Cashi Payment App functionality.

## Prerequisites

- Android emulator running (API 28+) or physical device connected
- Backend server started: `.\gradlew.bat :server:run` (Windows) or `./gradlew :server:run` (macOS/Linux)
- APK installed: `.\gradlew.bat :androidApp:installDebug` (Windows) or `./gradlew :androidApp:installDebug` (macOS/Linux)

---

## Scenario 1: Send a Valid Payment

1. Open the Cashi app on your device/emulator
2. Enter recipient email: `test@example.com`
3. Enter amount: `100.00`
4. Select currency: **USD** from the dropdown (tap to open, select USD)
5. Tap "Send Payment" button
6. **Expected Result**: Green success message "Payment sent successfully" appears at bottom

---

## Scenario 2: Validation Error (Invalid Email)

1. Clear the form (retype fields or use clear buttons)
2. Enter email: `invalid-email` (deliberately omit @ symbol)
3. Enter amount: `50`
4. Select currency: **EUR**
5. Tap "Send Payment"
6. **Expected Result**: Red error message "Invalid email format" appears inline below email field

---

## Scenario 3: View Transaction History

1. From payment screen, tap "History" button at bottom of screen
2. **Expected Result**: Transaction list screen appears
3. **Verify**: Previous payment (from Scenario 1) is visible in the list
4. **Verify**: Status shows "Success" with green indicator dot
5. **Verify**: Amount shows "$100.00" and email shows "test@example.com"

---

## Scenario 4: Real-time Update Demonstration

1. Keep History screen open on Device A
2. From Device B (or second emulator instance), send a new payment:
   - Email: `another@example.com`
   - Amount: `75.50`
   - Currency: **GBP**
3. **Expected Result**: New transaction appears automatically in Device A without manual refresh
4. **Verify**: New entry shows correct amount (£75.50), email, and current timestamp

---

## Scenario 5: Currency Support

1. Return to payment screen (tap back arrow ←)
2. Tap currency dropdown to open
3. **Expected Result**: Dropdown shows 16 currencies (USD, EUR, GBP, JPY, CAD, AUD, CHF, CNY, INR, SGD, NZD, SEK, NOK, DKK, PLN, MXN)
4. Select any non-USD currency (e.g., JPY - Japanese Yen)
5. Enter amount: `5000` and valid email
6. Tap "Send Payment"
7. **Expected Result**: Payment processes successfully with ¥ symbol displayed

---

## Scenario 6: Amount Validation

1. Clear the form
2. Enter email: `valid@example.com`
3. Enter amount: `100.999` (3 decimal places - invalid)
4. Select currency: **USD**
5. Tap "Send Payment"
6. **Expected Result**: Error message "Amount cannot have more than 2 decimal places"
7. Change amount to: `100.99` (2 decimal places)
8. Tap "Send Payment"
9. **Expected Result**: Payment succeeds

---

## Scenario 7: Maximum Amount Validation

1. Enter email: `valid@example.com`
2. Enter amount: `1500000` (exceeds $1M limit)
3. Tap "Send Payment"
4. **Expected Result**: Error message "Amount exceeds maximum limit of 1,000,000"
5. Change amount to: `1000000`
6. **Expected Result**: Payment succeeds (at limit)

---

## Test the Backend API Directly

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{"recipientEmail":"api-test@example.com","amount":250.00,"currency":"EUR"}'
```

**Expected Response**:
```json
{
  "id": "TXN-xxxxx",
  "status": "SUCCESS",
  "timestamp": 1234567890
}
```

---

## Troubleshooting Demo Issues

| Issue | Solution |
|-------|----------|
| "Cannot connect to server" | Verify backend is running on `http://localhost:8080` |
| App crashes on launch | Check `google-services.json` is in `androidApp/src/main/` |
| Payment fails with network error | For emulator, ensure using `10.0.2.2:8080` in configuration |
| Transactions don't appear | Check Firestore rules allow read/write |

---
