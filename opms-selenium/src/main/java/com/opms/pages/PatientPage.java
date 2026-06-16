package com.opms.pages;

import com.opms.base.DriverManager;
import com.opms.practice.LookupPage;
import com.opms.practice.TreatmentsSetupPage;
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

public class PatientPage {

    public static String fullName;

    public static void createPatient() throws InterruptedException {
        WebDriver driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Actions actions = new Actions(driver);

        TreatmentsSetupPage.main(null);

        WebElement closeRightPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/i[@class='fa fa-solid collaspse_icon bg-info fa-angle-double-right']")));
        closeRightPanel.click();

        WebElement selectLocationDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//ng-select[@bindlabel='practiceLocationName']")));
        actions.moveToElement(selectLocationDropdown).click().perform();

        WebElement selectLocation = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[text()= '" + LookupPage.location + "']")));
        actions.moveToElement(selectLocation).click().perform();

        Thread.sleep(3000);

        WebElement addPatient = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//i[contains(@class, 'fa fa-solid fa-user-plus')]")));
        actions.moveToElement(addPatient).click().perform();

        Thread.sleep(3000);
        String uniqueFirstName = TestDataGenerator.generateUniqueString();
        String uniqueLastName  = TestDataGenerator.generateUniqueString();
        String uniqueEmail     = TestDataGenerator.generateUniqueEmail();

        WebElement prefixSelect = driver.findElement(By.id("Prefix"));
        prefixSelect.click();

        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("ng-dropdown-panel")));
        dropdown.findElement(By.xpath("//span[text()='Mr.']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("First Name")))
                .sendKeys(uniqueFirstName);
        driver.findElement(By.id("Last Name")).sendKeys(uniqueLastName);

        WebElement suffix = driver.findElement(By.xpath("//input[@id='Suffix']"));
        suffix.sendKeys("BBB");
        suffix.sendKeys(Keys.TAB);

        WebElement dob = driver.findElement(By.xpath("//input[@id='datepicker-1']"));
        dob.sendKeys("13051995");
        dob.sendKeys(Keys.TAB);

        driver.findElement(By.xpath("//input[@id='Primary Phone']")).sendKeys("5485654785");
        driver.findElement(By.xpath("//input[@id='Email Address']")).sendKeys(uniqueEmail);
        driver.findElement(By.xpath("//label[@for='patientGender_Male']")).click();
        driver.findElement(By.xpath("//label[@for='patientLanguagePreference_English']")).click();
        driver.findElement(By.xpath("//label[@for='patientMaritalStatus_Single']")).click();

        driver.findElement(By.xpath("//ng-select[@placeholder='Select Dentist']")).click();
        WebElement dentistDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@class='ng-option ng-option-marked']")));
        dentistDropdown.findElement(By.xpath(
                "//span[text()='Johnny Bairstow (Orthopedic Dental Tooth Surgery CLinic)']")).click();

        WebElement savePatient = driver.findElement(By.xpath("//span[text()='Save & Close']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", savePatient);

        Thread.sleep(3000);
        fullName = uniqueLastName + ", " + uniqueFirstName;

        WebElement patientSearch = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Search Patient' or @id='patientName']")));
        patientSearch.sendKeys(fullName);

        System.out.println("New Patient Created Successfully Name: " + fullName);

        Thread.sleep(2000);
        WebElement patientClick = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li//span[@class='first-name-letter btn-primary p-3']"
                        + "/following-sibling::span[contains(text(),'" + fullName + "')]")));
        patientClick.click();
    }

    public static void main(String[] args) throws InterruptedException {
        createPatient();
    }
}
