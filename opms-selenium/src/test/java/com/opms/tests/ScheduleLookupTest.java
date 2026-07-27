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

    private static final String APPT_GROUP_NAME         = "QA Exam Appt";
    private static final String APPT_GROUP_DURATION      = "15";
    private static final String APPT_GROUP_NAME_2        = "QA Non Exam Appt";
    private static final String APPT_GROUP_DURATION_2    = "20";

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

        // Click the sub-section link by href pattern
        WebElement subMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'appointmentgroup')]")));
        js.executeScript("arguments[0].click();", subMenu);
        Thread.sleep(2000);
        dismissErrorDialog();

        System.out.println("ScheduleLookupTest: navigated to Practice Config → Schedule → " + subMenu);
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
}
