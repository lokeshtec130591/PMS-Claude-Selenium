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
        File[] files = folder.listFiles(f -> {
            String n = f.getName();
            // Skip Word temp files (start with ~$)
            if (n.startsWith("~$")) return false;
            String lower = n.toLowerCase();
            return lower.endsWith(".doc") || lower.endsWith(".docx");
        });

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

    private void clickFormSave() throws InterruptedException {
        // There are two btn-submit buttons — the LAST one is next to the Syncfusion editor.
        List<WebElement> submitBtns = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> {
                    List<WebElement> btns = d.findElements(
                            By.xpath("//button[contains(@class,'btn-submit')]"));
                    return btns.size() > 0 ? btns : null;
                });
        WebElement saveBtn = submitBtns.get(submitBtns.size() - 1);
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", saveBtn);
        Thread.sleep(400);
        js.executeScript("arguments[0].click();", saveBtn);
        System.out.println("LetterBulkImportTest: clicked form Save [class=" + saveBtn.getAttribute("class") + "]");
    }

    /** Returns true if a swal2 popup is visible and its text mentions duplicate/already exists. */
    private boolean isAlreadyExistsPopup() {
        try {
            WebElement popup = new WebDriverWait(driver, Duration.ofSeconds(4))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//div[contains(@class,'swal2-container')]")));
            String text = popup.getText().toLowerCase();
            return text.contains("already exist") || text.contains("duplicate") || text.contains("already been taken");
        } catch (Exception ignored) {}
        return false;
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
        Thread.sleep(2500);
        dismissErrorDialog();

        // Debug: dump all element IDs containing editor/syn/document keywords to find Syncfusion element
        try {
            String ids = (String) js.executeScript(
                "var els = document.querySelectorAll('[id]');" +
                "var result = [];" +
                "for(var i=0;i<els.length;i++){" +
                "  var id=els[i].id.toLowerCase();" +
                "  if(id.includes('syn')||id.includes('editor')||id.includes('document')||id.includes('attach')){" +
                "    result.push(els[i].id+'['+els[i].tagName+']');" +
                "  }" +
                "}" +
                "return result.join(', ');");
            System.out.println("LetterBulkImportTest: editor-related element IDs = " + ids);
        } catch (Exception ignored) {}

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

        // Attachment Content — Syncfusion Document Editor
        if (letterContent != null && !letterContent.isEmpty()) {
            try {
                // Wait for the Syncfusion container to be present
                WebElement syncContainer = new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector("ejs-documenteditorcontainer")));
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", syncContainer);
                Thread.sleep(500);

                // Use Syncfusion JS API to insert text directly into the editor
                Boolean inserted = (Boolean) js.executeScript(
                    "try {" +
                    "  var container = document.querySelector('ejs-documenteditorcontainer');" +
                    "  var inst = container.ej2_instances[0];" +
                    "  inst.documentEditor.openBlank();" +
                    "  inst.documentEditor.editor.insertText(arguments[0]);" +
                    "  return true;" +
                    "} catch(e) { return false; }",
                    letterContent);

                if (Boolean.TRUE.equals(inserted)) {
                    System.out.println("LetterBulkImportTest: inserted content into Syncfusion via JS API for [" + fileName + "]");
                } else {
                    // Fallback: click the editor and paste via clipboard
                    System.out.println("LetterBulkImportTest: JS API failed, trying clipboard paste for [" + fileName + "]");
                    actions.moveToElement(syncContainer).click().perform();
                    Thread.sleep(800);

                    StringSelection selection = new StringSelection(letterContent);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                    Thread.sleep(300);

                    Robot robot = new Robot();
                    robot.delay(200);
                    robot.keyPress(KeyEvent.VK_CONTROL);
                    robot.keyPress(KeyEvent.VK_A);
                    robot.keyRelease(KeyEvent.VK_A);
                    robot.keyRelease(KeyEvent.VK_CONTROL);
                    robot.delay(200);
                    robot.keyPress(KeyEvent.VK_CONTROL);
                    robot.keyPress(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_CONTROL);
                    robot.delay(1000);
                    System.out.println("LetterBulkImportTest: clipboard paste done for [" + fileName + "]");
                }
                Thread.sleep(500);
            } catch (Exception e) {
                System.out.println("LetterBulkImportTest: Syncfusion error for [" + fileName + "]: " + e.getMessage());
            }
        }

        clickFormSave();
        Thread.sleep(3000);

        // Check if "already exists" popup appeared — if so, rename with "2" suffix and save again
        String usedName = templateName;
        if (isAlreadyExistsPopup()) {
            System.out.println("LetterBulkImportTest: [" + templateName + "] already exists, renaming with suffix '2'.");
            // Dismiss the duplicate popup — wait for it to fully disappear
            try {
                WebElement okBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(@class,'swal2-confirm')] | //button[normalize-space(text())='OK']")));
                okBtn.click();
                Thread.sleep(1000);
                // Wait until the swal2 overlay is gone before interacting with the form
                new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.invisibilityOfElementLocated(
                        By.xpath("//div[contains(@class,'swal2-container')]")));
            } catch (Exception ignored) {}

            // Update Template Name field with "2" appended
            String renamedTemplate = templateName + "2";
            WebElement nameField2 = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@id='Template Name']")));
            nameField2.clear();
            nameField2.sendKeys(renamedTemplate);
            Thread.sleep(500);
            usedName = renamedTemplate;
            System.out.println("LetterBulkImportTest: renamed to [" + renamedTemplate + "], saving again.");

            // Save again — using same btn-submit form Save button
            clickFormSave();
            Thread.sleep(3000);

            // Dismiss success popup
            try {
                new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(@class,'swal2-confirm')] | //button[normalize-space(text())='OK']"))).click();
                Thread.sleep(500);
            } catch (Exception ignored) {}
        } else {
            // Dismiss success popup
            try {
                new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(@class,'swal2-confirm')] | //button[normalize-space(text())='OK']"))).click();
                Thread.sleep(500);
            } catch (Exception ignored) {}
        }
        dismissErrorDialog();

        String visibleError = getVisibleErrorText();
        Assert.assertTrue(visibleError.isEmpty(),
                "FAIL – Error visible after saving template [" + usedName + "]: " + visibleError);
        System.out.println("PASS – Template [" + usedName + "] imported successfully.");
    }
}
