package com.urmyfood.shop.presentation.main.account

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.ShopProfile
import com.urmyfood.shop.domain.model.ShopProfileImageType
import com.urmyfood.shop.domain.repository.ShopProfileRepository
import com.urmyfood.shop.domain.usecase.GetShopProfileUseCase
import com.urmyfood.shop.domain.usecase.UpdateShopProfileUseCase
import com.urmyfood.shop.domain.usecase.UploadShopProfileImageUseCase
import com.urmyfood.shop.presentation.auth.FakeTokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import okhttp3.MultipartBody
import okhttp3.RequestBody

@OptIn(ExperimentalCoroutinesApi::class)
class ShopProfileEditViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: FakeShopProfileRepository
    private lateinit var tokenStore: FakeTokenStore
    private lateinit var viewModel: ShopProfileEditViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = FakeShopProfileRepository()
        tokenStore = FakeTokenStore().also {
            it.saveToken("token", null, null, "SHOP")
        }
        viewModel = ShopProfileEditViewModel(
            GetShopProfileUseCase(repository, tokenStore),
            UpdateShopProfileUseCase(repository, tokenStore),
            UploadShopProfileImageUseCase(repository, tokenStore)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile success emits Success`() = runTest {
        viewModel.loadProfile()
        advanceUntilIdle()

        assertTrue(viewModel.loadState.value is ProfileUiState.Success)
    }

    @Test
    fun `loadProfileIfNeeded does not reload when profile is already loaded`() = runTest {
        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.loadProfileIfNeeded()
        advanceUntilIdle()

        assertEquals(1, repository.getProfileCallCount)
        assertTrue(viewModel.loadState.value is ProfileUiState.Success)
    }

    @Test
    fun `updateProfile success sends trimmed profile`() = runTest {
        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.updateProfile(
            shopName = "  Bếp Nhà B  ",
            logoUrl = " logo ",
            coverUrl = " cover ",
            category = " COM ",
            address = " 456 Nguyễn Huệ ",
            latitude = 10.77,
            longitude = 106.7,
            description = " Cơm sinh viên ",
            openingHours = " 09:00 - 21:00 ",
            isOpen = false
        )
        advanceUntilIdle()

        val state = viewModel.updateState.value
        assertTrue(state is ProfileEditUiState.Success)
        assertEquals("Bearer token", repository.lastUpdateToken)
        assertEquals("Bếp Nhà B", repository.lastUpdatedProfile?.shopName)
        assertEquals("COM", repository.lastUpdatedProfile?.category)
        assertEquals(10.77, repository.lastUpdatedProfile?.latitude)
        assertEquals(false, repository.lastUpdatedProfile?.isOpen)
    }

    @Test
    fun `updateProfile uploads selected images before saving profile`() = runTest {
        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.updateProfile(
            shopName = "Bếp Nhà B",
            logoUrl = "content://logo",
            coverUrl = "content://cover",
            category = "COM",
            address = "456 Nguyễn Huệ",
            latitude = 10.77,
            longitude = 106.7,
            description = "Cơm sinh viên",
            openingHours = "09:00 - 21:00",
            isOpen = true,
            logoImagePart = imagePart(),
            coverImagePart = imagePart()
        )
        advanceUntilIdle()

        assertEquals(listOf(ShopProfileImageType.LOGO, ShopProfileImageType.COVER), repository.uploadedTypes)
        assertEquals("https://cdn.example.com/logo.png", repository.lastUpdatedProfile?.logoUrl)
        assertEquals("https://cdn.example.com/cover.png", repository.lastUpdatedProfile?.coverUrl)
    }

    @Test
    fun `updateProfile with uploaded avatar and no cover url still saves profile`() = runTest {
        repository.profileResult = profile().copy(coverUrl = null)
        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.updateProfile(
            shopName = "Bếp Nhà B",
            logoUrl = "content://logo",
            coverUrl = null,
            category = "COM",
            address = "456 Nguyễn Huệ",
            latitude = 10.77,
            longitude = 106.7,
            description = "Cơm sinh viên",
            openingHours = "09:00 - 21:00",
            isOpen = true,
            logoImagePart = imagePart(),
            coverImagePart = null
        )
        advanceUntilIdle()

        val state = viewModel.updateState.value
        assertTrue(state is ProfileEditUiState.Success)
        assertEquals(listOf(ShopProfileImageType.LOGO), repository.uploadedTypes)
        assertEquals("https://cdn.example.com/logo.png", repository.lastUpdatedProfile?.logoUrl)
        assertEquals(null, repository.lastUpdatedProfile?.coverUrl)
    }

    @Test
    fun `updateProfile timeout emits Error instead of keeping Loading`() = runTest {
        viewModel.loadProfile()
        advanceUntilIdle()
        repository.updateDelayMs = Long.MAX_VALUE

        viewModel.updateProfile(
            shopName = "Bếp Nhà B",
            logoUrl = "https://cdn.example.com/logo.png",
            coverUrl = null,
            category = "COM",
            address = "456 Nguyễn Huệ",
            latitude = 10.77,
            longitude = 106.7,
            description = "Cơm sinh viên",
            openingHours = "09:00 - 21:00",
            isOpen = true
        )
        advanceTimeBy(45_001L)
        advanceUntilIdle()

        val state = viewModel.updateState.value
        assertTrue(state is ProfileEditUiState.Error)
        assertEquals("Lưu hồ sơ quá lâu. Vui lòng thử lại", (state as ProfileEditUiState.Error).message)
    }

    @Test
    fun `updateProfile loading message changes before update profile`() = runTest {
        viewModel.loadProfile()
        advanceUntilIdle()
        val states = mutableListOf<ProfileEditUiState>()
        val observer = Observer<ProfileEditUiState> { states += it }
        viewModel.updateState.observeForever(observer)

        try {
            viewModel.updateProfile(
                shopName = "Bếp Nhà B",
                logoUrl = "content://logo",
                coverUrl = null,
                category = "COM",
                address = "456 Nguyễn Huệ",
                latitude = 10.77,
                longitude = 106.7,
                description = "Cơm sinh viên",
                openingHours = "09:00 - 21:00",
                isOpen = true,
                logoImagePart = imagePart()
            )
            advanceUntilIdle()

            assertTrue(states.contains(ProfileEditUiState.Loading("Đang tải ảnh...")))
            assertTrue(states.contains(ProfileEditUiState.Loading("Đang lưu hồ sơ...")))
        } finally {
            viewModel.updateState.removeObserver(observer)
        }
    }

    @Test
    fun `updateProfile with blank shopName emits Error without repository call`() = runTest {
        viewModel.updateProfile(
            shopName = " ",
            logoUrl = null,
            coverUrl = null,
            category = "COM",
            address = "123 Lê Lợi",
            latitude = null,
            longitude = null,
            description = null,
            openingHours = "08:00 - 22:00",
            isOpen = true
        )
        advanceUntilIdle()

        val state = viewModel.updateState.value
        assertTrue(state is ProfileEditUiState.Error)
        assertEquals(null, repository.lastUpdatedProfile)
    }

    private class FakeShopProfileRepository : ShopProfileRepository {
        var lastUpdateToken: String? = null
        var lastUpdatedProfile: ShopProfile? = null
        var getProfileCallCount: Int = 0
        var profileResult: ShopProfile = profile()
        var updateDelayMs: Long = 0L
        val uploadedTypes = mutableListOf<ShopProfileImageType>()

        override suspend fun getMyProfile(token: String): Result<ShopProfile> {
            getProfileCallCount += 1
            return Result.Success(profileResult)
        }

        override suspend fun updateMyProfile(token: String, profile: ShopProfile): Result<ShopProfile> {
            if (updateDelayMs > 0) {
                delay(updateDelayMs)
            }
            lastUpdateToken = token
            lastUpdatedProfile = profile
            return Result.Success(profile)
        }

        override suspend fun uploadProfileImage(
            token: String,
            type: ShopProfileImageType,
            file: MultipartBody.Part
        ): Result<String> {
            uploadedTypes += type
            return Result.Success(
                if (type == ShopProfileImageType.LOGO) {
                    "https://cdn.example.com/logo.png"
                } else {
                    "https://cdn.example.com/cover.png"
                }
            )
        }
    }
}

private fun imagePart(): MultipartBody.Part {
    return MultipartBody.Part.createFormData("file", "image.png", RequestBody.create(null, "data"))
}

fun profile() = ShopProfile(
    id = 1L,
    shopId = 2L,
    shopName = "Bếp Nhà A",
    logoUrl = "https://example.com/logo.png",
    coverUrl = "https://example.com/cover.png",
    category = "COM",
    address = "123 Lê Lợi",
    latitude = 10.776,
    longitude = 106.700,
    description = "Cơm trưa",
    openingHours = "08:00 - 22:00",
    isOpen = true,
    verificationStatus = "APPROVED"
)
