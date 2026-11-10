/**
 * env.ts — one place for every setting the framework needs.
 *
 * Values are read from environment variables, or from a `.env` file in the
 * project folder (copy `.env.example` to `.env` to create one).
 * Every other file imports ENV from here instead of reading process.env directly,
 * so changing an environment (QA, staging, production) only means changing `.env`.
 */
import * as dotenv from 'dotenv';
import * as path from 'path';

// Load the .env file that sits next to package.json (if it exists).
dotenv.config({ path: path.resolve(__dirname, '../../.env'), quiet: true });

// Reads a variable and falls back to a default when it is missing or empty.
function read(name: string, fallback = ''): string {
  const value = process.env[name];
  return value && value.trim() !== '' ? value.trim() : fallback;
}

export const ENV = {
  // Address of the application; the login page path is added on top of it.
  baseUrl: read('BASE_URL', 'https://app.wingify.com'),

  // Hash route of the login page (Wingify is a single-page app, so it uses #/login).
  loginPath: read('LOGIN_PATH', '/#/login'),

  // Credentials for the successful-login test. Left empty on purpose:
  // when they are missing, that test is skipped instead of guessing.
  validEmail: read('WINGIFY_EMAIL'),
  validPassword: read('WINGIFY_PASSWORD'),

  // Maximum time (ms) to wait for an element or a page change.
  timeout: Number(read('TIMEOUT_MS', '20000')),

  // Run without a visible browser unless HEADLESS=false is set.
  headless: read('HEADLESS', 'true').toLowerCase() !== 'false',
};

// True only when both valid credentials have been provided.
export const hasValidCredentials = (): boolean => ENV.validEmail !== '' && ENV.validPassword !== '';
