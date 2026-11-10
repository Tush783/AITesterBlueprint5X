/**
 * LoginPage.ts — Page Object for the Wingify login page (https://app.wingify.com/#/login).
 *
 * How it connects:
 *   tests/*.spec.ts  ->  LoginPage (this file)  ->  BasePage (shared helpers)  ->  Playwright
 *
 * - Locators: every element is found with XPath, and only here. Tests never contain locators.
 * - Actions: methods that do something (type, click, log in).
 * - Validations: methods starting with "expect" that check the page with Playwright's expect().
 * Each public method is wrapped in step() so it shows up as a readable step in the Allure report.
 */
import { expect, type Locator, type Page } from '@playwright/test';
import { BasePage } from './BasePage';
import { ENV } from '../config/env';
import { LOGIN_PAGE_TEXT } from '../test-data/loginData';
import { step } from '../utils/allureHelper';

export class LoginPage extends BasePage {
  // The page contains several hidden forms (forgot password, SSO, reset) that reuse
  // the same field names, so every locator starts from the visible login form.
  private static readonly FORM = "//form[@id='js-login-form']";

  readonly loginForm: Locator;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly passwordVisibilityToggle: Locator;
  readonly signInButton: Locator;
  readonly rememberMeCheckbox: Locator;
  readonly rememberMeLabel: Locator;
  readonly forgotPasswordButton: Locator;
  readonly errorMessage: Locator;

  // Constructor: creates every locator once. Locators are lazy, so nothing is searched yet.
  constructor(page: Page) {
    super(page);
    const form = LoginPage.FORM;
    this.loginForm = this.xpath(form);
    this.emailInput = this.xpath(`${form}//input[@id='login-username']`);
    this.passwordInput = this.xpath(`${form}//input[@id='login-password']`);
    this.passwordVisibilityToggle = this.xpath(`${form}//button[@aria-label='Toggle password visibility']`);
    this.signInButton = this.xpath(`${form}//button[@id='js-login-btn']`);
    // The real checkbox is visually hidden; users click its label instead.
    this.rememberMeCheckbox = this.xpath("//input[@id='checkbox-remember']");
    this.rememberMeLabel = this.xpath(`${form}//label[@for='checkbox-remember']`);
    this.forgotPasswordButton = this.xpath(`${form}//button[@vwo-html-translate='login:forgotPassword']`);
    // Red/yellow box that appears above the form after a failed login.
    this.errorMessage = this.xpath("//div[@id='js-notification-box-msg']");
  }

  // ---------- Actions ----------

  // Opens the login page and waits until the login form is visible.
  async open(): Promise<void> {
    await step('Open the Wingify login page', async () => {
      await this.navigate(ENV.loginPath);
      await this.waitForVisible(this.loginForm);
      await this.waitForVisible(this.emailInput);
    });
  }

  // Types an email address into the email box.
  async enterEmail(email: string): Promise<void> {
    await step(`Enter email "${email}"`, () => this.type(this.emailInput, email));
  }

  // Types a password into the password box. The value is not shown in the report.
  async enterPassword(password: string): Promise<void> {
    await step('Enter password', () => this.type(this.passwordInput, password));
  }

  // Clicks the "Sign in" button.
  async clickSignIn(): Promise<void> {
    await step('Click Sign in', () => this.click(this.signInButton));
  }

  // Full login: email, password, then Sign in. Used by both valid and invalid tests.
  async login(email: string, password: string): Promise<void> {
    await step('Log in', async () => {
      await this.enterEmail(email);
      await this.enterPassword(password);
      await this.clickSignIn();
    });
  }

  // Clicks the "Remember me" label, which ticks or unticks the checkbox.
  async toggleRememberMe(): Promise<void> {
    await step('Toggle Remember me', () => this.click(this.rememberMeLabel));
  }

  // Clicks the eye button that shows or hides the password.
  async togglePasswordVisibility(): Promise<void> {
    await step('Toggle password visibility', () => this.click(this.passwordVisibilityToggle));
  }

  // Returns true if "Remember me" is ticked.
  async isRememberMeChecked(): Promise<boolean> {
    return this.rememberMeCheckbox.isChecked();
  }

  // Returns the error message text after waiting for it to appear.
  async getErrorMessage(): Promise<string> {
    return step('Read the login error message', () => this.textOf(this.errorMessage));
  }

  // ---------- Validations ----------

  // Checks the page title, the URL and that the login form is shown.
  async expectPageLoaded(): Promise<void> {
    await step('Verify the login page is displayed', async () => {
      await expect(this.page).toHaveTitle(LOGIN_PAGE_TEXT.title);
      await this.expectOnLoginUrl();
      await expect(this.loginForm).toBeVisible();
    });
  }

  // Checks the URL still points to the login page.
  async expectOnLoginUrl(): Promise<void> {
    await expect(this.page).toHaveURL(new RegExp(`${LOGIN_PAGE_TEXT.urlFragment}$`));
  }

  // Checks the email box: visible, editable, email type and the expected placeholder.
  async expectEmailFieldReady(): Promise<void> {
    await step('Verify the email field', async () => {
      await expect(this.emailInput).toBeVisible();
      await expect(this.emailInput).toBeEditable();
      await expect(this.emailInput).toHaveAttribute('type', 'email');
      await expect(this.emailInput).toHaveAttribute('placeholder', LOGIN_PAGE_TEXT.emailPlaceholder);
    });
  }

  // Checks the password box: visible, editable, hidden characters and the expected placeholder.
  async expectPasswordFieldReady(): Promise<void> {
    await step('Verify the password field', async () => {
      await expect(this.passwordInput).toBeVisible();
      await expect(this.passwordInput).toBeEditable();
      await this.expectPasswordMasked(true);
      await expect(this.passwordInput).toHaveAttribute('placeholder', LOGIN_PAGE_TEXT.passwordPlaceholder);
    });
  }

  // Checks whether the password characters are hidden (type=password) or shown (type=text).
  async expectPasswordMasked(masked: boolean): Promise<void> {
    await expect(this.passwordInput).toHaveAttribute('type', masked ? 'password' : 'text');
  }

  // Checks the Sign in button is visible, enabled and has the right text.
  async expectSignInButtonReady(): Promise<void> {
    await step('Verify the Sign in button', async () => {
      await expect(this.signInButton).toBeVisible();
      await expect(this.signInButton).toBeEnabled();
      await expect(this.signInButton).toHaveText(LOGIN_PAGE_TEXT.signInButton);
    });
  }

  // Checks the Remember me label is visible with the right text.
  async expectRememberMeReady(): Promise<void> {
    await step('Verify the Remember me option', async () => {
      await expect(this.rememberMeLabel).toBeVisible();
      await expect(this.rememberMeLabel).toHaveText(LOGIN_PAGE_TEXT.rememberMeLabel);
    });
  }

  // Checks the error box shows exactly the expected message.
  async expectErrorMessage(message: string): Promise<void> {
    await step(`Verify error message "${message}"`, async () => {
      await expect(this.errorMessage).toBeVisible({ timeout: ENV.timeout });
      await expect(this.errorMessage).toHaveText(message);
    });
  }

  // Checks what the boxes contain, for example after a failed login.
  async expectFieldValues(email: string, password: string): Promise<void> {
    await step('Verify email and password field values', async () => {
      await expect(this.emailInput).toHaveValue(email);
      await expect(this.passwordInput).toHaveValue(password);
    });
  }

  // Checks the user has left the login page after a successful login.
  async expectLoggedIn(): Promise<void> {
    await step('Verify navigation away from the login page', async () => {
      await expect(this.page).not.toHaveURL(new RegExp(LOGIN_PAGE_TEXT.urlFragment), { timeout: ENV.timeout });
      await expect(this.loginForm).toBeHidden();
    });
  }

  // Reloads the page and checks the user is still logged in, which shows a session exists.
  async expectSessionPersistsAfterReload(): Promise<void> {
    await step('Verify the session survives a page reload', async () => {
      await this.page.reload({ waitUntil: 'domcontentloaded' });
      await expect(this.page).not.toHaveURL(new RegExp(LOGIN_PAGE_TEXT.urlFragment), { timeout: ENV.timeout });
      const cookies = await this.page.context().cookies(ENV.baseUrl);
      expect(cookies.length, 'No cookies were stored for the application after login').toBeGreaterThan(0);
    });
  }
}
