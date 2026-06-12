package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;
import com.opms.practice.LookupPage;
import com.opms.utils.TestDataGenerator;

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
 * Tests for the Lookup (Location) and Insurance sections inside Practice Configuration.
 *
 * Navigation flow:
 *   Login → click Practice Configuration → click "Lookup"   → Location form visible directly
 *   Login → click Practice Configuration → click "Insurance" → Insurance form visible directly
 *
 * Lookup menu XPath (inside Practice Config left sidebar):
 *   //span[@class='pcoded-mtext ...' and text()='Lookup']
 * Insurance menu XPath (inside Practice Config left sidebar):
 *   //span[@class='pcoded-mtext ...' and text()='Insurance']
 */
public class LookupTest {

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
        Assert.assertTrue(LoginPage.isDashboardVisible(), "Login failed in LookupTest setup.");
        Thread.sleep(2000);
        System.out.println("LookupTest: logged in successfully.");
    }

    @AfterClass
    public void tearDownSuite() {
        DriverManager.quitDriver();
        System.out.println("LookupTest: browser closed.");
    }

    // ── Navigation helpers ────────────────────────────────────────────────────

    private void dismissErrorDialog() {
        try {
            List<WebElement> okBtns = driver.findElements(
                    By.xpath("//button[normalize-space(text())='OK']"));
            for (WebElement btn : okBtns) {
                if (btn.isDisplayed()) { btn.click(); Thread.sleep(400); break; }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Step 1: click Practice Configuration in the main nav.
     * Step 2: click the given sub-menu item (e.g. "Lookup" or "Insurance")
     *         in the Practice Config left sidebar.
     * The form for that section is then directly visible — no further sub-menu needed.
     */
    private void navigateToPracticeConfigSection(String sectionName) throws InterruptedException {
        dismissErrorDialog();

        // Scroll to top so the fixed navbar does not intercept the click
        js.executeScript("window.scrollTo(0, 0);");
        Thread.sleep(300);

        // Step 1 – open Practice Configuration via JS click (avoids fixed-header interception)
        WebElement practiceLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@title,'Practice configuration')]")));
        js.executeScript("arguments[0].click();", practiceLink);
        Thread.sleep(2000);
        dismissErrorDialog();

        // Step 2 – click the section in the sidebar (Lookup or Insurance)
        WebElement sectionMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath(String.format(PCODED_MTEXT, sectionName))));
        sectionMenu.click();
        Thread.sleep(2000);
        dismissErrorDialog();

        System.out.println("LookupTest: navigated to Practice Config → " + sectionName);
    }

    private void clickSaveButton() throws InterruptedException {
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]")));
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2000);
        dismissErrorDialog();
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

    private void selectDropdownOption(String ngSelectXpath, String optionText)
            throws InterruptedException {
        driver.findElement(By.xpath(ngSelectXpath)).click();
        Thread.sleep(500);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ng-option')]//span[text()='" + optionText + "']")))
            .click();
        Thread.sleep(300);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LOCATION TESTS  (Practice Config → Lookup)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 1, description = "TC_LK_P01 – Lookup page loads; location search field and form are visible")
    public void testLookupPageLoads() throws InterruptedException {
        navigateToPracticeConfigSection("Lookup");

        Assert.assertTrue(
                driver.findElement(By.xpath("//input[@placeholder='search location']")).isDisplayed(),
                "TC_LK_P01 FAIL – Location search field should be visible on Lookup page.");
        Assert.assertTrue(
                driver.findElement(By.xpath("//input[@id='Location Name']")).isDisplayed(),
                "TC_LK_P01 FAIL – Location Name form field should be visible on Lookup page.");
        System.out.println("TC_LK_P01 PASS – Lookup page loaded; search field and form are visible.");
    }

    @Test(priority = 2, description = "TC_LK_P02 – Search for the existing test location 'Mumbai'")
    public void testSearchExistingLocation() throws InterruptedException {
        navigateToPracticeConfigSection("Lookup");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='search location']")));
        searchBox.clear();
        searchBox.sendKeys(LookupPage.location);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(By.xpath(
                "//div[@class='d-flex flex-column']//span[contains(text(),'" + LookupPage.location + "')]"));
        Assert.assertFalse(results.isEmpty(),
                "TC_LK_P02 FAIL – Existing location '" + LookupPage.location + "' should appear in search results.");
        System.out.println("TC_LK_P02 PASS – Existing location found: " + LookupPage.location);
    }

    @Test(priority = 3, description = "TC_LK_P03 – All location form fields are visible and editable")
    public void testLocationFormFieldsVisible() throws InterruptedException {
        navigateToPracticeConfigSection("Lookup");

        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='Location Name']")).isDisplayed(),
            "TC_LK_P03 FAIL – Location Name field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='Tax ID(TIN)']")).isDisplayed(),
            "TC_LK_P03 FAIL – Tax ID field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='Address Line 1']")).isDisplayed(),
            "TC_LK_P03 FAIL – Address Line 1 field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='City']")).isDisplayed(),
            "TC_LK_P03 FAIL – City field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='Zip Code']")).isDisplayed(),
            "TC_LK_P03 FAIL – Zip Code field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='Office Phone']")).isDisplayed(),
            "TC_LK_P03 FAIL – Office Phone field not visible.");
        System.out.println("TC_LK_P03 PASS – All location form fields are visible.");
    }

    @Test(priority = 4, description = "TC_LK_P04 – Add a new location with all required fields and save")
    public void testAddNewLocationAllFields() throws InterruptedException {
        navigateToPracticeConfigSection("Lookup");

        String uniqueName = "AutoLoc " + TestDataGenerator.generateUniqueString();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Location Name']"))).sendKeys(uniqueName);
        driver.findElement(By.xpath("//input[@id='Tax ID(TIN)']")).sendKeys("12-3456789");
        driver.findElement(By.xpath("//input[@id='Address Line 1']")).sendKeys("100 Auto Test Street");
        driver.findElement(By.xpath("//input[@id='City']")).sendKeys("TestCity");

        selectDropdownOption("//div[@class='ng-placeholder' and text()='Select State']", "Alaska");

        driver.findElement(By.xpath("//input[@id='Zip Code']")).sendKeys("60001");
        driver.findElement(By.xpath("//input[@id='Office Phone']")).sendKeys("(312) 555-0100");

        selectDropdownOption("//ng-select[@placeholder='Select Time Zone']", "Eastern Standard Time");
        selectDropdownOption("//ng-select[@bindlabel='providerName']", "Babu, Lokesh");

        js.executeScript("window.scrollBy(0, 2500);");
        Thread.sleep(500);
        clickSaveButton();

        Assert.assertFalse(isErrorVisible(),
                "TC_LK_P04 FAIL – No validation error should appear when all location fields are valid.");
        System.out.println("TC_LK_P04 PASS – New location '" + uniqueName + "' saved successfully.");
    }

    // ── Location Negative Tests ───────────────────────────────────────────────

    @Test(priority = 5, description = "TC_LK_N01 – Save location without Location Name; expect validation error")
    public void testSaveLocationWithoutName() throws InterruptedException {
        navigateToPracticeConfigSection("Lookup");

        driver.findElement(By.xpath("//input[@id='Tax ID(TIN)']")).sendKeys("12-3456789");
        driver.findElement(By.xpath("//input[@id='Address Line 1']")).sendKeys("100 Test Street");
        driver.findElement(By.xpath("//input[@id='City']")).sendKeys("TestCity");
        driver.findElement(By.xpath("//input[@id='Zip Code']")).sendKeys("60001");
        driver.findElement(By.xpath("//input[@id='Office Phone']")).sendKeys("(312) 555-0101");

        clickSaveButton();

        Assert.assertTrue(isErrorVisible(),
                "TC_LK_N01 FAIL – Validation error should appear when Location Name is missing.");
        System.out.println("TC_LK_N01 PASS – Missing Location Name correctly blocked.");
    }

    @Test(priority = 6, description = "TC_LK_N02 – Save location without Address Line 1; expect validation error")
    public void testSaveLocationWithoutAddress() throws InterruptedException {
        navigateToPracticeConfigSection("Lookup");

        driver.findElement(By.xpath("//input[@id='Location Name']"))
              .sendKeys("NoAddr " + TestDataGenerator.generateUniqueString());
        driver.findElement(By.xpath("//input[@id='City']")).sendKeys("TestCity");
        driver.findElement(By.xpath("//input[@id='Zip Code']")).sendKeys("60001");

        clickSaveButton();

        Assert.assertTrue(isErrorVisible(),
                "TC_LK_N02 FAIL – Validation error should appear when Address Line 1 is missing.");
        System.out.println("TC_LK_N02 PASS – Missing Address Line 1 correctly blocked.");
    }

    @Test(priority = 7, description = "TC_LK_N03 – Save location with letters in Zip Code; expect validation error")
    public void testSaveLocationInvalidZipCode() throws InterruptedException {
        navigateToPracticeConfigSection("Lookup");

        driver.findElement(By.xpath("//input[@id='Location Name']"))
              .sendKeys("ZipTest " + TestDataGenerator.generateUniqueString());
        driver.findElement(By.xpath("//input[@id='Address Line 1']")).sendKeys("100 Test St");
        driver.findElement(By.xpath("//input[@id='City']")).sendKeys("TestCity");
        driver.findElement(By.xpath("//input[@id='Zip Code']")).sendKeys("ABCDE");

        clickSaveButton();

        Assert.assertTrue(isErrorVisible(),
                "TC_LK_N03 FAIL – Letters in Zip Code should be rejected.");
        System.out.println("TC_LK_N03 PASS – Invalid Zip Code (letters) correctly blocked.");
    }

    @Test(priority = 8, description = "TC_LK_N04 – Search for non-existent location; expect no results")
    public void testSearchNonExistentLocation() throws InterruptedException {
        navigateToPracticeConfigSection("Lookup");

        String randomName = "NONEXIST_" + TestDataGenerator.generateUniqueString();
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='search location']")));
        searchBox.clear();
        searchBox.sendKeys(randomName);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(By.xpath(
                "//div[@class='d-flex flex-column']//span[contains(text(),'" + randomName + "')]"));
        Assert.assertTrue(results.isEmpty(),
                "TC_LK_N04 FAIL – Non-existent location should not appear in search results.");
        System.out.println("TC_LK_N04 PASS – Non-existent location search returns no result.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INSURANCE TESTS  (Practice Config → Insurance)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 9, description = "TC_LK_P05 – Insurance page loads; search field and form are visible")
    public void testInsurancePageLoads() throws InterruptedException {
        navigateToPracticeConfigSection("Insurance");

        Assert.assertTrue(
                driver.findElement(By.xpath("//input[@placeholder='search insurance']")).isDisplayed(),
                "TC_LK_P05 FAIL – Insurance search field should be visible.");
        Assert.assertTrue(
                driver.findElement(By.xpath("//input[@id='Insurance Company Name']")).isDisplayed(),
                "TC_LK_P05 FAIL – Insurance Company Name form field should be visible.");
        System.out.println("TC_LK_P05 PASS – Insurance page loaded; search field and form are visible.");
    }

    @Test(priority = 10, description = "TC_LK_P06 – Search for the existing test insurance company")
    public void testSearchExistingInsurance() throws InterruptedException {
        navigateToPracticeConfigSection("Insurance");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='search insurance']")));
        searchBox.clear();
        searchBox.sendKeys(LookupPage.insurance);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(By.xpath(
                "//div[@class='d-flex flex-column']//span[contains(text(),'" + LookupPage.insurance + "')]"));
        Assert.assertFalse(results.isEmpty(),
                "TC_LK_P06 FAIL – Existing insurance '" + LookupPage.insurance + "' should appear in search.");
        System.out.println("TC_LK_P06 PASS – Existing insurance found: " + LookupPage.insurance);
    }

    @Test(priority = 11, description = "TC_LK_P07 – All insurance form fields are visible and editable")
    public void testInsuranceFormFieldsVisible() throws InterruptedException {
        navigateToPracticeConfigSection("Insurance");

        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='Insurance Company Name']")).isDisplayed(),
            "TC_LK_P07 FAIL – Insurance Company Name field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@placeholder='Phone']")).isDisplayed(),
            "TC_LK_P07 FAIL – Phone field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='Address Line 1']")).isDisplayed(),
            "TC_LK_P07 FAIL – Address Line 1 field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='City']")).isDisplayed(),
            "TC_LK_P07 FAIL – City field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='Zip Code']")).isDisplayed(),
            "TC_LK_P07 FAIL – Zip Code field not visible.");
        System.out.println("TC_LK_P07 PASS – All insurance form fields are visible.");
    }

    @Test(priority = 12, description = "TC_LK_P08 – Add a new insurance company with all required fields and save")
    public void testAddNewInsuranceAllFields() throws InterruptedException {
        navigateToPracticeConfigSection("Insurance");

        String uniqueName = "AutoIns " + TestDataGenerator.generateUniqueString();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Insurance Company Name']"))).sendKeys(uniqueName);
        driver.findElement(By.xpath("//input[@placeholder='Phone']")).sendKeys("(312) 555-0200");
        driver.findElement(By.xpath("//input[@id='Address Line 1']")).sendKeys("200 Insurance Ave");
        driver.findElement(By.xpath("//input[@id='City']")).sendKeys("InsCity");

        selectDropdownOption("//div[@class='ng-placeholder' and text()='Select State']", "Alaska");

        driver.findElement(By.xpath("//input[@id='Zip Code']")).sendKeys("60002");

        clickSaveButton();

        Assert.assertFalse(isErrorVisible(),
                "TC_LK_P08 FAIL – No error should appear when all insurance fields are valid.");
        System.out.println("TC_LK_P08 PASS – New insurance '" + uniqueName + "' saved successfully.");
    }

    // ── Insurance Negative Tests ──────────────────────────────────────────────

    @Test(priority = 13, description = "TC_LK_N05 – Save insurance without Company Name; expect validation error")
    public void testSaveInsuranceWithoutName() throws InterruptedException {
        navigateToPracticeConfigSection("Insurance");

        driver.findElement(By.xpath("//input[@placeholder='Phone']")).sendKeys("(312) 555-0201");
        driver.findElement(By.xpath("//input[@id='Address Line 1']")).sendKeys("200 Test Ave");
        driver.findElement(By.xpath("//input[@id='City']")).sendKeys("InsCity");
        driver.findElement(By.xpath("//input[@id='Zip Code']")).sendKeys("60002");

        clickSaveButton();

        Assert.assertTrue(isErrorVisible(),
                "TC_LK_N05 FAIL – Validation error should appear when Insurance Company Name is blank.");
        System.out.println("TC_LK_N05 PASS – Missing Insurance Company Name correctly blocked.");
    }

    @Test(priority = 14, description = "TC_LK_N06 – Save insurance with letters in phone number; expect validation error")
    public void testSaveInsuranceInvalidPhone() throws InterruptedException {
        navigateToPracticeConfigSection("Insurance");

        driver.findElement(By.xpath("//input[@id='Insurance Company Name']"))
              .sendKeys("PhoneTest " + TestDataGenerator.generateUniqueString());
        driver.findElement(By.xpath("//input[@placeholder='Phone']")).sendKeys("ABCDEFGHIJ");
        driver.findElement(By.xpath("//input[@id='Address Line 1']")).sendKeys("200 Test Ave");
        driver.findElement(By.xpath("//input[@id='City']")).sendKeys("InsCity");
        driver.findElement(By.xpath("//input[@id='Zip Code']")).sendKeys("60002");

        clickSaveButton();

        Assert.assertTrue(isErrorVisible(),
                "TC_LK_N06 FAIL – Letters in phone number should be rejected.");
        System.out.println("TC_LK_N06 PASS – Invalid phone number (letters) correctly blocked.");
    }

    @Test(priority = 15, description = "TC_LK_N07 – Search for non-existent insurance; expect no results")
    public void testSearchNonExistentInsurance() throws InterruptedException {
        navigateToPracticeConfigSection("Insurance");

        String randomName = "NONEXIST_" + TestDataGenerator.generateUniqueString();
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='search insurance']")));
        searchBox.clear();
        searchBox.sendKeys(randomName);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(By.xpath(
                "//div[@class='d-flex flex-column']//span[contains(text(),'" + randomName + "')]"));
        Assert.assertTrue(results.isEmpty(),
                "TC_LK_N07 FAIL – Non-existent insurance should not appear in search results.");
        System.out.println("TC_LK_N07 PASS – Non-existent insurance search returns no result.");
    }
}
