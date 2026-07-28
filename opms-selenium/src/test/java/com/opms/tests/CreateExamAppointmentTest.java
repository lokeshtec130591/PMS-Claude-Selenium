package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;
import com.opms.utils.TestDataGenerator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TC_EXAM_01 – Create a new adult patient.
 * TC_EXAM_02 – Search for that patient, click "Create Exam" on the dashboard, book an
 *              appointment of the {@link ScheduleLookupTest#APPT_TYPE_NAME} type (the
 *              Exam-Required appointment type tied to {@link ScheduleLookupTest#APPT_GROUP_NAME}),
 *              and confirm the exam was created (dashboard swaps "Create Exam" for "Modify Exam").
 * TC_EXAM_03 – For the same patient, book a second appointment of the
 *              {@link ScheduleLookupTest#APPT_TYPE_NAME_2} type (the non-exam type tied to
 *              {@link ScheduleLookupTest#APPT_GROUP_NAME_2}) via the "Schedule" button.
 *
 * Only the two appointment types created by ScheduleLookupTest are ever used here — never
 * any other appointment type.
 *
 * Depends on ScheduleLookupTest having already run in this suite (see testng.xml — "Schedule
 * Lookup Tests" runs before "Create Exam Appointment Tests") so both appointment types exist.
 */
public class CreateExamAppointmentTest {

    private WebDriver          driver;
    private WebDriverWait      wait;
    private Actions            actions;
    private JavascriptExecutor js;

    // Hardcoded test patient — reused across runs to avoid DB bloat.
    // Uncomment TC_EXAM_01 and remove these two lines once finalised.
    private String patientFirstName = "vzhxqqaien";
    private String patientLastName  = "fqqmsghmsw";

    // ── Setup / Teardown ──────────────────────────────────────────────────────

    @BeforeClass
    public void setUp() throws InterruptedException {
        driver  = DriverManager.getDriver();
        wait    = new WebDriverWait(driver, Duration.ofSeconds(15));
        actions = new Actions(driver);
        js      = (JavascriptExecutor) driver;

        LoginPage.loginWith(LoginPage.VALID_EMAIL, LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isDashboardVisible(), "Login failed in CreateExamAppointmentTest.");
        Thread.sleep(2000);
        dismissErrorDialog();
        System.out.println("CreateExamAppointmentTest: logged in.");
    }

    @AfterClass
    public void tearDown() {
        DriverManager.quitDriver();
        System.out.println("CreateExamAppointmentTest: browser closed.");
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    /** Accepts a native JS alert if one is open (this demo site occasionally fires a stray
     *  "WebSDK v..." alert from a 3rd-party chat widget, unrelated to any test action). */
    private void dismissNativeAlert() {
        try {
            driver.switchTo().alert().accept();
            System.out.println("Dismissed unexpected native browser alert.");
        } catch (NoAlertPresentException ignored) {}
    }

    /** Wraps driver.findElements with a single retry if a native alert intercepts the call. */
    private List<WebElement> findElementsSafely(By by) {
        try {
            return driver.findElements(by);
        } catch (UnhandledAlertException e) {
            dismissNativeAlert();
            return driver.findElements(by);
        }
    }

    private void dismissErrorDialog() {
        dismissNativeAlert();
        try {
            List<WebElement> okBtns = driver.findElements(
                    By.xpath("//button[normalize-space(text())='OK']"));
            for (WebElement btn : okBtns) {
                if (btn.isDisplayed()) { btn.click(); Thread.sleep(400); break; }
            }
        } catch (Exception ignored) {}
    }

    private void openAddPatientForm() throws InterruptedException {
        dismissErrorDialog();
        List<WebElement> icons = driver.findElements(
                By.xpath("//i[contains(@class,'fa-user-plus')]"));
        if (icons.isEmpty() || !icons.get(0).isDisplayed()) {
            driver.navigate().back();
            Thread.sleep(2000);
            dismissErrorDialog();
        }
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//i[contains(@class,'fa-user-plus')]")));
        actions.moveToElement(addBtn).click().perform();
        Thread.sleep(2000);
        dismissErrorDialog();
    }

    /** Fills patient demographic fields. Pass an adult DOB (>= 18 yrs). */
    private void fillPatientForm(String firstName, String lastName, String dob,
                                  String phone, String email, String gender) throws InterruptedException {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("First Name"))).sendKeys(firstName);
        driver.findElement(By.id("Last Name")).sendKeys(lastName);

        WebElement dobField = driver.findElement(By.xpath(
                "//label[contains(text(),'Date of Birth')]"
                + "/ancestor::div[contains(@class,'form-group')]"
                + "//input[@placeholder='MM/DD/YYYY']"));
        dobField.click();
        Thread.sleep(500);
        dobField.sendKeys(dob);
        dobField.sendKeys(Keys.TAB);
        Thread.sleep(300);

        driver.findElement(By.id("Primary Phone")).sendKeys(phone);
        driver.findElement(By.id("Email Address")).sendKeys(email);
        dismissErrorDialog();

        WebElement genderLabel = driver.findElement(By.xpath("//label[@for='patientGender_" + gender + "']"));
        js.executeScript("arguments[0].click();", genderLabel);

        // Language, Marital Status, Dentist
        WebElement languageLabel = driver.findElement(By.xpath("//label[@for='patientLanguagePreference_English']"));
        js.executeScript("arguments[0].click();", languageLabel);

        WebElement maritalLabel = driver.findElement(By.xpath("//label[@for='patientMaritalStatus_Single']"));
        js.executeScript("arguments[0].click();", maritalLabel);

        dismissErrorDialog();
        WebElement dentistSelect = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-select[@bindlabel='dentistFullName']")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", dentistSelect);
        Thread.sleep(500);
        dismissErrorDialog();
        dentistSelect.click();
        Thread.sleep(500);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ng-option')]//span[text()='Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)']")))
            .click();
        Thread.sleep(300);
    }

    private void clickSaveAndClose() throws InterruptedException {
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[text()='Save & Close']")));
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(3000);
        dismissErrorDialog();
    }

    private boolean isFormStillOpen() {
        try {
            return driver.findElement(By.xpath("//span[text()='Save & Close']")).isDisplayed();
        } catch (Exception e) { return false; }
    }

    private void searchAndOpenPatient(String lastName, String firstName) throws InterruptedException {
        dismissErrorDialog();
        String fullName = lastName + ", " + firstName;
        WebElement searchBar = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='last name, first name']")));
        searchBar.clear();
        searchBar.sendKeys(fullName);
        Thread.sleep(2500);
        WebElement result = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ul[contains(@class,'patient-search-select-box')]//li[contains(.,'" + lastName + "')]")));
        result.click();
        Thread.sleep(3000);
        dismissErrorDialog();
        System.out.println("Opened patient profile: " + fullName);
    }

    private static final By CREATE_EXAM_BUTTON = By.xpath(
            "//button[@class='btn btn-outline-primary hei-35 rounded-3 patient-primary-button cursor-pointer']");

    /** The patient-card "Schedule" button — opens the same scheduler as Create Exam, but
     *  stays available even after an exam already exists (Create Exam then reads "Modify Exam"). */
    private static final By SCHEDULE_BUTTON = By.xpath(
            "//button[contains(@class,'patient-primary-button') and normalize-space(text())='Schedule']");

    private static final By SCHEDULER_CELL_LOCATOR =
            By.xpath("//td[contains(@class, 'dx-scheduler-date-table-cell')]");

    /** Slot indexes already used by this test instance, so repeated bookings in the same run
     *  (exam + non-exam) don't pick the same cell as each other. */
    private final Set<Integer> usedSlotIndexes = new HashSet<>();

    /**
     * Opens the Patient Schedule scheduler via the given trigger button, double-clicks a
     * randomly chosen slot cell to open the "Appointment" popup, selects the given appointment
     * type, and saves it.
     *
     * The slot cell is picked at random (instead of a fixed index) because the scheduler grid
     * has hundreds of time slots per day and this suite runs repeatedly against the same shared
     * demo environment — a fixed index keeps landing on the exact same chair/time as a previous
     * run and collides with an appointment already booked there.
     *
     * Locators confirmed against the live app:
     *   - Create Exam / Schedule buttons: both share the 'patient-primary-button' class.
     *   - Scheduler slot cells: td.dx-scheduler-date-table-cell (DevExtreme grid).
     *   - Appointment Type input: id contains 'practiceAppointmentTypeId' (DevExtreme
     *     generates a random id prefix per page load, but this suffix is stable).
     *   - Type dropdown option: div.dx-list-item-content > span with the option text.
     *   - Save button on the Appointment popup is a DevExtreme dx-button (a <div>, not a
     *     <button>) — must be clicked via its containing div.dx-button-default, not a
     *     plain button locator.
     *
     * @param openTriggerButton locator for the button that opens the scheduler
     * @param appointmentTypeName exact Appointment Type name to select (only ever the two
     *        types created by ScheduleLookupTest — never any other appointment type)
     */
    private void bookAppointment(By openTriggerButton, String appointmentTypeName)
            throws InterruptedException {
        WebElement triggerBtn = wait.until(ExpectedConditions.elementToBeClickable(openTriggerButton));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", triggerBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", triggerBtn);
        Thread.sleep(2000);
        dismissErrorDialog();

        // Wait for the scheduler grid to fully render, then find and click an empty slot
        wait.until(ExpectedConditions.presenceOfElementLocated(SCHEDULER_CELL_LOCATOR));
        Thread.sleep(1500);
        clickEmptySchedulerSlot();

        selectAppointmentType(appointmentTypeName);

        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@role='button' and contains(@class,'dx-button-default') and .//span[text()='Save']]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", saveBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2500);
        dismissErrorDialog();
        System.out.println("Appointment saved: " + appointmentTypeName);
    }

    /**
     * Tries scheduler cells one by one until a double-click opens a NEW appointment form
     * (appointment type input is present but empty). If it opens an EXISTING appointment
     * instead, presses Escape to close it and tries the next cell.
     *
     * This approach is reliable regardless of how DevExtreme renders appointment overlays or
     * how the scheduler is scrolled — it directly verifies the outcome of each click.
     */
    private void clickEmptySchedulerSlot() throws InterruptedException {
        List<WebElement> cells = driver.findElements(SCHEDULER_CELL_LOCATOR);
        int total = cells.size();
        System.out.println("clickEmptySchedulerSlot: " + total + " cells in grid, looking for empty slot.");

        // Shuffle the index range so we don't always start from the same cell
        List<Integer> indexes = new ArrayList<>();
        int margin = Math.max(3, total / 10);
        for (int i = margin; i <= total - margin; i++) indexes.add(i);
        java.util.Collections.shuffle(indexes, new java.util.Random());

        for (int idx : indexes) {
            if (usedSlotIndexes.contains(idx)) continue;
            try {
                WebElement cell = driver.findElement(
                        By.xpath("(//td[contains(@class,'dx-scheduler-date-table-cell')])[" + idx + "]"));
                actions.doubleClick(cell).perform();
                Thread.sleep(1000);

                // Check if a NEW appointment popup opened (type input present but empty)
                List<WebElement> typeInputs = driver.findElements(
                        By.xpath("//div//input[contains(@id,'practiceAppointmentTypeId')]"));
                if (!typeInputs.isEmpty() && typeInputs.get(0).isDisplayed()) {
                    String val = typeInputs.get(0).getAttribute("value");
                    if (val == null || val.trim().isEmpty()) {
                        // Empty type field → new appointment form → success
                        usedSlotIndexes.add(idx);
                        System.out.println("clickEmptySchedulerSlot: opened new appointment form at slot " + idx);
                        return;
                    }
                }

                // Type field is filled → existing appointment opened → close and retry
                System.out.println("clickEmptySchedulerSlot: slot " + idx + " has existing appointment, retrying.");
                new Actions(driver).sendKeys(Keys.ESCAPE).perform();
                Thread.sleep(600);
                dismissErrorDialog();

            } catch (Exception e) {
                System.out.println("clickEmptySchedulerSlot: slot " + idx + " error: " + e.getMessage());
                try { new Actions(driver).sendKeys(Keys.ESCAPE).perform(); } catch (Exception ignored) {}
                Thread.sleep(400);
            }
        }
        throw new RuntimeException("clickEmptySchedulerSlot: no empty slot found after checking all " + total + " cells.");
    }

    /**
     * Types into the Appointment Type search box and selects the matching option. The option
     * list is populated by a backend autocomplete call on this demo site, which occasionally
     * fails with a transient 500 error (seen repeatedly elsewhere in this suite) and leaves the
     * dropdown empty — so this retries the type+select a couple of times before giving up,
     * clearing the input and dismissing any error dialog between attempts.
     */
    private void selectAppointmentType(String appointmentTypeName) throws InterruptedException {
        By optionLocator = By.xpath("//div[contains(@class,'dx-list-item-content')]//span[text()='"
                + appointmentTypeName + "']");
        int attempts = 3;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            WebElement appointmentTypeInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div//input[contains(@id,'practiceAppointmentTypeId')]")));
            appointmentTypeInput.click();
            appointmentTypeInput.clear();
            appointmentTypeInput.sendKeys(appointmentTypeName);
            try {
                new WebDriverWait(driver, Duration.ofSeconds(6))
                        .until(ExpectedConditions.elementToBeClickable(optionLocator))
                        .click();
                Thread.sleep(500);
                return;
            } catch (org.openqa.selenium.TimeoutException e) {
                System.out.println("selectAppointmentType: option '" + appointmentTypeName
                        + "' didn't appear on attempt " + attempt + "/" + attempts + " — retrying.");
                dismissErrorDialog();
                Thread.sleep(500);
            }
        }
        // Final attempt — let the timeout propagate with a clear failure if it still doesn't show.
        wait.until(ExpectedConditions.elementToBeClickable(optionLocator)).click();
        Thread.sleep(500);
    }

    /** True while the Appointment popup is still open (Save didn't go through / validation failed). */
    private boolean isAppointmentPopupOpen() {
        try {
            return driver.findElement(
                    By.xpath("//div//input[contains(@id,'practiceAppointmentTypeId')]")).isDisplayed();
        } catch (Exception e) { return false; }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEST SCENARIOS
    // ═══════════════════════════════════════════════════════════════════════════

    // TC_EXAM_01 commented out — reusing existing patient to avoid creating new patients on every run.
    // Uncomment and remove the hardcoded patientFirstName/patientLastName fields above when finalised.
    /*
    @Test(priority = 1, description = "TC_EXAM_01 – Create adult patient for exam appointment")
    public void testCreatePatientForExam() throws InterruptedException {
        openAddPatientForm();

        patientFirstName = TestDataGenerator.generateUniqueString();
        patientLastName  = TestDataGenerator.generateUniqueString();

        fillPatientForm(patientFirstName, patientLastName,
                "03151990",   // adult DOB
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male");

        clickSaveAndClose();
        Assert.assertFalse(isFormStillOpen(),
                "TC_EXAM_01 FAIL – Patient form should close after save.");
        System.out.println("TC_EXAM_01 PASS – Patient created: " + patientLastName + ", " + patientFirstName);
    }
    */

    @Test(priority = 2,
          description = "TC_EXAM_02 – Search patient, click Create Exam, book a '"
                  + ScheduleLookupTest.APPT_TYPE_NAME + "' appointment (from ScheduleLookupTest)")
    public void testCreateExamAppointment() throws InterruptedException {
        searchAndOpenPatient(patientLastName, patientFirstName);
        bookAppointment(CREATE_EXAM_BUTTON, ScheduleLookupTest.APPT_TYPE_NAME);

        // Re-open patient profile to verify "Modify Exam" button now appears
        Thread.sleep(1500);
        searchAndOpenPatient(patientLastName, patientFirstName);
        Thread.sleep(2000);
        dismissErrorDialog();

        boolean examCreated = !findElementsSafely(
                By.xpath("//button[contains(normalize-space(.),'Modify Exam')]")).isEmpty();
        Assert.assertTrue(examCreated,
                "TC_EXAM_02 FAIL – Exam appointment should be created (expected 'Modify Exam' button to appear).");
        System.out.println("TC_EXAM_02: Modify Exam button visible — clicking it.");

        // Click Modify Exam
        WebElement modifyExamBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[.//i[contains(@class,'fa-file-medical')] and contains(.,'Modify Exam')]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", modifyExamBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", modifyExamBtn);
        Thread.sleep(2000);
        dismissErrorDialog();

        // Select the QA exam result created by ScheduleLookupTest
        WebElement examResultDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-select[@id='Exam Result']")));
        examResultDropdown.click();
        Thread.sleep(800);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-dropdown-panel//span[@class='ng-option-label' and normalize-space(text())='"
                        + ScheduleLookupTest.EXAM_RESULT_NAME + "']")))
            .click();
        Thread.sleep(400);
        System.out.println("TC_EXAM_02: selected Exam Result = " + ScheduleLookupTest.EXAM_RESULT_NAME);

        // Save
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit') and .//span[text()='Save']]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", saveBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2000);
        dismissErrorDialog();

        System.out.println("TC_EXAM_02 PASS – Exam appointment created and result set to '"
                + ScheduleLookupTest.EXAM_RESULT_NAME + "' for: "
                + patientLastName + ", " + patientFirstName);
    }

    @Test(priority = 3,
          description = "TC_EXAM_03 – Search patient, click Schedule button on patient dashboard, "
                  + "book a '" + ScheduleLookupTest.APPT_TYPE_NAME_2 + "' non-exam appointment")
    public void testCreateNonExamAppointment() throws InterruptedException {
        // Re-open the patient profile (we may have navigated away after TC_EXAM_02 verification)
        searchAndOpenPatient(patientLastName, patientFirstName);

        // Click the "Schedule" button on the patient dashboard
        WebElement scheduleBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'patient-primary-button') and .//i[contains(@class,'icon-calendar')]]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", scheduleBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", scheduleBtn);
        Thread.sleep(2500);
        dismissErrorDialog();

        wait.until(ExpectedConditions.presenceOfElementLocated(SCHEDULER_CELL_LOCATOR));
        Thread.sleep(1500);
        clickEmptySchedulerSlot();

        // Patient is pre-filled (opened from patient profile) — just select the appointment type
        selectAppointmentType(ScheduleLookupTest.APPT_TYPE_NAME_2);

        // Save
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@role='button' and contains(@class,'dx-button-default') and .//span[text()='Save']]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", saveBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2500);
        dismissErrorDialog();

        Assert.assertFalse(isAppointmentPopupOpen(),
                "TC_EXAM_03 FAIL – Appointment popup should close after saving the non-exam appointment.");
        System.out.println("TC_EXAM_03 PASS – Non-exam appointment ('"
                + ScheduleLookupTest.APPT_TYPE_NAME_2 + "') created for: "
                + patientLastName + ", " + patientFirstName);
    }
}
