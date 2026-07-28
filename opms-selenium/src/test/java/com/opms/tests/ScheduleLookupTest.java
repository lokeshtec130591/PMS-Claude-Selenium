package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
 * Tests for Practice Configuration → Schedule → Appointment Group and Appointment Type lookups.
 *
 * Navigation flow:
 *   Login → Practice Configuration → Schedule → Appointment Group
 *   Login → Practice Configuration → Schedule → Appointment Type
 */
public class ScheduleLookupTest {

    // Package-visible (not private) so other test classes, e.g. CreateExamAppointmentTest,
    // can reference the exact Appointment Group / Type this class creates instead of
    // duplicating the literal names.
    static final String APPT_GROUP_NAME         = "QA Exam Appt";
    static final String APPT_GROUP_DURATION      = "15";
    static final String APPT_GROUP_NAME_2        = "QA Non Exam Appt";
    static final String APPT_GROUP_DURATION_2    = "20";
    static final String APPT_TYPE_NAME           = "QA Exam Appointment Type";
    static final String APPT_TYPE_NAME_2         = "QA Non Exam Appointment Type";
    static final String EXAM_RESULT_NAME         = "QA Treatment Recommended Exam";
    static final String EXAM_RESULT_CATEGORY     = "Tx Recommended";

    private WebDriver          driver;
    private WebDriverWait      wait;
    private Actions            actions;
    private JavascriptExecutor js;

    private static final String PCODED_MTEXT =
        "//span[@class='pcoded-mtext d-flex justify-content-center text-wrap text-center lh-sm' and text()='%s']";

    // ── One-time setup ────────────────────────────────────────────────────────

    @BeforeClass
    public void setUpSuite() throws InterruptedException {
        driver  = DriverManager.getDriver();
        wait    = new WebDriverWait(driver, Duration.ofSeconds(15));
        actions = new Actions(driver);
        js      = (JavascriptExecutor) driver;

        LoginPage.loginWith(LoginPage.VALID_EMAIL, LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isDashboardVisible(), "Login failed in ScheduleLookupTest setup.");
        Thread.sleep(2000);
        dismissErrorDialog();
        System.out.println("ScheduleLookupTest: logged in successfully.");
    }

    @AfterClass
    public void tearDownSuite() {
        DriverManager.quitDriver();
        System.out.println("ScheduleLookupTest: browser closed.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void dismissErrorDialog() {
        try {
            List<WebElement> okBtns = driver.findElements(
                    By.xpath("//button[normalize-space(text())='OK']"));
            for (WebElement btn : okBtns) {
                if (btn.isDisplayed()) { btn.click(); Thread.sleep(400); break; }
            }
        } catch (Exception ignored) {}
    }

    private void dismissSuccessPopup() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'swal2-confirm')] | //button[normalize-space(text())='OK']"))).click();
            Thread.sleep(500);
        } catch (Exception ignored) {}
    }

    /**
     * Navigates: Practice Configuration → Schedule (expand) → sub-section (e.g. "Appointment Group")
     */
    private void navigateToScheduleSection(String subSection) throws InterruptedException {
        dismissErrorDialog();
        js.executeScript("window.scrollTo(0, 0);");
        Thread.sleep(300);

        // Open Practice Configuration
        WebElement practiceLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@title,'Practice configuration')]")));
        js.executeScript("arguments[0].click();", practiceLink);
        Thread.sleep(2000);
        dismissErrorDialog();

        // Click "Schedule" menu item to expand it
        WebElement scheduleMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath(String.format(PCODED_MTEXT, "Schedule"))));
        js.executeScript("arguments[0].click();", scheduleMenu);
        Thread.sleep(1500);

        // Click the sub-section link by href pattern (subSection maps to href keyword)
        String hrefKeyword = subSection.toLowerCase().replace(" ", "");
        WebElement subMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'" + hrefKeyword + "')]")));
        js.executeScript("arguments[0].click();", subMenu);
        Thread.sleep(2000);
        dismissErrorDialog();

        System.out.println("ScheduleLookupTest: navigated to Practice Config → Schedule → " + subSection);
    }

    private boolean isErrorVisible() {
        try {
            List<WebElement> errors = driver.findElements(By.xpath(
                "//div[contains(@class,'validation_msg')]//span[normalize-space(text())!='']"
                + " | //div[contains(@class,'alert-danger') and normalize-space(text())!='']"
                + " | //div[contains(@class,'toast-error')]"
                + " | //div[contains(@class,'invalid-feedback') and normalize-space(text())!='']"));
            for (WebElement e : errors) if (e.isDisplayed()) return true;
        } catch (Exception ignored) {}
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // APPOINTMENT GROUP TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 1,
          description = "TC_SL_P01 – Create Appointment Group 'QA Exam Appt' if not already exist")
    public void testCreateAppointmentGroup() throws InterruptedException {
        navigateToScheduleSection("Appointment Group");

        // Search for the group to check if it already exists
        try {
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@placeholder='search']")));
            searchBox.clear();
            searchBox.sendKeys(APPT_GROUP_NAME);
            Thread.sleep(1500);

            // Check if a row with this name already exists
            List<WebElement> existingRows = driver.findElements(
                    By.xpath("//td[normalize-space(text())='" + APPT_GROUP_NAME + "']"
                           + " | //div[contains(@class,'list-item') and normalize-space(text())='" + APPT_GROUP_NAME + "']"
                           + " | //span[normalize-space(text())='" + APPT_GROUP_NAME + "']"));
            if (!existingRows.isEmpty()) {
                System.out.println("TC_SL_P01 SKIP – Appointment Group '" + APPT_GROUP_NAME + "' already exists.");
                return;
            }
            searchBox.clear();
            Thread.sleep(500);
        } catch (Exception ignored) {}

        // Click Add New button
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Add New') or contains(.,'Add Group') or contains(.,'New')]"
                       + " | //button[.//i[contains(@class,'fa-plus')]]")));
        js.executeScript("arguments[0].click();", addBtn);
        Thread.sleep(1500);
        dismissErrorDialog();

        // Fill Group Name
        WebElement groupNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Appointment Group Name']")));
        groupNameField.clear();
        groupNameField.sendKeys(APPT_GROUP_NAME);
        Thread.sleep(300);

        // Select Appointment Group Duration = 15
        WebElement durationDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-select[@id='Appointment Group Duration']")));
        durationDropdown.click();
        Thread.sleep(800);
        // Click the option directly inside the open dropdown panel
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-dropdown-panel//span[@class='ng-option-label' and normalize-space(text())='" + APPT_GROUP_DURATION + "']")))
            .click();
        Thread.sleep(300);
        System.out.println("ScheduleLookupTest: selected Duration = " + APPT_GROUP_DURATION);

        // Enable Exam Required checkbox
        try {
            WebElement examCheckbox = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//input[@id='checkbox-fill-1-s' and @formcontrolname='isExamRequired']")));
            if (!examCheckbox.isSelected()) {
                js.executeScript("arguments[0].click();", examCheckbox);
                Thread.sleep(300);
                System.out.println("ScheduleLookupTest: checked Exam Required.");
            } else {
                System.out.println("ScheduleLookupTest: Exam Required already checked.");
            }
        } catch (Exception e) {
            System.out.println("ScheduleLookupTest: Exam Required checkbox not found: " + e.getMessage());
        }

        // Save
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]"
                       + " | //button[normalize-space(text())='Save']"
                       + " | //button[contains(.,'Save')]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", saveBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2000);
        dismissSuccessPopup();
        dismissErrorDialog();

        Assert.assertFalse(isErrorVisible(),
                "TC_SL_P01 FAIL – Error visible after saving Appointment Group '" + APPT_GROUP_NAME + "'.");
        System.out.println("TC_SL_P01 PASS – Appointment Group '" + APPT_GROUP_NAME + "' created successfully.");
    }

    @Test(priority = 2,
          description = "TC_SL_P02 – Create Appointment Group 'QA Non Exam Appt' if not already exist")
    public void testCreateNonExamAppointmentGroup() throws InterruptedException {
        navigateToScheduleSection("Appointment Group");

        // Search to check if already exists
        try {
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@placeholder='search']")));
            searchBox.clear();
            searchBox.sendKeys(APPT_GROUP_NAME_2);
            Thread.sleep(1500);

            List<WebElement> existingRows = driver.findElements(
                    By.xpath("//td[normalize-space(text())='" + APPT_GROUP_NAME_2 + "']"
                           + " | //div[contains(@class,'list-item') and normalize-space(text())='" + APPT_GROUP_NAME_2 + "']"
                           + " | //span[normalize-space(text())='" + APPT_GROUP_NAME_2 + "']"));
            if (!existingRows.isEmpty()) {
                System.out.println("TC_SL_P02 SKIP – Appointment Group '" + APPT_GROUP_NAME_2 + "' already exists.");
                return;
            }
            searchBox.clear();
            Thread.sleep(500);
        } catch (Exception ignored) {}

        // Click Add New button
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Add New') or contains(.,'Add Group') or contains(.,'New')]"
                       + " | //button[.//i[contains(@class,'fa-plus')]]")));
        js.executeScript("arguments[0].click();", addBtn);
        Thread.sleep(1500);
        dismissErrorDialog();

        // Fill Group Name
        WebElement groupNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Appointment Group Name']")));
        groupNameField.clear();
        groupNameField.sendKeys(APPT_GROUP_NAME_2);
        Thread.sleep(300);

        // Select Duration = 20
        WebElement durationDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-select[@id='Appointment Group Duration']")));
        durationDropdown.click();
        Thread.sleep(800);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-dropdown-panel//span[@class='ng-option-label' and normalize-space(text())='" + APPT_GROUP_DURATION_2 + "']")))
            .click();
        Thread.sleep(300);
        System.out.println("ScheduleLookupTest: selected Duration = " + APPT_GROUP_DURATION_2);

        // Exam Required checkbox — leave unchecked (do not click)
        System.out.println("ScheduleLookupTest: Exam Required left unchecked for Non Exam group.");

        // Save
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]"
                       + " | //button[normalize-space(text())='Save']"
                       + " | //button[contains(.,'Save')]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", saveBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2000);
        dismissSuccessPopup();
        dismissErrorDialog();

        Assert.assertFalse(isErrorVisible(),
                "TC_SL_P02 FAIL – Error visible after saving Appointment Group '" + APPT_GROUP_NAME_2 + "'.");
        System.out.println("TC_SL_P02 PASS – Appointment Group '" + APPT_GROUP_NAME_2 + "' created successfully.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // APPOINTMENT TYPE TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 3,
          description = "TC_SL_P03 – Create Appointment Type 'QA Exam Appointment Type' if not already exist")
    public void testCreateAppointmentType() throws InterruptedException {
        navigateToScheduleSection("Appointment Type");

        // Search to check if already exists
        try {
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@placeholder='search']")));
            searchBox.clear();
            searchBox.sendKeys(APPT_TYPE_NAME);
            Thread.sleep(1500);

            List<WebElement> existingRows = driver.findElements(
                    By.xpath("//td[normalize-space(text())='" + APPT_TYPE_NAME + "']"
                           + " | //div[contains(@class,'list-item') and normalize-space(text())='" + APPT_TYPE_NAME + "']"
                           + " | //span[normalize-space(text())='" + APPT_TYPE_NAME + "']"));
            if (!existingRows.isEmpty()) {
                System.out.println("TC_SL_P03 SKIP – Appointment Type '" + APPT_TYPE_NAME + "' already exists.");
                return;
            }
            searchBox.clear();
            Thread.sleep(500);
        } catch (Exception ignored) {}

        // Click Add New button
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Add New') or contains(.,'Add Type') or contains(.,'New')]"
                       + " | //button[.//i[contains(@class,'fa-plus')]]")));
        js.executeScript("arguments[0].click();", addBtn);
        Thread.sleep(1500);
        dismissErrorDialog();

        // Fill Appointment Type Name
        WebElement typeNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Appointment Type Name']"
                       + " | //input[@placeholder='Appointment Type Name']"
                       + " | //input[contains(@id,'Type Name')]")));
        typeNameField.clear();
        typeNameField.sendKeys(APPT_TYPE_NAME);
        Thread.sleep(300);

        // Select Appointment Group dropdown = "QA Exam Appt"
        WebElement groupDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-select[contains(@id,'Appointment Group') or contains(@id,'appointment-group') or contains(@id,'appointmentGroup')]"
                       + " | //ng-select[@formcontrolname='appointmentGroupId' or @formcontrolname='appointmentGroup']")));
        groupDropdown.click();
        Thread.sleep(800);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-dropdown-panel//span[@class='ng-option-label' and normalize-space(text())='" + APPT_GROUP_NAME + "']")))
            .click();
        Thread.sleep(300);
        System.out.println("ScheduleLookupTest: selected Appointment Group = " + APPT_GROUP_NAME);

        // Save
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]"
                       + " | //button[normalize-space(text())='Save']"
                       + " | //button[contains(.,'Save')]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", saveBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2000);
        dismissSuccessPopup();
        dismissErrorDialog();

        Assert.assertFalse(isErrorVisible(),
                "TC_SL_P03 FAIL – Error visible after saving Appointment Type '" + APPT_TYPE_NAME + "'.");
        System.out.println("TC_SL_P03 PASS – Appointment Type '" + APPT_TYPE_NAME + "' created successfully.");
    }

    @Test(priority = 4,
          description = "TC_SL_P04 – Create Appointment Type 'QA Non Exam Appointment Type' if not already exist")
    public void testCreateNonExamAppointmentType() throws InterruptedException {
        navigateToScheduleSection("Appointment Type");

        // Search to check if already exists
        try {
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@placeholder='search']")));
            searchBox.clear();
            searchBox.sendKeys(APPT_TYPE_NAME_2);
            Thread.sleep(1500);

            List<WebElement> existingRows = driver.findElements(
                    By.xpath("//td[normalize-space(text())='" + APPT_TYPE_NAME_2 + "']"
                           + " | //div[contains(@class,'list-item') and normalize-space(text())='" + APPT_TYPE_NAME_2 + "']"
                           + " | //span[normalize-space(text())='" + APPT_TYPE_NAME_2 + "']"));
            if (!existingRows.isEmpty()) {
                System.out.println("TC_SL_P04 SKIP – Appointment Type '" + APPT_TYPE_NAME_2 + "' already exists.");
                return;
            }
            searchBox.clear();
            Thread.sleep(500);
        } catch (Exception ignored) {}

        // Click Add New button
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Add New') or contains(.,'Add Type') or contains(.,'New')]"
                       + " | //button[.//i[contains(@class,'fa-plus')]]")));
        js.executeScript("arguments[0].click();", addBtn);
        Thread.sleep(1500);
        dismissErrorDialog();

        // Fill Appointment Type Name
        WebElement typeNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Appointment Type Name']"
                       + " | //input[@placeholder='Appointment Type Name']"
                       + " | //input[contains(@id,'Type Name')]")));
        typeNameField.clear();
        typeNameField.sendKeys(APPT_TYPE_NAME_2);
        Thread.sleep(300);

        // Select Appointment Group dropdown = "QA Non Exam Appt"
        WebElement groupDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-select[contains(@id,'Appointment Group') or contains(@id,'appointment-group') or contains(@id,'appointmentGroup')]"
                       + " | //ng-select[@formcontrolname='appointmentGroupId' or @formcontrolname='appointmentGroup']")));
        groupDropdown.click();
        Thread.sleep(800);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-dropdown-panel//span[@class='ng-option-label' and normalize-space(text())='" + APPT_GROUP_NAME_2 + "']")))
            .click();
        Thread.sleep(300);
        System.out.println("ScheduleLookupTest: selected Appointment Group = " + APPT_GROUP_NAME_2);

        // Save
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]"
                       + " | //button[normalize-space(text())='Save']"
                       + " | //button[contains(.,'Save')]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", saveBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2000);
        dismissSuccessPopup();
        dismissErrorDialog();

        Assert.assertFalse(isErrorVisible(),
                "TC_SL_P04 FAIL – Error visible after saving Appointment Type '" + APPT_TYPE_NAME_2 + "'.");
        System.out.println("TC_SL_P04 PASS – Appointment Type '" + APPT_TYPE_NAME_2 + "' created successfully.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EXAM RESULT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 5,
          description = "TC_SL_P05 – Create Exam Result 'QA Treatment Recommended Exam' if not already exist")
    public void testCreateExamResult() throws InterruptedException {
        navigateToScheduleSection("Exam Result");

        // Search to check if already exists
        try {
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@placeholder='search']")));
            searchBox.clear();
            searchBox.sendKeys(EXAM_RESULT_NAME);
            Thread.sleep(1500);

            List<WebElement> existingRows = driver.findElements(
                    By.xpath("//td[normalize-space(text())='" + EXAM_RESULT_NAME + "']"
                           + " | //span[normalize-space(text())='" + EXAM_RESULT_NAME + "']"));
            if (!existingRows.isEmpty()) {
                System.out.println("TC_SL_P05 SKIP – Exam Result '" + EXAM_RESULT_NAME + "' already exists.");
                return;
            }
            searchBox.clear();
            Thread.sleep(500);
        } catch (Exception ignored) {}

        // Click Add New button
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Add New') or contains(.,'New')]"
                       + " | //button[.//i[contains(@class,'fa-plus')]]")));
        js.executeScript("arguments[0].click();", addBtn);
        Thread.sleep(1500);
        dismissErrorDialog();

        // Fill Exam Result name
        WebElement examResultField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Exam Result']")));
        examResultField.clear();
        examResultField.sendKeys(EXAM_RESULT_NAME);
        Thread.sleep(300);

        // Select Exam Result Category = "Tx Recommended"
        WebElement categoryDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-select[@id='Exam Result Category']")));
        categoryDropdown.click();
        Thread.sleep(800);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-dropdown-panel//span[@class='ng-option-label' and normalize-space(text())='" + EXAM_RESULT_CATEGORY + "']")))
            .click();
        Thread.sleep(300);
        System.out.println("ScheduleLookupTest: selected Exam Result Category = " + EXAM_RESULT_CATEGORY);

        // Save
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]"
                       + " | //button[normalize-space(text())='Save']"
                       + " | //button[contains(.,'Save')]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", saveBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2000);
        dismissSuccessPopup();
        dismissErrorDialog();

        Assert.assertFalse(isErrorVisible(),
                "TC_SL_P05 FAIL – Error visible after saving Exam Result '" + EXAM_RESULT_NAME + "'.");
        System.out.println("TC_SL_P05 PASS – Exam Result '" + EXAM_RESULT_NAME + "' created successfully.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // APPOINTMENT GROUP — SEARCH & VERIFY SCENARIOS (no data created)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 6,
          description = "TC_SL_P06 – Search Appointment Group 'QA Exam Appt' and verify it appears in list")
    public void testSearchAppointmentGroupExam() throws InterruptedException {
        navigateToScheduleSection("Appointment Group");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys(APPT_GROUP_NAME);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(
                By.xpath("//td[normalize-space(text())='" + APPT_GROUP_NAME + "']"
                       + " | //span[normalize-space(text())='" + APPT_GROUP_NAME + "']"));
        Assert.assertFalse(results.isEmpty(),
                "TC_SL_P06 FAIL – '" + APPT_GROUP_NAME + "' not found in Appointment Group list.");
        System.out.println("TC_SL_P06 PASS – '" + APPT_GROUP_NAME + "' found in list.");
    }

    @Test(priority = 7,
          description = "TC_SL_P07 – Search Appointment Group 'QA Non Exam Appt' and verify it appears in list")
    public void testSearchAppointmentGroupNonExam() throws InterruptedException {
        navigateToScheduleSection("Appointment Group");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys(APPT_GROUP_NAME_2);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(
                By.xpath("//td[normalize-space(text())='" + APPT_GROUP_NAME_2 + "']"
                       + " | //span[normalize-space(text())='" + APPT_GROUP_NAME_2 + "']"));
        Assert.assertFalse(results.isEmpty(),
                "TC_SL_P07 FAIL – '" + APPT_GROUP_NAME_2 + "' not found in Appointment Group list.");
        System.out.println("TC_SL_P07 PASS – '" + APPT_GROUP_NAME_2 + "' found in list.");
    }

    @Test(priority = 8,
          description = "TC_SL_P08 – Search non-existent Appointment Group and verify empty results")
    public void testSearchAppointmentGroupNoResult() throws InterruptedException {
        navigateToScheduleSection("Appointment Group");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys("ZZ_NONEXISTENT_GROUP_QA");
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(
                By.xpath("//td[normalize-space(text())='ZZ_NONEXISTENT_GROUP_QA']"
                       + " | //span[normalize-space(text())='ZZ_NONEXISTENT_GROUP_QA']"));
        Assert.assertTrue(results.isEmpty(),
                "TC_SL_P08 FAIL – Search for non-existent group returned unexpected results.");
        System.out.println("TC_SL_P08 PASS – No results shown for non-existent Appointment Group.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // APPOINTMENT TYPE — SEARCH & VERIFY SCENARIOS (no data created)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 9,
          description = "TC_SL_P09 – Search Appointment Type 'QA Exam Appointment Type' and verify it appears in list")
    public void testSearchAppointmentTypeExam() throws InterruptedException {
        navigateToScheduleSection("Appointment Type");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys(APPT_TYPE_NAME);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(
                By.xpath("//td[normalize-space(text())='" + APPT_TYPE_NAME + "']"
                       + " | //span[normalize-space(text())='" + APPT_TYPE_NAME + "']"));
        Assert.assertFalse(results.isEmpty(),
                "TC_SL_P09 FAIL – '" + APPT_TYPE_NAME + "' not found in Appointment Type list.");
        System.out.println("TC_SL_P09 PASS – '" + APPT_TYPE_NAME + "' found in list.");
    }

    @Test(priority = 10,
          description = "TC_SL_P10 – Search Appointment Type 'QA Non Exam Appointment Type' and verify it appears in list")
    public void testSearchAppointmentTypeNonExam() throws InterruptedException {
        navigateToScheduleSection("Appointment Type");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys(APPT_TYPE_NAME_2);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(
                By.xpath("//td[normalize-space(text())='" + APPT_TYPE_NAME_2 + "']"
                       + " | //span[normalize-space(text())='" + APPT_TYPE_NAME_2 + "']"));
        Assert.assertFalse(results.isEmpty(),
                "TC_SL_P10 FAIL – '" + APPT_TYPE_NAME_2 + "' not found in Appointment Type list.");
        System.out.println("TC_SL_P10 PASS – '" + APPT_TYPE_NAME_2 + "' found in list.");
    }

    @Test(priority = 11,
          description = "TC_SL_P11 – Search non-existent Appointment Type and verify empty results")
    public void testSearchAppointmentTypeNoResult() throws InterruptedException {
        navigateToScheduleSection("Appointment Type");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys("ZZ_NONEXISTENT_TYPE_QA");
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(
                By.xpath("//td[normalize-space(text())='ZZ_NONEXISTENT_TYPE_QA']"
                       + " | //span[normalize-space(text())='ZZ_NONEXISTENT_TYPE_QA']"));
        Assert.assertTrue(results.isEmpty(),
                "TC_SL_P11 FAIL – Search for non-existent type returned unexpected results.");
        System.out.println("TC_SL_P11 PASS – No results shown for non-existent Appointment Type.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EXAM RESULT — SEARCH & VERIFY SCENARIOS (no data created)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 12,
          description = "TC_SL_P12 – Search Exam Result 'QA Treatment Recommended Exam' and verify it appears in list")
    public void testSearchExamResult() throws InterruptedException {
        navigateToScheduleSection("Exam Result");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys(EXAM_RESULT_NAME);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(
                By.xpath("//td[normalize-space(text())='" + EXAM_RESULT_NAME + "']"
                       + " | //span[normalize-space(text())='" + EXAM_RESULT_NAME + "']"));
        Assert.assertFalse(results.isEmpty(),
                "TC_SL_P12 FAIL – '" + EXAM_RESULT_NAME + "' not found in Exam Result list.");
        System.out.println("TC_SL_P12 PASS – '" + EXAM_RESULT_NAME + "' found in list.");
    }

    @Test(priority = 13,
          description = "TC_SL_P13 – Search non-existent Exam Result and verify empty results")
    public void testSearchExamResultNoResult() throws InterruptedException {
        navigateToScheduleSection("Exam Result");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys("ZZ_NONEXISTENT_RESULT_QA");
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(
                By.xpath("//td[normalize-space(text())='ZZ_NONEXISTENT_RESULT_QA']"
                       + " | //span[normalize-space(text())='ZZ_NONEXISTENT_RESULT_QA']"));
        Assert.assertTrue(results.isEmpty(),
                "TC_SL_P13 FAIL – Search for non-existent exam result returned unexpected results.");
        System.out.println("TC_SL_P13 PASS – No results shown for non-existent Exam Result.");
    }
}
