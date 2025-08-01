package com.example.inkspira_adigitalartportfolio




import com.example.inkspira_adigitalartportfolio.model.data.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import com.example.inkspira_adigitalartportfolio.controller.remote.FirebaseRealtimeService
import com.example.inkspira_adigitalartportfolio.model.data.UserRole

import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

@RunWith(MockitoJUnitRunner::class)
class AuthRepositoryImplTest {

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockDbService: FirebaseRealtimeService

    @Mock
    private lateinit var mockAuthResult: AuthResult

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    @Mock
    private lateinit var mockLoginTask: Task<AuthResult>

    @Mock
    private lateinit var mockRegisterTask: Task<AuthResult>

    @Mock
    private lateinit var mockPasswordResetTask: Task<Void>

    private lateinit var authRepository: AuthRepositoryImpl

    private val testUserId = "test123"
    private val testEmail = "test@example.com"
    private val testPassword = "password123"
    private val testDisplayName = "Test User"

    private val testUser = UserModel(
        userId = testUserId,
        email = testEmail,
        displayName = testDisplayName,
        role = UserRole.USER,
        profileImageUrl = "",
        createdAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        authRepository = AuthRepositoryImpl(mockFirebaseAuth, mockDbService)
    }

    // MARK: - loginUser Tests

    @Test
    fun `loginUser with valid credentials returns success`() = runTest {
        // Given
        whenever(mockFirebaseAuth.signInWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockLoginTask)
        whenever(mockLoginTask.await()).thenReturn(mockAuthResult)
        whenever(mockAuthResult.user).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockDbService.getData("users", testUserId, UserModel::class.java))
            .thenReturn(NetworkResult.Success(testUser))

        // When
        val result = authRepository.loginUser(testEmail, testPassword)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(testUser, result.data)
        verify(mockFirebaseAuth).signInWithEmailAndPassword(testEmail, testPassword)
        verify(mockDbService).getData("users", testUserId, UserModel::class.java)
    }

    @Test
    fun `loginUser with invalid credentials returns error`() = runTest {
        // Given
        val exception = Exception("Invalid credentials")
        whenever(mockFirebaseAuth.signInWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockLoginTask)
        whenever(mockLoginTask.await()).thenThrow(exception)

        // When
        val result = authRepository.loginUser(testEmail, testPassword)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Login failed: Invalid credentials", result.message)
    }

    @Test
    fun `loginUser with null user returns error`() = runTest {
        // Given
        whenever(mockFirebaseAuth.signInWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockLoginTask)
        whenever(mockLoginTask.await()).thenReturn(mockAuthResult)
        whenever(mockAuthResult.user).thenReturn(null)

        // When
        val result = authRepository.loginUser(testEmail, testPassword)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Authentication failed - invalid credentials", result.message)
    }

    @Test
    fun `loginUser with user profile not found returns error`() = runTest {
        // Given
        whenever(mockFirebaseAuth.signInWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockLoginTask)
        whenever(mockLoginTask.await()).thenReturn(mockAuthResult)
        whenever(mockAuthResult.user).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockDbService.getData("users", testUserId, UserModel::class.java))
            .thenReturn(NetworkResult.Success(null))

        // When
        val result = authRepository.loginUser(testEmail, testPassword)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("User profile not found", result.message)
    }

    @Test
    fun `loginUser with database error returns error`() = runTest {
        // Given
        val dbError = "Database connection failed"
        whenever(mockFirebaseAuth.signInWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockLoginTask)
        whenever(mockLoginTask.await()).thenReturn(mockAuthResult)
        whenever(mockAuthResult.user).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockDbService.getData("users", testUserId, UserModel::class.java))
            .thenReturn(NetworkResult.Error(dbError))

        // When
        val result = authRepository.loginUser(testEmail, testPassword)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Failed to load user profile: $dbError", result.message)
    }

    @Test
    fun `loginUser with database loading returns loading`() = runTest {
        // Given
        whenever(mockFirebaseAuth.signInWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockLoginTask)
        whenever(mockLoginTask.await()).thenReturn(mockAuthResult)
        whenever(mockAuthResult.user).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockDbService.getData("users", testUserId, UserModel::class.java))
            .thenReturn(NetworkResult.Loading())

        // When
        val result = authRepository.loginUser(testEmail, testPassword)

        // Then
        assertTrue(result is NetworkResult.Loading)
    }

    // MARK: - registerUser Tests

    @Test
    fun `registerUser with valid data returns success`() = runTest {
        // Given
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockRegisterTask)
        whenever(mockRegisterTask.await()).thenReturn(mockAuthResult)
        whenever(mockAuthResult.user).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockDbService.saveData(eq("users"), eq(testUserId), any<Map<String, Any>>()))
            .thenReturn(NetworkResult.Success(Unit))

        // When
        val result = authRepository.registerUser(testEmail, testPassword, testDisplayName, "USER")

        // Then
        assertTrue(result is NetworkResult.Success)
        val userData = result.data as UserModel
        assertEquals(testUserId, userData.userId)
        assertEquals(testEmail, userData.email)
        assertEquals(testDisplayName, userData.displayName)
        assertEquals(UserRole.USER, userData.role)
        verify(mockFirebaseAuth).createUserWithEmailAndPassword(testEmail, testPassword)
        verify(mockDbService).saveData(eq("users"), eq(testUserId), any<Map<String, Any>>())
    }

    @Test
    fun `registerUser with invalid role returns error`() = runTest {
        // Given
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockRegisterTask)
        whenever(mockRegisterTask.await()).thenReturn(mockAuthResult)
        whenever(mockAuthResult.user).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)

        // When
        val result = authRepository.registerUser(testEmail, testPassword, testDisplayName, "INVALID_ROLE")

        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue(result.message.contains("Registration failed"))
    }

    @Test
    fun `registerUser with null user returns error`() = runTest {
        // Given
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockRegisterTask)
        whenever(mockRegisterTask.await()).thenReturn(mockAuthResult)
        whenever(mockAuthResult.user).thenReturn(null)

        // When
        val result = authRepository.registerUser(testEmail, testPassword, testDisplayName, "USER")

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Failed to create user account", result.message)
    }

    @Test
    fun `registerUser with database save failure returns error and deletes user`() = runTest {
        // Given
        val dbError = "Database save failed"
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockRegisterTask)
        whenever(mockRegisterTask.await()).thenReturn(mockAuthResult)
        whenever(mockAuthResult.user).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockDbService.saveData(eq("users"), eq(testUserId), any<Map<String, Any>>()))
            .thenReturn(NetworkResult.Error(dbError))

        // When
        val result = authRepository.registerUser(testEmail, testPassword, testDisplayName, "USER")

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Failed to create user profile: $dbError", result.message)
        verify(mockFirebaseUser).delete()
    }

    @Test
    fun `registerUser with firebase auth failure returns error`() = runTest {
        // Given
        val exception = Exception("Email already in use")
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockRegisterTask)
        whenever(mockRegisterTask.await()).thenThrow(exception)

        // When
        val result = authRepository.registerUser(testEmail, testPassword, testDisplayName, "USER")

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Registration failed: Email already in use", result.message)
    }

    @Test
    fun `registerUser with database loading returns loading`() = runTest {
        // Given
        whenever(mockFirebaseAuth.createUserWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockRegisterTask)
        whenever(mockRegisterTask.await()).thenReturn(mockAuthResult)
        whenever(mockAuthResult.user).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockDbService.saveData(eq("users"), eq(testUserId), any<Map<String, Any>>()))
            .thenReturn(NetworkResult.Loading())

        // When
        val result = authRepository.registerUser(testEmail, testPassword, testDisplayName, "USER")

        // Then
        assertTrue(result is NetworkResult.Loading)
    }

    // MARK: - logoutUser Tests

    @Test
    fun `logoutUser succeeds returns success`() = runTest {
        // When
        val result = authRepository.logoutUser()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(true, result.data)
        verify(mockFirebaseAuth).signOut()
    }

    @Test
    fun `logoutUser with exception returns error`() = runTest {
        // Given
        val exception = Exception("Logout failed")
        doThrow(exception).whenever(mockFirebaseAuth).signOut()

        // When
        val result = authRepository.logoutUser()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Logout failed: Logout failed", result.message)
    }

    // MARK: - isUserLoggedIn Tests

    @Test
    fun `isUserLoggedIn with current user returns true`() {
        // Given
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

        // When
        val result = authRepository.isUserLoggedIn()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isUserLoggedIn with null current user returns false`() {
        // Given
        whenever(mockFirebaseAuth.currentUser).thenReturn(null)

        // When
        val result = authRepository.isUserLoggedIn()

        // Then
        assertFalse(result)
    }

    // MARK: - getCurrentUserId Tests

    @Test
    fun `getCurrentUserId with current user returns user id`() {
        // Given
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)

        // When
        val result = authRepository.getCurrentUserId()

        // Then
        assertEquals(testUserId, result)
    }

    @Test
    fun `getCurrentUserId with null current user returns null`() {
        // Given
        whenever(mockFirebaseAuth.currentUser).thenReturn(null)

        // When
        val result = authRepository.getCurrentUserId()

        // Then
        assertNull(result)
    }

    // MARK: - sendPasswordResetEmail Tests

    @Test
    fun `sendPasswordResetEmail with valid email returns success`() = runTest {
        // Given
        whenever(mockFirebaseAuth.sendPasswordResetEmail(testEmail))
            .thenReturn(mockPasswordResetTask)
        whenever(mockPasswordResetTask.await()).thenReturn(null)

        // When
        val result = authRepository.sendPasswordResetEmail(testEmail)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(true, result.data)
        verify(mockFirebaseAuth).sendPasswordResetEmail(testEmail)
    }

    @Test
    fun `sendPasswordResetEmail with invalid email returns error`() = runTest {
        // Given
        val exception = Exception("Invalid email address")
        whenever(mockFirebaseAuth.sendPasswordResetEmail(testEmail))
            .thenReturn(mockPasswordResetTask)
        whenever(mockPasswordResetTask.await()).thenThrow(exception)

        // When
        val result = authRepository.sendPasswordResetEmail(testEmail)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Password reset failed: Invalid email address", result.message)
    }

    @Test
    fun `sendPasswordResetEmail with network error returns error`() = runTest {
        // Given
        val exception = Exception("Network error")
        whenever(mockFirebaseAuth.sendPasswordResetEmail(testEmail))
            .thenReturn(mockPasswordResetTask)
        whenever(mockPasswordResetTask.await()).thenThrow(exception)

        // When
        val result = authRepository.sendPasswordResetEmail(testEmail)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Password reset failed: Network error", result.message)
    }
}
