package com.opms.pages;

import com.opms.base.DriverManager;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PatientSearchPage {

    public static void searchAndOpenPatient(String fullName) throws InterruptedException {
        WebDriver driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        Thread.sleep(3000);
        WebElement patientSearch = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@value='patientName']")));
        patientSearch.sendKeys(fullName);

        WebElement patientClick = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li//span[@class='first-name-letter btn-primary p-3']"
                        + "/following-sibling::span[contains(text(),'" + fullName + "')]")));
        patientClick.click();
    }
}
