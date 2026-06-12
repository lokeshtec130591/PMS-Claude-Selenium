package com.opms.practice;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ContractConfigPage {

    public static void savePracticeContract(WebDriver driver, WebDriverWait wait, Actions actions)
            throws InterruptedException {

    //    LoginPage.login();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        WebElement practice = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@title,'Practice configuration')]")));
        practice.click();

        // Wait for the practice config page to load, then click the Contract tab inside it
        Thread.sleep(3000);
        WebElement contractMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li/a[@class='nav-link' and text()='Contract']")));
        actions.moveToElement(contractMenu).click().perform();

        Thread.sleep(4000);
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 300)");
        Thread.sleep(1000);
        WebElement recommendedDownPayment = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Recommended Down Payment']")));
        recommendedDownPayment.clear();
        recommendedDownPayment.sendKeys("600");

        Thread.sleep(2000);
        WebElement minDownPayment = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Minimum Down Payment']")));
        minDownPayment.clear();
        minDownPayment.sendKeys("300");

        Thread.sleep(2000);
        WebElement recommendedMonthlyPayment = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Recommended Monthly Payment']")));
        recommendedMonthlyPayment.clear();
        recommendedMonthlyPayment.sendKeys("500");

        Thread.sleep(2000);
        WebElement lowMonthlyPayment = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='Lowest Monthly Payment Allowed']")));
        lowMonthlyPayment.clear();
        lowMonthlyPayment.sendKeys("200");

        Thread.sleep(2000);
        WebElement interestPercentage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Interest Percentage']")));
        interestPercentage.clear();
        interestPercentage.sendKeys("3");

        Thread.sleep(2000);
        WebElement financeMonthPastTreatment = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Financing # Months Past Treatment']")));
        financeMonthPastTreatment.clear();
        financeMonthPastTreatment.sendKeys("4");

        WebElement saveContractButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[@class='btn btn-submit btn-light-primary']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveContractButton);

        System.out.println("Practice contract config saved successfully");
    }

    public static void main(String[] args) throws InterruptedException {
        WebDriver driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        Actions actions = new Actions(driver);
        savePracticeContract(driver, wait, actions);
    }
}
