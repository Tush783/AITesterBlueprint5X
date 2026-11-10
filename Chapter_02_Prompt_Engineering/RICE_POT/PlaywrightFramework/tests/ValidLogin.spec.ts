/**
 * ValidLogin.spec.ts — positive tests for the Wingify login page.
 *
 * Flow for every test:
 *   beforeAll  -> check the site is reachable (once for the file)
 *   beforeEach -> open the login page in a fresh browser page
 *   test       -> call LoginPage actions and validations (no locators here)
 *   afterEach  -> attach a screenshot to Allure if the test failed
 *   afterAll   -> close the API client created in beforeAll
 */
import type { APIRequestContext } from '@playwright/test';
import { test, openHealthCheckClient } from '../src/fixtures/testFixtures';
import { ENV, hasValidCredentials } from '../src/config/env';
import { SAMPLE_INPUT } from '../src/test-data/loginData';
import { label, note, attachOnFailure, Severity } from '../src/utils/allureHelper';

test.describe('Wingify login - valid scenarios', () => {
  let healthClient: APIRequestContext;

  test.beforeAll(async ({ playwright }) => {
    healthClient = await openHealthCheckClient(playwright);
  });

  test.beforeEach(async ({ loginPage }) => {
    await loginPage.open();
  });

  test.afterEach(async ({ page }, testInfo) => {
    await attachOnFailure(page, testInfo);
  });

  test.afterAll(async () => {
    await healthClient?.dispose();
  });

  // Login page visibility and URL validation.
  test('login page is displayed with the correct title and URL', async ({ loginPage }) => {
    await label('Login', 'Login page visibility', Severity.BLOCKER);
    await loginPage.expectPageLoaded();
  });

  // Email field: visible, editable, correct type and placeholder, keeps the typed value.
  test('email field is ready and accepts input', async ({ loginPage }) => {
    await label('Login', 'Field validation', Severity.CRITICAL);
    await loginPage.expectEmailFieldReady();
    await loginPage.enterEmail(SAMPLE_INPUT.email);
    await loginPage.expectFieldValues(SAMPLE_INPUT.email, '');
  });

  // Password field: hidden by default, and the eye button shows and hides it again.
  test('password field is masked and its visibility can be toggled', async ({ loginPage }) => {
    await label('Login', 'Field validation', Severity.CRITICAL);
    await loginPage.expectPasswordFieldReady();
    await loginPage.enterPassword(SAMPLE_INPUT.password);
    await loginPage.togglePasswordVisibility();
    await loginPage.expectPasswordMasked(false);
    await loginPage.togglePasswordVisibility();
    await loginPage.expectPasswordMasked(true);
  });

  // Sign in button: visible, enabled, correct text.
  test('Sign in button is visible and enabled', async ({ loginPage }) => {
    await label('Login', 'Login page visibility', Severity.CRITICAL);
    await loginPage.expectSignInButtonReady();
  });

  // Remember me: one click ticks it, a second click unticks it.
  test('Remember me can be ticked and unticked', async ({ loginPage }) => {
    await label('Login', 'Remember me', Severity.NORMAL);
    await loginPage.expectRememberMeReady();
    const initial = await loginPage.isRememberMeChecked();
    await loginPage.toggleRememberMe();
    test.expect(await loginPage.isRememberMeChecked()).toBe(!initial);
    await loginPage.toggleRememberMe();
    test.expect(await loginPage.isRememberMeChecked()).toBe(initial);
  });

  // Successful login, navigation and session creation.
  // Needs WINGIFY_EMAIL and WINGIFY_PASSWORD in .env; otherwise it is skipped.
  test('user with valid credentials is logged in and a session is created', async ({ loginPage }) => {
    await label('Login', 'Successful login', Severity.BLOCKER);
    test.skip(!hasValidCredentials(), 'Insufficient information to determine: WINGIFY_EMAIL and WINGIFY_PASSWORD are not set');
    await note(
      'Inference (low confidence): the page after login is not specified, so success is checked as ' +
        '"left #/login, login form hidden, still logged in after reload, cookies stored".',
    );
    await loginPage.login(ENV.validEmail, ENV.validPassword);
    await loginPage.expectLoggedIn();
    await loginPage.expectSessionPersistsAfterReload();
  });
});
