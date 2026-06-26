package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;
import com.opms.practice.LookupPage;
import com.opms.utils.TestDataGenerator;

import java.time.Duration;
import java.util.List;

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

/**
 * End-to-end test:
 *   TC1 – Create a minor patient with full Primary Contact details and save.
 *   TC2 – Search for the saved patient and open their profile.
 *   TC3 – Add insurance to the patient and confirm it saves.
 *
 * Patient name is stored as class fields so TC2 and TC3 can reference it.
 */
public class PatientCreationAndAddPatientInsurance {

    private WebDriver          driver;
    private WebDriverWait      wait;
    private Actions            actions;
    private JavascriptExecutor js;

    // Stored after TC1 so TC2 can search by name
    private String patientFirstName;
    private String patientLastName;

    private static final String PC_CARD =
        "//div[contains(@class,'card') and .//h5[contains(text(),'Primary Contact')]]";

    // ── One-time setup ────────────────────────────────────────────────────────

    @BeforeClass
    public void setUpSuite() throws InterruptedException {
        driver  = DriverManager.getDriver();
        wait    = new WebDriverWait(driver, Duration.ofSeconds(15));
        actions = new Actions(driver);
        js      = (JavascriptExecutor) driver;

        LoginPage.loginWith(LoginPage.VALID_EMAIL, LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isDashboardVisible(),
                "Login failed in PatientCreationAndAddPatientInsurance setup.");
        Thread.sleep(2000);
        System.out.println("PatientCreationAndAddPatientInsurance: logged in successfully.");

        // DEBUG: use existing patient to test TC2/TC3 without creating a new one each run
        patientFirstName = "gohxxbnipr";
        patientLastName  = "ctguywdtgi";
    }

    @AfterClass
    public void tearDownSuite() {
        DriverManager.quitDriver();
        System.out.println("PatientCreationAndAddPatientInsurance: browser closed.");
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

    private void openAddPatientForm() throws InterruptedException {
        dismissErrorDialog();

        List<WebElement> addIcons = driver.findElements(
                By.xpath("//i[contains(@class,'fa-user-plus')]"));
        if (addIcons.isEmpty() || !addIcons.get(0).isDisplayed()) {
            driver.navigate().back();
            Thread.sleep(2000);
            dismissErrorDialog();
        }

        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//i[contains(@class,'fa-user-plus')]")));
        actions.moveToElement(addBtn).click().perform();
        Thread.sleep(2000);
        dismissErrorDialog();
        System.out.println("Add Patient form opened.");
    }

    /** Fill the Patient Demographic section. */
    private void fillPatientForm(String prefix, String firstName, String lastName,
                                  String dob, String phone, String email,
                                  String gender, String language, String maritalStatus,
                                  String dentist) throws InterruptedException {
        if (prefix != null) {
            driver.findElement(By.id("Prefix")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("ng-dropdown-panel")))
                .findElement(By.xpath(".//span[text()='" + prefix + "']")).click();
        }
        if (firstName != null)
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("First Name")))
                .sendKeys(firstName);
        if (lastName != null)
            driver.findElement(By.id("Last Name")).sendKeys(lastName);

        if (dob != null) {
            WebElement dobField = driver.findElement(By.xpath(
                "//label[contains(text(),'Date of Birth')]" +
                "/ancestor::div[contains(@class,'form-group')]" +
                "//input[@placeholder='MM/DD/YYYY']"));
            dobField.click();
            Thread.sleep(500);
            dobField.sendKeys(dob);
            dobField.sendKeys(Keys.TAB);
            Thread.sleep(300);
        }
        if (phone != null)
            driver.findElement(By.id("Primary Phone")).sendKeys(phone);
        if (email != null)
            driver.findElement(By.id("Email Address")).sendKeys(email);
        if (gender != null)
            driver.findElement(By.xpath("//label[@for='patientGender_" + gender + "']")).click();
        if (language != null)
            driver.findElement(By.xpath("//label[@for='patientLanguagePreference_" + language + "']")).click();
        if (maritalStatus != null)
            driver.findElement(By.xpath("//label[@for='patientMaritalStatus_" + maritalStatus + "']")).click();
        if (dentist != null) {
            driver.findElement(By.xpath("//ng-select[@placeholder='Select Dentist']")).click();
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class,'ng-option')]//span[text()='" + dentist + "']")))
                .click();
        }
    }

    /** Fill the Address Information section. */
    private void fillAddressSection(String addr1, String addr2,
                                     String city, String state, String zip)
            throws InterruptedException {
        if (addr1 != null) driver.findElement(By.id("Address Line 1")).sendKeys(addr1);
        if (addr2 != null) driver.findElement(By.id("Address Line 2")).sendKeys(addr2);
        if (city  != null) driver.findElement(By.id("City")).sendKeys(city);
        if (state != null) {
            WebElement stateSelect = driver.findElement(
                    By.xpath("//ng-select[@bindlabel='stateName']"));
            stateSelect.click();
            Thread.sleep(400);
            stateSelect.findElement(By.tagName("input")).sendKeys(state);
            Thread.sleep(500);
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class,'ng-option')]//span[text()='" + state + "']")))
                .click();
            Thread.sleep(300);
        }
        if (zip != null) driver.findElement(By.id("Zip Code")).sendKeys(zip);
    }

    /** Fill the Primary Contact (guardian) section. */
    private void fillPrimaryContactSection(String firstName, String lastName,
                                            String dob, String email,
                                            String gender, String phone)
            throws InterruptedException {
        WebElement pcCard = driver.findElement(By.xpath(PC_CARD));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", pcCard);
        Thread.sleep(500);

        if (firstName != null) {
            WebElement fn = driver.findElement(By.xpath(PC_CARD + "//input[@id='First Name']"));
            fn.clear(); fn.sendKeys(firstName);
        }
        if (lastName != null) {
            WebElement ln = driver.findElement(By.xpath(PC_CARD + "//input[@id='Last Name']"));
            ln.clear(); ln.sendKeys(lastName);
        }
        if (dob != null) {
            List<WebElement> datepickers = driver.findElements(
                    By.xpath("//input[@placeholder='MM/DD/YYYY']"));
            WebElement pcDob = datepickers.get(datepickers.size() - 1);
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", pcDob);
            Thread.sleep(500);
            js.executeScript("window.scrollBy(0, -80);");
            Thread.sleep(300);
            wait.until(ExpectedConditions.elementToBeClickable(pcDob));
            pcDob.click();
            Thread.sleep(400);
            pcDob.sendKeys(Keys.HOME);
            Thread.sleep(200);
            pcDob.sendKeys(dob);
            Thread.sleep(300);
            pcDob.sendKeys(Keys.TAB);
            Thread.sleep(500);
        }
        if (email != null) {
            WebElement em = driver.findElement(By.xpath(PC_CARD + "//input[@id='Email Address']"));
            em.clear(); em.sendKeys(email);
        }
        if (gender != null) {
            WebElement genderEl = driver.findElement(By.xpath(
                    "//input[@id='patientContactGender_" + gender + "']"));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", genderEl);
            Thread.sleep(200);
            js.executeScript("arguments[0].click();", genderEl);
        }
        if (phone != null) {
            List<WebElement> phoneFields = driver.findElements(By.id("Primary phone"));
            WebElement ph = !phoneFields.isEmpty() ? phoneFields.get(0)
                    : driver.findElement(By.xpath(PC_CARD + "//input[@placeholder='Phone']"));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", ph);
            Thread.sleep(200);
            ph.clear(); ph.sendKeys(phone);
        }
    }

    private void clickSaveAndClose() throws InterruptedException {
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[text()='Save & Close']")));
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(3000);
        dismissErrorDialog();
    }

    private boolean isFormStillOpen() {
        try {
            return driver.findElement(By.xpath("//span[text()='Save & Close']")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TC1 – Create minor patient with full Primary Contact and save
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 1, enabled = false,
          description = "TC_PI_01 – Create minor patient with all Patient and Primary Contact details")
    public void testCreatePatientWithPrimaryContact() throws InterruptedException {
        openAddPatientForm();

        patientFirstName = TestDataGenerator.generateUniqueString();
        patientLastName  = TestDataGenerator.generateUniqueString();

        fillPatientForm(
                "Mr.",
                patientFirstName,
                patientLastName,
                "03152010",   // minor DOB → triggers Address + Primary Contact sections
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male", "English", "Single",
                "Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)");

        fillAddressSection(
                "123 Test Street", "Apt 1A", "Chicago", "Illinois", "60601");

        fillPrimaryContactSection(
                TestDataGenerator.generateUniqueString(),
                TestDataGenerator.generateUniqueString(),
                "05201980",
                TestDataGenerator.generateUniqueEmail(),
                "Female",
                TestDataGenerator.generatePhoneNumber());

        clickSaveAndClose();

        Assert.assertFalse(isFormStillOpen(),
                "TC_PI_01 FAIL – Patient form should close after saving.");
        System.out.println("TC_PI_01 PASS – Minor patient '" + patientFirstName + " " + patientLastName + "' created.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TC2 – Search for the saved patient and open their profile
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 2,
          description = "TC_PI_02 – Search for the created patient and open their profile")
    public void testSearchAndOpenPatient() throws InterruptedException {
        dismissErrorDialog();
        Thread.sleep(1000);

        String fullName = patientLastName + ", " + patientFirstName;

        WebElement searchBar = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='last name, first name ']")));
        searchBar.clear();
        searchBar.sendKeys(fullName);
        Thread.sleep(2500);

        // Try ng-option div first (ng-select dropdown), fall back to li span pattern
        WebElement patientResult = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ng-option') and contains(.,'" + patientLastName + "')]"
                        + " | //li[contains(.,'" + patientLastName + "')]")));
        patientResult.click();
        Thread.sleep(3000);
        dismissErrorDialog();

        // Verify patient profile opened
        boolean profileOpen = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + patientFirstName + "') or contains(text(),'" + patientLastName + "')]")).isEmpty();
        Assert.assertTrue(profileOpen,
                "TC_PI_02 FAIL – Patient profile should open after clicking search result.");
        System.out.println("TC_PI_02 PASS – Patient profile opened for: " + patientFirstName + " " + patientLastName);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TC3 – Add insurance to the patient
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 3,
          description = "TC_PI_03 – Add insurance to the patient and save",
          dependsOnMethods = "testSearchAndOpenPatient")
    public void testAddPatientInsurance() throws InterruptedException {
        dismissErrorDialog();

        // Scroll down to the Insurance section
        js.executeScript("window.scrollBy(0, 3000);");
        Thread.sleep(1500);

        // Click Add Insurance button
        WebElement addInsuranceBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[@class='insurance-summary-add-hide']"
                       + " | //button[contains(text(),'Add Insurance')]"
                       + " | //button[.//span[contains(text(),'Add Insurance')]]")));
        js.executeScript("arguments[0].click();", addInsuranceBtn);
        Thread.sleep(2000);
        dismissErrorDialog();

        // Select insurance company from ng-select dropdown
        WebElement insuranceDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-select[@id='Insurance Company']")));
        insuranceDropdown.click();
        Thread.sleep(500);
        // Type to filter the insurance name
        insuranceDropdown.findElement(By.tagName("input")).sendKeys(LookupPage.insurance);
        Thread.sleep(500);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ng-option')]//span[contains(text(),'"
                        + LookupPage.insurance + "')]"))).click();
        Thread.sleep(1000);

        // Scroll down to policyholder section
        js.executeScript("window.scrollBy(0, 4000);");
        Thread.sleep(1500);

        // Click "Who is the Policyholder?" — select Self/Patient
        WebElement policyholderBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(),'Who is the Policyholder?')]"
                       + "/following-sibling::div//button[contains(@class,'btn-secondary')]")));
        actions.moveToElement(policyholderBtn).click().perform();
        Thread.sleep(1500);

        // Click "Policyholder Address?" — use same address
        WebElement policyholderAddrBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(),'Policyholder Address?')]"
                       + "/following-sibling::div//button[contains(@class,'btn-secondary')]")));
        actions.moveToElement(policyholderAddrBtn).click().perform();
        Thread.sleep(1000);

        // Fill Group / Employer Name
        driver.findElement(By.xpath("//input[@id='Group / Employer Name']"))
              .sendKeys("AutomationTest");

        // Save insurance
        WebElement saveInsuranceBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]")));
        js.executeScript("arguments[0].click();", saveInsuranceBtn);
        Thread.sleep(2000);

        // Confirm the success dialog if it appears
        try {
            WebElement confirmBtn = driver.findElement(
                    By.xpath("//button[@class='swal2-confirm swal2-styled']"));
            if (confirmBtn.isDisplayed()) confirmBtn.click();
            Thread.sleep(1000);
        } catch (Exception ignored) {}

        dismissErrorDialog();

        // Verify insurance was saved — insurance company name should appear on the page
        boolean insuranceSaved = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + LookupPage.insurance + "')]")).isEmpty();
        Assert.assertTrue(insuranceSaved,
                "TC_PI_03 FAIL – Insurance should be saved and visible on patient profile.");
        System.out.println("TC_PI_03 PASS – Insurance '" + LookupPage.insurance + "' added to patient successfully.");
    }
}
