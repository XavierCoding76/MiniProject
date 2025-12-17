package com.example.miniproject.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.auth.AuthRepository
import com.example.miniproject.components.SearchResultItemData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserSearchViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _searchResult = MutableStateFlow<List<SearchResultItemData>?>(null)
    val searchResult: StateFlow<List<SearchResultItemData>?> = _searchResult

    fun searchUserByDisplayId(displayId: String) {
        viewModelScope.launch {
            // If the search query is blank, we can just clear the results.
            if (displayId.isBlank()) {
                _searchResult.value = emptyList()
                return@launch
            }

            // Perform the search using the efficient repository method.
            val user = authRepository.findUserByDisplayId(displayId)

            // Convert the result to the format expected by our generic search component.
            _searchResult.value = if (user != null) {
                listOf(SearchResultItemData(id = user.id, title = "${user.name} (${user.displayId})"))
            } else {
                emptyList()
            }
        }
    }
}
