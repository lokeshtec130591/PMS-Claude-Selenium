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

public class ContractPage {

    public static void signContract() throws Exception {
        InitialsPage.saveInitials();

        WebDriver driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        Actions actions = new Actions(driver);

        Thread.sleep(3000);
        jsExecutor.executeScript("window.scrollBy(0, 5000)");

        WebElement patientSignaturePad = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), 'Signer')]/following-sibling::div//canvas")));

        actions.moveToElement(patientSignaturePad)
                .clickAndHold()
                .moveByOffset(75, 0)
                .moveByOffset(0, 75)
                .moveByOffset(-50, 0)
                .moveByOffset(0, -50)
                .release().perform();

        jsExecutor.executeScript("window.scrollBy(0, 1000);");

        WebElement patientSignButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), 'Signer')]"
                        + "/following-sibling::div//button[@class = 'btn btn-light-primary ms-3']")));
        jsExecutor.executeScript("arguments[0].click();", patientSignButton);

        Thread.sleep(2000);
        WebElement witnessSignaturePad = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), 'Witness Signature')]/following-sibling::div//canvas")));

        actions.moveToElement(witnessSignaturePad)
                .clickAndHold()
                .moveByOffset(75, 0)
                .moveByOffset(0, 75)
                .moveByOffset(-50, 0)
                .moveByOffset(0, -50)
                .release().perform();

        WebElement witnessSignButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), 'Witness Signature')]"
                        + "/following-sibling::div//button[@class = 'btn btn-light-primary ms-3']")));
        jsExecutor.executeScript("arguments[0].click();", witnessSignButton);

        Thread.sleep(2000);

        WebElement finishBtn = driver.findElement(
                By.xpath("//button[@class='btn btn-light-primary btn-submit rounded-3'][1]"));
        jsExecutor.executeScript("arguments[0].click();", finishBtn);

        driver.findElement(By.xpath("//button[@class='swal2-confirm swal2-styled']")).click();

        WebElement securityPin = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div/input[@id = 'Staff Pin']")));
        securityPin.sendKeys("1234");

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@class = 'btn btn-submit btn-light-primary']//span [text() = 'Confirm']")))
                .click();

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@class = 'swal2-confirm swal2-styled' and text() = 'Ok']"))).click();

        System.out.println("Patient Service Contract Created Successfully");
    }

    public static void main(String[] args) throws Exception {
        signContract();
    }
}
