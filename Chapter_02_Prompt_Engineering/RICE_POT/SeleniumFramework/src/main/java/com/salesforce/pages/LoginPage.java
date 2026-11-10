package com.salesforce.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

// Page Object for the Salesforce login page.
// It holds the page elements and the actions a user can perform on them.
// Test classes call these methods instead of using Selenium directly.
public class LoginPage {

    // The browser this page object controls.
    private final WebDriver driver;
    // Waits for elements to become visible or clickable, up to the given timeout.
    private final WebDriverWait wait;

    // Username (email) text box.
    @FindBy(xpath = "//input[@id='username']")
    private WebElement usernameInput;

    // Password text box. Salesforce shows it only after a username is submitted.
    @FindBy(xpath = "//input[@id='password']")
    private WebElement passwordInput;

    // "Log In" button. It is used to submit both the username and the password.
    @FindBy(xpath = "//input[@id='Login']")
    private WebElement loginButton;

    // "Remember me" checkbox.
    @FindBy(xpath = "//input[@id='rememberUn']")
    private WebElement rememberMeCheckbox;

    // Text label shown next to the "Remember me" checkbox.
    @FindBy(xpath = "//label[@for='rememberUn']")
    private WebElement rememberMeLabel;

    // Red error message shown when login fails.
    @FindBy(xpath = "//div[@id='error']")
    private WebElement errorMessage;

    // Shows the submitted username above the password box.
    @FindBy(xpath = "//span[@id='idcard-identity']")
    private WebElement identityCard;

    // Link on the alternate "Log In with Email" page that switches back to the normal username form.
    @FindBy(xpath = "//a[contains(@href,'login=1') and contains(normalize-space(.),'Log In with Username')]")
    private WebElement loginWithUsernameLink;

    // Constructor: stores the browser, creates the wait object
    // and connects the @FindBy fields above to the real page elements.
    public LoginPage(WebDriver driver, Duration timeout) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver instance must not be null");
        }
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
        PageFactory.initElements(driver, this);
    }

    // Opens the login page and waits until the username box is visible.
    // Salesforce sometimes shows a different "Log In with Email" page;
    // in that case we click "Log In with Username" to return to the normal form.
    @Step("Open login page: {url}")
    public LoginPage open(String url) {
        try {
            driver.get(url);
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOf(usernameInput),
                    ExpectedConditions.visibilityOf(loginWithUsernameLink)));
            if (!isCurrentlyDisplayed(usernameInput)) {
                click(loginWithUsernameLink, "Log In with Username link");
            }
            wait.until(ExpectedConditions.visibilityOf(usernameInput));
            return this;
        } catch (TimeoutException e) {
            throw new LoginPageException("Login page did not load within timeout: " + url, e);
        } catch (WebDriverException e) {
            throw new LoginPageException("Unable to open login page: " + url, e);
        }
    }

    // Types the given text into the username box.
    @Step("Enter username: {username}")
    public LoginPage enterUsername(String username) {
        type(usernameInput, username, "username");
        return this;
    }

    // Types the given text into the password box.
    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        type(passwordInput, password, "password");
        return this;
    }

    // Clicks the "Log In" button.
    @Step("Click Log In button")
    public void clickLogin() {
        click(loginButton, "Log In button");
    }

    // Clicks the "Remember me" checkbox (ticks it if empty, unticks it if ticked).
    @Step("Toggle Remember me checkbox")
    public LoginPage toggleRememberMe() {
        click(rememberMeCheckbox, "Remember me checkbox");
        return this;
    }

    // Step 1 of login: enter the username and click "Log In".
    @Step("Submit username: {username}")
    public LoginPage submitUsername(String username) {
        enterUsername(username);
        clickLogin();
        return this;
    }

    // Full login in two steps.
    // Step 1: submit the username.
    // Step 2: if the password box appears, enter the password and click "Log In" again.
    // If an error appears instead, we stop so the test can read the error.
    @Step("Login with username: {username}")
    public void doLogin(String username, String password) {
        submitUsername(username);
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOf(passwordInput),
                    ExpectedConditions.visibilityOf(errorMessage)));
        } catch (TimeoutException e) {
            throw new LoginPageException("Neither password step nor error message appeared after submitting username", e);
        }
        if (isCurrentlyDisplayed(passwordInput)) {
            enterPassword(password);
            clickLogin();
        }
    }

    // Waits for the error message and returns its text.
    @Step("Read login error message")
    public String getErrorMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(errorMessage)).getText().trim();
        } catch (TimeoutException e) {
            throw new LoginPageException("Login error message was not displayed", e);
        }
    }

    // Returns true when the "Log In" button disappears,
    // meaning the user has moved past the login page.
    @Step("Wait for navigation away from login page")
    public boolean isLoginFormDismissed() {
        try {
            return wait.until(ExpectedConditions.invisibilityOf(loginButton));
        } catch (TimeoutException e) {
            return false;
        }
    }

    // Returns the browser tab title.
    public String getPageTitle() {
        try {
            return driver.getTitle();
        } catch (WebDriverException e) {
            throw new LoginPageException("Unable to read page title", e);
        }
    }

    // Returns the address currently shown in the browser.
    public String getCurrentUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (WebDriverException e) {
            throw new LoginPageException("Unable to read current URL", e);
        }
    }

    // Returns the username shown above the password box.
    public String getIdentityCardText() {
        return waitVisible(identityCard, "identity card").getText().trim();
    }

    // Returns the text currently typed in the username box.
    public String getUsernameValue() {
        return getProperty(usernameInput, "value", "username");
    }

    // Returns the text currently typed in the password box.
    public String getPasswordValue() {
        return getProperty(passwordInput, "value", "password");
    }

    // Returns the input type of the password box. "password" means the characters are hidden.
    public String getPasswordFieldType() {
        return getProperty(passwordInput, "type", "password");
    }

    // Returns the text written on the "Log In" button.
    public String getLoginButtonValue() {
        return getProperty(loginButton, "value", "Log In button");
    }

    // Returns the text next to the "Remember me" checkbox.
    public String getRememberMeLabelText() {
        return waitVisible(rememberMeLabel, "Remember me label").getText().trim();
    }

    // The is...Displayed methods below return true if the element becomes visible within the timeout.
    public boolean isUsernameDisplayed() {
        return isDisplayed(usernameInput);
    }

    public boolean isPasswordDisplayed() {
        return isDisplayed(passwordInput);
    }

    public boolean isLoginButtonEnabled() {
        return isDisplayed(loginButton) && loginButton.isEnabled();
    }

    public boolean isRememberMeDisplayed() {
        return isDisplayed(rememberMeCheckbox);
    }

    // Returns true if the "Remember me" checkbox is ticked.
    public boolean isRememberMeSelected() {
        try {
            return rememberMeCheckbox.isSelected();
        } catch (WebDriverException e) {
            throw new LoginPageException("Unable to read Remember me checkbox state", e);
        }
    }

    // Helper: waits for a text box, clears it, types the value,
    // then checks that the value really stayed in the box.
    private void type(WebElement element, String value, String fieldName) {
        try {
            String expected = value == null ? "" : value;
            WebElement field = wait.until(ExpectedConditions.elementToBeClickable(element));
            field.clear();
            if (!expected.isEmpty()) {
                field.sendKeys(expected);
            }
            wait.until(d -> expected.equals(field.getDomProperty("value")));
        } catch (TimeoutException e) {
            throw new LoginPageException("Value was not retained in " + fieldName + " field", e);
        } catch (WebDriverException e) {
            throw new LoginPageException("Unable to type into " + fieldName + " field", e);
        }
    }

    // Helper: waits until the element can be clicked, then clicks it.
    private void click(WebElement element, String elementName) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (TimeoutException e) {
            throw new LoginPageException(elementName + " was not clickable within timeout", e);
        } catch (WebDriverException e) {
            throw new LoginPageException("Unable to click " + elementName, e);
        }
    }

    // Helper: waits until the element is visible and returns it.
    private WebElement waitVisible(WebElement element, String elementName) {
        try {
            return wait.until(ExpectedConditions.visibilityOf(element));
        } catch (TimeoutException e) {
            throw new LoginPageException(elementName + " was not visible within timeout", e);
        }
    }

    // Helper: reads a property (for example "value" or "type") from a visible element.
    private String getProperty(WebElement element, String property, String elementName) {
        try {
            return waitVisible(element, elementName).getDomProperty(property);
        } catch (WebDriverException e) {
            throw new LoginPageException("Unable to read '" + property + "' of " + elementName, e);
        }
    }

    // Helper: waits for the element; returns false instead of failing if it never appears.
    private boolean isDisplayed(WebElement element) {
        try {
            return wait.until(ExpectedConditions.visibilityOf(element)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    // Helper: checks visibility right now without waiting.
    // Returns false if the element is not on the page.
    private boolean isCurrentlyDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    // Custom error thrown by this page object.
    // It gives a clear message (for example "password was not visible") and keeps the original Selenium error.
    public static class LoginPageException extends RuntimeException {
        public LoginPageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
