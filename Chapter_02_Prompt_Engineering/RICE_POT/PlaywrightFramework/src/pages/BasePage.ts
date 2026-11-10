/**
 * BasePage.ts — parent class for every page object.
 *
 * It holds the actions every page needs (open a URL, wait for an element,
 * type, click, read text). Page objects such as LoginPage extend it, so a new
 * page (for example a DashboardPage) gets all of these for free.
 *
 * Synchronisation rule: every helper waits with locator.waitFor() or expect(),
 * never with fixed delays such as waitForTimeout().
 */
import { expect, type Locator, type Page } from '@playwright/test';
import { ENV } from '../config/env';

export abstract class BasePage {
  readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  // Creates a locator from an XPath. All page objects use this, so XPath is the only strategy.
  protected xpath(expression: string): Locator {
    return this.page.locator(`xpath=${expression}`);
  }

  // Opens a path relative to BASE_URL and waits until the page has loaded.
  protected async navigate(path: string): Promise<void> {
    await this.page.goto(`${ENV.baseUrl}${path}`, { waitUntil: 'domcontentloaded' });
  }

  // Waits until an element is visible.
  protected async waitForVisible(element: Locator): Promise<void> {
    await element.waitFor({ state: 'visible', timeout: ENV.timeout });
  }

  // Clears a text box and types a value. An empty value leaves the box empty.
  protected async type(element: Locator, value: string): Promise<void> {
    await this.waitForVisible(element);
    await element.fill(value);
    await expect(element).toHaveValue(value);
  }

  // Waits until an element can be clicked, then clicks it.
  protected async click(element: Locator): Promise<void> {
    await this.waitForVisible(element);
    await expect(element).toBeEnabled({ timeout: ENV.timeout });
    await element.click();
  }

  // Returns the visible text of an element, without extra spaces.
  protected async textOf(element: Locator): Promise<string> {
    await this.waitForVisible(element);
    return (await element.innerText()).trim();
  }

  // Returns the browser tab title.
  async title(): Promise<string> {
    return this.page.title();
  }

  // Returns the current address.
  currentUrl(): string {
    return this.page.url();
  }
}
