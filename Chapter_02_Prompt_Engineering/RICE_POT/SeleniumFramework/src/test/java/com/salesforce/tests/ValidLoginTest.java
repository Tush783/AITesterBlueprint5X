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
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

// Positive (valid) test cases for the Salesforce login page.
// @Epic and @Feature group these tests in the Allure report.
@Epic("Salesforce CRM")
@Feature("Login")
public class ValidLoginTest {

    // Browser tab title expected on the login page.
    private static final String EXPECTED_TITLE = "Login | Salesforce";

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

    // Test 1: the login page opens with the correct title.
    @Test(priority = 1, description = "Login page loads with the expected title")
    @Story("Valid login page behaviour")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify the Salesforce login page loads and displays the expected browser title")
    public void verifyLoginPageTitle() {
        Assert.assertEquals(loginPage.getPageTitle(), EXPECTED_TITLE, "Login page title mismatch");
    }

    // Test 2: the username box, Log In button and Remember me checkbox are shown with the correct text.
    @Test(priority = 2, description = "Login form elements are displayed and usable")
    @Story("Valid login page behaviour")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify username field, Log In button and Remember me checkbox are present on the username step")
    public void verifyLoginFormElementsDisplayed() {
        Assert.assertTrue(loginPage.isUsernameDisplayed(), "Username field is not displayed");
        Assert.assertTrue(loginPage.isLoginButtonEnabled(), "Log In button is not enabled");
        Assert.assertTrue(loginPage.isRememberMeDisplayed(), "Remember me checkbox is not displayed");
        Assert.assertEquals(loginPage.getLoginButtonValue(), "Log In", "Log In button label mismatch");
        Assert.assertEquals(loginPage.getRememberMeLabelText(), "Remember me", "Remember me label mismatch");
    }

    // Test 3: after a username is entered and Log In is clicked,
    // the password box appears and the username is shown above it.
    @Test(priority = 3, description = "Submitting a username moves to the password step")
    @Story("Valid login page behaviour")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify the entered username is retained and shown on the identity card of the password step")
    public void verifyUsernameSubmissionShowsPasswordStep() {
        String username = "qa.automation@example.com";
        loginPage.enterUsername(username);
        Assert.assertEquals(loginPage.getUsernameValue(), username, "Username value not retained");
        loginPage.clickLogin();
        Assert.assertTrue(loginPage.isPasswordDisplayed(), "Password field is not displayed after submitting username");
        Assert.assertEquals(loginPage.getIdentityCardText(), username, "Identity card does not show the submitted username");
    }

    // Test 4: the password box keeps the typed value and hides the characters.
    @Test(priority = 4, description = "Password field accepts input and masks the entered value")
    @Story("Valid login page behaviour")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify the password input retains the typed value and is of type password so characters are masked")
    public void verifyPasswordFieldIsMasked() {
        String password = "Sample@12345";
        loginPage.submitUsername("qa.automation@example.com").enterPassword(password);
        Assert.assertEquals(loginPage.getPasswordValue(), password, "Password value not retained");
        Assert.assertEquals(loginPage.getPasswordFieldType(), "password", "Password field is not masked");
    }

    // Test 5: clicking Remember me once ticks it, clicking again unticks it.
    @Test(priority = 5, description = "Remember me checkbox can be selected and cleared")
    @Story("Remember me")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the Remember me checkbox toggles between selected and unselected states")
    public void verifyRememberMeToggle() {
        boolean initialState = loginPage.isRememberMeSelected();
        loginPage.toggleRememberMe();
        Assert.assertNotEquals(loginPage.isRememberMeSelected(), initialState, "Remember me did not toggle on click");
        loginPage.toggleRememberMe();
        Assert.assertEquals(loginPage.isRememberMeSelected(), initialState, "Remember me did not revert on second click");
    }

    // Test 6: real login with valid credentials.
    // Credentials come from -Dsf.username / -Dsf.password, or from the .env file
    // (VALID_USERNAME / VALID_PASSWORD). If none are found, the test is skipped.
    @Test(priority = 6, description = "User logs in with valid credentials")
    @Story("Valid login")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify a user with valid credentials leaves the login page; credentials come from -Dsf.username/-Dsf.password or the project .env file")
    public void verifySuccessfulLoginWithValidCredentials() {
        String username = resolveSetting("sf.username", "VALID_USERNAME");
        String password = resolveSetting("sf.password", "VALID_PASSWORD");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new SkipException("Insufficient information to determine: valid credentials not provided");
        }
        // Log in, then check that the login form is gone and the address has changed.
        loginPage.doLogin(username, password);
        Assert.assertTrue(loginPage.isLoginFormDismissed(), "Login form is still displayed after submitting valid credentials");
        Assert.assertNotEquals(loginPage.getCurrentUrl(), baseUrl, "URL did not change after valid login");
    }

    // Finds a setting value.
    // It first checks the command line (-Dname=value), then the .env file in the project folder.
    // Returns null if the value is not found in either place.
    private String resolveSetting(String systemProperty, String envFileKey) {
        String value = System.getProperty(systemProperty);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        Path envFile = Path.of(System.getProperty("user.dir"), ".env");
        if (!Files.isReadable(envFile)) {
            return null;
        }
        try {
            return Files.readAllLines(envFile).stream()
                    .map(String::trim)
                    .filter(line -> !line.startsWith("#") && line.contains("="))
                    .filter(line -> line.substring(0, line.indexOf('=')).trim().equalsIgnoreCase(envFileKey))
                    .map(line -> line.substring(line.indexOf('=') + 1).trim())
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read " + envFile, e);
        }
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
