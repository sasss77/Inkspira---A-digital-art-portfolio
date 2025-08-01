package com.example.inkspira_adigitalartportfolio



import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.android.gms.tasks.Task
import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.repository.ArtworkRepositoryImpl
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import com.example.inkspira_adigitalartportfolio.view.screens.ArtworkData
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class ArtworkRepositoryImplTest {

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirebaseDatabase: FirebaseDatabase

    @Mock
    private lateinit var mockArtworksRef: DatabaseReference

    @Mock
    private lateinit var mockUsersRef: DatabaseReference

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    @Mock
    private lateinit var mockDataSnapshot: DataSnapshot

    @Mock
    private lateinit var mockTask: Task<DataSnapshot>

    @Mock
    private lateinit var mockVoidTask: Task<Void>

    @Mock
    private lateinit var mockQuery: Query

    private lateinit var artworkRepository: ArtworkRepositoryImpl

    private val testUserId = "test_user_123"
    private val testArtworkId = "artwork_123"

    private val testArtworkModel = ArtworkModel(
        id = testArtworkId,
        title = "Test Artwork",
        description = "Test Description",
        imageUrl = "https://test.com/image.jpg",
        thumbnailUrl = "https://test.com/thumb.jpg",
        artistId = testUserId,
        uploadedAt = System.currentTimeMillis(),
        isPublic = true,
        likesCount = 10,
        viewsCount = 50,
//        category = "Digital Art"
    )

    private val testArtworkData = ArtworkData(
        id = testArtworkId,
        title = "Test Artwork",
        description = "Test Description",
        imageUrl = "https://test.com/image.jpg",
        isPublic = true,
        likesCount = 10,
        createdAt = testArtworkModel.uploadedAt,
        userId = testUserId
    )

    @Before
    fun setup() {
        // Mock static Firebase instances
        whenever(FirebaseAuth.getInstance()).thenReturn(mockFirebaseAuth)
        whenever(FirebaseDatabase.getInstance()).thenReturn(mockFirebaseDatabase)
        whenever(mockFirebaseDatabase.getReference("artworks")).thenReturn(mockArtworksRef)
        whenever(mockFirebaseDatabase.getReference("users")).thenReturn(mockUsersRef)

        artworkRepository = ArtworkRepositoryImpl()
    }

    // MARK: - getUserArtworks Tests

    @Test
    fun `getUserArtworks with authenticated user returns success`() = runTest {
        // Given
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockArtworksRef.orderByChild("artistId")).thenReturn(mockQuery)
        whenever(mockQuery.equalTo(testUserId)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)

        val mockChildSnapshot = mock<DataSnapshot>()
        whenever(mockDataSnapshot.children).thenReturn(listOf(mockChildSnapshot))
        whenever(mockChildSnapshot.getValue(ArtworkModel::class.java)).thenReturn(testArtworkModel)

        // When
        val result = artworkRepository.getUserArtworks()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data?.size)
        verify(mockArtworksRef).orderByChild("artistId")
    }

    @Test
    fun `getUserArtworks with null user returns error`() = runTest {
        // Given
        whenever(mockFirebaseAuth.currentUser).thenReturn(null)

        // When
        val result = artworkRepository.getUserArtworks()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("User not authenticated", result.message)
    }

    @Test
    fun `getUserArtworks with database exception returns error`() = runTest {
        // Given
        val exception = Exception("Database error")
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockArtworksRef.orderByChild("artistId")).thenReturn(mockQuery)
        whenever(mockQuery.equalTo(testUserId)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenThrow(exception)

        // When
        val result = artworkRepository.getUserArtworks()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Failed to load user artworks: Database error", result.message)
    }

    // MARK: - getPublicArtworks Tests

    @Test
    fun `getPublicArtworks with valid limit returns success`() = runTest {
        // Given
        val limit = 10
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockArtworksRef.orderByChild("isPublic")).thenReturn(mockQuery)
        whenever(mockQuery.equalTo(true)).thenReturn(mockQuery)
        whenever(mockQuery.limitToLast(limit)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)

        val otherUserArtwork = testArtworkModel.copy(artistId = "other_user")
        val mockChildSnapshot = mock<DataSnapshot>()
        whenever(mockDataSnapshot.children).thenReturn(listOf(mockChildSnapshot))
        whenever(mockChildSnapshot.getValue(ArtworkModel::class.java)).thenReturn(otherUserArtwork)

        // When
        val result = artworkRepository.getPublicArtworks(limit)

        // Then
        assertTrue(result is NetworkResult.Success)
        verify(mockQuery).limitToLast(limit)
    }

    @Test
    fun `getPublicArtworks excludes current user artworks`() = runTest {
        // Given
        val limit = 10
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockArtworksRef.orderByChild("isPublic")).thenReturn(mockQuery)
        whenever(mockQuery.equalTo(true)).thenReturn(mockQuery)
        whenever(mockQuery.limitToLast(limit)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)

        val mockChildSnapshot = mock<DataSnapshot>()
        whenever(mockDataSnapshot.children).thenReturn(listOf(mockChildSnapshot))
        whenever(mockChildSnapshot.getValue(ArtworkModel::class.java)).thenReturn(testArtworkModel)

        // When
        val result = artworkRepository.getPublicArtworks(limit)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(0, result.data?.size) // Current user's artwork should be excluded
    }

    // MARK: - searchArtworks Tests

    @Test
    fun `searchArtworks with valid query returns matching artworks`() = runTest {
        // Given
        val query = "test artwork"
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockArtworksRef.orderByChild("isPublic")).thenReturn(mockQuery)
        whenever(mockQuery.equalTo(true)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)

        val otherUserArtwork = testArtworkModel.copy(artistId = "other_user", title = "Test Artwork Match")
        val mockChildSnapshot = mock<DataSnapshot>()
        whenever(mockDataSnapshot.children).thenReturn(listOf(mockChildSnapshot))
        whenever(mockChildSnapshot.getValue(ArtworkModel::class.java)).thenReturn(otherUserArtwork)

        // When
        val result = artworkRepository.searchArtworks(query)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data?.size)
    }

    @Test
    fun `searchArtworks with blank query returns empty list`() = runTest {
        // When
        val result = artworkRepository.searchArtworks("")

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(0, result.data?.size)
    }

    @Test
    fun `searchArtworks with short terms returns empty list`() = runTest {
        // When
        val result = artworkRepository.searchArtworks("a b")

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(0, result.data?.size)
    }

    // MARK: - saveArtwork Tests

    @Test
    fun `saveArtwork with authenticated user returns success`() = runTest {
        // Given
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockArtworksRef.child(testArtworkId)).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.setValue(any())).thenReturn(mockVoidTask)
        whenever(mockVoidTask.await()).thenReturn(null)
        whenever(mockUsersRef.child(testUserId)).thenReturn(mockUsersRef)
        whenever(mockUsersRef.child("artworkCount")).thenReturn(mockUsersRef)
        whenever(mockUsersRef.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)
        whenever(mockDataSnapshot.value).thenReturn(5L)
        whenever(mockUsersRef.setValue(any())).thenReturn(mockVoidTask)

        // When
        val result = artworkRepository.saveArtwork(testArtworkModel)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(true, result.data)
        verify(mockArtworksRef).setValue(any())
    }

    @Test
    fun `saveArtwork with null user returns error`() = runTest {
        // Given
        whenever(mockFirebaseAuth.currentUser).thenReturn(null)

        // When
        val result = artworkRepository.saveArtwork(testArtworkModel)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("User not authenticated", result.message)
    }

    // MARK: - updateArtwork Tests

    @Test
    fun `updateArtwork with valid ownership returns success`() = runTest {
        // Given
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockArtworksRef.child(testArtworkId)).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)
        whenever(mockDataSnapshot.getValue(ArtworkModel::class.java)).thenReturn(testArtworkModel)
        whenever(mockArtworksRef.setValue(any())).thenReturn(mockVoidTask)
        whenever(mockVoidTask.await()).thenReturn(null)

        // When
        val result = artworkRepository.updateArtwork(testArtworkModel)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(true, result.data)
    }

    @Test
    fun `updateArtwork with unauthorized user returns error`() = runTest {
        // Given
        val otherUserId = "other_user"
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(otherUserId)
        whenever(mockArtworksRef.child(testArtworkId)).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)
        whenever(mockDataSnapshot.getValue(ArtworkModel::class.java)).thenReturn(testArtworkModel)

        // When
        val result = artworkRepository.updateArtwork(testArtworkModel)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Unauthorized: Cannot update artwork", result.message)
    }

    // MARK: - deleteArtwork Tests

    @Test
    fun `deleteArtwork with valid ownership returns success`() = runTest {
        // Given
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockArtworksRef.child(testArtworkId)).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)
        whenever(mockDataSnapshot.getValue(ArtworkModel::class.java)).thenReturn(testArtworkModel)
        whenever(mockArtworksRef.removeValue()).thenReturn(mockVoidTask)
        whenever(mockVoidTask.await()).thenReturn(null)
        whenever(mockUsersRef.child(testUserId)).thenReturn(mockUsersRef)
        whenever(mockUsersRef.child("artworkCount")).thenReturn(mockUsersRef)
        whenever(mockUsersRef.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)
        whenever(mockDataSnapshot.value).thenReturn(5L)
        whenever(mockUsersRef.setValue(any())).thenReturn(mockVoidTask)

        // When
        val result = artworkRepository.deleteArtwork(testArtworkId)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(true, result.data)
        verify(mockArtworksRef).removeValue()
    }

    @Test
    fun `deleteArtwork with unauthorized user returns error`() = runTest {
        // Given
        val otherUserId = "other_user"
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(otherUserId)
        whenever(mockArtworksRef.child(testArtworkId)).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)
        whenever(mockDataSnapshot.getValue(ArtworkModel::class.java)).thenReturn(testArtworkModel)

        // When
        val result = artworkRepository.deleteArtwork(testArtworkId)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Unauthorized: Cannot delete artwork", result.message)
        verify(mockArtworksRef, never()).removeValue()
    }

    // MARK: - updateArtworkLikes Tests

    @Test
    fun `updateArtworkLikes with valid count returns success`() = runTest {
        // Given
        val newLikesCount = 15
        whenever(mockArtworksRef.child(testArtworkId)).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.child("likesCount")).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.setValue(newLikesCount)).thenReturn(mockVoidTask)
        whenever(mockVoidTask.await()).thenReturn(null)

        // When
        val result = artworkRepository.updateArtworkLikes(testArtworkId, newLikesCount)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(true, result.data)
        verify(mockArtworksRef).setValue(newLikesCount)
    }

    @Test
    fun `updateArtworkLikes with negative count sets to zero`() = runTest {
        // Given
        val negativeLikesCount = -5
        whenever(mockArtworksRef.child(testArtworkId)).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.child("likesCount")).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.setValue(0)).thenReturn(mockVoidTask)
        whenever(mockVoidTask.await()).thenReturn(null)

        // When
        val result = artworkRepository.updateArtworkLikes(testArtworkId, negativeLikesCount)

        // Then
        assertTrue(result is NetworkResult.Success)
        verify(mockArtworksRef).setValue(0)
    }

    // MARK: - incrementViewCount Tests

    @Test
    fun `incrementViewCount increases count by one`() = runTest {
        // Given
        val currentViews = 10
        whenever(mockArtworksRef.child(testArtworkId)).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.child("viewsCount")).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)
        whenever(mockDataSnapshot.value).thenReturn(currentViews.toLong())
        whenever(mockArtworksRef.setValue(currentViews + 1)).thenReturn(mockVoidTask)
        whenever(mockVoidTask.await()).thenReturn(null)

        // When
        val result = artworkRepository.incrementViewCount(testArtworkId)

        // Then
        assertTrue(result is NetworkResult.Success)
        verify(mockArtworksRef).setValue(currentViews + 1)
    }

    @Test
    fun `incrementViewCount with null current views starts from zero`() = runTest {
        // Given
        whenever(mockArtworksRef.child(testArtworkId)).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.child("viewsCount")).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)
        whenever(mockDataSnapshot.value).thenReturn(null)
        whenever(mockArtworksRef.setValue(1)).thenReturn(mockVoidTask)
        whenever(mockVoidTask.await()).thenReturn(null)

        // When
        val result = artworkRepository.incrementViewCount(testArtworkId)

        // Then
        assertTrue(result is NetworkResult.Success)
        verify(mockArtworksRef).setValue(1)
    }

    // MARK: - getArtworkById Tests

    @Test
    fun `getArtworkById with existing artwork returns success`() = runTest {
        // Given
        whenever(mockArtworksRef.child(testArtworkId)).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)
        whenever(mockDataSnapshot.exists()).thenReturn(true)
        whenever(mockDataSnapshot.getValue(ArtworkModel::class.java)).thenReturn(testArtworkModel)

        // When
        val result = artworkRepository.getArtworkById(testArtworkId)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(testArtworkId, result.data?.id)
    }

    @Test
    fun `getArtworkById with non-existing artwork returns error`() = runTest {
        // Given
        whenever(mockArtworksRef.child(testArtworkId)).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)
        whenever(mockDataSnapshot.exists()).thenReturn(false)

        // When
        val result = artworkRepository.getArtworkById(testArtworkId)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Artwork not found", result.message)
    }

    @Test
    fun `getArtworkById with invalid data returns error`() = runTest {
        // Given
        whenever(mockArtworksRef.child(testArtworkId)).thenReturn(mockArtworksRef)
        whenever(mockArtworksRef.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)
        whenever(mockDataSnapshot.exists()).thenReturn(true)
        whenever(mockDataSnapshot.getValue(ArtworkModel::class.java)).thenReturn(null)

        // When
        val result = artworkRepository.getArtworkById(testArtworkId)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Invalid artwork data", result.message)
    }

    // MARK: - getTrendingArtworks Tests

    @Test
    fun `getTrendingArtworks returns artworks sorted by engagement`() = runTest {
        // Given
        val limit = 5
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockArtworksRef.orderByChild("isPublic")).thenReturn(mockQuery)
        whenever(mockQuery.equalTo(true)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)

        val recentTime = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L) // 3 days ago
        val recentArtwork = testArtworkModel.copy(
            artistId = "other_user",
            uploadedAt = recentTime,
            likesCount = 20,
            viewsCount = 100
        )
        val mockChildSnapshot = mock<DataSnapshot>()
        whenever(mockDataSnapshot.children).thenReturn(listOf(mockChildSnapshot))
        whenever(mockChildSnapshot.getValue(ArtworkModel::class.java)).thenReturn(recentArtwork)

        // When
        val result = artworkRepository.getTrendingArtworks(limit)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data?.size)
    }

    // MARK: - getArtworksByCategory Tests

    @Test
    fun `getArtworksByCategory with specific category returns filtered artworks`() = runTest {
        // Given
        val category = "Digital Art"
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
        whenever(mockArtworksRef.orderByChild("isPublic")).thenReturn(mockQuery)
        whenever(mockQuery.equalTo(true)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)

        val categoryArtwork = testArtworkModel.copy(artistId = "other_user", category = category)
        val mockChildSnapshot = mock<DataSnapshot>()
        whenever(mockDataSnapshot.children).thenReturn(listOf(mockChildSnapshot))
        whenever(mockChildSnapshot.getValue(ArtworkModel::class.java)).thenReturn(categoryArtwork)

        // When
        val result = artworkRepository.getArtworksByCategory(category)

        // Then
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `getArtworksByCategory with All category returns all public artworks`() = runTest {
        // Given - using spy to capture the method call
        val artworkRepository = spy(ArtworkRepositoryImpl())
        doReturn(NetworkResult.Success(listOf(testArtworkData)))
            .whenever(artworkRepository).getPublicArtworks()

        // When
        val result = artworkRepository.getArtworksByCategory("All")

        // Then
        assertTrue(result is NetworkResult.Success)
        verify(artworkRepository).getPublicArtworks()
    }

    // MARK: - getCategories Tests

    @Test
    fun `getCategories returns All plus unique categories`() = runTest {
        // Given
        whenever(mockArtworksRef.orderByChild("isPublic")).thenReturn(mockQuery)
        whenever(mockQuery.equalTo(true)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(mockTask)
        whenever(mockTask.await()).thenReturn(mockDataSnapshot)
        whenever(mockDataSnapshot.children).thenReturn(emptyList())

        // When
        val result = artworkRepository.getCategories()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals("All", result.data?.first())
    }
}
