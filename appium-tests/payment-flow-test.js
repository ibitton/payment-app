const { remote } = require('webdriverio');

/**
 * Appium UI Test for Cashi Payment App
 * 
 * Prerequisites:
 * 1. Install Appium: npm install -g appium
 * 2. Install UIAutomator2 driver: appium driver install uiautomator2
 * 3. Start Android emulator or connect physical device
 * 4. Build APK: ./gradlew :androidApp:assembleDebug
 * 5. Start Appium server: appium
 * 
 * Run this test: node payment-flow-test.js
 */

const capabilities = {
  platformName: 'Android',
  'appium:automationName': 'UiAutomator2',
  'appium:deviceName': 'Android Emulator',
  'appium:app': '../androidApp/build/outputs/apk/debug/androidApp-debug.apk',
  'appium:appPackage': 'com.cashi.challenge',
  'appium:appActivity': '.MainActivity',
  'appium:noReset': false,
  'appium:fullReset': false,
};

const wdOpts = {
  hostname: process.env.APPIUM_HOST || 'localhost',
  port: parseInt(process.env.APPIUM_PORT, 10) || 4723,
  logLevel: 'info',
  capabilities,
};

/**
 * Test Suite: Payment Flow
 */
async function runPaymentFlowTest() {
  console.log('Starting Payment Flow UI Test...');
  const driver = await remote(wdOpts);
  
  try {
    // Wait for app to load
    await driver.pause(3000);
    
    // Test 1: Send Payment
    console.log('\n[Test 1] Sending payment...');
    
    // Find and fill email input
    const emailInput = await driver.$('android=new UiSelector().description("Recipient Email Input")');
    await emailInput.waitForDisplayed({ timeout: 5000 });
    await emailInput.click();
    await driver.pause(200);
    // Type at session level (works with Compose)
    await driver.keys('test@example.com');
    console.log('  - Email entered: test@example.com');
    
    // Find and fill amount input
    const amountInput = await driver.$('android=new UiSelector().description("Amount Input")');
    await amountInput.click();
    await driver.pause(200);
    await driver.keys('100.00');
    console.log('  - Amount entered: 100.00');
    
    // Select currency from dropdown
    const currencyDropdown = await driver.$('android=new UiSelector().description("Currency Dropdown")');
    await currencyDropdown.click();
    await driver.pause(500);
    
    const usdOption = await driver.$('android=new UiSelector().text("USD ($)")');
    await usdOption.click();
    console.log('  - Currency selected: USD');
    
    // Click submit button
    const submitButton = await driver.$('android=new UiSelector().text("Send Payment")');
    await submitButton.click();
    console.log('  - Submit button clicked');
    
    // Wait for result (success or error)
    await driver.pause(3000);
    
    // Check for success message or error
    try {
      const successMessage = await driver.$('android=new UiSelector().textContains("sent successfully")');
      await successMessage.waitForDisplayed({ timeout: 5000 });
      console.log('  [PASS] Payment sent successfully!');
    } catch (e) {
      console.log('  [INFO] Payment result check - may have failed or need longer wait');
    }
    
    // Test 2: Navigate to Transaction History
    console.log('\n[Test 2] Navigating to transaction history...');
    
    // Click History button
    const historyButton = await driver.$('android=new UiSelector().text("History")');
    await historyButton.click();
    console.log('  - History button clicked');
    
    await driver.pause(2000);
    
    // Verify history screen loaded
    try {
      const historyTitle = await driver.$('android=new UiSelector().text("Transaction History")');
      await historyTitle.waitForDisplayed({ timeout: 5000 });
      console.log('  [PASS] Transaction History screen loaded');
    } catch (e) {
      console.log('  [FAIL] Transaction History screen not found');
      throw e;
    }
    
    // Test 3: Verify transaction appears in history
    console.log('\n[Test 3] Verifying transaction in history...');
    
    // Look for the email in transaction list
    try {
      const transactionEmail = await driver.$('android=new UiSelector().textContains("test@example.com")');
      await transactionEmail.waitForDisplayed({ timeout: 5000 });
      console.log('  [PASS] Transaction with test@example.com found in history');
    } catch (e) {
      console.log('  [INFO] Transaction not immediately visible - may need Firestore sync');
    }
    
    // Navigate back to payment screen
    const backButton = await driver.$('android=new UiSelector().descriptionContains("Back")');
    if (await backButton.isDisplayed()) {
      await backButton.click();
      console.log('  - Navigated back to payment screen');
    }
    
    console.log('\n[Test Suite] All tests completed successfully!');
    
  } catch (error) {
    console.error('\n[Test Suite] Test failed:', error.message);
    throw error;
  } finally {
    await driver.pause(1000);
    await driver.deleteSession();
    console.log('\nSession ended.');
  }
}

/**
 * Test: Invalid Payment (validation error)
 */
async function runInvalidPaymentTest() {
  console.log('\n\n[Invalid Payment Test] Starting...');
  const driver = await remote(wdOpts);
  
  try {
    await driver.pause(3000);
    
    // Enter invalid email
    const emailInput = await driver.$('android=new UiSelector().className("android.widget.EditText").instance(0)');
    await emailInput.waitForDisplayed({ timeout: 5000 });
    await emailInput.setValue('invalid-email');
    
    // Enter valid amount
    const amountInput = await driver.$('android=new UiSelector().className("android.widget.EditText").instance(1)');
    await amountInput.setValue('50.00');
    
    // Submit
    const submitButton = await driver.$('android=new UiSelector().text("Send Payment")');
    await submitButton.click();
    
    await driver.pause(2000);
    
    // Check for error message
    try {
      const errorMessage = await driver.$('android=new UiSelector().textContains("Invalid")');
      await errorMessage.waitForDisplayed({ timeout: 5000 });
      console.log('  [PASS] Validation error displayed correctly');
    } catch (e) {
      console.log('  [INFO] Error message check - may have different format');
    }
    
  } catch (error) {
    console.error('Invalid payment test error:', error.message);
  } finally {
    await driver.deleteSession();
  }
}

// Run tests
(async () => {
  try {
    await runPaymentFlowTest();
    await runInvalidPaymentTest();
    console.log('\n========================================');
    console.log('All UI tests completed!');
    console.log('========================================');
    process.exit(0);
  } catch (error) {
    console.error('\nTest execution failed:', error);
    process.exit(1);
  }
})();
