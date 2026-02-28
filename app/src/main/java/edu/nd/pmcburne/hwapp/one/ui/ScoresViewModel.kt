package edu.nd.pmcburne.hwapp.one.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.nd.pmcburne.hwapp.one.ScoresRepository
import edu.nd.pmcburne.hwapp.one.GameEntity
import edu.nd.pmcburne.hwapp.one.Sport
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScoresUiState(
    val selectedDate: LocalDate,
    val selectedSport: Sport,
    val isRefreshing: Boolean,
    val isOnline: Boolean,
    val games: List<GameEntity>,
    val lastError: String?,
)

class ScoresViewModel(
    application: Application,
    private val repository: ScoresRepository,
) : AndroidViewModel(application) {
    private val _uiState =
        MutableStateFlow(
            ScoresUiState(
                selectedDate = LocalDate.now(),
                selectedSport = Sport.Men,
                isRefreshing = false,
                isOnline = true,
                games = emptyList(),
                lastError = null,
            ),
        )
    val uiState: StateFlow<ScoresUiState> = _uiState.asStateFlow()

    private var gamesJob: Job? = null

    init {
        startObservingGames()
        refresh()
    }

    fun setDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        startObservingGames()
        refresh()
    }

    fun setSport(sport: Sport) {
        _uiState.update { it.copy(selectedSport = sport) }
        startObservingGames()
        refresh()
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(lastError = null) }
            val online = isOnline(getApplication())
            _uiState.update { it.copy(isOnline = online) }
            if (!online) return@launch

            _uiState.update { it.copy(isRefreshing = true) }
            try {
                repository.refresh(
                    sport = _uiState.value.selectedSport,
                    date = _uiState.value.selectedDate,
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(lastError = e.message ?: "Failed to refresh") }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun startObservingGames() {
        gamesJob?.cancel()
        val sport = _uiState.value.selectedSport
        val date = _uiState.value.selectedDate
        gamesJob =
            viewModelScope.launch {
                repository.getGames(sport = sport, date = date).collect { games ->
                    _uiState.update { it.copy(games = games) }
                }
            }
    }
}

private fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

class ScoresViewModelFactory(
    private val application: Application,
    private val repository: ScoresRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScoresViewModel::class.java)) {
            return ScoresViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
