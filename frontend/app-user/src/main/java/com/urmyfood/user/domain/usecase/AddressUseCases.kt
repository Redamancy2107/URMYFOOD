package com.urmyfood.user.domain.usecase

import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.data.model.AddressResponse
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.AddressRepository

class GetAddressesUseCase(
    private val addressRepository: AddressRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(): Result<List<AddressResponse>> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return addressRepository.getMyAddresses("Bearer $token")
    }
}

class CreateAddressUseCase(
    private val addressRepository: AddressRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(
        label: String, name: String, phone: String, detail: String, isDefault: Boolean
    ): Result<AddressResponse> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return addressRepository.createAddress("Bearer $token", label, name, phone, detail, isDefault)
    }
}

class UpdateAddressUseCase(
    private val addressRepository: AddressRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(
        id: Long, label: String, name: String, phone: String, detail: String, isDefault: Boolean
    ): Result<AddressResponse> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return addressRepository.updateAddress("Bearer $token", id, label, name, phone, detail, isDefault)
    }
}

class DeleteAddressUseCase(
    private val addressRepository: AddressRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return addressRepository.deleteAddress("Bearer $token", id)
    }
}

class SetDefaultAddressUseCase(
    private val addressRepository: AddressRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(id: Long): Result<AddressResponse> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return addressRepository.setDefault("Bearer $token", id)
    }
}
