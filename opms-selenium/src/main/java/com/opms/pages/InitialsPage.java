package com.opms.pages;

import com.opms.base.DriverManager;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InitialsPage {

    public static void saveInitials() throws Exception {
        PaymentSliderPage.savePaymentPlan();

        WebDriver driver = DriverManager.getDriver();
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        Thread.sleep(3000);
        jsExecutor.executeScript("window.scrollBy(0, 2500);");

        WebElement yesRadioLabel = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[@id='isPatientFrp_Yes' and @type='radio']/following-sibling::label[@for='isPatientFrp_Yes']")));
        jsExecutor.executeScript("arguments[0].click();", yesRadioLabel);

        jsExecutor.executeScript("window.scrollBy(0, 1000);");
        driver.findElement(By.xpath("//input[@id='Enter Your Initial_1']")).sendKeys("q");
        driver.findElement(By.xpath("//input[@id='Enter Your Initial_2']")).sendKeys("q");
        driver.findElement(By.xpath("//input[@id='Enter Your Initial_3']")).sendKeys("q");
        driver.findElement(By.xpath("//input[@id='Enter Your Initial_4']")).sendKeys("q");

        WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@class='btn btn-submit rounded-3 btn-light-primary']")));
        jsExecutor.executeScript("arguments[0].click();", continueBtn);

        System.out.println("Patient Initials Step 03 Created Successfully");
    }

    public static void main(String[] args) throws Exception {
        saveInitials();
    }
}
