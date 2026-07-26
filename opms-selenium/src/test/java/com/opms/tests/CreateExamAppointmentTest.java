package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;
import com.opms.utils.TestDataGenerator;

import java.time.Duration;
import java.util.List;

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
 * TC_EXAM_02 – Search for that patient, click "Create Exam" on the dashboard, book a
 *              "New Patient Exam" appointment on the scheduler, and confirm the exam
 *              was created (dashboard swaps "Create Exam" for "Modify Exam").
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

    /**
     * Clicks "Create Exam" on the patient dashboard, books a "New Patient Exam" appointment
     * on the scheduler that opens, and saves it.
     *
     * Locators confirmed against the live app:
     *   - Create Exam button: exact class 'btn btn-outline-primary hei-35 rounded-3
     *     patient-primary-button cursor-pointer' (provided/confirmed working).
     *   - Scheduler slot cells: td.dx-scheduler-date-table-cell (DevExtreme grid).
     *   - Appointment Type input: id contains 'practiceAppointmentTypeId' (DevExtreme
     *     generates a random id prefix per page load, but this suffix is stable).
     *   - Type dropdown option: div.dx-list-item-content > span with the option text.
     *   - Save button on the Appointment popup is a DevExtreme dx-button (a <div>, not a
     *     <button>) — must be clicked via its containing div.dx-button-default, not a
     *     plain button locator.
     */
    private void createExamAppointment() throws InterruptedException {
        WebElement createExamBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@class='btn btn-outline-primary hei-35 rounded-3 patient-primary-button cursor-pointer']")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", createExamBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", createExamBtn);
        Thread.sleep(2000);
        dismissErrorDialog();

        // Wait for the Patient Schedule scheduler to render, then double-click a slot cell
        // to open the "Appointment" popup (pre-fills patient, doctor, date/time, position).
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(@class,'dx-scheduler-date-table-cell')]")));
        Thread.sleep(1000);
        WebElement slotCell = driver.findElement(
                By.xpath("(//td[contains(@class, 'dx-scheduler-date-table-cell')])[5]"));
        actions.doubleClick(slotCell).perform();
        Thread.sleep(1500);
        dismissErrorDialog();

        WebElement appointmentTypeInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div//input[contains(@id,'practiceAppointmentTypeId')]")));
        appointmentTypeInput.click();
        appointmentTypeInput.sendKeys("New Patient Exam");
        Thread.sleep(800);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'dx-list-item-content')]//span[text()='New Patient Exam']")))
            .click();
        Thread.sleep(500);

        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'dx-button-default')]//span[text()='Save']")));
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2500);
        dismissErrorDialog();
        System.out.println("Exam appointment saved.");
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
          description = "TC_EXAM_02 – Search patient, click Create Exam, book a New Patient Exam appointment",
          dependsOnMethods = "testCreatePatientForExam")
    public void testCreateExamAppointment() throws InterruptedException {
        searchAndOpenPatient(patientLastName, patientFirstName);
        createExamAppointment();

        boolean examCreated = !findElementsSafely(
                By.xpath("//button[contains(normalize-space(.),'Modify Exam')]")).isEmpty();
        Assert.assertTrue(examCreated,
                "TC_EXAM_02 FAIL – Exam appointment should be created (expected 'Modify Exam' button to appear).");
        System.out.println("TC_EXAM_02 PASS – Exam appointment created for: "
                + patientLastName + ", " + patientFirstName);
    }
}
