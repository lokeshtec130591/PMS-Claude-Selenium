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

public class PaymentSliderPage {

    public static void savePaymentPlan() throws Exception {
        TreatmentPlanPage.addTreatmentPlan();

        WebDriver driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement downPaymentSlider = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@class='slider-panel']//span[@aria-label='ngx-slider']")));
        downPaymentSlider.click();

        Actions drag = new Actions(driver);
        drag.moveToElement(downPaymentSlider).clickAndHold().moveByOffset(100, 0).release().perform();

        WebElement monthlyPaymentSlider = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@class='slider-panel monthly-payment-slider']"
                        + "//span[@class='ngx-slider-span ngx-slider-pointer ngx-slider-pointer-min']")));
        monthlyPaymentSlider.click();
        drag.moveToElement(monthlyPaymentSlider).clickAndHold().moveByOffset(100, 0).release().perform();

        Thread.sleep(2000);

        WebElement saveAndContinue = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button/span[text()= 'Save & Continue']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveAndContinue);

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@class='swal2-confirm swal2-styled' and text()= 'OK']"))).click();

        System.out.println("Treatment Payment Plan step 2 Created Successfully");
    }

    public static void main(String[] args) throws Exception {
        savePaymentPlan();
    }
}
