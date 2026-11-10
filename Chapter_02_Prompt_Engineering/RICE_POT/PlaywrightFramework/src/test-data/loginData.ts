/**
 * loginData.ts — all test data in one place.
 *
 * Tests never hard-code text or input values; they import them from here.
 * Every expected value below was copied from the live Wingify login page,
 * not guessed. If the application text changes, update it here only.
 */

// Text and attributes shown on the login page.
export const LOGIN_PAGE_TEXT = {
  title: 'Login - Wingify',
  urlFragment: '#/login',
  emailPlaceholder: 'Enter email ID',
  passwordPlaceholder: 'Enter password',
  signInButton: 'Sign in',
  rememberMeLabel: 'Remember me',
};

// Message Wingify shows for every failed login attempt (wrong or empty values).
export const LOGIN_ERROR_MESSAGE = 'Your email, password, IP address or location did not match';

// Shape of one invalid-login test case.
export interface InvalidLoginCase {
  title: string; // name shown in the test report
  email: string; // value typed into the email box ('' = leave empty)
  password: string; // value typed into the password box ('' = leave empty)
}

// Data-driven invalid cases. InvalidLogin.spec.ts creates one test per row.
// Only example.com addresses are used, so no real account can be locked out.
export const INVALID_LOGIN_CASES: InvalidLoginCase[] = [
  { title: 'unregistered email and wrong password', email: 'invalid.user@example.com', password: 'WrongPass@123' },
  { title: 'empty email and empty password', email: '', password: '' },
  { title: 'valid email format with empty password', email: 'invalid.user@example.com', password: '' },
  { title: 'empty email with a password', email: '', password: 'WrongPass@123' },
  { title: 'email without @ and domain', email: 'invaliduser', password: 'WrongPass@123' },
];

// Sample values used by the field-behaviour tests (they are never submitted).
export const SAMPLE_INPUT = {
  email: 'qa.automation@example.com',
  password: 'Sample@12345',
};
