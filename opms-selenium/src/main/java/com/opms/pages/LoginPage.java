package com.opms.pages;

import com.opms.base.DriverManager;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {

    public static void login() throws InterruptedException {
        WebDriver driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        driver.get("https://demo.orthopms.com/login");

        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        email.sendKeys("lokeshqa1@test.com");

        WebElement password = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        password.sendKeys("Lokesh@123");

        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@type='submit']")));
        loginBtn.click();

        System.out.println("Logged In successfully");
    }
}
