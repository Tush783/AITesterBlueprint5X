/**
 * allureHelper.ts — small reusable helpers for the Allure report.
 *
 * Page objects wrap their actions in step() so each action appears as a
 * readable step in the report. Tests call label() to tag themselves with a
 * feature, story and severity.
 */
import * as allure from 'allure-js-commons';
import type { Page, TestInfo } from '@playwright/test';

// Severity levels re-exported so tests do not need to import allure-js-commons.
export const Severity = allure.Severity;

// Runs an action as a named Allure step and returns its result.
export async function step<T>(name: string, action: () => Promise<T>): Promise<T> {
  return allure.step(name, action);
}

// Adds feature, story and severity labels to the current test.
export async function label(feature: string, story: string, severity: string): Promise<void> {
  await allure.epic('Wingify');
  await allure.feature(feature);
  await allure.story(story);
  await allure.severity(severity);
}

// Adds a note to the report, for example to mark an inferred check.
export async function note(text: string): Promise<void> {
  await allure.description(text);
}

// Takes a full-page screenshot and attaches it to the report.
export async function attachScreenshot(page: Page, name: string): Promise<void> {
  const image = await page.screenshot({ fullPage: true });
  await allure.attachment(name, image, 'image/png');
}

// Called after each test: when it failed, attach a screenshot and the page URL.
export async function attachOnFailure(page: Page, testInfo: TestInfo): Promise<void> {
  if (testInfo.status !== testInfo.expectedStatus) {
    await attachScreenshot(page, `Failure - ${testInfo.title}`);
    await allure.attachment('Page URL', page.url(), 'text/plain');
  }
}
