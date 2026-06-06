package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.CardPage;
import com.opms.pages.PaymentPage;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PaymentLedgerTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor jsExecutor;

    @BeforeClass
    public void setUp() throws Exception {
        CardPage.addCreditCard();

        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        jsExecutor = (JavascriptExecutor) driver;

        // Navigate to Contract section so payment buttons are visible
        WebElement clickContractMenu = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//span[@class='pcoded-mtext d-flex justify-content-center text-wrap text-center lh-sm'"
                        + " and text()='Contract']")));
        jsExecutor.executeScript("arguments[0].click();", clickContractMenu);

        Thread.sleep(2000);

        // Run all payments
        PaymentPage.multiplePayments(wait, driver);

        Thread.sleep(3000);
        WebElement clickLedger = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div/h6[@class='text-primary text-center mt-3' and text()='Ledger']")));
        jsExecutor.executeScript("arguments[0].click();", clickLedger);

        Thread.sleep(3000);
        jsExecutor.executeScript("window.scrollBy(0, 3000)");
    }

    @Test(priority = 1)
    public void validateDownPaymentInLedger() {
        WebElement dpText = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[2]/div/h6"
                        + "[contains(@class, 'wid-200') and contains(text(),'Down Payment')]")));

        Assert.assertEquals(dpText.getText(), "Down Payment", "DP Text not match");
        System.out.println("DownPayment Text Matched successfully");

        WebElement dpPaymentType = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[3]/h6[@title= 'Cash']")));

        Assert.assertEquals(dpPaymentType.getText(), PaymentPage.getCashPayMethod,
                "DPPayMethod Cash not match");
        System.out.println("DP Payment Method Cash Matched successfully");

        WebElement dpPayment = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[5]/h6/span"
                        + "[contains(@class, 'text-success') and contains(text(), '($1,075.00)')]")));

        Assert.assertEquals(dpPayment.getText(),
                "(" + PaymentPage.getDownPaymentAmount + ")", "DP Payment not match");

        System.out.println("DownPayment Amount " + PaymentPage.getDownPaymentAmount + " Matched successfully");
    }

    @Test(priority = 2)
    public void validateFirstInvoicePaymentInLedger() {
        WebElement firstInvoiceTypeText = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[2]/div/h6"
                        + "[contains(@class, 'wid-200') and contains(text(),'Due Payment')]")));

        Assert.assertEquals(firstInvoiceTypeText.getText(), "Due Payment",
                "First Invoice Payment Text not match");
        System.out.println("First Invoice type as Due Payment Text Matched successfully");

        WebElement firstInvoicePaymentType = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[3]/h6[@title= 'PaperCheck']")));

        Assert.assertEquals(firstInvoicePaymentType.getText(), PaymentPage.getPaperCheckPayMethod,
                "First Invoice PayMethod not match");
        System.out.println("First Invoice Payment method PaperCheck Matched successfully");

        WebElement firstInvoicePayment = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[5]/h6/span"
                        + "[contains(@class, 'text-success') and contains(text(), '($245.42)')]")));

        Assert.assertEquals(firstInvoicePayment.getText(),
                "(" + PaymentPage.getFirstMonthPaymentAmount + ")",
                "First Invoice Payment not match");

        System.out.println("First Invoice Payment Amount Matched successfully");
    }

    @Test(priority = 3)
    public void validateSecondInvoicePaymentInLedger() {
        WebElement secondInvoicePaymentType = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[3]/h6[@title= 'CashierCheck']")));

        Assert.assertEquals(secondInvoicePaymentType.getText(), PaymentPage.getCashierCheckPayMethod,
                "Second Invoice PayMethod CashierCheck not match");
        System.out.println("Second Invoice Payment method CashierCheck Matched successfully");

        WebElement secondInvoicePayment = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//tr[@class='main-row' and .//td[3]/h6[@title='CashierCheck']]"
                        + "/td[5]/h6/span[contains(@class, 'text-success') and contains(text(), '($245.42)')]")));

        Assert.assertEquals(secondInvoicePayment.getText(),
                "(" + PaymentPage.getSecondMonthPaymentAmount + ")",
                "Second Invoice Payment not match");

        System.out.println("Second Invoice Payment Amount Matched successfully");
    }

    @AfterClass
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
