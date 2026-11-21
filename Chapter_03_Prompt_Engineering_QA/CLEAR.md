# CLEAR

## CONTEXT
I am a QA engineer preparing to test the login at [app.wingify.com](https://app.wingify.com/#/login) for this exercise. Assume the approved requirements are:

- Registerd Users can log in with valid credentials
- Invalid Credentials must not grant access and give error. 
- Email And passwords are mandatory.
- Successful login opens the authenticated dashboard.
- Invalid login gives error message "Your email, password, IP address or location did not match"

## LIMITS
- Write exactly 10 test cases. 
- Use only the requirements supplied above. 
- Use placeholders for credentials. Never include a real password. 
- Do not invent exact validation messages or password policy. 
- Set execution status to "not run" 
- If an expected result depends on missing requirements. Mark it "requires clarification."

## EXAMPLES
User is filled example as the format:

Test Case ID: VWO-LOGIN-TC-001
Summary: Verify login with valid registered credentials
Preconditions: An active registered test account is available; the use is logged out
Test Data: <Resitered_email>, <Valid_Password>
Steps:
1. Open https://app.vwo.com/.
2. Enter the registered email.
3. Enter the valid password.
4. Submit the login form.

Expected Result: Login suceeds and the authenticated dashboard opens.
Priority: High
Test Type: Positive
Stauts: Not Run 

## ACTION
Write 10 distinct test cases covering valid login, incorrect password, unregistered email, empty fields, and access control before and after login. Write the numbered steps and observable expected results. 

## REVIEW
- Every test case traces to supply requirements. 
- Test cases are distinct. 
- Expected results describe observable behavior. 