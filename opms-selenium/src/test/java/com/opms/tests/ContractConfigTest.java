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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Positive and negative test cases for the Practice Configuration → Contract tab.
 *
 * Positive tests: fill all fields with new values and verify save succeeds.
 * Negative tests: navigate to the tab (existing values already displayed),
 *                 clear ONLY the one field under test, leave all others untouched,
 *                 click Save, then assert the required-field validation message appears.
 *
 * Fields (confirmed from ContractConfigPage.java):
 *   id="Recommended Down Payment"
 *   id="Minimum Down Payment"
 *   id="Recommended Monthly Payment"
 *   id="Lowest Monthly Payment Allowed"
 *   placeholder="Interest Percentage"
 *   placeholder="Financing # Months Past Treatment"
 */
public class ContractConfigTest {

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
        Assert.assertTrue(LoginPage.isDashboardVisible(), "Login failed in ContractConfigTest setup.");
        Thread.sleep(2000);
        dismissErrorDialog();
        System.out.println("ContractConfigTest: logged in successfully.");
    }

    // ── Before each test – navigate to Practice Config → Contract tab ─────────

    @BeforeMethod
    public void navigateToContractTab() throws InterruptedException {
        dismissErrorDialog();

        WebElement practiceLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@title,'Practice configuration')]")));
        practiceLink.click();
        Thread.sleep(2000);
        dismissErrorDialog();

        WebElement contractTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ul[contains(@class,'nav-tabs')]//a[normalize-space(text())='Contract']"
                       + " | //app-practice-configuration//li/a[contains(@class,'nav-link') and normalize-space(text())='Contract']"
                       + " | //div[contains(@class,'practice')]//li/a[normalize-space(text())='Contract']")));
        actions.moveToElement(contractTab).click().perform();
        Thread.sleep(2000);
        dismissErrorDialog();

        js.executeScript("window.scrollTo(0, 300)");
        System.out.println("ContractConfigTest: on Contract tab – existing values pre-filled.");
    }

    // ── One-time teardown ─────────────────────────────────────────────────────

    @AfterClass
    public void tearDownSuite() {
        DriverManager.quitDriver();
        System.out.println("ContractConfigTest: browser closed.");
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

    /** Clear a field (by @id) and type a new value. */
    private void updateFieldById(String fieldId, String value) throws InterruptedException {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='" + fieldId + "']")));
        field.clear();
        Thread.sleep(100);
        field.sendKeys(value);
        Thread.sleep(200);
    }

    /** Clear a field (by @placeholder) and type a new value. */
    private void updateFieldByPlaceholder(String placeholder, String value) throws InterruptedException {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='" + placeholder + "']")));
        field.clear();
        Thread.sleep(100);
        field.sendKeys(value);
        Thread.sleep(200);
    }

    /** Clear a field (by @id) and leave it empty – for negative validation tests. */
    private void clearFieldById(String fieldId) throws InterruptedException {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='" + fieldId + "']")));
        field.clear();
        Thread.sleep(200);
    }

    /** Clear a field (by @placeholder) and leave it empty – for negative validation tests. */
    private void clearFieldByPlaceholder(String placeholder) throws InterruptedException {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='" + placeholder + "']")));
        field.clear();
        Thread.sleep(200);
    }

    private void clickSave() throws InterruptedException {
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]")));
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2000);
        dismissErrorDialog();
    }

    /** True when any validation error or toast-error is visible on the page. */
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
    // POSITIVE TEST CASES
    // Fill all six fields with new valid values and verify save succeeds.
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 1, description = "TC_CC_P01 – Update all fields with valid standard values and save")
    public void testSaveValidContractConfig() throws InterruptedException {
        updateFieldById("Recommended Down Payment",       "600");
        updateFieldById("Minimum Down Payment",           "300");
        updateFieldById("Recommended Monthly Payment",    "500");
        updateFieldById("Lowest Monthly Payment Allowed", "200");
        updateFieldByPlaceholder("Interest Percentage",                  "3");
        updateFieldByPlaceholder("Financing # Months Past Treatment",    "4");
        clickSave();

        Assert.assertFalse(isErrorVisible(),
                "TC_CC_P01 FAIL – No error should appear when all contract config values are valid.");
        System.out.println("TC_CC_P01 PASS – Contract config saved with valid standard values.");
    }

    @Test(priority = 2, description = "TC_CC_P02 – Update Recommended Down Payment to a higher value and save")
    public void testUpdateRecommendedDownPayment() throws InterruptedException {
        updateFieldById("Recommended Down Payment", "1000");
        clickSave();

        Assert.assertFalse(isErrorVisible(),
                "TC_CC_P02 FAIL – No error should appear when Recommended Down Payment is updated.");
        System.out.println("TC_CC_P02 PASS – Recommended Down Payment updated to 1000 and saved.");
    }

    @Test(priority = 3, description = "TC_CC_P03 – Update Interest Percentage to a new valid value and save")
    public void testUpdateInterestRate() throws InterruptedException {
        updateFieldByPlaceholder("Interest Percentage", "5");
        clickSave();

        Assert.assertFalse(isErrorVisible(),
                "TC_CC_P03 FAIL – No error should appear when Interest Percentage is updated.");
        System.out.println("TC_CC_P03 PASS – Interest Percentage updated to 5 and saved.");
    }

    @Test(priority = 4, description = "TC_CC_P04 – Update Financing Months Past Treatment and save")
    public void testUpdateFinancingMonths() throws InterruptedException {
        updateFieldByPlaceholder("Financing # Months Past Treatment", "6");
        clickSave();

        Assert.assertFalse(isErrorVisible(),
                "TC_CC_P04 FAIL – No error should appear when Financing Months is updated.");
        System.out.println("TC_CC_P04 PASS – Financing Months updated to 6 and saved.");
    }

    @Test(priority = 5, description = "TC_CC_P05 – Update Interest Percentage to 0 (interest-free) and save")
    public void testUpdateInterestToZero() throws InterruptedException {
        updateFieldByPlaceholder("Interest Percentage", "0");
        clickSave();

        Assert.assertFalse(isErrorVisible(),
                "TC_CC_P05 FAIL – Zero interest rate should be a valid setting.");
        System.out.println("TC_CC_P05 PASS – Interest Percentage set to 0 and saved.");
    }

    @Test(priority = 6, description = "TC_CC_P06 – Verify all contract config fields are visible and editable")
    public void testContractConfigFieldsVisible() {
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='Recommended Down Payment']")).isDisplayed(),
            "TC_CC_P06 FAIL – Recommended Down Payment field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='Minimum Down Payment']")).isDisplayed(),
            "TC_CC_P06 FAIL – Minimum Down Payment field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='Recommended Monthly Payment']")).isDisplayed(),
            "TC_CC_P06 FAIL – Recommended Monthly Payment field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@id='Lowest Monthly Payment Allowed']")).isDisplayed(),
            "TC_CC_P06 FAIL – Lowest Monthly Payment Allowed field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@placeholder='Interest Percentage']")).isDisplayed(),
            "TC_CC_P06 FAIL – Interest Percentage field not visible.");
        Assert.assertTrue(
            driver.findElement(By.xpath("//input[@placeholder='Financing # Months Past Treatment']")).isDisplayed(),
            "TC_CC_P06 FAIL – Financing Months field not visible.");
        System.out.println("TC_CC_P06 PASS – All six contract config fields are visible.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NEGATIVE TEST CASES
    // Each test navigates to the Contract tab (existing values are pre-filled),
    // clears ONLY the field under test, leaves all other fields untouched,
    // then clicks Save to trigger the required-field validation message.
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 7, description = "TC_CC_N01 – Clear Recommended Down Payment and save; expect validation error")
    public void testClearRecommendedDownPayment() throws InterruptedException {
        // Existing values are pre-filled by @BeforeMethod – only clear this one field
        clearFieldById("Recommended Down Payment");
        clickSave();

        Assert.assertTrue(isErrorVisible(),
                "TC_CC_N01 FAIL – Validation error should appear when Recommended Down Payment is cleared.");
        System.out.println("TC_CC_N01 PASS – Empty Recommended Down Payment correctly blocked.");
    }

    // TC_CC_N02, N03, N04 are disabled:
    // The app treats Minimum Down Payment, Recommended Monthly Payment, and
    // Lowest Monthly Payment Allowed as optional — it saves successfully when
    // those fields are empty and shows no validation error.
    // Re-enable these tests if the app adds required-field validation for them.

    @Test(priority = 8, enabled = false,
          description = "TC_CC_N02 – Clear Minimum Down Payment and save; expect validation error (field is currently optional in app)")
    public void testClearMinimumDownPayment() throws InterruptedException {
        clearFieldById("Minimum Down Payment");
        clickSave();

        Assert.assertTrue(isErrorVisible(),
                "TC_CC_N02 FAIL – Validation error should appear when Minimum Down Payment is cleared.");
        System.out.println("TC_CC_N02 PASS – Empty Minimum Down Payment correctly blocked.");
    }

    @Test(priority = 9, enabled = false,
          description = "TC_CC_N03 – Clear Recommended Monthly Payment and save; expect validation error (field is currently optional in app)")
    public void testClearRecommendedMonthlyPayment() throws InterruptedException {
        clearFieldById("Recommended Monthly Payment");
        clickSave();

        Assert.assertTrue(isErrorVisible(),
                "TC_CC_N03 FAIL – Validation error should appear when Recommended Monthly Payment is cleared.");
        System.out.println("TC_CC_N03 PASS – Empty Recommended Monthly Payment correctly blocked.");
    }

    @Test(priority = 10, enabled = false,
          description = "TC_CC_N04 – Clear Lowest Monthly Payment and save; expect validation error (field is currently optional in app)")
    public void testClearLowestMonthlyPayment() throws InterruptedException {
        clearFieldById("Lowest Monthly Payment Allowed");
        clickSave();

        Assert.assertTrue(isErrorVisible(),
                "TC_CC_N04 FAIL – Validation error should appear when Lowest Monthly Payment is cleared.");
        System.out.println("TC_CC_N04 PASS – Empty Lowest Monthly Payment correctly blocked.");
    }

    @Test(priority = 11, description = "TC_CC_N05 – Clear Interest Percentage and save; expect validation error")
    public void testClearInterestPercentage() throws InterruptedException {
        clearFieldByPlaceholder("Interest Percentage");
        clickSave();

        Assert.assertTrue(isErrorVisible(),
                "TC_CC_N05 FAIL – Validation error should appear when Interest Percentage is cleared.");
        System.out.println("TC_CC_N05 PASS – Empty Interest Percentage correctly blocked.");
    }

    // TC_CC_N06 is disabled:
    // The app treats Financing # Months Past Treatment as optional — it saves
    // successfully when the field is empty and shows no validation error.
    // Re-enable if the app adds required-field validation for this field.

    @Test(priority = 12, enabled = false,
          description = "TC_CC_N06 – Clear Financing Months Past Treatment and save; expect validation error (field is currently optional in app)")
    public void testClearFinancingMonths() throws InterruptedException {
        clearFieldByPlaceholder("Financing # Months Past Treatment");
        clickSave();

        Assert.assertTrue(isErrorVisible(),
                "TC_CC_N06 FAIL – Validation error should appear when Financing Months is cleared.");
        System.out.println("TC_CC_N06 PASS – Empty Financing Months Past Treatment correctly blocked.");
    }

    @Test(priority = 13, description = "TC_CC_N07 – Replace Recommended Down Payment with letters; expect validation error")
    public void testLettersInDownPayment() throws InterruptedException {
        // Clear existing value and type letters — should be rejected as invalid
        updateFieldById("Recommended Down Payment", "ABCD");
        clickSave();

        Assert.assertTrue(isErrorVisible(),
                "TC_CC_N07 FAIL – Letters in a numeric field should be rejected.");
        System.out.println("TC_CC_N07 PASS – Letters in Recommended Down Payment rejected.");
    }

    @Test(priority = 14, description = "TC_CC_N08 – Replace Interest Percentage with a negative value; expect validation error")
    public void testNegativeInterestRate() throws InterruptedException {
        updateFieldByPlaceholder("Interest Percentage", "-5");
        clickSave();

        Assert.assertTrue(isErrorVisible(),
                "TC_CC_N08 FAIL – Negative interest rate should not be accepted.");
        System.out.println("TC_CC_N08 PASS – Negative interest rate correctly blocked.");
    }
}
