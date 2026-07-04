package com.opms.practice;

import com.opms.base.DriverManager;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LookupPage {

    public static final String location = "Mumbai";
    public static final String insurance  = "Hanover Insurance Company";
    public static final String insurance2 = "Delta Insurance";

    public static void addLocation(WebDriver driver, String location, Actions actions)
            throws InterruptedException {

        ContractConfigPage.main(null);

        WebElement clickLocationMenu = driver.findElement(By.xpath(
                "//span[@class='pcoded-mtext d-flex justify-content-center text-wrap text-center lh-sm' and text()='Lookup']"));
        clickLocationMenu.click();

        WebElement searchLocation = driver.findElement(By.xpath("//input[@placeholder='search location']"));
        searchLocation.sendKeys(location);

        List<WebElement> findLocation = driver.findElements(
                By.xpath("//div[@class='d-flex flex-column']//span[contains(text(), '" + location + "')]"));

        if (!findLocation.isEmpty()) {
            System.out.println("Location Already Exist");
        } else {
            WebElement locationName = driver.findElement(By.xpath("//input[@id='Location Name']"));
            locationName.sendKeys(location);

            WebElement taxId = driver.findElement(By.xpath("//input[@id='Tax ID(TIN)']"));
            taxId.sendKeys("23-4324324");

            WebElement address1 = driver.findElement(By.xpath("//input[@id='Address Line 1']"));
            address1.sendKeys("Test address 1");

            WebElement city = driver.findElement(By.xpath("//input[@id='City']"));
            city.sendKeys("Mumbai");

            WebElement selectStateDropdown = driver.findElement(
                    By.xpath("//div[@class='ng-placeholder' and text()='Select State']"));
            actions.moveToElement(selectStateDropdown).click().perform();

            WebElement selectState = driver.findElement(By.xpath("//span[text()='Alaska']"));
            selectState.click();

            WebElement zipcode = driver.findElement(By.xpath("//input[@id='Zip Code']"));
            zipcode.sendKeys("60020");

            WebElement officePhone = driver.findElement(By.xpath("//input[@id='Office Phone']"));
            officePhone.sendKeys("(354) 252-3432");

            WebElement selectTimezoneDropdown = driver.findElement(
                    By.xpath("//ng-select[@placeholder='Select Time Zone']"));
            selectTimezoneDropdown.click();

            WebElement selectTimezone = driver.findElement(By.xpath("//span[text()='Eastern Standard Time']"));
            selectTimezone.click();

            WebElement selectDoctorDropdown = driver.findElement(
                    By.xpath("//ng-select[@bindlabel='providerName']"));
            selectDoctorDropdown.click();

            WebElement selectPrimaryDoctor = driver.findElement(By.xpath("//span[text()='Babu, Lokesh']"));
            selectPrimaryDoctor.click();

            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 2500);");

            WebElement saveButton = driver.findElement(
                    By.xpath("//button[@class='btn btn-submit btn-light-primary']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);
        }
    }

    public static void addInsurance(WebDriver driver, String insurance, Actions actions)
            throws InterruptedException {
    	
        WebElement clickInsuranceMenu = driver.findElement(By.xpath(
                "//span[@class='pcoded-mtext d-flex justify-content-center text-wrap text-center lh-sm' and text()='Insurance']"));
        clickInsuranceMenu.click();

        WebElement searchInsurance = driver.findElement(By.xpath("//input[@placeholder='search insurance']"));
        searchInsurance.sendKeys(insurance);

        List<WebElement> findInsurance = driver.findElements(
                By.xpath("//div[@class='d-flex flex-column']//span[contains(text(), '" + insurance + "')]"));

        if (!findInsurance.isEmpty()) {
            System.out.println("Insurance Already Exist");
        } else {
            WebElement insuranceName = driver.findElement(By.xpath("//input[@id='Insurance Company Name']"));
            insuranceName.sendKeys(insurance);

            WebElement companyPhone = driver.findElement(By.xpath("//input[@placeholder='Phone']"));
            companyPhone.sendKeys("(324) 324-3243");

            WebElement address1 = driver.findElement(By.xpath("//input[@id='Address Line 1']"));
            address1.sendKeys("Test address 1");

            WebElement city = driver.findElement(By.xpath("//input[@id='City']"));
            city.sendKeys("Mumbai");

            WebElement selectStateDropdown = driver.findElement(
                    By.xpath("//div[@class='ng-placeholder' and text()='Select State']"));
            actions.moveToElement(selectStateDropdown).click().perform();

            WebElement selectState = driver.findElement(By.xpath("//span[text()='Alaska']"));
            selectState.click();

            WebElement zipcode = driver.findElement(By.xpath("//input[@id='Zip Code']"));
            zipcode.sendKeys("60020");

            WebElement saveButton = driver.findElement(
                    By.xpath("//button[@class='btn btn-submit btn-light-primary']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        WebDriver driver = DriverManager.getDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        Actions actions = new Actions(driver);

        addLocation(driver, location, actions);
        addInsurance(driver, insurance, actions);
    }
}
