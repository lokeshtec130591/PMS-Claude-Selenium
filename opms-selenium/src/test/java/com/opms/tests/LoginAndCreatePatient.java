package com.opms.tests;

import com.opms.base.DriverManager;
import com.opms.pages.LoginPage;
import com.opms.pages.PatientPage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class LoginAndCreatePatient {

    @Test(priority = 1)
    public void testLogin() throws InterruptedException {
        LoginPage.login();
    }

    @Test(priority = 2)
    public void testCreatePatient() throws InterruptedException {
        PatientPage.createPatient();
    }

    @AfterClass
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
