package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.CardPage;
import com.opms.practice.TreatmentsSetupPage;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TreatmentLedgerTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor jsExecutor;

    @BeforeClass
    public void setUp() throws Exception {
        CardPage.addCreditCard();
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        jsExecutor = (JavascriptExecutor) driver;
    }

    @Test(priority = 1)
    public void validateTxOption() throws Exception {
        WebElement clickContractMenu = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//span[@class='pcoded-mtext d-flex justify-content-center text-wrap text-center lh-sm'"
                        + " and text()='Contract']")));
        clickContractMenu.click();

        jsExecutor.executeScript("window.scrollBy(0, 3000)");

        WebElement clickLedger = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div/h6[@class='text-primary text-center mt-3' and text()='Ledger']")));
        jsExecutor.executeScript("arguments[0].click();", clickLedger);

        Thread.sleep(3000);

        WebElement getTxOptionName = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div[contains(@class, 'ledger-height-card')]"
                        + "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[2]/div/h6[contains(@class, 'text-truncate')"
                        + " and contains(text(), 'Automation Treatment Option three')]")));

        Assert.assertEquals(getTxOptionName.getText(), TreatmentsSetupPage.txOptionName,
                "Treatment Option Name doesn't match.");

        WebElement getTxOptionAmount = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[3]/h6/span[contains(text(), '$2,486.54')]")));

        Assert.assertEquals(getTxOptionAmount.getText(),
                "$" + TreatmentsSetupPage.txOptionAmount, "Treatment Amount doesn't match.");

        System.out.println("All assertions passed: Treatment Option Name and Amount are correct.");
    }

    @Test(priority = 2)
    public void validateProcedureCode() {
        WebElement getProcedureCode1 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[2]/div/h6"
                        + "[contains(text(), 'D0150 (comprehensive oral evaluation)')]")));

        Assert.assertEquals(getProcedureCode1.getText(), TreatmentsSetupPage.proccode01,
                "Procedure Code 01 Name doesn't match.");

        WebElement getProcCode1Amount = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[3]/h6/span[contains(text(), '$543.29')]")));

        Assert.assertEquals(getProcCode1Amount.getText(),
                "$" + TreatmentsSetupPage.procCode01Amount, "Procedure 01 Amount doesn't match.");

        System.out.println("Procedure Code 01 Name and Amount Assertion Passed");

        WebElement getProcedureCode2 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[2]/div/h6"
                        + "[contains(text(), 'D0160 (Detailed and Extensive Evaluation, Problem-focused)')]")));

        Assert.assertEquals(getProcedureCode2.getText(),
                "D0160 (Detailed and Extensive Evaluation, Problem-focused)",
                "Procedure Code 02 Name doesn't match.");

        WebElement getProcCode2Amount = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[3]/h6/span[contains(text(), '$409.36')]")));

        Assert.assertEquals(getProcCode2Amount.getText(),
                "$" + TreatmentsSetupPage.procCode02Amount, "Procedure 02 Amount doesn't match.");

        System.out.println("Procedure Code 02 Name and Amount Assertion Passed");
    }

    @Test(priority = 3)
    public void validateTxExpense() {
        WebElement getTxExpense = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[2]/div/h6"
                        + "[contains(text(), '" + TreatmentsSetupPage.txExpense + "')]")));

        Assert.assertEquals(getTxExpense.getText(), TreatmentsSetupPage.txExpense,
                "Treatment Expense Name doesn't match.");

        WebElement getTxExpenseAmount = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[3]/h6/span[contains(text(), '$876.54')]")));

        Assert.assertEquals(getTxExpenseAmount.getText(),
                "$" + TreatmentsSetupPage.txExpenseAmount, "Treatment Expense Amount doesn't match.");

        System.out.println("Treatment Expense Name and Amount Assertion Passed");
    }

    @Test(priority = 4)
    public void validateTxCourtesy() {
        WebElement getTxCourtesy = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[2]/div/h6"
                        + "[contains(text(), '" + TreatmentsSetupPage.txCourtesy + "')]")));

        Assert.assertEquals(getTxCourtesy.getText(), TreatmentsSetupPage.txCourtesy,
                "Treatment Courtesy Name doesn't match.");

        WebElement getTxCourtesyAmount = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//table[contains(@class, 'payment-dashboard')]"
                        + "/tbody/tr[@class='main-row']/td[3]/h6/span[contains(text(), '($160.82)')]")));

        Assert.assertEquals(getTxCourtesyAmount.getText(),
                "($" + TreatmentsSetupPage.txCourtesyAmt + ")",
                "Treatment Courtesy Amount doesn't match.");

        System.out.println("Treatment Courtesy Name and Amount Assertion Passed");
    }

    @AfterClass
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
