/**
 * testFixtures.ts — the "test" object that every spec file imports.
 *
 * It extends Playwright's built-in test with a `loginPage` fixture:
 * any test that asks for { loginPage } receives a ready LoginPage object
 * for its own fresh browser page. Tests therefore never write `new LoginPage(page)`.
 *
 * To add another page later, add one more fixture here (for example dashboardPage).
 */
import { test as base, expect, type APIRequestContext, type PlaywrightWorkerArgs } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { ENV } from '../config/env';

type PageFixtures = {
  loginPage: LoginPage;
};

export const test = base.extend<PageFixtures>({
  loginPage: async ({ page }, use) => {
    await use(new LoginPage(page));
  },
});

export { expect };

/**
 * Used in test.beforeAll(): creates an API client and checks the login page
 * answers with HTTP 200 before any browser test starts. If the site is down,
 * the suite fails immediately with a clear message instead of many timeouts.
 * The returned client is closed in test.afterAll().
 */
export async function openHealthCheckClient(playwright: PlaywrightWorkerArgs['playwright']): Promise<APIRequestContext> {
  const client = await playwright.request.newContext({ baseURL: ENV.baseUrl });
  const response = await client.get('/');
  expect(response.status(), `Application at ${ENV.baseUrl} is not reachable`).toBe(200);
  return client;
}
