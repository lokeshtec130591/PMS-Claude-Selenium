package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;
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
 * Tests for Practice Configuration → Letter tab.
 *
 * TC_LT_P01 – Add a new letter and save successfully
 */
public class LetterTest {

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
        Assert.assertTrue(LoginPage.isDashboardVisible(), "Login failed in LetterTest setup.");
        Thread.sleep(2000);
        dismissErrorDialog();
        System.out.println("LetterTest: logged in successfully.");
    }

    @AfterClass
    public void tearDownSuite() {
        DriverManager.quitDriver();
        System.out.println("LetterTest: browser closed.");
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

    private void navigateToLetterSection() throws InterruptedException {
        dismissErrorDialog();
        js.executeScript("window.scrollTo(0, 0);");
        Thread.sleep(300);

        WebElement practiceLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@title,'Practice configuration')]")));
        js.executeScript("arguments[0].click();", practiceLink);
        Thread.sleep(2000);
        dismissErrorDialog();

        WebElement letterMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath(String.format(PCODED_MTEXT, "Template"))));
        js.executeScript("arguments[0].click();", letterMenu);
        Thread.sleep(2000);
        dismissErrorDialog();
        System.out.println("LetterTest: navigated to Letter section.");
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
    // POSITIVE TEST CASES
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 1, description = "TC_LT_P01 – Add a new letter and save successfully")
    public void testAddNewLetter() throws InterruptedException {
        navigateToLetterSection();

        // Click Add New Letter button
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Add') and contains(.,'Letter')]"
                       + " | //button[contains(@class,'btn') and contains(.,'New')]"
                       + " | //button[contains(.,'Add New')]"
                       + " | //i[contains(@class,'fa-plus')]/..")));
        js.executeScript("arguments[0].click();", addBtn);
        Thread.sleep(2000);
        dismissErrorDialog();
        System.out.println("LetterTest: clicked Add New Letter button.");

        // Fill Template Name
        String letterName = "AutoLetter_" + TestDataGenerator.generateUniqueString();
        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Template Name']")));
        nameField.clear();
        nameField.sendKeys(letterName);
        Thread.sleep(300);

        // Fill Subject (mandatory)
        WebElement subjectField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Subject']")));
        subjectField.clear();
        subjectField.sendKeys("Automation Test Subject");
        Thread.sleep(300);

        // Fill Email Content via TinyMCE iframe
        try {
            WebElement tinyFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("iframe[id*='tiny-angular'][id$='_ifr']")));
            driver.switchTo().frame(tinyFrame);
            WebElement tinymceBody = driver.findElement(By.id("tinymce"));
            tinymceBody.click();
            tinymceBody.sendKeys("Automation test email content.");
            driver.switchTo().defaultContent();
            Thread.sleep(300);
        } catch (Exception e) {
            System.out.println("LetterTest: TinyMCE iframe not found: " + e.getMessage());
            driver.switchTo().defaultContent();
        }

        // Fill Attachment Content via Syncfusion document editor
        try {
            WebElement syncEditor = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("syncontainer_editor_viewerContainer")));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", syncEditor);
            Thread.sleep(300);
            syncEditor.click();
            Thread.sleep(500);
            actions.sendKeys("Automation test attachment content.").perform();
            Thread.sleep(300);
        } catch (Exception e) {
            System.out.println("LetterTest: Syncfusion editor not found: " + e.getMessage());
        }

        // Click Save button
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]"
                       + " | //button[normalize-space(text())='Save']"
                       + " | //button[contains(.,'Save')]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", saveBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2000);

        // Dismiss success popup
        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'swal2-confirm')] | //button[normalize-space(text())='OK']"))).click();
            Thread.sleep(500);
        } catch (Exception ignored) {}
        dismissErrorDialog();

        Assert.assertFalse(isErrorVisible(),
                "TC_LT_P01 FAIL – No error should appear when a valid letter is saved.");
        System.out.println("TC_LT_P01 PASS – New letter [" + letterName + "] added and saved successfully.");
    }
}
