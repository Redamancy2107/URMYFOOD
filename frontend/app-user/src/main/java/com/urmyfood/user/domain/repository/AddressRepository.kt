package com.urmyfood.user.domain.repository

import com.urmyfood.user.data.model.AddressResponse
import com.urmyfood.user.domain.model.Result

interface AddressRepository {
    suspend fun getMyAddresses(token: String): Result<List<AddressResponse>>
    suspend fun createAddress(token: String, label: String, name: String, phone: String, detail: String, isDefault: Boolean): Result<AddressResponse>
    suspend fun updateAddress(token: String, id: Long, label: String, name: String, phone: String, detail: String, isDefault: Boolean): Result<AddressResponse>
    suspend fun deleteAddress(token: String, id: Long): Result<Unit>
    suspend fun setDefault(token: String, id: Long): Result<AddressResponse>
}
