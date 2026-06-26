package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;
import com.opms.practice.LookupPage;
import com.opms.utils.TestDataGenerator;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Positive and negative test cases for the Create Patient form.
 * Browser opens once; login and location selection happen once in @BeforeClass.
 * @BeforeMethod closes any open overlay/alert and reopens the Add Patient form.
 *
 * Validation structure confirmed from live DOM inspection:
 *   <div class="form-group has-error">
 *     <input ...>
 *     <app-validation-error>
 *       <div class="validation_msg"><span>Field is required.</span></div>
 *     </app-validation-error>
 *   </div>
 *
 * Save & Close button: <button class="btn btn-submit ..."><span>Save & Close</span></button>
 * Close X button:      <button aria-label="Close" title="Close" ...>
 */
public class CreatePatientTest {

    private WebDriver          driver;
    private WebDriverWait      wait;
    private Actions            actions;
    private JavascriptExecutor js;

    // ── One-time setup ────────────────────────────────────────────────────────

    @BeforeClass
    public void setUpSuite() throws InterruptedException {
        driver  = DriverManager.getDriver();
        wait    = new WebDriverWait(driver, Duration.ofSeconds(15));
        actions = new Actions(driver);
        js      = (JavascriptExecutor) driver;

        LoginPage.loginWith(LoginPage.VALID_EMAIL, LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isDashboardVisible(), "Login failed in setUpSuite.");
        Thread.sleep(3000);

        // Close the right-side panel if present
        try {
            WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div/i[contains(@class,'collaspse_icon')]")));
            panel.click();
            Thread.sleep(1000);
        } catch (Exception ignored) {
            System.out.println("Right panel not present, skipping.");
        }

        // Select location once
        WebElement locationDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//ng-select[@bindlabel='practiceLocationName']")));
        actions.moveToElement(locationDropdown).click().perform();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[normalize-space(text())='" + LookupPage.location + "']"))).click();
        Thread.sleep(2000);

        System.out.println("Suite setup done – logged in and location selected.");
    }

    // ── Before each test ─────────────────────────────────────────────────────

    @BeforeMethod
    public void resetForm() throws InterruptedException {

        // 1. Dismiss the recurring "Error Encountered" server-error dialog if present
        //    (appears on every page load; OK button is a plain <button> with text "OK")
        dismissErrorDialog();

        // 2. Close the Add Patient overlay if still open (left open after a negative test)
        List<WebElement> closeIcons = driver.findElements(
                By.xpath("//button[@aria-label='Close' and @title='Close']"));
        if (!closeIcons.isEmpty() && closeIcons.get(0).isDisplayed()) {
            closeIcons.get(0).click();
            Thread.sleep(1000);
        }

        // 3. Dismiss error dialog again — it can reappear after closing the overlay
        dismissErrorDialog();

        // 4. If we navigated away from the patient list (e.g. TC_P06 clicked a search result),
        //    go back so the Add Patient button is reachable
        List<WebElement> addBtns = driver.findElements(
                By.xpath("//i[contains(@class,'fa-user-plus')]"));
        if (addBtns.isEmpty() || !addBtns.get(0).isDisplayed()) {
            driver.navigate().back();
            Thread.sleep(2000);
            dismissErrorDialog();
        }

        // 5. Open a fresh Add Patient form
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//i[contains(@class,'fa-user-plus')]")));
        actions.moveToElement(addBtn).click().perform();
        Thread.sleep(2000);

        // 6. Dismiss error dialog that fires when the Add Patient form opens
        dismissErrorDialog();

        System.out.println("resetForm: Add Patient form opened.");
    }

    // ── One-time teardown ─────────────────────────────────────────────────────

    @AfterClass
    public void tearDownSuite() {
        DriverManager.quitDriver();
        System.out.println("Browser closed after all patient tests.");
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Dismisses the "Error Encountered" 500-server-error dialog that appears on
     * every page load in this demo environment. The dialog has a plain OK button.
     */
    private void dismissErrorDialog() throws InterruptedException {
        try {
            // The dialog contains the heading "Error Encountered." and an OK button
            List<WebElement> okBtns = driver.findElements(
                    By.xpath("//button[normalize-space(text())='OK']"));
            for (WebElement btn : okBtns) {
                if (btn.isDisplayed()) {
                    btn.click();
                    Thread.sleep(500);
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Fills the Add Patient form fields.
     * Pass null for any parameter to leave that field empty (negative case testing).
     * DOB must be 8 chars MMDDYYYY — matches the field placeholder MM/DD/YYYY.
     *
     * Field IDs confirmed from live DOM:
     *   "First Name", "Last Name", "datepicker-1", "Primary Phone", "Email Address"
     *   patientGender_Male/Female/Other, patientLanguagePreference_English/Spanish
     *   patientMaritalStatus_Single/Married/Divorced/Separated/Widowed
     *   ng-select[placeholder='Select Dentist']
     */
    private void fillPatientForm(String prefix, String firstName, String lastName,
                                  String dob, String phone, String email,
                                  String gender, String language, String maritalStatus,
                                  String dentist) throws InterruptedException {
        if (prefix != null) {
            driver.findElement(By.id("Prefix")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.className("ng-dropdown-panel")))
                .findElement(By.xpath(".//span[text()='" + prefix + "']")).click();
        }

        if (firstName != null)
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("First Name")))
                .sendKeys(firstName);

        if (lastName != null)
            driver.findElement(By.id("Last Name")).sendKeys(lastName);

        if (dob != null) {
            // Use label-based XPath — datepicker ID increments every time the form opens.
            // Sending all 8 digits as one call; Kendo auto-advances MM→DD→YYYY per segment.
            WebElement dobField = driver.findElement(By.xpath(
                "//label[contains(text(),'Date of Birth')]" +
                "/ancestor::div[contains(@class,'form-group')]" +
                "//input[@placeholder='MM/DD/YYYY']"));
            dobField.click();
            Thread.sleep(500);
            dobField.sendKeys(dob);   // MMDDYYYY — all 8 digits at once
            dobField.sendKeys(Keys.TAB);
            Thread.sleep(300);
        }

        if (phone != null)
            driver.findElement(By.id("Primary Phone")).sendKeys(phone);

        if (email != null)
            driver.findElement(By.id("Email Address")).sendKeys(email);

        if (gender != null)
            driver.findElement(By.xpath(
                    "//label[@for='patientGender_" + gender + "']")).click();

        if (language != null)
            driver.findElement(By.xpath(
                    "//label[@for='patientLanguagePreference_" + language + "']")).click();

        if (maritalStatus != null)
            driver.findElement(By.xpath(
                    "//label[@for='patientMaritalStatus_" + maritalStatus + "']")).click();

        if (dentist != null) {
            driver.findElement(By.xpath("//ng-select[@placeholder='Select Dentist']")).click();
            // Directly click the exact option — avoids chaining on the first ng-option div
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class,'ng-option')]//span[text()='" + dentist + "']")))
                .click();
        }
    }

    /**
     * Clicks Save & Close.
     * Confirmed HTML: <button class="btn btn-submit ..."><span>Save & Close</span></button>
     */
    private void clickSave() throws InterruptedException {
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[text()='Save & Close']")));
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2000);
    }

    /**
     * Returns true when the Save & Close button is still visible — meaning the form
     * has NOT been submitted successfully (stays open due to validation errors).
     */
    private boolean isFormStillOpen() {
        try {
            return driver.findElement(
                    By.xpath("//span[text()='Save & Close']")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true when at least one validation error message is visible.
     *
     * Confirmed DOM structure (from live inspection):
     *   <div class="validation_msg"><span>Field is required.</span></div>
     *
     * This is rendered by the <app-validation-error> Angular component inside
     * each <div class="form-group has-error"> when Save & Close is clicked.
     */
    private boolean isValidationErrorVisible() {
        try {
            List<WebElement> errors = driver.findElements(
                    By.xpath("//div[contains(@class,'validation_msg')]" +
                             "//span[normalize-space(text())!='']"));
            for (WebElement e : errors) {
                if (e.isDisplayed()) {
                    System.out.println("Validation error visible: " + e.getText().trim());
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Returns the validation error text shown below a specific field.
     *
     * For input fields:  finds the ancestor form-group then reads validation_msg/span.
     * For ng-select:     finds by placeholder attribute on ng-select.
     */
    private String getFieldValidationMessage(String fieldId) {
        // DOB — datepicker ID is dynamic; locate via label instead
        if ("datepicker-1".equals(fieldId)) {
            try {
                WebElement msg = driver.findElement(By.xpath(
                    "//label[contains(text(),'Date of Birth')]" +
                    "/ancestor::div[contains(@class,'form-group')]" +
                    "//div[contains(@class,'validation_msg')]/span"));
                if (msg.isDisplayed() && !msg.getText().trim().isEmpty())
                    return msg.getText().trim();
            } catch (Exception ignored) {}
            return "(no DOB message found)";
        }
        String[] xpaths = {
            "//input[@id='" + fieldId + "']/ancestor::div[contains(@class,'form-group')]" +
                "//div[contains(@class,'validation_msg')]/span",
            "//ng-select[@id='" + fieldId + "']/ancestor::div[contains(@class,'form-group')]" +
                "//div[contains(@class,'validation_msg')]/span"
        };
        for (String xpath : xpaths) {
            try {
                WebElement msg = driver.findElement(By.xpath(xpath));
                if (msg.isDisplayed() && !msg.getText().trim().isEmpty())
                    return msg.getText().trim();
            } catch (Exception ignored) {}
        }
        return "(no message found for field: " + fieldId + ")";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // POSITIVE TEST CASES
    // Success = form overlay closes after clicking Save & Close.
    // The app closes the modal on success with no toast/SweetAlert,
    // so we assert the form is NO LONGER open (assertFalse isFormStillOpen).
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 1, enabled = false, description = "TC_P01 – Create patient with all valid required fields")
    public void testCreatePatientAllValidFields() throws InterruptedException {
        fillPatientForm("Mr.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(3000);
        dismissErrorDialog();

        Assert.assertFalse(isFormStillOpen(),
                "TC_P01 FAIL – Form should close after saving with all valid fields.");
        System.out.println("TC_P01 PASS – Patient created; form closed successfully.");
    }

    @Test(priority = 2,enabled = false, description = "TC_P02 – Create patient with prefix Mrs.")
    public void testCreatePatientWithPrefixMrs() throws InterruptedException {
        fillPatientForm("Mrs.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Female", "English", "Married",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(3000);
        dismissErrorDialog();

        Assert.assertFalse(isFormStillOpen(),
                "TC_P02 FAIL – Form should close after saving patient with prefix Mrs.");
        System.out.println("TC_P02 PASS – Patient with prefix Mrs. saved.");
    }

    @Test(priority = 3,enabled = false, description = "TC_P03 – Create patient with prefix Ms.")
    public void testCreatePatientWithPrefixMs() throws InterruptedException {
        fillPatientForm("Ms.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Female", "Spanish", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(3000);
        dismissErrorDialog();

        Assert.assertFalse(isFormStillOpen(),
                "TC_P03 FAIL – Form should close after saving patient with prefix Ms.");
        System.out.println("TC_P03 PASS – Patient with prefix Ms. saved.");
    }

    @Test(priority = 4, enabled = false,description = "TC_P04 – Create female patient")
    public void testCreateFemalePatient() throws InterruptedException {
        fillPatientForm("Mrs.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Female", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(3000);
        dismissErrorDialog();

        Assert.assertFalse(isFormStillOpen(),
                "TC_P04 FAIL – Form should close after saving female patient.");
        System.out.println("TC_P04 PASS – Female patient saved.");
    }

    @Test(priority = 5, enabled = false, description = "TC_P05 – Create patient with marital status Married")
    public void testCreatePatientMarried() throws InterruptedException {
        fillPatientForm("Mr.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Married",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(3000);
        dismissErrorDialog();

        Assert.assertFalse(isFormStillOpen(),
                "TC_P05 FAIL – Form should close after saving patient with Married status.");
        System.out.println("TC_P05 PASS – Patient with Married status saved.");
    }

    @Test(priority = 6, description = "TC_P06 – Verify patient appears in search after creation")
    public void testPatientAppearsInSearch() throws InterruptedException {
        String fn = TestDataGenerator.generateUniqueString();
        String ln = TestDataGenerator.generateUniqueString();

        fillPatientForm("Mr.", fn, ln,
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(3000);
        dismissErrorDialog();

        Assert.assertFalse(isFormStillOpen(),
                "TC_P06 FAIL – Form should close after save.");

        String fullName = ln + ", " + fn;
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='last name, first name ']")));
        searchBox.clear();
        searchBox.sendKeys(fullName);
        Thread.sleep(2000);

        WebElement result = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//li//span[@class='first-name-letter btn-primary p-3']"
                        + "/following-sibling::span[contains(text(),'" + fullName + "')]")));

        Assert.assertTrue(result.isDisplayed(),
                "TC_P06 FAIL – Patient should appear in search results after creation.");
        System.out.println("TC_P06 PASS – Patient '" + fullName + "' found in search.");
    }

    @Test(priority = 7,enabled = false, description = "TC_P07 – Create patient with language preference Spanish")
    public void testCreatePatientSpanishLanguage() throws InterruptedException {
        fillPatientForm("Mr.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male", "Spanish", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(3000);
        dismissErrorDialog();

        Assert.assertFalse(isFormStillOpen(),
                "TC_P07 FAIL – Form should close after saving patient with Spanish language.");
        System.out.println("TC_P07 PASS – Patient with Spanish language saved.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NEGATIVE TEST CASES
    // Each test: fill form (with one field missing/invalid) → click Save & Close
    // → validation_msg spans appear under each required field → assert form open
    // → assert validation error visible → log the specific error message.
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 8, description = "TC_N01 – Submit form with all fields blank")
    public void testSubmitBlankForm() throws InterruptedException {
        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N01 FAIL – Form should stay open when all fields are blank.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N01 FAIL – Validation errors should be shown on required fields.");
        System.out.println("TC_N01 PASS – Blank form correctly blocked with validation errors.");
    }

    @Test(priority = 9, description = "TC_N02 – Submit without First Name")
    public void testMissingFirstName() throws InterruptedException {
        fillPatientForm("Mr.", null,
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N02 FAIL – Form should stay open when First Name is missing.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N02 FAIL – Validation error should appear on First Name field.");
        System.out.println("TC_N02 PASS – Error: " + getFieldValidationMessage("First Name"));
    }

    @Test(priority = 10, description = "TC_N03 – Submit without Last Name")
    public void testMissingLastName() throws InterruptedException {
        fillPatientForm("Mr.", TestDataGenerator.generateUniqueString(),
                null,
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N03 FAIL – Form should stay open when Last Name is missing.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N03 FAIL – Validation error should appear on Last Name field.");
        System.out.println("TC_N03 PASS – Error: " + getFieldValidationMessage("Last Name"));
    }

    @Test(priority = 11, description = "TC_N04 – Submit without Date of Birth")
    public void testMissingDOB() throws InterruptedException {
        fillPatientForm("Mr.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                null,
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N04 FAIL – Form should stay open when DOB is missing.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N04 FAIL – Validation error should appear on Date of Birth field.");
        System.out.println("TC_N04 PASS – Error: " + getFieldValidationMessage("datepicker-1"));
    }

    @Test(priority = 12, description = "TC_N05 – Submit without Phone Number")
    public void testMissingPhone() throws InterruptedException {
        fillPatientForm("Mr.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                null,
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N05 FAIL – Form should stay open when Phone is missing.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N05 FAIL – Validation error should appear on Phone Number field.");
        System.out.println("TC_N05 PASS – Error: " + getFieldValidationMessage("Primary Phone"));
    }

    @Test(priority = 13, description = "TC_N06 – Submit with invalid email format (no @)")
    public void testInvalidEmailFormat() throws InterruptedException {
        fillPatientForm("Mr.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                "invalidemail.com",
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N06 FAIL – Form should stay open for invalid email format.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N06 FAIL – Validation error should appear on Email field.");
        System.out.println("TC_N06 PASS – Error: " + getFieldValidationMessage("Email Address"));
    }

    @Test(priority = 14, description = "TC_N07 – Submit with email missing domain")
    public void testInvalidEmailMissingDomain() throws InterruptedException {
        fillPatientForm("Mr.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                "testuser@",
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N07 FAIL – Form should stay open for email missing domain.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N07 FAIL – Validation error should appear on Email field.");
        System.out.println("TC_N07 PASS – Error: " + getFieldValidationMessage("Email Address"));
    }

    @Test(priority = 15, description = "TC_N08 – Submit with phone number less than 10 digits")
    public void testShortPhoneNumber() throws InterruptedException {
        fillPatientForm("Mr.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                "12345",
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N08 FAIL – Form should stay open for short phone number.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N08 FAIL – Validation error should appear on Phone field.");
        System.out.println("TC_N08 PASS – Error: " + getFieldValidationMessage("Primary Phone"));
    }

    @Test(priority = 16, description = "TC_N09 – Submit with letters in phone number field")
    public void testAlphaInPhoneNumber() throws InterruptedException {
        fillPatientForm("Mr.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                "ABCDEFGHIJ",
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N09 FAIL – Form should stay open when letters entered in phone field.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N09 FAIL – Validation error should appear on Phone field.");
        System.out.println("TC_N09 PASS – Error: " + getFieldValidationMessage("Primary Phone"));
    }

    @Test(priority = 17, description = "TC_N10 – Submit with future date as Date of Birth")
    public void testFutureDOB() throws InterruptedException {
        // MM=12 DD=31 YYYY=2099 → December 31, 2099 (future date)
        fillPatientForm("Mr.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                "12312099",
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N10 FAIL – Form should stay open for future Date of Birth.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N10 FAIL – Validation error should appear on DOB field.");
        System.out.println("TC_N10 PASS – Error: " + getFieldValidationMessage("datepicker-1"));
    }

    @Test(priority = 18, description = "TC_N11 – Submit with numbers in First Name")
    public void testNumericFirstName() throws InterruptedException {
        fillPatientForm("Mr.", "12345",
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N11 FAIL – Form should stay open when numbers in First Name.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N11 FAIL – Validation error should appear on First Name field.");
        System.out.println("TC_N11 PASS – Error: " + getFieldValidationMessage("First Name"));
    }

    @Test(priority = 19, description = "TC_N12 – Submit with special characters in First Name")
    public void testSpecialCharFirstName() throws InterruptedException {
        fillPatientForm("Mr.", "@#$%^&*()",
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N12 FAIL – Form should stay open for special characters in First Name.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N12 FAIL – Validation error should appear on First Name field.");
        System.out.println("TC_N12 PASS – Error: " + getFieldValidationMessage("First Name"));
    }

    @Test(priority = 20, description = "TC_N13 – Submit without selecting Gender")
    public void testMissingGender() throws InterruptedException {
        fillPatientForm("Mr.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                null, "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N13 FAIL – Form should stay open when Gender is not selected.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N13 FAIL – Validation error should appear on Gender (at birth) field.");
        System.out.println("TC_N13 PASS – Missing Gender correctly blocked.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MINOR PATIENT – ADDRESS + PRIMARY CONTACT
    // When DOB makes the patient under 18, the form reveals an Address Information
    // section and a Primary Contact (guardian) section.
    //
    // Address Information fields (unique IDs — no scoping needed):
    //   id="Address Line 1", id="Address Line 2", id="City",
    //   ng-select[@bindlabel='stateName'], id="Zip Code"
    //
    // Primary Contact fields (shared IDs with patient — scoped via card header):
    //   PC card anchor: //div[contains(@class,'card') and .//h5[contains(text(),'Primary Contact')]]
    //   id="First Name", id="Last Name"  → scoped to PC card
    //   id="datepicker-2" (dynamic)      → placeholder='MM/DD/YYYY' scoped to PC card
    //   id="Email Address"               → scoped to PC card
    //   id="patientContactGender_Male/Female/Other"  → unique IDs (vs patientGender_*)
    //   id="Primary phone"  (lowercase p) → unique (vs patient's "Primary Phone")
    // ═══════════════════════════════════════════════════════════════════════════

    private static final String PC_CARD =
        "//div[contains(@class,'card') and .//h5[contains(text(),'Primary Contact')]]";

    /** Fill Address Information section fields. */
    private void fillAddressSection(String addr1, String addr2,
                                     String city, String state, String zip)
            throws InterruptedException {
        if (addr1 != null)
            driver.findElement(By.id("Address Line 1")).sendKeys(addr1);
        if (addr2 != null)
            driver.findElement(By.id("Address Line 2")).sendKeys(addr2);
        if (city != null)
            driver.findElement(By.id("City")).sendKeys(city);
        if (state != null) {
            // Open ng-select, then type to filter — the full list has 50+ states and
            // elementToBeClickable times out for off-screen options without filtering first
            WebElement stateSelect = driver.findElement(By.xpath("//ng-select[@bindlabel='stateName']"));
            stateSelect.click();
            Thread.sleep(400);
            // Type state name into ng-select's internal search input to filter
            stateSelect.findElement(By.tagName("input")).sendKeys(state);
            Thread.sleep(500);
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class,'ng-option')]//span[text()='" + state + "']")))
                .click();
            Thread.sleep(300);
        }
        if (zip != null)
            driver.findElement(By.id("Zip Code")).sendKeys(zip);
    }

    /**
     * Fill Primary Contact (guardian) section.
     * Fields with duplicate IDs are scoped to the PC card via PC_CARD ancestor XPath.
     * DOB format: MMDDYYYY (Kendo datepicker, single sendKeys call).
     */
    private void fillPrimaryContactSection(String firstName, String lastName,
                                            String dob, String email,
                                            String gender, String phone)
            throws InterruptedException {
        // Scroll PC section into view before interacting to avoid click interception
        WebElement pcCard = driver.findElement(By.xpath(PC_CARD));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", pcCard);
        Thread.sleep(500);

        if (firstName != null) {
            // Scope by PC card to avoid collision with patient's First Name field
            WebElement fn = driver.findElement(By.xpath(PC_CARD + "//input[@id='First Name']"));
            fn.clear();
            fn.sendKeys(firstName);
        }

        if (lastName != null) {
            WebElement ln = driver.findElement(By.xpath(PC_CARD + "//input[@id='Last Name']"));
            ln.clear();
            ln.sendKeys(lastName);
        }

        if (dob != null) {
            // PC DOB datepicker ID is dynamic — find the last MM/DD/YYYY input on page.
            // patient DOB is always first; PC DOB is always last.
            List<WebElement> datepickers = driver.findElements(
                    By.xpath("//input[@placeholder='MM/DD/YYYY']"));
            WebElement pcDob = datepickers.get(datepickers.size() - 1);
            // Scroll field to center, then nudge up to clear any fixed top navbar
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", pcDob);
            Thread.sleep(500);
            js.executeScript("window.scrollBy(0, -80);");
            Thread.sleep(300);
            // Click to focus, then HOME to force cursor to MM segment before typing.
            // Without HOME, center-click lands on YYYY causing month/day/YYYY result.
            wait.until(ExpectedConditions.elementToBeClickable(pcDob));
            pcDob.click();
            Thread.sleep(400);
            pcDob.sendKeys(Keys.HOME);
            Thread.sleep(200);
            // Send all 8 digits — Kendo auto-advances MM → DD → YYYY
            pcDob.sendKeys(dob);
            Thread.sleep(300);
            pcDob.sendKeys(Keys.TAB);
            Thread.sleep(500);
        }

        if (email != null) {
            WebElement em = driver.findElement(By.xpath(PC_CARD + "//input[@id='Email Address']"));
            em.clear();
            em.sendKeys(email);
        }

        if (gender != null) {
            WebElement genderEl = driver.findElement(By.xpath(
                    "//input[@id='patientContactGender_" + gender + "']"));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", genderEl);
            Thread.sleep(200);
            js.executeScript("arguments[0].click();", genderEl);
        }

        if (phone != null) {
            // Try id="Primary phone" (lowercase p); fall back to label-scoped XPath in PC card
            List<WebElement> phoneFields = driver.findElements(By.id("Primary phone"));
            WebElement ph;
            if (!phoneFields.isEmpty()) {
                ph = phoneFields.get(0);
            } else {
                ph = driver.findElement(By.xpath(PC_CARD + "//input[contains(@id,'phone') or contains(@id,'Phone')]"));
            }
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", ph);
            Thread.sleep(200);
            ph.clear();
            ph.sendKeys(phone);
        }
    }

    @Test(priority = 22,
          description = "TC_P08 – Create minor patient (DOB < 18) with Address Info and Primary Contact details")
    public void testCreateMinorPatientWithAddressAndPrimaryContact()
            throws InterruptedException {

        // ── Patient demographics (DOB = 03/15/2010 → age ~16, under 18) ─────
        fillPatientForm("Mr.", TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                "03152010",   // MMDDYYYY — minor DOB triggers address & primary contact sections
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        // ── Address Information section ───────────────────────────────────────
        fillAddressSection(
                "123 Minor Test Street",  // Address Line 1
                "Apt 4B",                 // Address Line 2
                "Chicago",                // City
                "Illinois",               // State (ng-select option text)
                "60601");                 // Zip Code

        // ── Primary Contact section (parent / guardian) ───────────────────────
        fillPrimaryContactSection(
                TestDataGenerator.generateUniqueString(),   // PC First Name
                TestDataGenerator.generateUniqueString(),   // PC Last Name
                "05201980",                                  // PC DOB — parent (MMDDYYYY)
                TestDataGenerator.generateUniqueEmail(),    // PC Email Address
                "Female",                                    // PC Gender
                TestDataGenerator.generatePhoneNumber());   // PC Primary phone

        clickSave();
        Thread.sleep(3000);
        dismissErrorDialog();

        Assert.assertFalse(isFormStillOpen(),
                "TC_P08 FAIL – Form should close after saving minor patient with " +
                "Address Information and Primary Contact details.");
        System.out.println("TC_P08 PASS – Minor patient with Address Info and Primary Contact saved successfully.");
    }

    @Test(priority = 21, description = "TC_N14 – Submit with whitespace-only First Name")
    public void testWhitespaceFirstName() throws InterruptedException {
        fillPatientForm("Mr.", "     ",
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateDOB(),
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        clickSave();
        Thread.sleep(2000);

        Assert.assertTrue(isFormStillOpen(),
                "TC_N14 FAIL – Form should stay open for whitespace-only First Name.");
        Assert.assertTrue(isValidationErrorVisible(),
                "TC_N14 FAIL – Validation error should appear on First Name field.");
        System.out.println("TC_N14 PASS – Error: " + getFieldValidationMessage("First Name"));
    }
}
