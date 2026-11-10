/**
 * InvalidLogin.spec.ts — negative tests for the Wingify login page.
 *
 * The invalid cases are data-driven: one test is created for each row of
 * INVALID_LOGIN_CASES in src/test-data/loginData.ts. To add a case, add a row there.
 *
 * Flow for every test:
 *   beforeAll  -> check the site is reachable (once for the file)
 *   beforeEach -> open the login page in a fresh browser page
 *   test       -> log in with bad data and check the error (no locators here)
 *   afterEach  -> attach a screenshot to Allure if the test failed
 *   afterAll   -> close the API client created in beforeAll
 */
import type { APIRequestContext } from '@playwright/test';
import { test, openHealthCheckClient } from '../src/fixtures/testFixtures';
import { INVALID_LOGIN_CASES, LOGIN_ERROR_MESSAGE } from '../src/test-data/loginData';
import { label, attachOnFailure, Severity } from '../src/utils/allureHelper';

test.describe('Wingify login - invalid scenarios', () => {
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

  // One test per data row: the error is shown and the user stays on the login page.
  for (const data of INVALID_LOGIN_CASES) {
    test(`login is rejected: ${data.title}`, async ({ loginPage }) => {
      await label('Login', 'Invalid credential handling', Severity.CRITICAL);
      await loginPage.login(data.email, data.password);
      await loginPage.expectErrorMessage(LOGIN_ERROR_MESSAGE);
      await loginPage.expectOnLoginUrl();
    });
  }

  // After a failed login the password box is emptied, while the email is kept.
  test('password is cleared and email is kept after a failed login', async ({ loginPage }) => {
    await label('Login', 'Field validation', Severity.CRITICAL);
    const [data] = INVALID_LOGIN_CASES;
    await loginPage.login(data.email, data.password);
    await loginPage.expectErrorMessage(LOGIN_ERROR_MESSAGE);
    await loginPage.expectFieldValues(data.email, '');
  });
});
