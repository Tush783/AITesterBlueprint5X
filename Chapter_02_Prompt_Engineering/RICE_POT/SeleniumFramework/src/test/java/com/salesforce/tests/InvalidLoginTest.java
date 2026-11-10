package com.salesforce.tests;

import com.salesforce.pages.LoginPage;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.time.Duration;

// Negative (invalid) test cases for the Salesforce login page.
// Each test checks that login is blocked and the correct error is shown.
@Epic("Salesforce CRM")
@Feature("Login")
public class InvalidLoginTest {

    // Error messages shown by Salesforce. The tests check that the shown error contains this text.
    private static final String INVALID_CREDENTIALS_ERROR = "Please check your username and password.";
    private static final String EMPTY_USERNAME_ERROR = "Please enter your username.";
    private static final String EMPTY_PASSWORD_ERROR = "Please enter your password.";

    // The browser used by the current test.
    private WebDriver driver;
    // Page object with all login page actions.
    private LoginPage loginPage;
    // Login page address, read from testng.xml.
    private String baseUrl;
    // Maximum time to wait for elements, read from testng.xml.
    private Duration timeout;

    // Runs once before the tests in this class.
    // Reads baseUrl and timeoutSeconds from testng.xml.
    // Both can be overridden from the command line, for example -DbaseUrl=...
    @BeforeTest
    @Parameters({"baseUrl", "timeoutSeconds"})
    public void loadConfiguration(String baseUrl, String timeoutSeconds) {
        this.baseUrl = System.getProperty("baseUrl", baseUrl);
        this.timeout = Duration.ofSeconds(Long.parseLong(System.getProperty("timeoutSeconds", timeoutSeconds)));
    }

    // Runs before every test: starts a new Chrome browser and opens the login page.
    // The browser is visible by default; run with -Dheadless=true to hide it.
    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        try {
            ChromeOptions options = new ChromeOptions();
            if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
                options.addArguments("--headless=new");
            }
            options.addArguments("--start-maximized", "--disable-notifications", "--window-size=1920,1080");
            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(timeout.multipliedBy(2));
            loginPage = new LoginPage(driver, timeout).open(baseUrl);
        } catch (WebDriverException e) {
            tearDownDriver();
            throw new IllegalStateException("Browser setup failed for " + baseUrl, e);
        }
    }

    // Test data: each row is {username, password} for an account that does not exist.
    // Test 1 runs once for each row.
    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        return new Object[][]{
                {"invalid.user@example.com", "WrongPass@123"},
                {"qa.automation.nonexistent@example.com", "Invalid#987"}
        };
    }

    // Test 1: wrong username and password show the "check your username and password" error
    // and the user stays on the login page.
    @Test(priority = 1, dataProvider = "invalidCredentials", description = "Login fails with invalid username and password")
    @Story("Invalid login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify an error is displayed and the user stays on the login page for invalid credentials")
    public void verifyLoginFailsWithInvalidCredentials(String username, String password) {
        loginPage.doLogin(username, password);
        String error = loginPage.getErrorMessage();
        Assert.assertTrue(error.contains(INVALID_CREDENTIALS_ERROR), "Unexpected error message: " + error);
        Assert.assertTrue(loginPage.isUsernameDisplayed(), "User navigated away from login page with invalid credentials");
    }

    // Test 2: clicking Log In with an empty username shows "Please enter your username."
    @Test(priority = 2, description = "Login fails when username is empty")
    @Story("Invalid login")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the username required message is displayed when username is blank")
    public void verifyLoginFailsWithEmptyUsername() {
        loginPage.doLogin("", "WrongPass@123");
        String error = loginPage.getErrorMessage();
        Assert.assertTrue(error.contains(EMPTY_USERNAME_ERROR), "Empty username error mismatch: " + error);
    }

    // Test 3: a username followed by an empty password shows "Please enter your password."
    @Test(priority = 3, description = "Login fails when password is empty")
    @Story("Invalid login")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the password required message is displayed when password is blank")
    public void verifyLoginFailsWithEmptyPassword() {
        loginPage.doLogin("invalid.user@example.com", "");
        String error = loginPage.getErrorMessage();
        Assert.assertTrue(error.contains(EMPTY_PASSWORD_ERROR), "Empty password error mismatch: " + error);
    }

    // Test 4: both fields empty shows "Please enter your username."
    @Test(priority = 4, description = "Login fails when both fields are empty")
    @Story("Invalid login")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the username required message is displayed when both fields are blank")
    public void verifyLoginFailsWithEmptyCredentials() {
        loginPage.doLogin("", "");
        String error = loginPage.getErrorMessage();
        Assert.assertTrue(error.contains(EMPTY_USERNAME_ERROR), "Empty credentials error mismatch: " + error);
    }

    // Test 5: a username that is not an email (no @) does not open the password step;
    // Salesforce shows "Please enter your password." right away.
    @Test(priority = 5, description = "Non-email username does not reach the password step")
    @Story("Invalid login")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify a username without email format is rejected on the username step with the password required message")
    public void verifyNonEmailUsernameIsRejected() {
        loginPage.submitUsername("invaliduser");
        String error = loginPage.getErrorMessage();
        Assert.assertTrue(error.contains(EMPTY_PASSWORD_ERROR), "Non-email username error mismatch: " + error);
        Assert.assertTrue(loginPage.isUsernameDisplayed(), "Username step is no longer displayed");
    }

    // Test 6: after a failed login, the password box is emptied for security.
    @Test(priority = 6, description = "Password field is cleared after failed login")
    @Story("Invalid login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify the password is not retained in the field after an unsuccessful login attempt")
    public void verifyPasswordClearedAfterFailedLogin() {
        loginPage.doLogin("invalid.user@example.com", "WrongPass@123");
        loginPage.getErrorMessage();
        Assert.assertEquals(loginPage.getPasswordValue(), "", "Password was retained after failed login");
    }

    // Runs after every test.
    // If the test failed, it takes a screenshot and attaches it to the Allure report.
    // It always closes the browser at the end.
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (driver != null && result.getStatus() == ITestResult.FAILURE) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("Failure - " + result.getName(), "image/png", new ByteArrayInputStream(screenshot), "png");
            }
        } catch (WebDriverException e) {
            Allure.addAttachment("Screenshot error", e.getMessage());
        } finally {
            tearDownDriver();
        }
    }

    // Closes the browser safely, even if closing it throws an error.
    private void tearDownDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (WebDriverException e) {
                Allure.addAttachment("Driver quit error", e.getMessage());
            } finally {
                driver = null;
            }
        }
    }
}
