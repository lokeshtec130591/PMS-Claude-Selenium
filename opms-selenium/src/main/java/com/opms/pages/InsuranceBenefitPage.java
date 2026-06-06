package com.opms.pages;

import com.opms.base.DriverManager;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class InsuranceBenefitPage {

    public static void addInsuranceBenefit() throws Exception {
        WebDriver driver = DriverManager.getDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;

        InsurancePage.addInsurance();

        WebElement addBenefitButton = driver.findElement(By.xpath("//div/button[text()= 'Add Benefit ']"));
        jsExecutor.executeScript("arguments[0].click();", addBenefitButton);

        Thread.sleep(3000);
        driver.findElement(By.xpath("//input[@id='Available Coverage']")).sendKeys("5000");

        Thread.sleep(2000);
        jsExecutor.executeScript("window.scrollBy(0, 4000);");
        Thread.sleep(2000);
        jsExecutor.executeScript("window.scrollBy(0, 2000);");

        driver.findElement(By.xpath("//label[@for= 'I confirm']")).click();

        WebElement statusDropdown = driver.findElement(By.xpath("//ng-select[@id= 'Status']"));
        statusDropdown.click();
        driver.findElement(By.xpath(
                "//ng-dropdown-panel/div/div/div[@class ='ng-option']//span[text()='Verified']")).click();

        WebElement saveBenefitButton = driver.findElement(
                By.xpath("//button[@class='btn btn-submit btn-light-primary']"));
        jsExecutor.executeScript("arguments[0].click();", saveBenefitButton);

        Thread.sleep(4000);
        System.out.println("Insurance Benefit Added Successfully");

        driver.findElement(By.xpath(
                "//span[@class = 'pcoded-mtext d-flex justify-content-center text-wrap text-center lh-sm'"
                        + " and text()='Patient']")).click();
    }

    public static void main(String[] args) throws Exception {
        addInsuranceBenefit();
    }
}
