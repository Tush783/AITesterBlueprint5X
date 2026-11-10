/**
 * playwright.config.ts — settings for the whole test run.
 *
 * Values that change between environments (URL, timeout, headless) come from
 * src/config/env.ts, which reads them from `.env` or environment variables.
 */
import { defineConfig, devices } from '@playwright/test';
import { ENV } from './src/config/env';

export default defineConfig({
  // Folder that holds the *.spec.ts files.
  testDir: './tests',

  // Maximum time for one test, and for each expect() check.
  timeout: 60_000,
  expect: { timeout: ENV.timeout },

  // Tests run in parallel, but with only 2 workers to keep the number of
  // login attempts per second low (the site checks IP address and location).
  fullyParallel: true,
  workers: 2,

  // Fail the run if someone leaves test.only in the code on CI.
  forbidOnly: !!process.env.CI,

  // No retries locally; one retry on CI to absorb network hiccups.
  retries: process.env.CI ? 1 : 0,

  // Console output, Playwright's HTML report, and Allure results.
  // detail: false keeps only the readable steps from the page objects in Allure
  // and leaves out Playwright's low-level steps such as Fill "<typed value>",
  // so passwords are never written to the Allure results.
  reporter: [
    ['list'],
    ['html', { open: 'never', outputFolder: 'playwright-report' }],
    ['allure-playwright', { resultsDir: 'allure-results', detail: false }],
  ],

  // Settings shared by every test.
  use: {
    baseURL: ENV.baseUrl,
    headless: ENV.headless,
    actionTimeout: ENV.timeout,
    navigationTimeout: ENV.timeout * 2,
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    trace: 'retain-on-failure',
  },

  // Browsers to run on. Add Firefox or WebKit here to run the same tests on them.
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
