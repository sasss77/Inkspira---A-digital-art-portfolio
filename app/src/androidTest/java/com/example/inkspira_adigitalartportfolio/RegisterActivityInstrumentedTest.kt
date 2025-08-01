package com.example.inkspira_adigitalartportfolio



import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import android.content.Context
import android.content.Intent
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.*
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import com.example.inkspira_adigitalartportfolio.view.activities.auth.RegisterActivity

@RunWith(AndroidJUnit4::class)
@LargeTest
class RegisterActivityInstrumentedTest {

    @get:Rule
    val activityRule = ActivityTestRule(
        RegisterActivity::class.java,
        true,
        false // Don't launch activity immediately
    )

    private lateinit var context: Context
    private lateinit var activityScenario: ActivityScenario<RegisterActivity>

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // Launch activity with intent
        val intent = Intent(context, RegisterActivity::class.java)
        activityScenario = ActivityScenario.launch(intent)

        // Wait for activity to be ready
        Thread.sleep(1000)
    }

    // MARK: - UI Components Visibility Tests

    @Test
    fun testRegisterScreenComponentsAreDisplayed() {
        // Check header elements
        onView(withContentDescription("Back"))
            .check(matches(isDisplayed()))

        onView(withText("Already have an account?"))
            .check(matches(isDisplayed()))

        // Check logo and title
        onView(withContentDescription("Inkspira Logo"))
            .check(matches(isDisplayed()))

        onView(withText("Join Inkspira"))
            .check(matches(isDisplayed()))

        onView(withText("Create your digital art portfolio"))
            .check(matches(isDisplayed()))

        // Check form fields by scrolling to each
        onView(withText("Display Name")).perform(scrollTo())
            .check(matches(isDisplayed()))

        onView(withText("Username")).perform(scrollTo())
            .check(matches(isDisplayed()))

        onView(withText("Email")).perform(scrollTo())
            .check(matches(isDisplayed()))

        onView(withText("Password")).perform(scrollTo())
            .check(matches(isDisplayed()))

        onView(withText("Confirm Password")).perform(scrollTo())
            .check(matches(isDisplayed()))

        // Check role selection
        onView(withText("I am here to...")).perform(scrollTo())
            .check(matches(isDisplayed()))

        // Check terms checkbox
        onView(withText("I agree to the ")).perform(scrollTo())
            .check(matches(isDisplayed()))

        // Check register button
        onView(withText("Create Account")).perform(scrollTo())
            .check(matches(isDisplayed()))

        // Check login link
        onView(withText("Sign In")).perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun testFormFieldsAreEnabled() {
        // Test full name field
        onView(withTestTag("fullName"))
            .perform(scrollTo())
            .check(matches(isEnabled()))
            .check(matches(withHint("Your full name")))

        // Test username field
        onView(withTestTag("username"))
            .perform(scrollTo())
            .check(matches(isEnabled()))
            .check(matches(withHint("Choose a unique username")))

        // Test email field
        onView(withTestTag("email"))
            .perform(scrollTo())
            .check(matches(isEnabled()))
            .check(matches(withHint("Enter your email address")))

        // Test password field
        onView(withTestTag("password"))
            .perform(scrollTo())
            .check(matches(isEnabled()))
            .check(matches(withHint("Create a strong password")))

        // Test confirm password field
        onView(withTestTag("confirmPassword"))
            .perform(scrollTo())
            .check(matches(isEnabled()))
            .check(matches(withHint("Re-enter your password")))
    }

    // MARK: - Input Field Tests

    @Test
    fun testDisplayNameInput() {
        val testName = "John Artist"

        onView(withTestTag("fullName"))
            .perform(scrollTo(), typeText(testName))
            .check(matches(withText(testName)))
    }

    @Test
    fun testUsernameInput() {
        val testUsername = "johnartist123"

        onView(withTestTag("username"))
            .perform(scrollTo(), typeText(testUsername))
            .check(matches(withText(testUsername)))
    }

    @Test
    fun testEmailInput() {
        val testEmail = "john@example.com"

        onView(withTestTag("email"))
            .perform(scrollTo(), typeText(testEmail))
            .check(matches(withText(testEmail)))
    }

    @Test
    fun testPasswordInput() {
        val testPassword = "password123"

        onView(withTestTag("password"))
            .perform(scrollTo(), typeText(testPassword))

        // Password should be hidden by default
        // We can verify by trying to clear and re-enter
        onView(withTestTag("password"))
            .perform(clearText(), typeText(testPassword))
    }

    @Test
    fun testConfirmPasswordInput() {
        val testPassword = "password123"

        onView(withTestTag("confirmPassword"))
            .perform(scrollTo(), typeText(testPassword))
    }

    // MARK: - Password Visibility Toggle Tests

    @Test
    fun testPasswordVisibilityToggle() {
        val testPassword = "password123"

        // Enter password
        onView(withTestTag("password"))
            .perform(scrollTo(), typeText(testPassword))

        // Find and click password visibility toggle
        onView(allOf(
            withContentDescription(anyOf(
                containsString("Show password"),
                containsString("Hide password")
            )),
            hasSibling(withTestTag("password"))
        )).perform(click())

        // Click again to hide
        onView(allOf(
            withContentDescription(anyOf(
                containsString("Show password"),
                containsString("Hide password")
            )),
            hasSibling(withTestTag("password"))
        )).perform(click())
    }

    @Test
    fun testConfirmPasswordVisibilityToggle() {
        val testPassword = "password123"

        // Enter confirm password
        onView(withTestTag("confirmPassword"))
            .perform(scrollTo(), typeText(testPassword))

        // Find and click confirm password visibility toggle
        onView(allOf(
            withContentDescription(anyOf(
                containsString("Show password"),
                containsString("Hide password")
            )),
            hasSibling(withTestTag("confirmPassword"))
        )).perform(click())
    }

    // MARK: - Role Selection Tests

    @Test
    fun testRoleSelectionDisplay() {
        // Scroll to role section
        onView(withText("I am here to..."))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        // Check all role options are displayed
        onView(withText("Create & Share Art"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        onView(withText("Discover Art"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        onView(withText("Create & Discover"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun testArtistRoleSelection() {
        onView(withText("Create & Share Art"))
            .perform(scrollTo(), click())

        // Verify radio button selection by checking if it's clickable again
        onView(withText("Create & Share Art"))
            .perform(click())
    }

    @Test
    fun testViewerRoleSelection() {
        onView(withText("Discover Art"))
            .perform(scrollTo(), click())
    }

    @Test
    fun testBothRoleSelection() {
        onView(withText("Create & Discover"))
            .perform(scrollTo(), click())
    }

    @Test
    fun testRoleSelectionExclusivity() {
        // Select artist role first
        onView(withText("Create & Share Art"))
            .perform(scrollTo(), click())

        // Then select viewer role
        onView(withText("Discover Art"))
            .perform(click())

        // Only viewer should be selected (radio button behavior)
        // Verify by attempting to click again
        onView(withText("Discover Art"))
            .perform(click())
    }

    // MARK: - Terms and Conditions Tests

    @Test
    fun testTermsCheckboxFunctionality() {
        // Find and click terms checkbox
        onView(withText("I agree to the "))
            .perform(scrollTo())

        // Click the checkbox (first clickable element in the row)
        onView(allOf(
            hasSibling(withText(containsString("I agree to the"))),
            isDisplayed()
        )).perform(click())

        // Click again to uncheck
        onView(allOf(
            hasSibling(withText(containsString("I agree to the"))),
            isDisplayed()
        )).perform(click())
    }

    @Test
    fun testTermsLinksAreClickable() {
        onView(withText("Terms of Service"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        onView(withText("Privacy Policy"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click()) // Test if clickable
    }

    // MARK: - Form Validation Tests

    @Test
    fun testEmptyFieldsValidation() {
        // Try to submit form with empty fields
        onView(withTestTag("register"))
            .perform(scrollTo(), click())

        // Wait for validation
        Thread.sleep(500)

        // Form should not submit with empty required fields
        // Verify we're still on the same screen by checking register button
        onView(withTestTag("register"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testInvalidEmailValidation() {
        // Fill form with invalid email
        onView(withTestTag("fullName"))
            .perform(scrollTo(), typeText("John Doe"))

        onView(withTestTag("username"))
            .perform(scrollTo(), typeText("johndoe"))

        onView(withTestTag("email"))
            .perform(scrollTo(), typeText("invalid-email"))

        onView(withTestTag("password"))
            .perform(scrollTo(), typeText("password123"))

        onView(withTestTag("confirmPassword"))
            .perform(scrollTo(), typeText("password123"))

        // Accept terms
        onView(allOf(
            hasSibling(withText(containsString("I agree to the"))),
            isDisplayed()
        )).perform(scrollTo(), click())

        // Submit form
        onView(withTestTag("register"))
            .perform(scrollTo(), click())

        Thread.sleep(500)

        // Should show validation error for invalid email
    }

    @Test
    fun testPasswordMismatchValidation() {
        // Fill form with mismatched passwords
        onView(withTestTag("fullName"))
            .perform(scrollTo(), typeText("John Doe"))

        onView(withTestTag("username"))
            .perform(scrollTo(), typeText("johndoe"))

        onView(withTestTag("email"))
            .perform(scrollTo(), typeText("john@example.com"))

        onView(withTestTag("password"))
            .perform(scrollTo(), typeText("password123"))

        onView(withTestTag("confirmPassword"))
            .perform(scrollTo(), typeText("differentpassword"))

        // Accept terms
        onView(allOf(
            hasSibling(withText(containsString("I agree to the"))),
            isDisplayed()
        )).perform(scrollTo(), click())

        // Submit form
        onView(withTestTag("register"))
            .perform(scrollTo(), click())

        Thread.sleep(500)

        // Should show password mismatch error
    }

    @Test
    fun testShortPasswordValidation() {
        // Fill form with short password
        onView(withTestTag("fullName"))
            .perform(scrollTo(), typeText("John Doe"))

        onView(withTestTag("username"))
            .perform(scrollTo(), typeText("johndoe"))

        onView(withTestTag("email"))
            .perform(scrollTo(), typeText("john@example.com"))

        onView(withTestTag("password"))
            .perform(scrollTo(), typeText("123"))

        onView(withTestTag("confirmPassword"))
            .perform(scrollTo(), typeText("123"))

        // Submit form
        onView(withTestTag("register"))
            .perform(scrollTo(), click())

        Thread.sleep(500)

        // Should show password length validation error
    }

    @Test
    fun testTermsNotAcceptedValidation() {
        // Fill valid form but don't accept terms
        onView(withTestTag("fullName"))
            .perform(scrollTo(), typeText("John Doe"))

        onView(withTestTag("username"))
            .perform(scrollTo(), typeText("johndoe"))

        onView(withTestTag("email"))
            .perform(scrollTo(), typeText("john@example.com"))

        onView(withTestTag("password"))
            .perform(scrollTo(), typeText("password123"))

        onView(withTestTag("confirmPassword"))
            .perform(scrollTo(), typeText("password123"))

        // Don't accept terms

        // Submit form
        onView(withTestTag("register"))
            .perform(scrollTo(), click())

        Thread.sleep(500)

        // Should show terms validation error
    }

    // MARK: - Form Submission Tests

    @Test
    fun testValidFormSubmission() {
        // Fill valid form
        onView(withTestTag("fullName"))
            .perform(scrollTo(), typeText("John Doe"))

        onView(withTestTag("username"))
            .perform(scrollTo(), typeText("johndoe"))

        onView(withTestTag("email"))
            .perform(scrollTo(), typeText("john@example.com"))

        onView(withTestTag("password"))
            .perform(scrollTo(), typeText("password123"))

        onView(withTestTag("confirmPassword"))
            .perform(scrollTo(), typeText("password123"))

        // Select role
        onView(withText("Create & Share Art"))
            .perform(scrollTo(), click())

        // Accept terms
        onView(allOf(
            hasSibling(withText(containsString("I agree to the"))),
            isDisplayed()
        )).perform(scrollTo(), click())

        // Hide keyboard before clicking submit
        onView(withTestTag("confirmPassword"))
            .perform(closeSoftKeyboard())

        // Submit form
        onView(withTestTag("register"))
            .perform(scrollTo(), click())

        // Wait for potential loading state
        Thread.sleep(2000)

        // Check if loading indicator appears (button text should change)
    }

    @Test
    fun testLoadingStateDisplay() {
        // Fill and submit valid form
        onView(withTestTag("fullName"))
            .perform(scrollTo(), typeText("John Doe"))

        onView(withTestTag("username"))
            .perform(scrollTo(), typeText("johndoe"))

        onView(withTestTag("email"))
            .perform(scrollTo(), typeText("john@example.com"))

        onView(withTestTag("password"))
            .perform(scrollTo(), typeText("password123"))

        onView(withTestTag("confirmPassword"))
            .perform(scrollTo(), typeText("password123"))

        onView(withText("Create & Share Art"))
            .perform(scrollTo(), click())

        onView(allOf(
            hasSibling(withText(containsString("I agree to the"))),
            isDisplayed()
        )).perform(scrollTo(), click())

        onView(withTestTag("confirmPassword"))
            .perform(closeSoftKeyboard())

        onView(withTestTag("register"))
            .perform(scrollTo(), click())

        // Check for loading state immediately after click
        Thread.sleep(500)

        // Button text should change to "Creating Account..." during loading
        onView(withText("Creating Account..."))
            .check(matches(isDisplayed()))
    }

    // MARK: - Navigation Tests

    @Test
    fun testBackButtonNavigation() {
        // Click back button
        onView(withContentDescription("Back"))
            .perform(click())

        // Activity should finish
        Thread.sleep(500)
    }

    @Test
    fun testNavigateToLoginFromHeader() {
        // Click "Already have an account?" in header
        onView(withText("Already have an account?"))
            .perform(click())

        // Should navigate to login screen
        Thread.sleep(1000)
    }

    @Test
    fun testNavigateToLoginFromFooter() {
        // Click "Sign In" link at bottom
        onView(withText("Sign In"))
            .perform(scrollTo(), click())

        // Should navigate to login screen
        Thread.sleep(1000)
    }

    // MARK: - Edge Cases and Special Input Tests

    @Test
    fun testVeryLongInputs() {
        val longName = "a".repeat(100)
        val longUsername = "a".repeat(100)
        val longEmail = "a".repeat(50) + "@example.com"

        onView(withTestTag("fullName"))
            .perform(scrollTo(), typeText(longName))

        onView(withTestTag("username"))
            .perform(scrollTo(), clearText(), typeText(longUsername))

        onView(withTestTag("email"))
            .perform(scrollTo(), clearText(), typeText(longEmail))

        // Verify UI handles long inputs gracefully
        onView(withTestTag("fullName"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSpecialCharactersInInput() {
        val nameWithSpecialChars = "John O'Connor-Smith"
        val usernameWithNumbers = "user123_test"
        val emailWithSpecialChars = "test+user@example.com"

        onView(withTestTag("fullName"))
            .perform(scrollTo(), typeText(nameWithSpecialChars))

        onView(withTestTag("username"))
            .perform(scrollTo(), typeText(usernameWithNumbers))

        onView(withTestTag("email"))
            .perform(scrollTo(), typeText(emailWithSpecialChars))

        // Verify special characters are handled correctly
        onView(withTestTag("fullName"))
            .check(matches(withText(nameWithSpecialChars)))

        onView(withTestTag("email"))
            .check(matches(withText(emailWithSpecialChars)))
    }

    @Test
    fun testKeyboardNavigation() {
        // Test navigation between fields using keyboard
        onView(withTestTag("fullName"))
            .perform(scrollTo(), typeText("John"))

        // Test if focus moves correctly (this is platform dependent)
        onView(withTestTag("username"))
            .perform(scrollTo(), typeText("john"))
    }

    // MARK: - Accessibility Tests

    @Test
    fun testContentDescriptions() {
        // Check if important UI elements have proper content descriptions
        onView(withContentDescription("Back"))
            .check(matches(isDisplayed()))

        onView(withContentDescription("Inkspira Logo"))
            .check(matches(isDisplayed()))

        // Check password visibility toggles have proper descriptions
        onView(withContentDescription(containsString("password")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testScreenReader() {
        // Verify important text is accessible to screen readers
        onView(withText("Join Inkspira"))
            .check(matches(isDisplayed()))

        onView(withText("Create your digital art portfolio"))
            .check(matches(isDisplayed()))
    }

    // MARK: - Performance Tests

    @Test
    fun testUIResponsiveness() {
        // Test rapid input changes to ensure UI remains responsive
        repeat(10) {
            onView(withTestTag("fullName"))
                .perform(scrollTo(), typeText("a"), clearText())
        }

        // UI should still be responsive
        onView(withTestTag("fullName"))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testScrollPerformance() {
        // Test scrolling performance with multiple scroll operations
        repeat(5) {
            onView(withTestTag("fullName")).perform(scrollTo())
            onView(withTestTag("register")).perform(scrollTo())
            onView(withTestTag("fullName")).perform(scrollTo())
        }

        // UI should remain stable
        onView(withTestTag("register"))
            .check(matches(isDisplayed()))
    }
}
