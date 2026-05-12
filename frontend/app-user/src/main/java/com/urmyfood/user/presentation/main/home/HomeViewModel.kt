package com.urmyfood.user.presentation.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.GetPostsUseCase
import kotlinx.coroutines.launch

sealed class NewsfeedUiState {
    object Loading : NewsfeedUiState()
    data class Success(val posts: List<FoodPost>) : NewsfeedUiState()
    data class Error(val message: String) : NewsfeedUiState()
}

/**
 * ViewModel for the Home screen.
 */
class HomeViewModel(private val getPostsUseCase: GetPostsUseCase) : ViewModel() {
    
    private val _uiState = MutableLiveData<NewsfeedUiState>(NewsfeedUiState.Loading)
    val uiState: LiveData<NewsfeedUiState> = _uiState

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = NewsfeedUiState.Loading
            when (val result = getPostsUseCase()) {
                is Result.Success -> _uiState.value = NewsfeedUiState.Success(result.data)
                is Result.Error -> _uiState.value = NewsfeedUiState.Error(result.message)
            }
        }
    }

    /**
     * Factory for creating HomeViewModel.
     */
    class Factory(private val getPostsUseCase: GetPostsUseCase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(getPostsUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
