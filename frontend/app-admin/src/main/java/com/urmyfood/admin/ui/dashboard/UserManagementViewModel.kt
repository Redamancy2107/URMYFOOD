package com.urmyfood.admin.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urmyfood.admin.data.model.AccountProfile
import com.urmyfood.admin.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UserManagementState {
    object Idle : UserManagementState()
    object Loading : UserManagementState()
    data class Success(val accounts: List<AccountProfile>) : UserManagementState()
    data class Error(val message: String) : UserManagementState()
}

class UserManagementViewModel : ViewModel() {
    private val repository = AdminRepository()

    private val _uiState = MutableStateFlow<UserManagementState>(UserManagementState.Idle)
    val uiState: StateFlow<UserManagementState> = _uiState

    var currentRole: String? = null
    var currentSortBy: String = "createdAt"
    var currentSortDir: String = "DESC"

    fun loadAccounts(role: String? = currentRole) {
        currentRole = role
        _uiState.value = UserManagementState.Loading
        viewModelScope.launch {
            val result = repository.getAllAccounts(0, 100, role, currentSortBy, currentSortDir)
            result.onSuccess { page ->
                _uiState.value = UserManagementState.Success(page.content)
            }.onFailure { error ->
                _uiState.value = UserManagementState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun setSort(sortBy: String) {
        if (currentSortBy == sortBy) {
            currentSortDir = if (currentSortDir == "ASC") "DESC" else "ASC"
        } else {
            currentSortBy = sortBy
            currentSortDir = "ASC"
        }
        loadAccounts()
    }

    fun toggleAccountActive(account: AccountProfile, reason: String) {
        viewModelScope.launch {
            val result = repository.lockUnlockAccount(account.id, !account.isActive, reason)
            result.onSuccess {
                loadAccounts()
            }.onFailure { error ->
                _uiState.value = UserManagementState.Error("Cập nhật thất bại: ${error.message}")
            }
        }
    }

    fun deleteAccount(account: AccountProfile, reason: String) {
        viewModelScope.launch {
            val result = repository.deleteAccount(account.id, reason)
            result.onSuccess {
                loadAccounts()
            }.onFailure { error ->
                _uiState.value = UserManagementState.Error("Xóa thất bại: ${error.message}")
            }
        }
    }
}
