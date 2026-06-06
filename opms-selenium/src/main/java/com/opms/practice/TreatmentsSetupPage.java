package com.opms.practice;

import com.opms.base.DriverManager;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TreatmentsSetupPage {

    public static final String txTypeName    = "Automation Treatment Type three";
    public static final String txOptionName  = "Automation Treatment Option three";
    public static final String txOptionAmount = "2,486.54";
    public static final String txExpense     = "Automation Extra Braces three";
    public static final String txExpenseAmount = "876.54";
    public static final String txCourtesy   = "Patient New Discount three";
    public static final String txCourtesyAmt = "160.82";
    public static final String proccode01   = "D0150 (comprehensive oral evaluation)";
    public static final String proccode02   = "D0160 (Detailed and Extensive Evaluation, Problem-focused)";
    public static final String procCode01Amount = "543.29";
    public static final String procCode02Amount = "409.36";

    public static void createTreatmentType(WebDriverWait wait, WebDriver driver, Actions actions,
            String txTypeName) throws InterruptedException {

        LookupPage.main(null);
        Thread.sleep(3000);

        WebElement clickTreatmentMenu = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[@class='pcoded-mtext d-flex justify-content-center text-wrap text-center lh-sm' and text()='Treatment']")));
        clickTreatmentMenu.click();

        WebElement searchTxType = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchTxType.sendKeys(txTypeName);

        List<WebElement> findTxTypeElements = driver.findElements(
                By.xpath("//div[@class='d-flex flex-column']//span[contains(text(), '" + txTypeName + "')]"));

        if (!findTxTypeElements.isEmpty()) {
            System.out.println("TxType already exists: " + txTypeName);
        } else {
            System.out.println("TxType does not exist.");

            WebElement typeName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@placeholder ='Type Name']")));
            typeName.sendKeys(txTypeName);

            WebElement typeDescription = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@id='Description']")));
            typeDescription.sendKeys(txTypeName);

            WebElement saveTxType = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//button[@class='btn btn-submit btn-light-primary']//span[contains(text(), 'Save Treatment Type')]")));
            actions.moveToElement(saveTxType).click().perform();
        }
    }

    public static void createTreatmentOption(WebDriverWait wait, WebDriver driver, String txOptionName,
            String txOptionAmount) throws InterruptedException {

        Actions actions = new Actions(driver);

        WebElement clickTxOptionMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[@class='nav-item']/a[text()='Treatment Option']")));
        actions.moveToElement(clickTxOptionMenu).click().perform();

        Thread.sleep(3000);
        WebElement searchTxOption = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchTxOption.sendKeys(txOptionName);

        List<WebElement> findTxOptionElements = driver.findElements(
                By.xpath("//div[@class='d-flex flex-column']//span[contains(text(), '" + txOptionName + "')]"));

        if (!findTxOptionElements.isEmpty()) {
            System.out.println("TxOption already exists: " + findTxOptionElements.get(0).getText());
        } else {
            System.out.println("TxOption does not exist.");

            WebElement txName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@id ='Treatment Name']")));
            txName.sendKeys(txOptionName);

            WebElement txOptionFee = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@id='Provider Fee']")));
            txOptionFee.sendKeys(txOptionAmount);

            WebElement txOptionDescription = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@id='Description']")));
            txOptionDescription.sendKeys(txOptionName);

            WebElement txOptionMinDownPayment = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@id='Minimum Down Payment']")));
            txOptionMinDownPayment.sendKeys("300");

            WebElement txHighLength = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@placeholder='Low Range']")));
            txHighLength.sendKeys("12");

            WebElement txLowLength = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@placeholder='High Range']")));
            txLowLength.sendKeys("5");

            WebElement saveTxOption = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//button[@class='btn btn-submit btn-light-primary']//span[contains(text(), 'Save Treatment Option')]")));
            actions.moveToElement(saveTxOption).click().perform();
        }
    }

    public static void createTreatmentExpense(WebDriverWait wait, WebDriver driver, Actions actions,
            String txExpense, String txExpenseAmount) throws InterruptedException {

        WebElement clickTxExpenseMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[@class='nav-item']/a[text()='Treatment Expense']")));
        actions.moveToElement(clickTxExpenseMenu).click().perform();

        Thread.sleep(3000);
        WebElement searchTxExpense = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchTxExpense.sendKeys(txExpense);

        List<WebElement> findTxExpenseElements = driver.findElements(
                By.xpath("//div[@class='d-flex flex-column']//span[contains(text(), '" + txExpense + "')]"));

        if (!findTxExpenseElements.isEmpty()) {
            System.out.println("TxExpense already exists: " + findTxExpenseElements.get(0).getText());
        } else {
            System.out.println("TxExpense does not exist.");

            WebElement txExpenseName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@id ='Expense Name']")));
            txExpenseName.sendKeys(txExpense);

            WebElement txExpenseFee = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@id ='Provider Fee']")));
            txExpenseFee.sendKeys(txExpenseAmount);

            WebElement saveTxExpense = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//button[@class='btn btn-submit btn-light-primary']//span[contains(text(), 'Save Treatment Expense')]")));
            actions.moveToElement(saveTxExpense).click().perform();
        }
    }

    public static void createTreatmentCourtesy(WebDriverWait wait, WebDriver driver, Actions actions,
            String txCourtesy, String txCourtesyAmt) throws InterruptedException {

        WebElement clickTxCourtesyMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[@class='nav-item']/a[text()='Treatment Courtesy']")));
        actions.moveToElement(clickTxCourtesyMenu).click().perform();

        Thread.sleep(3000);
        WebElement searchTxCourtesy = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchTxCourtesy.sendKeys(txCourtesy);

        List<WebElement> findTxCourtesyElements = driver.findElements(
                By.xpath("//div[@class='d-flex flex-column']//span[contains(text(), '" + txCourtesy + "')]"));

        if (!findTxCourtesyElements.isEmpty()) {
            System.out.println("TxCourtesy already exists: " + findTxCourtesyElements.get(0).getText());
        } else {
            System.out.println("Courtesy does not exist.");

            WebElement txCourtesyName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@id ='Courtesy']")));
            txCourtesyName.sendKeys(txCourtesy);

            WebElement txCourtesyAmount = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@id ='Amount']")));
            txCourtesyAmount.sendKeys(txCourtesyAmt);

            WebElement saveTxCourtesy = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//button[@class='btn btn-submit btn-light-primary']//span[contains(text(), 'Save Treatment Courtesy')]")));
            actions.moveToElement(saveTxCourtesy).click().perform();
        }
    }

    public static void createTreatmentProcedureCode(WebDriverWait wait, WebDriver driver, Actions actions,
            String procCode01Amount, String procCode02Amount) throws InterruptedException {

        WebElement clickTxProcedureMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[@class='nav-item']/a[text()='Procedure Code Fee']")));
        actions.moveToElement(clickTxProcedureMenu).click().perform();

        Thread.sleep(3000);
        String trimmedProcCode01 = proccode01.split(" ")[0];
        WebElement searchTxProcCode1 = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchTxProcCode1.sendKeys(trimmedProcCode01);

        List<WebElement> findTxProcCode1Elements = driver.findElements(
                By.xpath("//div[@class='d-flex flex-column']//span[contains(text(), '" + trimmedProcCode01 + "')]"));

        if (!findTxProcCode1Elements.isEmpty()) {
            System.out.println("Tx Procedure Code 1 already exists: " + findTxProcCode1Elements.get(0).getText());
        } else {
            System.out.println("Tx Procedure Code 1 does not exist.");
        }

        Thread.sleep(3000);
        searchTxProcCode1.clear();
        String trimmedProcCode02 = proccode02.split(" ")[0];
        WebElement searchTxProcCode2 = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div/input[@placeholder='search']")));
        searchTxProcCode2.sendKeys(trimmedProcCode02);

        List<WebElement> findTxProcCode2Elements = driver.findElements(
                By.xpath("//div[@class='d-flex flex-column']//span[contains(text(), '" + trimmedProcCode02 + "')]"));

        if (!findTxProcCode2Elements.isEmpty()) {
            System.out.println("Tx Procedure Code 2 already exists: " + findTxProcCode2Elements.get(0).getText());
        } else {
            System.out.println("Tx Procedure Code 2 does not exist.");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        WebDriver driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        Actions actions = new Actions(driver);

        createTreatmentType(wait, driver, actions, txTypeName);
        createTreatmentOption(wait, driver, txOptionName, txOptionAmount);
        createTreatmentExpense(wait, driver, actions, txExpense, txExpenseAmount);
        createTreatmentCourtesy(wait, driver, actions, txCourtesy, txCourtesyAmt);
        createTreatmentProcedureCode(wait, driver, actions, procCode01Amount, procCode02Amount);
    }
}
