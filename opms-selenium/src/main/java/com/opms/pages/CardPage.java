package com.opms.pages;

import com.opms.base.DriverManager;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CardPage {

    public static void addCreditCard() throws Exception {
        ContractPage.signContract();

        WebDriver driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        Actions actions = new Actions(driver);

        WebElement clickCardMenu = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//span[@class='pcoded-mtext d-flex justify-content-center text-wrap text-center lh-sm'"
                        + " and text()='Card']")));
        jsExecutor.executeScript("arguments[0].click();", clickCardMenu);

        WebElement addCard = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div/button[@class='btn btn-light-primary ms-3' and text()='Add Card']")));
        jsExecutor.executeScript("arguments[0].click();", addCard);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='cardHolderName']")))
                .sendKeys("Automation Card Name");

        driver.switchTo().frame(driver.findElement(By.xpath("//iframe[@id='_card']")));
        driver.findElement(By.xpath("//input[@name='cardnumber']")).sendKeys("4761344136141390");
        driver.switchTo().defaultContent();

        driver.switchTo().frame(driver.findElement(By.xpath("//iframe[@id='_exp']")));
        driver.findElement(By.xpath("//input[@name='exp-date']")).sendKeys("0827");
        driver.switchTo().defaultContent();

        driver.switchTo().frame(driver.findElement(By.xpath("//iframe[@id='_cvc']")));
        driver.findElement(By.xpath("//input[@name='cvc']")).sendKeys("581");
        driver.switchTo().defaultContent();

        driver.findElement(By.xpath("//input[@id='Address Line 1']")).sendKeys("Testing Address");
        jsExecutor.executeScript("window.scrollBy(0, 4000);");
        driver.findElement(By.xpath("//input[@id='City']")).sendKeys("Chennai");

        WebElement stateDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ng-select[@placeholder='Select State']")));
        actions.moveToElement(stateDropdown).click().perform();

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='a60e8c064db9-1']"))).click();

        driver.findElement(By.xpath("//input[@placeholder ='Zip Code']")).sendKeys("60005");

        driver.findElement(By.xpath(
                "//div/button[@class='btn btn-light-primary d-flex flex-wrap align-items-center"
                        + " justify-content-center hei-35 rounded-3 patient-primary-button cursor-pointer']"))
                .click();

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div/button[@class='swal2-confirm swal2-styled' and text()='OK']"))).click();

        System.out.println("Patient Credit Card Created Successfully");
    }

    public static void main(String[] args) throws Exception {
        addCreditCard();
    }
}
