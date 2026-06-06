package com.opms.pages;

import com.opms.base.DriverManager;
import com.opms.practice.TreatmentsSetupPage;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TreatmentPlanPage {

    public static void addTreatmentPlan() throws Exception {
        InsuranceBenefitPage.addInsuranceBenefit();

        WebDriver driver = DriverManager.getDriver();
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        Actions actions = new Actions(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement clickTreatmentIcon = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div[@class='border border-white p-3 border-radius-20 h-100']"
                        + "//i[@class='feather icon-arrow-up-right f-24 cursor-pointer text-white']")));
        jsExecutor.executeScript("arguments[0].click();", clickTreatmentIcon);

        WebElement clickTxTypeDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@class='ng-placeholder' and text ()='Select treatment Type']")));
        actions.moveToElement(clickTxTypeDropdown).click().perform();

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[@class='ng-option-label' and text()='" + TreatmentsSetupPage.txTypeName + "']")))
                .click();

        WebElement clickTxOptionDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@class='ng-placeholder' and text ()='Select treatment option']")));
        actions.moveToElement(clickTxOptionDropdown).click().perform();

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[@class='ng-option-label' and text()='" + TreatmentsSetupPage.txOptionName + "']")))
                .click();

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@class = 'btn btn-light-success']"))).click();

        jsExecutor.executeScript("window.scrollBy(0, 5000)");

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[normalize-space()='D0150']"))).click();
        driver.findElement(By.xpath("//label[normalize-space()='D0160']")).click();

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Ok']"))).click();

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@class = 'btn btn-light-warning']"))).click();

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[normalize-space()='" + TreatmentsSetupPage.txExpense + "']"))).click();

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Ok']"))).click();

        jsExecutor.executeScript("window.scrollBy(0, 2000);");
        jsExecutor.executeScript("window.scrollBy(0, 1000);");

        WebElement insuranceAmount = driver.findElement(By.xpath("//div/input[@id = 'insuranceBenefit_0']"));
        insuranceAmount.clear();
        insuranceAmount.sendKeys("160.34");

        WebElement networkDiscount = driver.findElement(By.xpath("//div/input[@id = 'networkDiscount']"));
        networkDiscount.clear();
        networkDiscount.sendKeys("220");

        WebElement addCourtesy = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div/button[@class = 'btn btn-light-info']")));
        jsExecutor.executeScript("arguments[0].click();", addCourtesy);

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[normalize-space()='" + TreatmentsSetupPage.txCourtesy + "']"))).click();

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Ok']"))).click();

        jsExecutor.executeScript("window.scrollBy(0, 2000);");

        WebElement saveTreatmentPlan = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button/span[text ()='Save & Continue']")));
        jsExecutor.executeScript("arguments[0].click();", saveTreatmentPlan);

        System.out.println("Treatment Plan Step 01 Created Successfully");
        Thread.sleep(2000);
    }

    public static void main(String[] args) throws Exception {
        addTreatmentPlan();
    }
}
