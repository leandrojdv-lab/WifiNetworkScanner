package com.example.wifinetworkscanner.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ScanHistorySearchFilter
import com.example.wifinetworkscanner.domain.usecase.ObserveAllScanHistoryUseCase
import com.example.wifinetworkscanner.domain.usecase.SearchScanHistoryUseCase
import com.example.wifinetworkscanner.ui.text.UiText
import com.example.wifinetworkscanner.utils.logger.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val observeAllScanHistoryUseCase: ObserveAllScanHistoryUseCase,
    private val searchScanHistoryUseCase: SearchScanHistoryUseCase
) : ViewModel() {

    private val searchFilter = MutableStateFlow(ScanHistorySearchFilter())

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SearchUiEffect>()
    val uiEffect: SharedFlow<SearchUiEffect> = _uiEffect.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AppLogger.error(TAG, "Erro inesperado na pesquisa.", throwable)

        val message = UiText.StringResource(
            resId = R.string.search_load_failed_message
        )

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                errorMessage = message
            )
        }

        viewModelScope.launch {
            _uiEffect.emit(
                SearchUiEffect.ShowMessage(message = message)
            )
        }
    }

    init {
        observeSearchResults()
    }

    fun updateQuery(query: String) {
        val updatedFilter = searchFilter.value.copy(
            query = query.take(MAX_QUERY_LENGTH)
        )

        searchFilter.value = updatedFilter

        _uiState.update { currentState ->
            currentState.copy(searchFilter = updatedFilter)
        }
    }

    fun updateOpenPortText(openPortText: String) {
        val sanitizedOpenPort = openPortText
            .filter { character -> character.isDigit() }
            .take(MAX_PORT_LENGTH)

        val updatedFilter = searchFilter.value.copy(
            openPortText = sanitizedOpenPort
        )

        searchFilter.value = updatedFilter

        _uiState.update { currentState ->
            currentState.copy(searchFilter = updatedFilter)
        }
    }

    fun clearFilters() {
        val updatedFilter = ScanHistorySearchFilter()

        searchFilter.value = updatedFilter

        _uiState.update { currentState ->
            currentState.copy(searchFilter = updatedFilter)
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchResults() {
        viewModelScope.launch(exceptionHandler) {
            observeAllScanHistoryUseCase()
                .combine(
                    searchFilter
                        .debounce(SEARCH_DEBOUNCE_MILLIS)
                        .distinctUntilChanged()
                ) { scanHistoryList, filter ->
                    val results = searchScanHistoryUseCase(
                        scanHistoryList = scanHistoryList,
                        filter = filter
                    )

                    SearchResultState(
                        searchFilter = filter,
                        results = results
                    )
                }
                .collect { resultState ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            searchFilter = resultState.searchFilter,
                            results = resultState.results,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private data class SearchResultState(
        val searchFilter: ScanHistorySearchFilter,
        val results: List<ScanHistory>
    )

    private companion object {
        const val TAG = "SearchViewModel"
        const val MAX_QUERY_LENGTH = 80
        const val MAX_PORT_LENGTH = 5
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}