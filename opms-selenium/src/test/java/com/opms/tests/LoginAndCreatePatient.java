package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;
import com.opms.practice.LookupPage;
import com.opms.utils.TestDataGenerator;

import java.time.Duration;

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
import org.testng.annotations.Test;

public class LoginAndCreatePatient {

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;
    private JavascriptExecutor js;

    public static String fullName;

    @BeforeClass
    public void setUp() {
        driver  = DriverManager.getDriver();
        wait    = new WebDriverWait(driver, Duration.ofSeconds(15));
        actions = new Actions(driver);
        js      = (JavascriptExecutor) driver;
    }

    // ── STEP 1 : Login ────────────────────────────────────────────────────────

    @Test(priority = 1, description = "Login with valid credentials")
    public void testLogin() {
        LoginPage.loginWith(LoginPage.VALID_EMAIL, LoginPage.VALID_PASSWORD);

        // Verify dashboard is visible after login
        Assert.assertTrue(LoginPage.isDashboardVisible(),
                "Dashboard should be visible after successful login.");
        System.out.println("Login successful.");
    }

    // ── STEP 2 : Select location ──────────────────────────────────────────────

    @Test(priority = 2, description = "Select practice location from dropdown",
            dependsOnMethods = "testLogin")
    public void testSelectLocation() throws InterruptedException {
        // Close the right panel if it is open
        try {
            WebElement closePanel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div/i[contains(@class,'collaspse_icon')]")));
            closePanel.click();
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Right panel not present, skipping close.");
        }

        WebElement locationDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//ng-select[@bindlabel='practiceLocationName']")));
        actions.moveToElement(locationDropdown).click().perform();

        WebElement locationOption = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[normalize-space(text())='" + LookupPage.location + "']")));
        actions.moveToElement(locationOption).click().perform();

        Thread.sleep(2000);
        System.out.println("Location '" + LookupPage.location + "' selected.");
    }

    // ── STEP 3 : Create patient ───────────────────────────────────────────────

    @Test(priority = 3, description = "Create a new patient with valid details",
            dependsOnMethods = "testSelectLocation")
    public void testCreatePatient() throws InterruptedException {

        // Click Add Patient button
        WebElement addPatient = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//i[contains(@class,'fa-user-plus')]")));
        actions.moveToElement(addPatient).click().perform();
        Thread.sleep(2000);

        // Generate unique patient data
        String firstName = TestDataGenerator.generateUniqueString();
        String lastName  = TestDataGenerator.generateUniqueString();
        String email     = TestDataGenerator.generateUniqueEmail();

        // Prefix
        driver.findElement(By.id("Prefix")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("ng-dropdown-panel")))
            .findElement(By.xpath("//span[text()='Mr.']")).click();

        // Name fields
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("First Name")))
                .sendKeys(firstName);
        driver.findElement(By.id("Last Name")).sendKeys(lastName);

        // Suffix
        WebElement suffix = driver.findElement(By.xpath("//input[@id='Suffix']"));
        suffix.sendKeys("BBB");
        suffix.sendKeys(Keys.TAB);

        // Date of Birth — label-based XPath avoids dynamic datepicker ID
        WebElement dob = driver.findElement(By.xpath(
            "//label[contains(text(),'Date of Birth')]" +
            "/ancestor::div[contains(@class,'form-group')]" +
            "//input[@placeholder='MM/DD/YYYY']"));
        dob.click();
        Thread.sleep(500);
        dob.sendKeys("05131995");  // MMDDYYYY — all at once; Kendo auto-advances per segment
        dob.sendKeys(Keys.TAB);

        // Contact details
        driver.findElement(By.xpath("//input[@id='Primary Phone']")).sendKeys("5485654785");
        driver.findElement(By.xpath("//input[@id='Email Address']")).sendKeys(email);

        // Demographics
        driver.findElement(By.xpath("//label[@for='patientGender_Male']")).click();
        driver.findElement(By.xpath("//label[@for='patientLanguagePreference_English']")).click();
        driver.findElement(By.xpath("//label[@for='patientMaritalStatus_Single']")).click();

        // Dentist — directly target the exact option span
        driver.findElement(By.xpath("//ng-select[@placeholder='Select Dentist']")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
            "//div[contains(@class,'ng-option')]" +
            "//span[text()='Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)']")))
            .click();

        // Save
        WebElement saveBtn = driver.findElement(By.xpath("//span[text()='Save & Close']"));
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(3000);

        fullName = lastName + ", " + firstName;
        System.out.println("Patient created: " + fullName);
    }

    // ── STEP 4 : Search and verify patient ───────────────────────────────────

    @Test(priority = 4, description = "Search for the newly created patient and verify",
            dependsOnMethods = "testCreatePatient")
    public void testSearchPatient() throws InterruptedException {
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Search Patient' or @id='patientName']")));
        searchBox.clear();
        searchBox.sendKeys(fullName);
        Thread.sleep(2000);

        WebElement patientResult = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li//span[contains(@class,'first-name-letter')]"
                        + "/following-sibling::span[contains(text(),'" + fullName + "')]")));

        Assert.assertTrue(patientResult.isDisplayed(),
                "Patient '" + fullName + "' should appear in search results.");
        patientResult.click();

        System.out.println("Patient '" + fullName + "' found and selected successfully.");
    }

    @AfterClass
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
