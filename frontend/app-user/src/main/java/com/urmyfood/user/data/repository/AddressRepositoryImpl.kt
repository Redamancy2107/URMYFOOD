package com.urmyfood.user.data.repository

import com.urmyfood.user.data.model.AddressRequest
import com.urmyfood.user.data.model.AddressResponse
import com.urmyfood.user.data.remote.AddressApiService
import com.urmyfood.user.data.util.toUserMessage
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.AddressRepository

class AddressRepositoryImpl(
    private val addressApiService: AddressApiService
) : AddressRepository {

    override suspend fun getMyAddresses(token: String): Result<List<AddressResponse>> {
        return safeApiCall { addressApiService.getMyAddresses(token) }
    }

    override suspend fun createAddress(
        token: String, label: String, name: String, phone: String, detail: String, isDefault: Boolean
    ): Result<AddressResponse> {
        val request = AddressRequest(label, name, phone, detail, isDefault)
        return safeApiCall { addressApiService.createAddress(token, request) }
    }

    override suspend fun updateAddress(
        token: String, id: Long, label: String, name: String, phone: String, detail: String, isDefault: Boolean
    ): Result<AddressResponse> {
        val request = AddressRequest(label, name, phone, detail, isDefault)
        return safeApiCall { addressApiService.updateAddress(token, id, request) }
    }

    override suspend fun deleteAddress(token: String, id: Long): Result<Unit> {
        return try {
            val response = addressApiService.deleteAddress(token, id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Lỗi máy chủ: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    override suspend fun setDefault(token: String, id: Long): Result<AddressResponse> {
        return safeApiCall { addressApiService.setDefault(token, id) }
    }

    private suspend fun <T> safeApiCall(
        call: suspend () -> retrofit2.Response<com.urmyfood.user.data.model.ApiResponse<T>>
    ): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.Success(body.data)
                } else {
                    Result.Error(body?.message ?: "Thao tác thất bại")
                }
            } else {
                Result.Error("Lỗi máy chủ: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }
}
