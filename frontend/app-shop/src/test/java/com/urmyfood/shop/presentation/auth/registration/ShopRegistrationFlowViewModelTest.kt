package com.urmyfood.shop.presentation.auth.registration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.ShopCategory
import com.urmyfood.shop.domain.model.ShopRegistrationData
import com.urmyfood.shop.domain.repository.ShopVerificationRepository
import com.urmyfood.shop.domain.usecase.SubmitShopVerificationUseCase
import com.urmyfood.shop.presentation.auth.FakeTokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShopRegistrationFlowViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeShopVerificationRepository
    private lateinit var tokenStore: FakeTokenStore
    private lateinit var viewModel: ShopRegistrationFlowViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeShopVerificationRepository()
        tokenStore = FakeTokenStore().also {
            it.saveToken("token", null, null, "SHOP")
        }
        viewModel = ShopRegistrationFlowViewModel(SubmitShopVerificationUseCase(repository, tokenStore))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Initial state ---

    @Test
    fun `initial step1 uiState is Idle`() {
        assertEquals(ShopRegistrationFlowViewModel.Step1UiState.Idle, viewModel.step1UiState.value)
    }

    @Test
    fun `initial step2 uiState is Idle`() {
        assertEquals(ShopRegistrationFlowViewModel.Step2UiState.Idle, viewModel.step2UiState.value)
    }

    @Test
    fun `initial step3 uiState is Idle`() {
        assertEquals(ShopRegistrationFlowViewModel.Step3UiState.Idle, viewModel.step3UiState.value)
    }

    @Test
    fun `initial fields are empty or null`() {
        assertEquals("", viewModel.shopName.value)
        assertNull(viewModel.selectedCategory.value)
        assertEquals("", viewModel.address.value)
        assertNull(viewModel.cccdFrontUri.value)
        assertNull(viewModel.cccdBackUri.value)
        assertEquals(emptyList<String>(), viewModel.shopPhotoUris.value)
    }

    // --- Setters ---

    @Test
    fun `setShopName updates shopName`() {
        viewModel.setShopName("Quán Cơm Ngon")
        assertEquals("Quán Cơm Ngon", viewModel.shopName.value)
    }

    @Test
    fun `selectCategory updates selectedCategory`() {
        viewModel.selectCategory(ShopCategory.COM)
        assertEquals(ShopCategory.COM, viewModel.selectedCategory.value)
    }

    @Test
    fun `setAddress updates address and coordinates`() {
        viewModel.setAddress("123 Lê Lợi", 10.776, 106.700)
        assertEquals("123 Lê Lợi", viewModel.address.value)
        assertEquals(10.776, viewModel.latitude.value!!, 0.001)
        assertEquals(106.700, viewModel.longitude.value!!, 0.001)
    }

    @Test
    fun `updateAddressText updates address only`() {
        viewModel.updateAddressText("456 Nguyễn Huệ")
        assertEquals("456 Nguyễn Huệ", viewModel.address.value)
    }

    @Test
    fun `setCccdFront updates cccdFrontUri`() {
        viewModel.setCccdFront("content://front")
        assertEquals("content://front", viewModel.cccdFrontUri.value)
    }

    @Test
    fun `setCccdBack updates cccdBackUri`() {
        viewModel.setCccdBack("content://back")
        assertEquals("content://back", viewModel.cccdBackUri.value)
    }

    // --- addShopPhotos ---

    @Test
    fun `addShopPhotos adds uris to list`() {
        viewModel.addShopPhotos(listOf("uri1", "uri2"))
        assertEquals(listOf("uri1", "uri2"), viewModel.shopPhotoUris.value)
    }

    @Test
    fun `addShopPhotos caps total at maxPhotos`() {
        viewModel.addShopPhotos(listOf("u1", "u2", "u3"))
        viewModel.addShopPhotos(listOf("u4", "u5", "u6"))
        assertEquals(ShopRegistrationFlowViewModel.MAX_PHOTOS, viewModel.shopPhotoUris.value!!.size)
    }

    @Test
    fun `addShopPhotos when full does not add more`() {
        viewModel.addShopPhotos(listOf("u1", "u2", "u3", "u4", "u5"))
        viewModel.addShopPhotos(listOf("u6"))
        assertEquals(5, viewModel.shopPhotoUris.value!!.size)
    }

    // --- removeShopPhoto ---

    @Test
    fun `removeShopPhoto removes correct index`() {
        viewModel.addShopPhotos(listOf("u1", "u2", "u3"))
        viewModel.removeShopPhoto(1)
        assertEquals(listOf("u1", "u3"), viewModel.shopPhotoUris.value)
    }

    @Test
    fun `removeShopPhoto with out of bounds index does nothing`() {
        viewModel.addShopPhotos(listOf("u1", "u2"))
        viewModel.removeShopPhoto(5)
        assertEquals(2, viewModel.shopPhotoUris.value!!.size)
    }

    @Test
    fun `removeShopPhoto with negative index does nothing`() {
        viewModel.addShopPhotos(listOf("u1"))
        viewModel.removeShopPhoto(-1)
        assertEquals(1, viewModel.shopPhotoUris.value!!.size)
    }

    // --- validateStep1 ---

    @Test
    fun `validateStep1 with all fields missing emits ValidationError with 3 entries`() {
        viewModel.validateStep1()
        val state = viewModel.step1UiState.value
        assertTrue(state is ShopRegistrationFlowViewModel.Step1UiState.ValidationError)
        val errors = (state as ShopRegistrationFlowViewModel.Step1UiState.ValidationError).fields
        assertEquals(3, errors.size)
        assertTrue(errors.containsKey(ShopRegistrationFlowViewModel.Field.SHOP_NAME))
        assertTrue(errors.containsKey(ShopRegistrationFlowViewModel.Field.CATEGORY))
        assertTrue(errors.containsKey(ShopRegistrationFlowViewModel.Field.ADDRESS))
    }

    @Test
    fun `validateStep1 with blank shopName emits ValidationError for SHOP_NAME only`() {
        fillStep1(shopName = "")
        viewModel.validateStep1()
        val errors = (viewModel.step1UiState.value as ShopRegistrationFlowViewModel.Step1UiState.ValidationError).fields
        assertTrue(errors.containsKey(ShopRegistrationFlowViewModel.Field.SHOP_NAME))
        assertEquals(1, errors.size)
    }

    @Test
    fun `validateStep1 with no category emits ValidationError for CATEGORY only`() {
        fillStep1(category = null)
        viewModel.validateStep1()
        val errors = (viewModel.step1UiState.value as ShopRegistrationFlowViewModel.Step1UiState.ValidationError).fields
        assertTrue(errors.containsKey(ShopRegistrationFlowViewModel.Field.CATEGORY))
        assertEquals(1, errors.size)
    }

    @Test
    fun `validateStep1 with blank address emits ValidationError for ADDRESS only`() {
        fillStep1(address = "")
        viewModel.validateStep1()
        val errors = (viewModel.step1UiState.value as ShopRegistrationFlowViewModel.Step1UiState.ValidationError).fields
        assertTrue(errors.containsKey(ShopRegistrationFlowViewModel.Field.ADDRESS))
        assertEquals(1, errors.size)
    }

    @Test
    fun `validateStep1 with all valid fields emits Proceed`() {
        fillStep1()
        viewModel.validateStep1()
        assertEquals(ShopRegistrationFlowViewModel.Step1UiState.Proceed, viewModel.step1UiState.value)
    }

    // --- validateStep2 ---

    @Test
    fun `validateStep2 with no images emits ValidationError with 2 entries`() {
        viewModel.validateStep2()
        val state = viewModel.step2UiState.value
        assertTrue(state is ShopRegistrationFlowViewModel.Step2UiState.ValidationError)
        val errors = (state as ShopRegistrationFlowViewModel.Step2UiState.ValidationError).fields
        assertEquals(2, errors.size)
    }

    @Test
    fun `validateStep2 with no cccdFront emits ValidationError for CCCD_FRONT only`() {
        viewModel.setCccdBack("content://back")
        viewModel.validateStep2()
        val errors = (viewModel.step2UiState.value as ShopRegistrationFlowViewModel.Step2UiState.ValidationError).fields
        assertTrue(errors.containsKey(ShopRegistrationFlowViewModel.Field.CCCD_FRONT))
        assertEquals(1, errors.size)
    }

    @Test
    fun `validateStep2 with all images emits Proceed`() {
        viewModel.setCccdFront("content://front")
        viewModel.setCccdBack("content://back")
        viewModel.validateStep2()
        assertEquals(ShopRegistrationFlowViewModel.Step2UiState.Proceed, viewModel.step2UiState.value)
    }

    // --- validateStep3 ---

    @Test
    fun `validateStep3 with no photos emits ValidationError for SHOP_PHOTOS`() {
        viewModel.validateStep3()
        val state = viewModel.step3UiState.value
        assertTrue(state is ShopRegistrationFlowViewModel.Step3UiState.ValidationError)
        val errors = (state as ShopRegistrationFlowViewModel.Step3UiState.ValidationError).fields
        assertTrue(errors.containsKey(ShopRegistrationFlowViewModel.Field.SHOP_PHOTOS))
    }

    @Test
    fun `validateStep3 with photos transitions through Loading to Success`() = runTest {
        fillStep1()
        viewModel.setCccdFront("content://front")
        viewModel.setCccdBack("content://back")
        viewModel.addShopPhotos(listOf("content://photo1"))
        viewModel.validateStep3()
        assertEquals(ShopRegistrationFlowViewModel.Step3UiState.Loading, viewModel.step3UiState.value)
        advanceUntilIdle()
        assertEquals(ShopRegistrationFlowViewModel.Step3UiState.Success, viewModel.step3UiState.value)
        assertEquals("token", repository.lastToken)
        assertEquals("Quán Cơm", repository.lastData?.shopName)
    }

    @Test
    fun `validateStep3 with submit error emits Error`() = runTest {
        repository.result = Result.Error("Gửi hồ sơ xác minh thất bại")
        fillStep1()
        viewModel.setCccdFront("content://front")
        viewModel.setCccdBack("content://back")
        viewModel.addShopPhotos(listOf("content://photo1"))
        viewModel.validateStep3()
        advanceUntilIdle()
        val state = viewModel.step3UiState.value
        assertTrue(state is ShopRegistrationFlowViewModel.Step3UiState.Error)
        assertEquals("Gửi hồ sơ xác minh thất bại", (state as ShopRegistrationFlowViewModel.Step3UiState.Error).message)
    }

    // --- Helpers ---

    private fun fillStep1(
        shopName: String = "Quán Cơm",
        category: ShopCategory? = ShopCategory.COM,
        address: String = "123 Lê Lợi"
    ) {
        viewModel.setShopName(shopName)
        category?.let { viewModel.selectCategory(it) }
        viewModel.updateAddressText(address)
    }

    private class FakeShopVerificationRepository : ShopVerificationRepository {
        var result: Result<Unit> = Result.Success(Unit)
        var lastToken: String? = null
        var lastData: ShopRegistrationData? = null

        override suspend fun submitVerification(token: String, data: ShopRegistrationData): Result<Unit> {
            lastToken = token
            lastData = data
            return result
        }
    }
}
