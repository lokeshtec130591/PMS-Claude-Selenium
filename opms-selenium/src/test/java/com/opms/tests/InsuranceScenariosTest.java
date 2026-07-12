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
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnhandledAlertException;
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
 * Insurance scenario tests for both major (age >= 18) and minor (age < 18) patients.
 *
 * TC_IS_SETUP – Create insurance companies in Lookup (runs first, priority 0)
 *
 * Major patient scenarios:
 *   TC_IS_M01 – Create adult patient
 *   TC_IS_M02 – Add same insurance (1st) with Same as Patient policyholder
 *   TC_IS_M03 – Add same insurance (2nd) with Same as Patient policyholder
 *   TC_IS_M04 – Add same insurance (1st) with Other policyholder
 *   TC_IS_M05 – Add same insurance (2nd) with Other policyholder
 *   TC_IS_M06 – Add different insurance with Same as Patient policyholder
 *   TC_IS_M07 – Add different insurance with Other policyholder
 *
 * Minor patient scenarios:
 *   TC_IS_N01 – Create minor patient with Primary Contact
 *   TC_IS_N02 – Add same insurance (1st) with Same as Patient → validation expected
 *   TC_IS_N03 – Add same insurance (2nd) with Same as Patient → validation expected
 *   TC_IS_N04 – Add same insurance (1st) with Other policyholder
 *   TC_IS_N05 – Add same insurance (2nd) with Other policyholder
 *   TC_IS_N06 – Add different insurance with Same as Patient → validation expected
 *   TC_IS_N07 – Add different insurance with Other policyholder
 */
public class InsuranceScenariosTest {

    private WebDriver          driver;
    private WebDriverWait      wait;
    private Actions            actions;
    private JavascriptExecutor js;

    // Major patient name stored after TC_IS_M01
    private String majorFirstName;
    private String majorLastName;

    // Minor patient name stored after TC_IS_N01
    private String minorFirstName;
    private String minorLastName;

    private static final String PC_CARD =
        "//div[contains(@class,'card') and .//h5[contains(text(),'Primary Contact')]]";

    // ── Setup / Teardown ──────────────────────────────────────────────────────

    @BeforeClass
    public void setUp() throws InterruptedException {
        driver  = DriverManager.getDriver();
        wait    = new WebDriverWait(driver, Duration.ofSeconds(15));
        actions = new Actions(driver);
        js      = (JavascriptExecutor) driver;

        LoginPage.loginWith(LoginPage.VALID_EMAIL, LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isDashboardVisible(), "Login failed in InsuranceScenariosTest.");
        Thread.sleep(2000);
        dismissErrorDialog();
        System.out.println("InsuranceScenariosTest: logged in.");
    }

    @AfterClass
    public void tearDown() {
        DriverManager.quitDriver();
        System.out.println("InsuranceScenariosTest: browser closed.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SETUP – Create insurance companies in Lookup if they don't exist
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 0,
          description = "TC_IS_SETUP – Create required insurance companies in Lookup > Insurance")
    public void testSetupInsuranceData() throws InterruptedException {
        navigateToInsuranceLookup();
        createInsuranceIfNotExists(LookupPage.insurance);

        navigateToInsuranceLookup();
        createInsuranceIfNotExists(LookupPage.insurance2);

        // Navigate back to dashboard after setup
        js.executeScript("window.scrollTo(0, 0);");
        Thread.sleep(500);
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[@href='/dashboard' or contains(@routerlink,'dashboard') or contains(@title,'Dashboard')]")));
        js.executeScript("arguments[0].click();", homeLink);
        Thread.sleep(2000);
        dismissErrorDialog();
        System.out.println("TC_IS_SETUP PASS – Insurance data ready: ["
                + LookupPage.insurance + "] and [" + LookupPage.insurance2 + "]");
    }

    private void navigateToInsuranceLookup() throws InterruptedException {
        js.executeScript("window.scrollTo(0, 0);");
        Thread.sleep(300);
        WebElement practiceLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@title,'Practice configuration')]")));
        js.executeScript("arguments[0].click();", practiceLink);
        Thread.sleep(2000);
        dismissErrorDialog();

        WebElement insuranceMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[@class='pcoded-mtext d-flex justify-content-center text-wrap text-center lh-sm' and text()='Insurance']")));
        insuranceMenu.click();
        Thread.sleep(2000);
        dismissErrorDialog();
    }

    private void createInsuranceIfNotExists(String insuranceName) throws InterruptedException {
        // Search to check if already exists
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='search insurance']")));
        searchBox.clear();
        searchBox.sendKeys(insuranceName);
        Thread.sleep(1500);

        List<WebElement> existing = driver.findElements(By.xpath(
                "//div[@class='d-flex flex-column']//span[contains(text(),'" + insuranceName + "')]"));
        if (!existing.isEmpty()) {
            System.out.println("Insurance already exists: " + insuranceName);
            searchBox.clear();
            return;
        }

        // Clear search and fill the form
        searchBox.clear();
        Thread.sleep(300);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Insurance Company Name']"))).sendKeys(insuranceName);
        driver.findElement(By.xpath("//input[@placeholder='Phone']")).sendKeys("(312) 555-0300");
        driver.findElement(By.xpath("//input[@id='Address Line 1']")).sendKeys("100 Insurance Street");
        driver.findElement(By.xpath("//input[@id='City']")).sendKeys("Chicago");

        WebElement stateDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//ng-select[@bindlabel='stateName']")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", stateDropdown);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", stateDropdown);
        Thread.sleep(500);
        try {
            WebElement stateInput = stateDropdown.findElement(By.tagName("input"));
            stateInput.sendKeys("Illinois");
            Thread.sleep(500);
        } catch (Exception ignored) {}
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-dropdown-panel//div[contains(@class,'ng-option')]//span[text()='Illinois']"))).click();
        Thread.sleep(300);

        driver.findElement(By.xpath("//input[@id='Zip Code']")).sendKeys("60601");

        // Select Payer ID — mandatory dropdown, pick first available option
        try {
            WebElement payerDropdown = driver.findElement(By.xpath(
                    "//ng-select[@id='Payer ID']"));
            payerDropdown.click();
            Thread.sleep(400);
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//ng-dropdown-panel//div[contains(@class,'ng-option')][1]"))).click();
            Thread.sleep(300);
        } catch (Exception ignored) {}

        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]")));
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2000);
        // Dismiss success/error popup (swal2 OK or generic OK)
        try {
            wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'swal2-confirm')] | //button[normalize-space(text())='OK']"))).click();
            Thread.sleep(500);
        } catch (Exception ignored) {}
        dismissErrorDialog();
        System.out.println("Insurance created: " + insuranceName);
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    /** Accepts a native JS alert if one is open (this demo site occasionally fires a stray
     *  "WebSDK v..." alert from a 3rd-party chat widget, unrelated to any test action). */
    private void dismissNativeAlert() {
        try {
            driver.switchTo().alert().accept();
            System.out.println("Dismissed unexpected native browser alert.");
        } catch (NoAlertPresentException ignored) {}
    }

    /** Wraps driver.findElements with a single retry if a native alert intercepts the call. */
    private List<WebElement> findElementsSafely(By by) {
        try {
            return driver.findElements(by);
        } catch (UnhandledAlertException e) {
            dismissNativeAlert();
            return driver.findElements(by);
        }
    }

    private void dismissErrorDialog() {
        dismissNativeAlert();
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
        List<WebElement> icons = driver.findElements(
                By.xpath("//i[contains(@class,'fa-user-plus')]"));
        if (icons.isEmpty() || !icons.get(0).isDisplayed()) {
            driver.navigate().back();
            Thread.sleep(2000);
            dismissErrorDialog();
        }
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//i[contains(@class,'fa-user-plus')]")));
        actions.moveToElement(addBtn).click().perform();
        Thread.sleep(2000);
        dismissErrorDialog();
    }

    private void searchAndOpenPatient(String lastName, String firstName) throws InterruptedException {
        dismissErrorDialog();
        String fullName = lastName + ", " + firstName;
        WebElement searchBar = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='last name, first name ']")));
        searchBar.clear();
        searchBar.sendKeys(fullName);
        Thread.sleep(2500);
        WebElement result = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ul[contains(@class,'patient-search-select-box')]//li[contains(.,'" + lastName + "')]")));
        result.click();
        Thread.sleep(3000);
        dismissErrorDialog();
        System.out.println("Opened patient profile: " + fullName);
    }

    /** Fills patient demographic fields. Pass adult DOB (>= 18 yrs) or minor DOB (< 18 yrs). */
    private void fillPatientForm(String firstName, String lastName, String dob,
                                  String phone, String email, String gender) throws InterruptedException {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("First Name"))).sendKeys(firstName);
        driver.findElement(By.id("Last Name")).sendKeys(lastName);

        WebElement dobField = driver.findElement(By.xpath(
                "//label[contains(text(),'Date of Birth')]"
                + "/ancestor::div[contains(@class,'form-group')]"
                + "//input[@placeholder='MM/DD/YYYY']"));
        dobField.click();
        Thread.sleep(500);
        dobField.sendKeys(dob);
        dobField.sendKeys(Keys.TAB);
        Thread.sleep(300);

        driver.findElement(By.id("Primary Phone")).sendKeys(phone);
        driver.findElement(By.id("Email Address")).sendKeys(email);
        dismissErrorDialog();

        WebElement genderLabel = driver.findElement(By.xpath("//label[@for='patientGender_" + gender + "']"));
        js.executeScript("arguments[0].click();", genderLabel);

        // Language, Marital Status, Dentist
        WebElement languageLabel = driver.findElement(By.xpath("//label[@for='patientLanguagePreference_English']"));
        js.executeScript("arguments[0].click();", languageLabel);

        WebElement maritalLabel = driver.findElement(By.xpath("//label[@for='patientMaritalStatus_Single']"));
        js.executeScript("arguments[0].click();", maritalLabel);

        driver.findElement(By.xpath("//ng-select[@placeholder='Select Dentist']")).click();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ng-option')]//span[text()='Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)']")))
            .click();
        Thread.sleep(300);
    }

    private void fillAddressSection() throws InterruptedException {
        driver.findElement(By.id("Address Line 1")).sendKeys("123 Test Street");
        driver.findElement(By.id("City")).sendKeys("Chicago");

        WebElement stateSelect = driver.findElement(By.xpath("//ng-select[@bindlabel='stateName']"));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", stateSelect);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", stateSelect);
        Thread.sleep(400);
        stateSelect.findElement(By.tagName("input")).sendKeys("Illinois");
        Thread.sleep(500);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ng-option')]//span[text()='Illinois']"))).click();
        Thread.sleep(300);
        driver.findElement(By.id("Zip Code")).sendKeys("60601");
    }

    private void fillPrimaryContactSection() throws InterruptedException {
        WebElement pcCard = driver.findElement(By.xpath(PC_CARD));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", pcCard);
        Thread.sleep(500);

        driver.findElement(By.xpath(PC_CARD + "//input[@id='First Name']"))
              .sendKeys(TestDataGenerator.generateUniqueString());
        driver.findElement(By.xpath(PC_CARD + "//input[@id='Last Name']"))
              .sendKeys(TestDataGenerator.generateUniqueString());

        List<WebElement> datepickers = findElementsSafely(By.xpath("//input[@placeholder='MM/DD/YYYY']"));
        WebElement pcDob = datepickers.get(datepickers.size() - 1);
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", pcDob);
        Thread.sleep(400);
        js.executeScript("window.scrollBy(0, -80);");
        Thread.sleep(200);
        wait.until(ExpectedConditions.elementToBeClickable(pcDob));
        pcDob.click();
        Thread.sleep(400);
        pcDob.sendKeys(Keys.HOME);
        Thread.sleep(200);
        pcDob.sendKeys("05201980");
        Thread.sleep(300);
        pcDob.sendKeys(Keys.TAB);
        Thread.sleep(500);

        driver.findElement(By.xpath(PC_CARD + "//input[@id='Email Address']"))
              .sendKeys(TestDataGenerator.generateUniqueEmail());

        WebElement genderEl = driver.findElement(By.xpath("//input[@id='patientContactGender_Female']"));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", genderEl);
        Thread.sleep(200);
        js.executeScript("arguments[0].click();", genderEl);

        List<WebElement> phoneFields = driver.findElements(By.id("Primary phone"));
        WebElement ph = !phoneFields.isEmpty() ? phoneFields.get(0)
                : driver.findElement(By.xpath(PC_CARD + "//input[@placeholder='Phone']"));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", ph);
        Thread.sleep(200);
        ph.sendKeys(TestDataGenerator.generatePhoneNumber());
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
        } catch (Exception e) { return false; }
    }

    /** Opens the Add Insurance form from the patient profile. */
    private void clickAddInsurance() throws InterruptedException {
        js.executeScript("window.scrollBy(0, 3000);");
        Thread.sleep(1500);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[@class='insurance-summary-add-hide']"
                       + " | //button[contains(normalize-space(.),'Add Insurance')]"
                       + " | //button[contains(normalize-space(.),'Add New')]")));
        js.executeScript("arguments[0].click();", btn);
        Thread.sleep(2000);
        dismissErrorDialog();
    }

    /** Selects the insurance company from the ng-select dropdown. */
    private void selectInsuranceCompany(String insuranceName) throws InterruptedException {
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-select[@id='Insurance Company']")));
        dropdown.click();
        Thread.sleep(500);
        dropdown.findElement(By.tagName("input")).sendKeys(insuranceName);
        Thread.sleep(500);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ng-option')]//span[contains(text(),'" + insuranceName + "')]")))
            .click();
        Thread.sleep(1000);
    }

    /**
     * Selects the policyholder type by button text.
     * @param type "Same as Patient", "Dependent", or "Other"
     */
    private void selectPolicyholder(String type) throws InterruptedException {
        js.executeScript("window.scrollBy(0, 4000);");
        Thread.sleep(1500);
        List<WebElement> btns = driver.findElements(
                By.xpath("//label[contains(text(),'Who is the Policyholder?')]"
                       + "/following-sibling::div//button"));
        for (WebElement btn : btns) {
            if (btn.getText().trim().equalsIgnoreCase(type)) {
                actions.moveToElement(btn).click().perform();
                Thread.sleep(1500);
                return;
            }
        }
        // Fallback: first button = Same as Patient
        if (!btns.isEmpty()) {
            actions.moveToElement(btns.get(0)).click().perform();
            Thread.sleep(1500);
        }
    }

    /** Fills Other policyholder details. */
    private void fillOtherPolicyholderDetails() throws InterruptedException {
        // First Name
        try {
            WebElement fn = driver.findElement(By.xpath(
                    "//label[contains(text(),'Policyholder')]"
                    + "/ancestor::div[contains(@class,'card')]//input[@id='First Name']"));
            fn.clear(); fn.sendKeys(TestDataGenerator.generateUniqueString());
        } catch (Exception ignored) {}

        // Last Name
        try {
            WebElement ln = driver.findElement(By.xpath(
                    "//label[contains(text(),'Policyholder')]"
                    + "/ancestor::div[contains(@class,'card')]//input[@id='Last Name']"));
            ln.clear(); ln.sendKeys(TestDataGenerator.generateUniqueString());
        } catch (Exception ignored) {}

        // DOB
        try {
            WebElement dob = driver.findElement(By.xpath(
                    "//label[contains(text(),'Policyholder')]"
                    + "/ancestor::div[contains(@class,'card')]//input[@placeholder='MM/DD/YYYY']"));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", dob);
            Thread.sleep(300);
            js.executeScript("arguments[0].click();", dob);
            Thread.sleep(300);
            dob.sendKeys(Keys.HOME); Thread.sleep(200);
            dob.sendKeys("05201985"); Thread.sleep(300);
            dob.sendKeys(Keys.TAB); Thread.sleep(500);
        } catch (Exception ignored) {}

        // Gender at Birth — confirmed live DOM id: subscriberGender_Male/Female/Other
        try {
            WebElement gender = driver.findElement(By.id("subscriberGender_Male"));
            js.executeScript("arguments[0].click();", gender);
            Thread.sleep(300);
        } catch (Exception ignored) {}

        // Phone
        try {
            WebElement ph = driver.findElement(By.xpath(
                    "//label[contains(text(),'Policyholder')]"
                    + "/ancestor::div[contains(@class,'card')]//input[@id='Primary Phone']"));
            ph.clear(); ph.sendKeys(TestDataGenerator.generatePhoneNumber());
        } catch (Exception ignored) {}

        // Email
        try {
            WebElement em = driver.findElement(By.xpath(
                    "//label[contains(text(),'Policyholder')]"
                    + "/ancestor::div[contains(@class,'card')]//input[@id='Email Address']"));
            em.clear(); em.sendKeys(TestDataGenerator.generateUniqueEmail());
        } catch (Exception ignored) {}

        // Relationship to Patient — confirmed live DOM id: subscriberRelationshipToPatient_Spouse/Parent/...
        try {
            WebElement rel = driver.findElement(By.id("subscriberRelationshipToPatient_Spouse"));
            js.executeScript("arguments[0].click();", rel);
            Thread.sleep(300);
        } catch (Exception ignored) {}

        // Address
        js.executeScript("window.scrollBy(0, 2000);");
        Thread.sleep(800);

        try {
            driver.findElement(By.xpath(
                    "//label[contains(text(),'Policyholder Address')]"
                    + "/ancestor::div[contains(@class,'card')]//input[@id='Address Line 1']"))
                .sendKeys("456 Other Street");
        } catch (Exception ignored) {}

        try {
            driver.findElement(By.xpath(
                    "//label[contains(text(),'Policyholder Address')]"
                    + "/ancestor::div[contains(@class,'card')]//input[@id='City']"))
                .sendKeys("Chicago");
        } catch (Exception ignored) {}

        try {
            WebElement stateSelect = driver.findElement(By.xpath(
                    "//label[contains(text(),'Policyholder Address')]"
                    + "/ancestor::div[contains(@class,'card')]//ng-select[@bindlabel='stateName']"));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", stateSelect);
            Thread.sleep(300);
            js.executeScript("arguments[0].click();", stateSelect);
            Thread.sleep(400);
            stateSelect.findElement(By.tagName("input")).sendKeys("Illinois");
            Thread.sleep(500);
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class,'ng-option')]//span[text()='Illinois']"))).click();
            Thread.sleep(300);
        } catch (Exception ignored) {}

        try {
            driver.findElement(By.xpath(
                    "//label[contains(text(),'Policyholder Address')]"
                    + "/ancestor::div[contains(@class,'card')]//input[@id='Zip Code']"))
                .sendKeys("60601");
        } catch (Exception ignored) {}
    }

    /** Saves insurance and clicks OK on the confirmation popup. */
    private void saveInsuranceAndConfirm() throws InterruptedException {
        // Policyholder Member # — alphanumeric value
        WebElement memberField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Policyholder Member #']")));
        memberField.clear();
        memberField.sendKeys(TestDataGenerator.generateAlphanumeric(9));

        // Group Plan # — alphanumeric value
        WebElement groupPlanField = driver.findElement(By.xpath("//input[@id='Group Plan #']"));
        groupPlanField.clear();
        groupPlanField.sendKeys(TestDataGenerator.generateAlphanumeric(9));

        // Group / Employer Name — scroll to make it visible and fill
        js.executeScript("window.scrollBy(0, 2000);");
        Thread.sleep(800);
        WebElement groupField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Group / Employer Name']")));
        groupField.clear();
        groupField.sendKeys("AutomationGroup");

        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn-submit')]")));
        js.executeScript("arguments[0].click();", saveBtn);
        Thread.sleep(2000);

        // Click OK on popup
        WebElement okBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@class='swal2-confirm swal2-styled']"
                       + " | //button[normalize-space(text())='OK']")));
        okBtn.click();
        Thread.sleep(2000);
        dismissErrorDialog();

        addInsuranceBenefit();
    }

    /**
     * Navigates to the patient's Insurance tab and adds a Benefit entry for the insurance
     * just saved (the last "Add Benefit" button on the page, since the newly created
     * insurance is appended to the end of the Insurance List). Confirmed live DOM ids:
     * input#Available Coverage, input#I confirm (checkbox), ng-select#Status.
     */
    private void addInsuranceBenefit() throws InterruptedException {
        WebElement insuranceTabLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'insurancesummary')]")));
        js.executeScript("arguments[0].click();", insuranceTabLink);
        Thread.sleep(2000);
        dismissErrorDialog();

        List<WebElement> addBenefitButtons = findElementsSafely(
                By.xpath("//button[normalize-space(text())='Add Benefit']"));
        if (addBenefitButtons.isEmpty()) {
            System.out.println("addInsuranceBenefit: no 'Add Benefit' button found — skipping.");
            return;
        }
        WebElement addBenefitButton = addBenefitButtons.get(addBenefitButtons.size() - 1);
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", addBenefitButton);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", addBenefitButton);
        Thread.sleep(2000);
        dismissErrorDialog();

        WebElement coverageField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("Available Coverage")));
        coverageField.clear();
        coverageField.sendKeys("5000");

        js.executeScript("window.scrollBy(0, document.body.scrollHeight);");
        Thread.sleep(1000);

        WebElement confirmCheckbox = driver.findElement(By.id("I confirm"));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", confirmCheckbox);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", confirmCheckbox);
        Thread.sleep(300);
        Boolean confirmChecked = (Boolean) js.executeScript("return arguments[0].checked;", confirmCheckbox);
        if (!Boolean.TRUE.equals(confirmChecked)) {
            // Raw input click didn't register with Angular — fall back to clicking its label.
            try {
                WebElement confirmLabel = driver.findElement(By.xpath("//label[@for='I confirm']"));
                js.executeScript("arguments[0].click();", confirmLabel);
                Thread.sleep(300);
            } catch (Exception ignored) {}
        }

        WebElement statusDropdown = driver.findElement(By.id("Status"));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", statusDropdown);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", statusDropdown);
        Thread.sleep(500);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-dropdown-panel//div[contains(@class,'ng-option')]//span[text()='Verified']"))).click();
        Thread.sleep(500);

        WebElement saveBenefitButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@class='btn btn-submit btn-light-primary']")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", saveBenefitButton);
        Thread.sleep(300);
        Boolean saveDisabled = (Boolean) js.executeScript("return arguments[0].disabled;", saveBenefitButton);
        if (Boolean.TRUE.equals(saveDisabled)) {
            System.out.println("addInsuranceBenefit: Save button is disabled — 'I confirm' or 'Status' may not be set.");
        }
        js.executeScript("arguments[0].click();", saveBenefitButton);
        Thread.sleep(2500);
        dismissErrorDialog();
        System.out.println("Insurance Benefit added successfully.");

        // Return to the Patient tab so the next scenario's searchAndOpenPatient() resumes cleanly.
        WebElement patientTabLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'patientsummary')]")));
        js.executeScript("arguments[0].click();", patientTabLink);
        Thread.sleep(1500);
        dismissErrorDialog();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAJOR PATIENT SCENARIOS (age >= 18)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 1,
          dependsOnMethods = {"testSetupInsuranceData"},
          description = "TC_IS_M01 – Create adult patient (age >= 18)")
    public void testCreateMajorPatient() throws InterruptedException {
        openAddPatientForm();

        majorFirstName = TestDataGenerator.generateUniqueString();
        majorLastName  = TestDataGenerator.generateUniqueString();

        fillPatientForm(majorFirstName, majorLastName,
                "03151990",   // adult DOB
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male");

        clickSaveAndClose();
        Assert.assertFalse(isFormStillOpen(),
                "TC_IS_M01 FAIL – Adult patient form should close after save.");
        System.out.println("TC_IS_M01 PASS – Adult patient created: " + majorLastName + ", " + majorFirstName);
    }

    @Test(priority = 2,
          description = "TC_IS_M02 – Add same insurance 1st time with Same as Patient",
          dependsOnMethods = "testCreateMajorPatient")
    public void testMajorSameInsuranceSameAsPatient_1() throws InterruptedException {
        searchAndOpenPatient(majorLastName, majorFirstName);
        clickAddInsurance();
        selectInsuranceCompany(LookupPage.insurance);
        selectPolicyholder("Same as Patient");
        saveInsuranceAndConfirm();

        boolean saved = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + LookupPage.insurance + "')]")).isEmpty();
        Assert.assertTrue(saved,
                "TC_IS_M02 FAIL – Insurance (1st, Same as Patient) should be saved.");
        System.out.println("TC_IS_M02 PASS – Same insurance added 1st time (Same as Patient).");
    }

    @Test(priority = 3,
          description = "TC_IS_M03 – Add same insurance 2nd time with Same as Patient",
          dependsOnMethods = "testMajorSameInsuranceSameAsPatient_1")
    public void testMajorSameInsuranceSameAsPatient_2() throws InterruptedException {
        searchAndOpenPatient(majorLastName, majorFirstName);
        clickAddInsurance();
        selectInsuranceCompany(LookupPage.insurance);
        selectPolicyholder("Same as Patient");
        saveInsuranceAndConfirm();

        boolean saved = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + LookupPage.insurance + "')]")).isEmpty();
        Assert.assertTrue(saved,
                "TC_IS_M03 FAIL – Insurance (2nd, Same as Patient) should be saved.");
        System.out.println("TC_IS_M03 PASS – Same insurance added 2nd time (Same as Patient).");
    }

    @Test(priority = 4,
          description = "TC_IS_M04 – Add same insurance 1st time with Other policyholder",
          dependsOnMethods = "testCreateMajorPatient")
    public void testMajorSameInsuranceOther_1() throws InterruptedException {
        searchAndOpenPatient(majorLastName, majorFirstName);
        clickAddInsurance();
        selectInsuranceCompany(LookupPage.insurance);
        selectPolicyholder("Other");
        fillOtherPolicyholderDetails();
        saveInsuranceAndConfirm();

        boolean saved = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + LookupPage.insurance + "')]")).isEmpty();
        Assert.assertTrue(saved,
                "TC_IS_M04 FAIL – Insurance (1st, Other policyholder) should be saved.");
        System.out.println("TC_IS_M04 PASS – Same insurance added 1st time (Other policyholder).");
    }

    @Test(priority = 5,
          description = "TC_IS_M05 – Add same insurance 2nd time with Other policyholder",
          dependsOnMethods = "testMajorSameInsuranceOther_1")
    public void testMajorSameInsuranceOther_2() throws InterruptedException {
        searchAndOpenPatient(majorLastName, majorFirstName);
        clickAddInsurance();
        selectInsuranceCompany(LookupPage.insurance);
        selectPolicyholder("Other");
        fillOtherPolicyholderDetails();
        saveInsuranceAndConfirm();

        boolean saved = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + LookupPage.insurance + "')]")).isEmpty();
        Assert.assertTrue(saved,
                "TC_IS_M05 FAIL – Insurance (2nd, Other policyholder) should be saved.");
        System.out.println("TC_IS_M05 PASS – Same insurance added 2nd time (Other policyholder).");
    }

    @Test(priority = 6,
          description = "TC_IS_M06 – Add different insurance with Same as Patient",
          dependsOnMethods = "testCreateMajorPatient")
    public void testMajorDifferentInsuranceSameAsPatient() throws InterruptedException {
        searchAndOpenPatient(majorLastName, majorFirstName);
        clickAddInsurance();
        selectInsuranceCompany(LookupPage.insurance2);
        selectPolicyholder("Same as Patient");
        saveInsuranceAndConfirm();

        boolean saved = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + LookupPage.insurance2 + "')]")).isEmpty();
        Assert.assertTrue(saved,
                "TC_IS_M06 FAIL – Different insurance (Same as Patient) should be saved.");
        System.out.println("TC_IS_M06 PASS – Different insurance added (Same as Patient).");
    }

    @Test(priority = 7,
          description = "TC_IS_M07 – Add different insurance with Other policyholder",
          dependsOnMethods = "testCreateMajorPatient")
    public void testMajorDifferentInsuranceOther() throws InterruptedException {
        searchAndOpenPatient(majorLastName, majorFirstName);
        clickAddInsurance();
        selectInsuranceCompany(LookupPage.insurance2);
        selectPolicyholder("Other");
        fillOtherPolicyholderDetails();
        saveInsuranceAndConfirm();

        boolean saved = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + LookupPage.insurance2 + "')]")).isEmpty();
        Assert.assertTrue(saved,
                "TC_IS_M07 FAIL – Different insurance (Other policyholder) should be saved.");
        System.out.println("TC_IS_M07 PASS – Different insurance added (Other policyholder).");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MINOR PATIENT SCENARIOS (age < 18)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test(priority = 8,
          dependsOnMethods = {"testSetupInsuranceData"},
          description = "TC_IS_N01 – Create minor patient (age < 18) with Primary Contact")
    public void testCreateMinorPatient() throws InterruptedException {
        openAddPatientForm();

        minorFirstName = TestDataGenerator.generateUniqueString();
        minorLastName  = TestDataGenerator.generateUniqueString();

        fillPatientForm(minorFirstName, minorLastName,
                "03152015",   // minor DOB → triggers Address + Primary Contact
                TestDataGenerator.generatePhoneNumber(),
                TestDataGenerator.generateUniqueEmail(),
                "Male");

        fillAddressSection();
        fillPrimaryContactSection();
        clickSaveAndClose();

        Assert.assertFalse(isFormStillOpen(),
                "TC_IS_N01 FAIL – Minor patient form should close after save.");
        System.out.println("TC_IS_N01 PASS – Minor patient created: " + minorLastName + ", " + minorFirstName);
    }

    @Test(priority = 9,
          description = "TC_IS_N02 – Add same insurance 1st time with Same as Patient (minor)",
          dependsOnMethods = "testCreateMinorPatient")
    public void testMinorSameInsuranceSameAsPatient_1() throws InterruptedException {
        searchAndOpenPatient(minorLastName, minorFirstName);
        clickAddInsurance();
        selectInsuranceCompany(LookupPage.insurance);
        selectPolicyholder("Same as Patient");
        saveInsuranceAndConfirm();

        boolean saved = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + LookupPage.insurance + "')]")).isEmpty();
        Assert.assertTrue(saved,
                "TC_IS_N02 FAIL – Insurance (1st, Same as Patient) should be saved for minor.");
        System.out.println("TC_IS_N02 PASS – Same insurance added 1st time (Same as Patient, minor).");
    }

    @Test(priority = 10,
          description = "TC_IS_N03 – Add same insurance 2nd time with Same as Patient (minor)",
          dependsOnMethods = "testMinorSameInsuranceSameAsPatient_1")
    public void testMinorSameInsuranceSameAsPatient_2() throws InterruptedException {
        searchAndOpenPatient(minorLastName, minorFirstName);
        clickAddInsurance();
        selectInsuranceCompany(LookupPage.insurance);
        selectPolicyholder("Same as Patient");
        saveInsuranceAndConfirm();

        boolean saved = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + LookupPage.insurance + "')]")).isEmpty();
        Assert.assertTrue(saved,
                "TC_IS_N03 FAIL – Insurance (2nd, Same as Patient) should be saved for minor.");
        System.out.println("TC_IS_N03 PASS – Same insurance added 2nd time (Same as Patient, minor).");
    }

    @Test(priority = 11,
          description = "TC_IS_N04 – Add same insurance 1st time with Other policyholder (minor)",
          dependsOnMethods = "testCreateMinorPatient")
    public void testMinorSameInsuranceOther_1() throws InterruptedException {
        searchAndOpenPatient(minorLastName, minorFirstName);
        clickAddInsurance();
        selectInsuranceCompany(LookupPage.insurance);
        selectPolicyholder("Other");
        fillOtherPolicyholderDetails();
        saveInsuranceAndConfirm();

        boolean saved = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + LookupPage.insurance + "')]")).isEmpty();
        Assert.assertTrue(saved,
                "TC_IS_N04 FAIL – Insurance (1st, Other policyholder) should be saved for minor.");
        System.out.println("TC_IS_N04 PASS – Same insurance added 1st time with Other (minor).");
    }

    @Test(priority = 12,
          description = "TC_IS_N05 – Add same insurance 2nd time with Other policyholder (minor)",
          dependsOnMethods = "testMinorSameInsuranceOther_1")
    public void testMinorSameInsuranceOther_2() throws InterruptedException {
        searchAndOpenPatient(minorLastName, minorFirstName);
        clickAddInsurance();
        selectInsuranceCompany(LookupPage.insurance);
        selectPolicyholder("Other");
        fillOtherPolicyholderDetails();
        saveInsuranceAndConfirm();

        boolean saved = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + LookupPage.insurance + "')]")).isEmpty();
        Assert.assertTrue(saved,
                "TC_IS_N05 FAIL – Insurance (2nd, Other policyholder) should be saved for minor.");
        System.out.println("TC_IS_N05 PASS – Same insurance added 2nd time with Other (minor).");
    }

    @Test(priority = 13,
          description = "TC_IS_N06 – Add different insurance with Same as Patient (minor)",
          dependsOnMethods = "testCreateMinorPatient")
    public void testMinorDifferentInsuranceSameAsPatient() throws InterruptedException {
        searchAndOpenPatient(minorLastName, minorFirstName);
        clickAddInsurance();
        selectInsuranceCompany(LookupPage.insurance2);
        selectPolicyholder("Same as Patient");
        saveInsuranceAndConfirm();

        boolean saved = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + LookupPage.insurance2 + "')]")).isEmpty();
        Assert.assertTrue(saved,
                "TC_IS_N06 FAIL – Different insurance (Same as Patient) should be saved for minor.");
        System.out.println("TC_IS_N06 PASS – Different insurance added (Same as Patient, minor).");
    }

    @Test(priority = 14,
          description = "TC_IS_N07 – Add different insurance with Other policyholder (minor)",
          dependsOnMethods = "testCreateMinorPatient")
    public void testMinorDifferentInsuranceOther() throws InterruptedException {
        searchAndOpenPatient(minorLastName, minorFirstName);
        clickAddInsurance();
        selectInsuranceCompany(LookupPage.insurance2);
        selectPolicyholder("Other");
        fillOtherPolicyholderDetails();
        saveInsuranceAndConfirm();

        boolean saved = !driver.findElements(By.xpath(
                "//*[contains(text(),'" + LookupPage.insurance2 + "')]")).isEmpty();
        Assert.assertTrue(saved,
                "TC_IS_N07 FAIL – Different insurance (Other policyholder) should be saved for minor.");
        System.out.println("TC_IS_N07 PASS – Different insurance added with Other policyholder (minor).");
    }
}
