package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;
import com.opms.utils.TestDataGenerator;

import java.time.Duration;
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

    private String patientFirstName;
    private String patientLastName;

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

        // Wait for the Patient Schedule scheduler to render, then double-click a random slot
        // cell to open the "Appointment" popup (pre-fills patient, doctor, date/time, position).
        wait.until(ExpectedConditions.presenceOfElementLocated(SCHEDULER_CELL_LOCATOR));
        Thread.sleep(1000);
        int totalCells = driver.findElements(SCHEDULER_CELL_LOCATOR).size();
        int slotIndex = pickRandomSlotIndex(totalCells);
        WebElement slotCell = driver.findElement(
                By.xpath("(//td[contains(@class, 'dx-scheduler-date-table-cell')])[" + slotIndex + "]"));
        System.out.println("Booking '" + appointmentTypeName + "' on slot " + slotIndex + " of " + totalCells);
        actions.doubleClick(slotCell).perform();
        Thread.sleep(1500);
        dismissErrorDialog();

        selectAppointmentType(appointmentTypeName);

        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'dx-button-default')]//span[text()='Save']")));
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2500);
        dismissErrorDialog();
        System.out.println("Appointment saved: " + appointmentTypeName);
    }

    /**
     * Picks a random 1-based cell index, staying away from the first/last few rows (which fall
     * outside typical business hours) and avoiding any index already used earlier in this test
     * instance so back-to-back bookings don't target the same cell as each other.
     */
    private int pickRandomSlotIndex(int totalCells) {
        int margin = Math.max(5, totalCells / 20);
        int low = margin;
        int high = Math.max(low + 1, totalCells - margin);
        int index;
        int attempts = 0;
        do {
            index = ThreadLocalRandom.current().nextInt(low, high);
            attempts++;
        } while (usedSlotIndexes.contains(index) && attempts < 20);
        usedSlotIndexes.add(index);
        return index;
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

    @Test(priority = 2,
          description = "TC_EXAM_02 – Search patient, click Create Exam, book a '"
                  + ScheduleLookupTest.APPT_TYPE_NAME + "' appointment (from ScheduleLookupTest)",
          dependsOnMethods = "testCreatePatientForExam")
    public void testCreateExamAppointment() throws InterruptedException {
        searchAndOpenPatient(patientLastName, patientFirstName);
        bookAppointment(CREATE_EXAM_BUTTON, ScheduleLookupTest.APPT_TYPE_NAME);

        boolean examCreated = !findElementsSafely(
                By.xpath("//button[contains(normalize-space(.),'Modify Exam')]")).isEmpty();
        Assert.assertTrue(examCreated,
                "TC_EXAM_02 FAIL – Exam appointment should be created (expected 'Modify Exam' button to appear).");
        System.out.println("TC_EXAM_02 PASS – Exam appointment created for: "
                + patientLastName + ", " + patientFirstName);
    }

    @Test(priority = 3,
          description = "TC_EXAM_03 – Book a '" + ScheduleLookupTest.APPT_TYPE_NAME_2
                  + "' (non-exam) appointment for the same patient via the Schedule button "
                  + "(from ScheduleLookupTest)",
          dependsOnMethods = "testCreateExamAppointment")
    public void testCreateNonExamAppointment() throws InterruptedException {
        // Create Exam has already become "Modify Exam" by this point, so the general
        // "Schedule" button is used to open the same scheduler for a second appointment.
        bookAppointment(SCHEDULE_BUTTON, ScheduleLookupTest.APPT_TYPE_NAME_2);

        Assert.assertFalse(isAppointmentPopupOpen(),
                "TC_EXAM_03 FAIL – Appointment popup should close after saving the non-exam appointment.");
        System.out.println("TC_EXAM_03 PASS – Non-exam appointment ('" + ScheduleLookupTest.APPT_TYPE_NAME_2
                + "') created for: " + patientLastName + ", " + patientFirstName);
    }
}
