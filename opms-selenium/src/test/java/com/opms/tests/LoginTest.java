package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Login positive and negative test scenarios.
 *
 * Each @Test gets a fresh browser session via @BeforeMethod / @AfterMethod
 * so failures do not bleed into each other.
 */
public class LoginTest {

    @BeforeMethod
    public void openBrowser() {
        DriverManager.getDriver(); // initialises driver
    }

    @AfterMethod
    public void closeBrowser() {
        DriverManager.quitDriver();
    }

    // ─── POSITIVE TEST CASES ───────────────────────────────────────────────────

    @Test(priority = 1, description = "Valid email and valid password should navigate to dashboard")
    public void testValidLogin() {
        LoginPage.loginWith(LoginPage.VALID_EMAIL, LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isDashboardVisible(),
                "Dashboard should be visible after valid login.");
        System.out.println("TC01 PASS – valid credentials login successful.");
    }

    @Test(priority = 2, description = "Email with uppercase letters should be treated as case-insensitive")
    public void testValidLoginEmailCaseInsensitive() {
        LoginPage.loginWith(LoginPage.VALID_EMAIL.toUpperCase(), LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isDashboardVisible(),
                "Login should succeed even when email is in uppercase.");
        System.out.println("TC02 PASS – case-insensitive email login successful.");
    }

    @Test(priority = 3, description = "Login page URL should redirect to login when not authenticated")
    public void testLoginPageLoads() {
        DriverManager.getDriver().get(LoginPage.LOGIN_URL);
        Assert.assertTrue(DriverManager.getDriver().getCurrentUrl().contains("/login"),
                "Login page URL should contain '/login'.");
        System.out.println("TC03 PASS – login page loads correctly.");
    }

    // ─── NEGATIVE TEST CASES ───────────────────────────────────────────────────

    @Test(priority = 4, description = "Wrong password should show error and stay on login page")
    public void testInvalidPassword() {
        LoginPage.loginWith(LoginPage.VALID_EMAIL, "WrongPassword@99");
        Assert.assertTrue(LoginPage.isStillOnLoginPage(),
                "Should remain on login page after wrong password.");
        System.out.println("TC04 PASS – invalid password blocked correctly.");
    }

    @Test(priority = 5, description = "Wrong email should show error and stay on login page")
    public void testInvalidEmail() {
        LoginPage.loginWith("wronguser@test.com", LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isStillOnLoginPage(),
                "Should remain on login page after wrong email.");
        System.out.println("TC05 PASS – invalid email blocked correctly.");
    }

    @Test(priority = 6, description = "Both email and password wrong should stay on login page")
    public void testInvalidEmailAndPassword() {
        LoginPage.loginWith("nouser@fake.com", "BadPass@000");
        Assert.assertTrue(LoginPage.isStillOnLoginPage(),
                "Should remain on login page when both credentials are wrong.");
        System.out.println("TC06 PASS – both invalid credentials blocked correctly.");
    }

    @Test(priority = 7, description = "Blank email should not submit / show validation error")
    public void testBlankEmail() {
        LoginPage.loginWith("", LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isStillOnLoginPage(),
                "Should remain on login page when email is blank.");
        System.out.println("TC07 PASS – blank email prevented submission.");
    }

    @Test(priority = 8, description = "Blank password should not submit / show validation error")
    public void testBlankPassword() {
        LoginPage.loginWith(LoginPage.VALID_EMAIL, "");
        Assert.assertTrue(LoginPage.isStillOnLoginPage(),
                "Should remain on login page when password is blank.");
        System.out.println("TC08 PASS – blank password prevented submission.");
    }

    @Test(priority = 9, description = "Both fields blank should not submit")
    public void testBlankEmailAndPassword() {
        LoginPage.loginWith("", "");
        Assert.assertTrue(LoginPage.isStillOnLoginPage(),
                "Should remain on login page when both fields are blank.");
        System.out.println("TC09 PASS – blank credentials prevented submission.");
    }

    @Test(priority = 10, description = "Invalid email format (no @) should not submit")
    public void testInvalidEmailFormat() {
        LoginPage.loginWith("notanemail", LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isStillOnLoginPage(),
                "Should remain on login page for malformed email.");
        System.out.println("TC10 PASS – malformed email format blocked.");
    }

    @Test(priority = 11, description = "SQL injection in email field should be rejected")
    public void testSqlInjectionInEmail() {
        LoginPage.loginWith("' OR '1'='1", LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isStillOnLoginPage(),
                "SQL injection in email should not bypass login.");
        System.out.println("TC11 PASS – SQL injection in email rejected.");
    }

    @Test(priority = 12, description = "SQL injection in password field should be rejected")
    public void testSqlInjectionInPassword() {
        LoginPage.loginWith(LoginPage.VALID_EMAIL, "' OR '1'='1");
        Assert.assertTrue(LoginPage.isStillOnLoginPage(),
                "SQL injection in password should not bypass login.");
        System.out.println("TC12 PASS – SQL injection in password rejected.");
    }

    @Test(priority = 13, description = "XSS script in email field should be treated as plain text and rejected")
    public void testXssInEmail() {
        LoginPage.loginWith("<script>alert('xss')</script>", LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isStillOnLoginPage(),
                "XSS payload in email should not bypass login.");
        System.out.println("TC13 PASS – XSS in email field rejected.");
    }

    @Test(priority = 14, description = "Whitespace-only email should not submit")
    public void testWhitespaceEmail() {
        LoginPage.loginWith("   ", LoginPage.VALID_PASSWORD);
        Assert.assertTrue(LoginPage.isStillOnLoginPage(),
                "Whitespace-only email should not pass login.");
        System.out.println("TC14 PASS – whitespace email rejected.");
    }

    @Test(priority = 15, description = "Password with only spaces should not log in")
    public void testWhitespacePassword() {
        LoginPage.loginWith(LoginPage.VALID_EMAIL, "   ");
        Assert.assertTrue(LoginPage.isStillOnLoginPage(),
                "Whitespace-only password should not pass login.");
        System.out.println("TC15 PASS – whitespace password rejected.");
    }
}
