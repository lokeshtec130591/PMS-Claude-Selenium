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
            String subject        = nameWithoutExt;
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

        // Debug: print all visible button texts on the page
        try {
            List<WebElement> allBtns = driver.findElements(By.tagName("button"));
            StringBuilder btnLog = new StringBuilder("LetterBulkImportTest: buttons on page = [");
            for (WebElement b : allBtns) {
                if (b.isDisplayed()) btnLog.append("'").append(b.getText().trim()).append("' ");
            }
            System.out.println(btnLog.append("]").toString());
        } catch (Exception ignored) {}

        // Click Add New / Add Letter button — try multiple XPath patterns
        WebElement addBtn = null;
        String[] addBtnXpaths = {
            "//button[contains(normalize-space(.),'Add New')]",
            "//button[contains(normalize-space(.),'Add Letter')]",
            "//button[contains(normalize-space(.),'Add Template')]",
            "//button[contains(normalize-space(.),'New Template')]",
            "//button[.//i[contains(@class,'fa-plus')]]",
            "//button[contains(@class,'btn-primary')]",
            "//button[contains(@class,'add')]",
            "//a[contains(@class,'btn') and (contains(.,'Add') or contains(.,'New'))]"
        };
        for (String xp : addBtnXpaths) {
            try {
                addBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(By.xpath(xp)));
                System.out.println("LetterBulkImportTest: Add button found with XPath: " + xp);
                break;
            } catch (Exception ignored) {}
        }
        if (addBtn == null) {
            throw new RuntimeException("Could not find Add button on Template page for [" + fileName + "]");
        }
        js.executeScript("arguments[0].click();", addBtn);
        Thread.sleep(2000);
        dismissErrorDialog();

        // Template Name = filename without extension
        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Template Name']")));
        nameField.clear();
        nameField.sendKeys(templateName);
        Thread.sleep(300);

        // Subject = template name value
        WebElement subjectField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Subject']")));
        subjectField.clear();
        subjectField.sendKeys(subject);
        // Press Tab to release focus from Subject before touching Syncfusion
        subjectField.sendKeys(org.openqa.selenium.Keys.TAB);
        Thread.sleep(500);

        // Email Content (TinyMCE) — mandatory field, fill with letter content
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
            WebElement tinyFrame = shortWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("iframe[id*='tiny-angular'][id$='_ifr']")));
            driver.switchTo().frame(tinyFrame);
            WebElement tinymceBody = driver.findElement(By.id("tinymce"));
            tinymceBody.click();
            tinymceBody.sendKeys(letterContent);
            driver.switchTo().defaultContent();
            Thread.sleep(500);
            System.out.println("LetterBulkImportTest: filled Email Content for [" + fileName + "]");
        } catch (Exception e) {
            driver.switchTo().defaultContent();
            System.out.println("LetterBulkImportTest: TinyMCE not found for [" + fileName + "]: " + e.getMessage());
        }

        // Syncfusion document editor — paste full letter text via system clipboard
        if (letterContent != null && !letterContent.isEmpty()) {
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement syncEditor = shortWait.until(ExpectedConditions.presenceOfElementLocated(
                        By.id("syncontainer_editor_viewerContainer")));
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", syncEditor);
                Thread.sleep(500);

                // Use Actions.click() to properly transfer keyboard focus to Syncfusion
                actions.moveToElement(syncEditor).click().perform();
                Thread.sleep(1000);
                System.out.println("LetterBulkImportTest: clicked Syncfusion editor for [" + fileName + "]");

                // Set letter text on the OS system clipboard via Java AWT
                StringSelection selection = new StringSelection(letterContent);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                Thread.sleep(400);

                // Ctrl+A to clear existing content, then Ctrl+V to paste
                Robot robot = new Robot();
                robot.delay(300);
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_A);
                robot.keyRelease(KeyEvent.VK_A);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.delay(300);

                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.delay(1200);

                System.out.println("LetterBulkImportTest: pasted content into Syncfusion for [" + fileName + "]");
            } catch (Exception e) {
                System.out.println("LetterBulkImportTest: Syncfusion editor error for [" + fileName + "]: " + e.getMessage());
            }
        }

        // Save — target the form Save button (not the Syncfusion toolbar Save)
        // The form Save button is outside the ejs-documenteditorcontainer toolbar
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]"
                       + " | //div[not(ancestor::ejs-documenteditorcontainer)]//button[normalize-space(text())='Save']"
                       + " | //form//button[normalize-space(text())='Save']"
                       + " | //app-template//button[normalize-space(text())='Save']")));
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
