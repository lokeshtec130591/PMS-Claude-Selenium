package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Bulk-imports letters from Word documents in D:\LetterTemplate_Copy into the
 * Practice Configuration → Template section.
 *
 * For each .DOC / .DOCX file:
 *   Template Name  = filename without extension  (e.g. AWPP006)
 *   Subject        = "Subject- <filename>"       (e.g. Subject- AWPP006)
 *   Email Content  = skipped (not mandatory)
 *   Syncfusion     = full letter text pasted in
 */
public class LetterBulkImportTest {

    private static final String LETTER_FOLDER = "D:\\LetterTemplate_Copy";

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
        wait    = new WebDriverWait(driver, Duration.ofSeconds(20));
        actions = new Actions(driver);
        js      = (JavascriptExecutor) driver;

        LoginPage.loginWith(LoginPage.VALID_EMAIL, LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isDashboardVisible(), "Login failed in LetterBulkImportTest setup.");
        Thread.sleep(2000);
        dismissErrorDialog();
        System.out.println("LetterBulkImportTest: logged in successfully.");
    }

    @AfterClass
    public void tearDownSuite() {
        DriverManager.quitDriver();
        System.out.println("LetterBulkImportTest: browser closed.");
    }

    // ── Data provider: reads all .DOC/.DOCX files from the folder ─────────────

    @DataProvider(name = "letterFiles")
    public Object[][] letterFiles() throws Exception {
        File folder = new File(LETTER_FOLDER);
        File[] files = folder.listFiles(f ->
                f.getName().toLowerCase().endsWith(".doc") ||
                f.getName().toLowerCase().endsWith(".docx"));

        if (files == null || files.length == 0) {
            throw new RuntimeException("No Word documents found in: " + LETTER_FOLDER);
        }

        List<Object[]> rows = new ArrayList<>();
        for (File f : files) {
            String nameWithoutExt = f.getName().replaceAll("(?i)\\.(docx?)$", "");
            String subject        = "Subject- " + nameWithoutExt;
            String content        = extractDocText(f);
            rows.add(new Object[]{nameWithoutExt, subject, content, f.getName()});
        }
        return rows.toArray(new Object[0][]);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Extracts plain text from a .DOC or .DOCX file using Apache POI. */
    private String extractDocText(File file) {
        try {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".docx")) {
                try (org.apache.poi.xwpf.usermodel.XWPFDocument doc =
                             new org.apache.poi.xwpf.usermodel.XWPFDocument(new FileInputStream(file))) {
                    org.apache.poi.xwpf.extractor.XWPFWordExtractor extractor =
                            new org.apache.poi.xwpf.extractor.XWPFWordExtractor(doc);
                    return extractor.getText().trim();
                }
            } else {
                try (HWPFDocument doc = new HWPFDocument(new FileInputStream(file))) {
                    WordExtractor extractor = new WordExtractor(doc);
                    return extractor.getText().trim();
                }
            }
        } catch (Exception e) {
            System.out.println("LetterBulkImportTest: failed to read " + file.getName() + " – " + e.getMessage());
            return "";
        }
    }

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
        System.out.println("LetterBulkImportTest: navigated to Template section.");
    }

    private String getVisibleErrorText() {
        try {
            List<WebElement> errors = driver.findElements(By.xpath(
                "//div[contains(@class,'validation_msg')]//span[normalize-space(text())!='']"
                + " | //div[contains(@class,'alert-danger') and normalize-space(text())!='']"
                + " | //div[contains(@class,'toast-error')]"
                + " | //div[contains(@class,'invalid-feedback') and normalize-space(text())!='']"
                + " | //div[contains(@class,'swal2-html-container') and normalize-space(text())!='']"));
            StringBuilder sb = new StringBuilder();
            for (WebElement e : errors) {
                if (e.isDisplayed()) {
                    String t = e.getText().trim();
                    if (!t.isEmpty()) sb.append(t).append(" | ");
                }
            }
            return sb.toString();
        } catch (Exception ignored) {}
        return "";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BULK IMPORT TEST
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 1,
          dataProvider = "letterFiles",
          description = "Bulk import: create one template per Word document")
    public void testImportLetterFromDoc(String templateName, String subject,
                                        String letterContent, String fileName)
            throws InterruptedException {

        System.out.println("LetterBulkImportTest: importing [" + fileName + "]");
        navigateToLetterSection();

        // Click Add New / Add Letter button
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Add') and contains(.,'Letter')]"
                       + " | //button[contains(@class,'btn') and contains(.,'New')]"
                       + " | //button[contains(.,'Add New')]"
                       + " | //i[contains(@class,'fa-plus')]/..")));
        js.executeScript("arguments[0].click();", addBtn);
        Thread.sleep(2000);
        dismissErrorDialog();

        // Template Name = filename without extension
        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Template Name']")));
        nameField.clear();
        nameField.sendKeys(templateName);
        Thread.sleep(300);

        // Subject = "Subject- <filename>"
        WebElement subjectField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Subject']")));
        subjectField.clear();
        subjectField.sendKeys(subject);
        Thread.sleep(300);

        // Syncfusion document editor — paste full letter text via system clipboard
        if (letterContent != null && !letterContent.isEmpty()) {
            try {
                WebElement syncEditor = wait.until(ExpectedConditions.elementToBeClickable(
                        By.id("syncontainer_editor_viewerContainer")));
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", syncEditor);
                Thread.sleep(500);
                syncEditor.click();
                Thread.sleep(800);

                // Put letter text onto the real system clipboard using Java AWT
                StringSelection selection = new StringSelection(letterContent);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                Thread.sleep(300);

                // Ctrl+A (clear existing) then Ctrl+V (paste) via Robot so it hits the OS clipboard
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_A);
                robot.keyRelease(KeyEvent.VK_A);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                Thread.sleep(200);

                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                Thread.sleep(1000);

                System.out.println("LetterBulkImportTest: pasted content into Syncfusion for [" + fileName + "]");
            } catch (Exception e) {
                System.out.println("LetterBulkImportTest: Syncfusion editor error for [" + fileName + "]: " + e.getMessage());
            }
        }

        // Save
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]"
                       + " | //button[normalize-space(text())='Save']"
                       + " | //button[contains(.,'Save')]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", saveBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(3000);

        // Dismiss success popup
        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'swal2-confirm')] | //button[normalize-space(text())='OK']"))).click();
            Thread.sleep(500);
        } catch (Exception ignored) {}
        dismissErrorDialog();

        String visibleError = getVisibleErrorText();
        Assert.assertTrue(visibleError.isEmpty(),
                "FAIL – Error visible after saving template [" + templateName + "]: " + visibleError);
        System.out.println("PASS – Template [" + templateName + "] imported successfully.");
    }
}
