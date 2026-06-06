package com.opms.pages;

import com.opms.base.DriverManager;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class AppointmentPage {

    public static void addExamAppt() {
        WebDriver driver = DriverManager.getDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        WebElement clickExam = driver.findElement(By.xpath(
                "//button[@class='btn btn-outline-primary hei-35 rounded-3 patient-primary-button cursor-pointer']"));
        clickExam.click();

        WebElement selectApptSlot = driver.findElement(
                By.xpath("(//td[contains(@class, 'dx-scheduler-date-table-cell')])[5]"));
        new Actions(driver).doubleClick(selectApptSlot).perform();

        WebElement selectApptDropdown = driver.findElement(By.xpath(
                "//div//input[contains(@id,'practiceAppointmentTypeId')]"));
        selectApptDropdown.click();

        driver.findElement(By.xpath(
                "//div[contains(@class, 'dx-list-item-content')]//span[text()='New Patient Exam']")).click();

        driver.findElement(By.xpath("//div//input[@class='dx-button-submit-input']")).click();
    }

    public static void main(String[] args) {
        addExamAppt();
    }
}
