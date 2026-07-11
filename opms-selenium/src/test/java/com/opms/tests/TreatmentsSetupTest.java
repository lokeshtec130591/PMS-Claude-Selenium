package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;
import com.opms.practice.TreatmentsSetupPage;
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
 * Positive and negative tests for the Treatment Setup page.
 *
 * Navigation:
 *   Sidebar "Treatment" → tabs: Treatment Type | Treatment Option |
 *   Treatment Expense | Treatment Courtesy | Procedure Code Fee
 *
 * Fields per tab (confirmed from TreatmentsSetupPage.java):
 *
 * Treatment Type:
 *   placeholder="Type Name", id="Description"
 *   save: //button//span[contains(text(),'Save Treatment Type')]
 *
 * Treatment Option:
 *   id="Treatment Name", id="Provider Fee", id="Description",
 *   id="Minimum Down Payment", placeholder="Low Range", placeholder="High Range"
 *   save: //button//span[contains(text(),'Save Treatment Option')]
 *
 * Treatment Expense:
 *   id="Expense Name", id="Provider Fee"
 *   save: //button//span[contains(text(),'Save Treatment Expense')]
 *
 * Treatment Courtesy:
 *   id="Courtesy", id="Amount"
 *   save: //button//span[contains(text(),'Save Treatment Courtesy')]
 *
 * Procedure Code Fee:
 *   Read-only search / lookup – no add form in scope.
 */
public class TreatmentsSetupTest {

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
        Assert.assertTrue(LoginPage.isDashboardVisible(), "Login failed in TreatmentsSetupTest setup.");
        Thread.sleep(2000);
        dismissErrorDialog();
        System.out.println("TreatmentsSetupTest: logged in successfully.");
    }

    @AfterClass
    public void tearDownSuite() {
        DriverManager.quitDriver();
        System.out.println("TreatmentsSetupTest: browser closed.");
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

        System.out.println("TreatmentsSetupTest: navigated to Practice Config → " + sectionName);
    }

    

    private void clickTab(String tabText) throws InterruptedException {
        WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[@class='nav-item']/a[text()='" + tabText + "']")));
        actions.moveToElement(tab).click().perform();
        Thread.sleep(2000);
        dismissErrorDialog();
    }

    private void clickSaveWithLabel(String labelPartial) throws InterruptedException {
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]//span[contains(text(),'" + labelPartial + "')]")));
        actions.moveToElement(saveBtn).click().perform();
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

    // ═══════════════════════════════════════════════════════════════════════════
    // TREATMENT TYPE – POSITIVE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 1, description = "TC_TS_P01 – Treatment Setup page loads; Treatment Type tab visible")
    public void testTreatmentPageLoads() throws InterruptedException {
    	navigateToPracticeConfigSection("Treatment");

        Assert.assertTrue(
                driver.findElement(By.xpath("//div/input[@placeholder='search']")).isDisplayed(),
                "TC_TS_P01 FAIL – Search field not visible on Treatment Setup page.");
        System.out.println("TC_TS_P01 PASS – Treatment Setup page loaded successfully.");
    }

    @Test(priority = 2, description = "TC_TS_P02 – Search for the existing test treatment type")
    public void testSearchExistingTreatmentType() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys(TreatmentsSetupPage.txTypeName);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(By.xpath(
                "//div[@class='d-flex flex-column']//span[contains(text(),'"
                + TreatmentsSetupPage.txTypeName + "')]"));
        Assert.assertFalse(results.isEmpty(),
                "TC_TS_P02 FAIL – Existing treatment type should appear in search results.");
        System.out.println("TC_TS_P02 PASS – Existing treatment type found: " + TreatmentsSetupPage.txTypeName);
    }

    @Test(priority = 3, description = "TC_TS_P03 – Add a new treatment type with valid name and description")
    public void testAddNewTreatmentType() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");

        String typeName = "AutoType " + TestDataGenerator.generateUniqueString();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Type Name']"))).sendKeys(typeName);
        driver.findElement(By.xpath("//input[@id='Description']")).sendKeys(typeName + " description");

        clickSaveWithLabel("Save Treatment Type");

        Assert.assertFalse(isErrorVisible(),
                "TC_TS_P03 FAIL – No error should appear when saving a valid treatment type.");
        System.out.println("TC_TS_P03 PASS – New treatment type '" + typeName + "' added.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TREATMENT TYPE – NEGATIVE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 4, description = "TC_TS_N01 – Save treatment type without name (required field)")
    public void testSaveTreatmentTypeWithoutName() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");

        // Leave Type Name blank; fill description only
        driver.findElement(By.xpath("//input[@id='Description']")).sendKeys("No name description");

        clickSaveWithLabel("Save Treatment Type");

        Assert.assertTrue(isErrorVisible(),
                "TC_TS_N01 FAIL – Validation error should appear when Treatment Type name is blank.");
        System.out.println("TC_TS_N01 PASS – Missing Treatment Type name blocked.");
    }

    @Test(priority = 5, description = "TC_TS_N02 – Search for non-existent treatment type returns no result")
    public void testSearchNonExistentTreatmentType() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");

        String randomName = "NONEXIST_" + TestDataGenerator.generateUniqueString();
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys(randomName);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(By.xpath(
                "//div[@class='d-flex flex-column']//span[contains(text(),'" + randomName + "')]"));
        Assert.assertTrue(results.isEmpty(),
                "TC_TS_N02 FAIL – Non-existent treatment type should not appear in search.");
        System.out.println("TC_TS_N02 PASS – Non-existent treatment type search returns no result.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TREATMENT OPTION – POSITIVE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 6, description = "TC_TS_P04 – Search for the existing test treatment option")
    public void testSearchExistingTreatmentOption() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Treatment Option");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys(TreatmentsSetupPage.txOptionName);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(By.xpath(
                "//div[@class='d-flex flex-column']//span[contains(text(),'"
                + TreatmentsSetupPage.txOptionName + "')]"));
        Assert.assertFalse(results.isEmpty(),
                "TC_TS_P04 FAIL – Existing treatment option should appear in search.");
        System.out.println("TC_TS_P04 PASS – Existing treatment option found: " + TreatmentsSetupPage.txOptionName);
    }

    @Test(priority = 7, description = "TC_TS_P05 – Add a new treatment option with all required fields")
    public void testAddNewTreatmentOption() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Treatment Option");

        String optionName = "AutoOpt " + TestDataGenerator.generateUniqueString();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Treatment Name']"))).sendKeys(optionName);
        driver.findElement(By.xpath("//input[@id='Provider Fee']")).sendKeys("1500.00");
        driver.findElement(By.xpath("//input[@id='Description']")).sendKeys(optionName + " desc");
        driver.findElement(By.xpath("//input[@id='Minimum Down Payment']")).sendKeys("200");
        driver.findElement(By.xpath("//input[@placeholder='Low Range']")).sendKeys("12");
        driver.findElement(By.xpath("//input[@placeholder='High Range']")).sendKeys("6");

        clickSaveWithLabel("Save Treatment Option");

        Assert.assertFalse(isErrorVisible(),
                "TC_TS_P05 FAIL – No error should appear for a valid treatment option.");
        System.out.println("TC_TS_P05 PASS – New treatment option '" + optionName + "' added.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TREATMENT OPTION – NEGATIVE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 8, description = "TC_TS_N03 – Save treatment option without Treatment Name")
    public void testSaveTreatmentOptionWithoutName() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Treatment Option");

        driver.findElement(By.xpath("//input[@id='Provider Fee']")).sendKeys("1500.00");
        driver.findElement(By.xpath("//input[@id='Minimum Down Payment']")).sendKeys("200");
        driver.findElement(By.xpath("//input[@placeholder='Low Range']")).sendKeys("12");
        driver.findElement(By.xpath("//input[@placeholder='High Range']")).sendKeys("6");

        clickSaveWithLabel("Save Treatment Option");

        Assert.assertTrue(isErrorVisible(),
                "TC_TS_N03 FAIL – Validation error should appear when Treatment Option name is blank.");
        System.out.println("TC_TS_N03 PASS – Missing Treatment Option name blocked.");
    }

    @Test(priority = 9, description = "TC_TS_N04 – Save treatment option without Provider Fee")
    public void testSaveTreatmentOptionWithoutFee() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Treatment Option");

        driver.findElement(By.xpath("//input[@id='Treatment Name']"))
              .sendKeys("NoFee " + TestDataGenerator.generateUniqueString());
        driver.findElement(By.xpath("//input[@id='Minimum Down Payment']")).sendKeys("200");
        driver.findElement(By.xpath("//input[@placeholder='Low Range']")).sendKeys("12");
        driver.findElement(By.xpath("//input[@placeholder='High Range']")).sendKeys("6");

        clickSaveWithLabel("Save Treatment Option");

        Assert.assertTrue(isErrorVisible(),
                "TC_TS_N04 FAIL – Validation error should appear when Provider Fee is missing.");
        System.out.println("TC_TS_N04 PASS – Missing Provider Fee blocked.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TREATMENT EXPENSE – POSITIVE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 10, description = "TC_TS_P06 – Search for the existing test treatment expense")
    public void testSearchExistingTreatmentExpense() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Treatment Expense");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys(TreatmentsSetupPage.txExpense);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(By.xpath(
                "//div[@class='d-flex flex-column']//span[contains(text(),'"
                + TreatmentsSetupPage.txExpense + "')]"));
        Assert.assertFalse(results.isEmpty(),
                "TC_TS_P06 FAIL – Existing treatment expense should appear in search.");
        System.out.println("TC_TS_P06 PASS – Existing treatment expense found: " + TreatmentsSetupPage.txExpense);
    }

    @Test(priority = 11, description = "TC_TS_P07 – Add a new treatment expense with valid name and fee")
    public void testAddNewTreatmentExpense() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Treatment Expense");

        String expenseName = "AutoExp " + TestDataGenerator.generateUniqueString();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Expense Name']"))).sendKeys(expenseName);
        driver.findElement(By.xpath("//input[@id='Provider Fee']")).sendKeys("750.00");

        clickSaveWithLabel("Save Treatment Expense");

        Assert.assertFalse(isErrorVisible(),
                "TC_TS_P07 FAIL – No error should appear for a valid treatment expense.");
        System.out.println("TC_TS_P07 PASS – New treatment expense '" + expenseName + "' added.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TREATMENT EXPENSE – NEGATIVE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 12, description = "TC_TS_N05 – Save treatment expense without Expense Name")
    public void testSaveTreatmentExpenseWithoutName() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Treatment Expense");

        driver.findElement(By.xpath("//input[@id='Provider Fee']")).sendKeys("750.00");

        clickSaveWithLabel("Save Treatment Expense");

        Assert.assertTrue(isErrorVisible(),
                "TC_TS_N05 FAIL – Validation error should appear when Expense Name is blank.");
        System.out.println("TC_TS_N05 PASS – Missing Expense Name blocked.");
    }

    @Test(priority = 13, description = "TC_TS_N06 – Save treatment expense without Provider Fee")
    public void testSaveTreatmentExpenseWithoutFee() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Treatment Expense");

        driver.findElement(By.xpath("//input[@id='Expense Name']"))
              .sendKeys("NoFeeExp " + TestDataGenerator.generateUniqueString());

        clickSaveWithLabel("Save Treatment Expense");

        Assert.assertTrue(isErrorVisible(),
                "TC_TS_N06 FAIL – Validation error should appear when Provider Fee is missing.");
        System.out.println("TC_TS_N06 PASS – Missing Provider Fee for expense blocked.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TREATMENT COURTESY – POSITIVE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 14, description = "TC_TS_P08 – Search for the existing test treatment courtesy")
    public void testSearchExistingTreatmentCourtesy() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Treatment Courtesy");

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys(TreatmentsSetupPage.txCourtesy);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(By.xpath(
                "//div[@class='d-flex flex-column']//span[contains(text(),'"
                + TreatmentsSetupPage.txCourtesy + "')]"));
        Assert.assertFalse(results.isEmpty(),
                "TC_TS_P08 FAIL – Existing treatment courtesy should appear in search.");
        System.out.println("TC_TS_P08 PASS – Existing treatment courtesy found: " + TreatmentsSetupPage.txCourtesy);
    }

    @Test(priority = 15, description = "TC_TS_P09 – Add a new treatment courtesy with valid name and amount")
    public void testAddNewTreatmentCourtesy() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Treatment Courtesy");

        String courtesyName = "AutoCourt " + TestDataGenerator.generateUniqueString();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Courtesy']"))).sendKeys(courtesyName);
        driver.findElement(By.xpath("//input[@id='Amount']")).sendKeys("100.00");

        clickSaveWithLabel("Save Treatment Courtesy");

        Assert.assertFalse(isErrorVisible(),
                "TC_TS_P09 FAIL – No error should appear for a valid treatment courtesy.");
        System.out.println("TC_TS_P09 PASS – New treatment courtesy '" + courtesyName + "' added.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TREATMENT COURTESY – NEGATIVE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 16, description = "TC_TS_N07 – Save treatment courtesy without Courtesy name")
    public void testSaveTreatmentCourtesyWithoutName() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Treatment Courtesy");

        driver.findElement(By.xpath("//input[@id='Amount']")).sendKeys("100.00");

        clickSaveWithLabel("Save Treatment Courtesy");

        Assert.assertTrue(isErrorVisible(),
                "TC_TS_N07 FAIL – Validation error should appear when Courtesy name is blank.");
        System.out.println("TC_TS_N07 PASS – Missing Courtesy name blocked.");
    }

    @Test(priority = 17, description = "TC_TS_N08 – Save treatment courtesy without Amount")
    public void testSaveTreatmentCourtesyWithoutAmount() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Treatment Courtesy");

        driver.findElement(By.xpath("//input[@id='Courtesy']"))
              .sendKeys("NoAmt " + TestDataGenerator.generateUniqueString());

        clickSaveWithLabel("Save Treatment Courtesy");

        Assert.assertTrue(isErrorVisible(),
                "TC_TS_N08 FAIL – Validation error should appear when Amount is missing.");
        System.out.println("TC_TS_N08 PASS – Missing Courtesy amount blocked.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PROCEDURE CODE FEE – POSITIVE
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 18, description = "TC_TS_P10 – Search for existing procedure code D0150")
    public void testSearchExistingProcedureCode01() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Procedure Code Fee");

        String code = TreatmentsSetupPage.proccode01.split(" ")[0];
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys(code);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(By.xpath(
                "//div[@class='d-flex flex-column']//span[contains(text(),'" + code + "')]"));
        Assert.assertFalse(results.isEmpty(),
                "TC_TS_P10 FAIL – Procedure code D0150 should appear in search results.");
        System.out.println("TC_TS_P10 PASS – Procedure code " + code + " found in search.");
    }

    @Test(priority = 19, description = "TC_TS_P11 – Search for existing procedure code D0160")
    public void testSearchExistingProcedureCode02() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Procedure Code Fee");

        String code = TreatmentsSetupPage.proccode02.split(" ")[0];
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys(code);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(By.xpath(
                "//div[@class='d-flex flex-column']//span[contains(text(),'" + code + "')]"));
        Assert.assertFalse(results.isEmpty(),
                "TC_TS_P11 FAIL – Procedure code D0160 should appear in search results.");
        System.out.println("TC_TS_P11 PASS – Procedure code " + code + " found in search.");
    }

    @Test(priority = 20, description = "TC_TS_N09 – Search for non-existent procedure code returns no result")
    public void testSearchNonExistentProcedureCode() throws InterruptedException {
        navigateToPracticeConfigSection("Treatment");
        clickTab("Procedure Code Fee");

        String randomCode = "ZZZZ9999";
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchBox.clear();
        searchBox.sendKeys(randomCode);
        Thread.sleep(1500);

        List<WebElement> results = driver.findElements(By.xpath(
                "//div[@class='d-flex flex-column']//span[contains(text(),'" + randomCode + "')]"));
        Assert.assertTrue(results.isEmpty(),
                "TC_TS_N09 FAIL – Non-existent procedure code should return no results.");
        System.out.println("TC_TS_N09 PASS – Non-existent procedure code search returns no result.");
    }
}
