package com.opms.pages;

import com.opms.base.DriverManager;
import com.opms.practice.LookupPage;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class InsurancePage {

    public static void addInsurance() throws Exception {
        WebDriver driver = DriverManager.getDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        Actions actions = new Actions(driver);

        PatientPage.createPatient();

        jsExecutor.executeScript("window.scrollBy(0, 3000);");

        WebElement addInsuranceButton = driver.findElement(
                By.xpath("//span[@class='insurance-summary-add-hide']"));
        jsExecutor.executeScript("arguments[0].click();", addInsuranceButton);

        WebElement insuranceDropdown = driver.findElement(By.xpath("//ng-select[@id='Insurance Company']"));
        insuranceDropdown.click();

        driver.findElement(By.xpath(
                "//div/span[@class='ng-option-label' and contains(text(), '" + LookupPage.insurance + "')]"))
                .click();

        jsExecutor.executeScript("window.scrollBy(0, 4000);");
        Thread.sleep(2000);

        WebElement policyholderInfo = driver.findElement(By.xpath(
                "//div[@class='col-lg-12 mb-3']/label[text()='Who is the Policyholder?']"
                        + "/following-sibling::div/button[@class='btn btn-secondary']"));
        actions.moveToElement(policyholderInfo).click().perform();

        Thread.sleep(2000);
        WebElement policyholderAddress = driver.findElement(By.xpath(
                "//div[@class='col-lg-12 mb-3']/label[text()='Policyholder Address?']"
                        + "/following-sibling::div/button[@class='btn btn-secondary']"));
        actions.moveToElement(policyholderAddress).click().perform();

        driver.findElement(By.xpath("//input[@id='Group / Employer Name']")).sendKeys("AutomationTest");

        WebElement saveInsuranceButton = driver.findElement(
                By.xpath("//button[@class='btn btn-submit btn-light-primary']"));
        jsExecutor.executeScript("arguments[0].click();", saveInsuranceButton);

        driver.findElement(By.xpath("//button[@class='swal2-confirm swal2-styled']")).click();

        System.out.println("Patient Insurance Added Successfully");
        Thread.sleep(2000);

        driver.findElement(By.xpath(
                "//span[@class = 'pcoded-mtext d-flex justify-content-center text-wrap text-center lh-sm'"
                        + "and text()='Insurance']")).click();
    }

    public static void main(String[] args) throws Exception {
        addInsurance();
    }
}
