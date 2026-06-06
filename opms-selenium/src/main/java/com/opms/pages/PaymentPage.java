package com.opms.pages;

import com.opms.base.DriverManager;
import com.opms.utils.PaymentInvoiceDetail;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PaymentPage {

    public static String getDownPaymentAmount;
    public static String getCashPayMethod;
    public static String getInvoiceAmount;
    public static String getPaperCheckPayMethod;
    public static String getCashierCheckPayMethod;
    public static String getFirstMonthPaymentAmount;
    public static String getSecondMonthPaymentAmount;
    public static String getOtherCollectionPayMethod;
    public static String getThirdMonthPaymentAmount;
    public static String getCreditcardCollectionPayMethod;
    public static String getFourthMonthPaymentAmount;

    private static final List<PaymentInvoiceDetail> paymentDetailsList = new ArrayList<>();

    public static void downPaymentCash(WebDriverWait wait, WebDriver driver) throws Exception {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        WebElement clickPatientPlan = driver.findElement(By.xpath(
                "//div[@class ='card p-3 contract-card cursor-pointer border border-primary contrat-patient-card']"));
        clickPatientPlan.click();

        WebElement clickDpPay = driver.findElement(
                By.xpath("//div/button[@class='btn btn-sm btn-light-primary float-end' and text()='Pay Now']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", clickDpPay);

        WebElement downPayment = driver.findElement(By.xpath(
                "//div/h5[@class='h6 text-center' and text()='Pay Invoice']"
                        + "/following-sibling::h6[@class='f-14 fw-normal text-primary text-center']"));
        getDownPaymentAmount = downPayment.getText();

        driver.findElement(By.xpath("//div[@class='card p-4 mb-0 text-center payment-amount-card']")).click();
        driver.findElement(By.xpath("//div/input[@id='amount']")).sendKeys(getDownPaymentAmount);

        WebElement cashBtn = driver.findElement(By.xpath("//span[@class='ms-2 f-12' and text()='Cash']"));
        getCashPayMethod = cashBtn.getText();
        cashBtn.click();

        driver.findElement(By.xpath(
                "//button[@class='btn btn-submit rounded-3 btn-light-primary']/span[text()='Pay Now']")).click();

        Thread.sleep(3000);
        driver.findElement(By.xpath("//button[@class='btn btn-light-primary btn-submit rounded-3']")).click();
    }

    public static void firstInvoicePayPaperCheck(WebDriverWait wait, WebDriver driver)
            throws InterruptedException {

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        WebElement invoicePay = driver.findElement(
                By.xpath("//div/button[@class='btn btn-sm btn-light-primary float-end' and text()='Pay Now']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", invoicePay);

        WebElement firstInvoicePay = driver.findElement(By.xpath(
                "//div/h5[@class='h6 text-center' and text()='Pay Invoice']"
                        + "/following-sibling::h6[@class='f-14 fw-normal text-primary text-center']"));
        getInvoiceAmount = firstInvoicePay.getText();

        driver.findElement(By.xpath("//div[@class='card p-4 mb-0 text-center payment-amount-card']")).click();
        driver.findElement(By.xpath("//div/input[@id='amount']")).sendKeys(getInvoiceAmount);

        WebElement paperCheckBtn = driver.findElement(
                By.xpath("//span[@class='ms-2 f-12' and text()='Paper Check']"));
        getPaperCheckPayMethod = paperCheckBtn.getText();
        paperCheckBtn.click();

        driver.findElement(By.xpath(
                "//button[@class='btn btn-submit rounded-3 btn-light-primary']/span[text()='Pay Now']")).click();

        Thread.sleep(3000);
        driver.findElement(By.xpath("//button[@class='btn btn-light-primary btn-submit rounded-3']")).click();
    }

    public static void multiplePayments(WebDriverWait wait, WebDriver driver) throws Exception {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        String[] paymentMethods = {
            "Cash", "Paper Check", "Cashier Check", "Other Collection",
            "Desktop Transcation", "Credit Or Debit Card"
        };

        for (String paymentMethod : paymentMethods) {
            WebElement payNowBtn = driver.findElement(By.xpath(
                    "//div/button[@class='btn btn-sm btn-light-primary float-end' and normalize-space(text())='Pay Now']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", payNowBtn);

            WebElement invoiceAmountEl = driver.findElement(By.xpath(
                    "//div/h5[@class='h6 text-center' and text()='Pay Invoice']"
                            + "/following-sibling::h6[@class='f-14 fw-normal text-primary text-center']"));
            String currentInvoiceAmount = invoiceAmountEl.getText();

            driver.findElement(By.xpath(
                    "//div[@class='card p-4 mb-0 text-center payment-amount-card']")).click();
            driver.findElement(By.xpath("//div/input[@id='amount']")).sendKeys(currentInvoiceAmount);

            WebElement paymentMethodBtn = driver.findElement(
                    By.xpath("//span[@class='ms-2 f-12' and text()='" + paymentMethod + "']"));
            String selectedMethod = paymentMethodBtn.getText();
            paymentMethodBtn.click();

            if (paymentMethod.equals("Cash")) {
                getCashPayMethod = selectedMethod;
                getDownPaymentAmount = currentInvoiceAmount;
            } else if (paymentMethod.equals("Paper Check")) {
                getPaperCheckPayMethod = selectedMethod.replace(" ", "");
                getFirstMonthPaymentAmount = currentInvoiceAmount;
            } else if (paymentMethod.equals("Cashier Check")) {
                getCashierCheckPayMethod = selectedMethod.replace(" ", "");
                getSecondMonthPaymentAmount = currentInvoiceAmount;
            } else if (paymentMethod.equals("Other Collection")) {
                getOtherCollectionPayMethod = selectedMethod.replace(" ", "");
                getThirdMonthPaymentAmount = currentInvoiceAmount;
            } else if (paymentMethod.equals("Credit Or Debit Card")) {
                getCreditcardCollectionPayMethod = selectedMethod.replace(" ", "");
                getFourthMonthPaymentAmount = currentInvoiceAmount;
            }

            paymentDetailsList.add(new PaymentInvoiceDetail(selectedMethod, currentInvoiceAmount));

            driver.findElement(By.xpath(
                    "//button[@class='btn btn-submit float-end ms-2 btn-light-primary']"
                            + "/span[text()='Pay Now']")).click();

            Thread.sleep(3000);
            driver.findElement(By.xpath(
                    "//button[@class='btn btn-submit float-end ms-2 btn-secondary']")).click();

            System.out.println("Payment method: " + selectedMethod + ", Invoice Amount: " + currentInvoiceAmount);
            Thread.sleep(2000);
        }
    }

    public static void main(String[] args) throws Exception {
        WebDriver driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        multiplePayments(wait, driver);
    }
}
