package com.opms.pages;

import com.opms.base.DriverManager;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {

    public static final String VALID_EMAIL    = "lokeshqa1@test.com";
    public static final String VALID_PASSWORD = "Lokesh@123";
    public static final String LOGIN_URL      = "https://demo.orthopms.com/login";

    /** Login with valid credentials (used by other page-object chains). */
    public static void login() throws InterruptedException {
        loginWith(VALID_EMAIL, VALID_PASSWORD);
    }

    /**
     * Navigate to the login page, fill credentials, and submit.
     * Does NOT assert success or failure — callers are responsible for assertions.
     */
    public static void loginWith(String email, String password) {
        WebDriver driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(LOGIN_URL);

        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        emailField.clear();
        emailField.sendKeys(email);

        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        passwordField.clear();
        passwordField.sendKeys(password);

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@type='submit']"))).click();
    }

    /** Returns the visible error/toast message after a failed login attempt. */
    public static String getErrorMessage() {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10));
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath(
                    "//div[contains(@class,'alert-danger')]"
                    + " | //div[contains(@class,'toast-error')]"
                    + " | //div[contains(@class,'error-message')]"
                    + " | //*[contains(@class,'invalid-feedback') and normalize-space(text())!='']")));
        return errorMsg.getText().trim();
    }

    /** Returns true when the post-login dashboard/home element is visible. */
    public static boolean isDashboardVisible() {
        try {
            WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath(
                        "//a[contains(@class,'navbar-brand')]"
                        + " | //div[contains(@class,'dashboard')]"
                        + " | //span[contains(@class,'pcoded-mtext') and text()='Contract']")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns true when the URL still contains '/login' (i.e., login did not succeed). */
    public static boolean isStillOnLoginPage() {
        return DriverManager.getDriver().getCurrentUrl().contains("/login");
    }
}
