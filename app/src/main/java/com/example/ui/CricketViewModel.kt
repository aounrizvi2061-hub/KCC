package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Delivery
import com.example.data.model.Match
import com.example.data.model.Player
import com.example.data.model.Team
import com.example.data.repository.CricketRepository
import com.example.data.scoring.CricketCalculationEngine
import com.example.data.scoring.InningsScorecard
import com.example.data.scoring.PlayerStats
import com.example.data.scoring.TestMatchContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UserRole {
    ADMIN,
    SCORER,
    VIEWER
}

sealed class Screen {
    object Dashboard : Screen()
    object LiveScoring : Screen()
    data class MatchCentre(val matchId: String) : Screen()
    object Teams : Screen()
    object Players : Screen()
    object CreateMatch : Screen()
    object Statistics : Screen()
    data class PlayerDetail(val playerId: String) : Screen()
}

class CricketViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CricketRepository(application)

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screenStack = mutableListOf<Screen>(Screen.Dashboard)

    private val _userRole = MutableStateFlow(UserRole.SCORER)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedMatch = MutableStateFlow<Match?>(null)
    val selectedMatch: StateFlow<Match?> = _selectedMatch.asStateFlow()

    private val _activeDeliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val activeDeliveries: StateFlow<List<Delivery>> = _activeDeliveries.asStateFlow()

    val teams: StateFlow<List<Team>> = repository.observeTeams()
        .catch { e -> _errorMessage.value = e.localizedMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val players: StateFlow<List<Player>> = repository.observePlayers()
        .catch { e -> _errorMessage.value = e.localizedMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matches: StateFlow<List<Match>> = repository.observeMatches()
        .catch { e -> _errorMessage.value = e.localizedMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize KCC club data if database is empty on first launch
        viewModelScope.launch {
            try {
                if (repository.currentUserIdOrNull() != null) {
                    repository.seedInitialKccDataIfEmpty()
                }
            } catch (e: Exception) {
                // Non-fatal if offline
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _screenStack.add(screen)
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (_screenStack.size > 1) {
            _screenStack.removeAt(_screenStack.lastIndex)
            _currentScreen.value = _screenStack.last()
            return true
        }
        return false
    }

    fun setUserRole(role: UserRole) {
        _userRole.value = role
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun selectMatch(match: Match) {
        _selectedMatch.value = match
        viewModelScope.launch {
            repository.observeDeliveries(match.id).collectLatest { dels ->
                _activeDeliveries.value = dels
            }
        }
    }

    fun seedData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.seedInitialKccDataIfEmpty()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Scoring actions
    fun recordDelivery(
        runsBat: Int,
        extraRuns: Int,
        extraType: String,
        isWicket: Boolean,
        wicketType: String,
        dismissedPlayerId: String,
        nextBatterId: String,
        fielderId: String,
        commentaryText: String
    ) {
        val currentMatch = _selectedMatch.value ?: return
        if (_userRole.value == UserRole.VIEWER) {
            _errorMessage.value = "Viewer mode is read-only"
            return
        }

        viewModelScope.launch {
            try {
                val currentInningsDels = _activeDeliveries.value.filter {
                    it.inningsNumber == currentMatch.currentInningsIndex
                }
                val legalBallsSoFar = currentInningsDels.count {
                    it.extraType.uppercase() != "WIDE" && it.extraType.uppercase() != "NO_BALL"
                }

                val isLegal = extraType.uppercase() != "WIDE" && extraType.uppercase() != "NO_BALL"
                val overNumber = (legalBallsSoFar / 6).toLong()
                val ballInOver = ((legalBallsSoFar % 6) + 1).toLong()
                val legalBallNumber = if (isLegal) ballInOver else (legalBallsSoFar % 6).toLong()

                var striker = currentMatch.currentStrikerId
                var nonStriker = currentMatch.currentNonStrikerId
                val bowler = currentMatch.currentBowlerId

                // Strike rotation rule: odd runs rotate strike
                val rotateOnRuns = (runsBat + (if (extraType.uppercase() == "BYE" || extraType.uppercase() == "LEG_BYE") extraRuns else 0)) % 2 != 0

                if (rotateOnRuns) {
                    val temp = striker
                    striker = nonStriker
                    nonStriker = temp
                }

                // If wicket fell
                if (isWicket && nextBatterId.isNotBlank()) {
                    if (dismissedPlayerId == striker || dismissedPlayerId.isBlank()) {
                        striker = nextBatterId
                    } else if (dismissedPlayerId == nonStriker) {
                        nonStriker = nextBatterId
                    }
                }

                // Over completion check (6 legal balls in over)
                val newLegalBallsTotal = legalBallsSoFar + (if (isLegal) 1 else 0)
                val isOverComplete = isLegal && (newLegalBallsTotal % 6 == 0)

                if (isOverComplete) {
                    // Strike rotates at the end of the over
                    val temp = striker
                    striker = nonStriker
                    nonStriker = temp
                }

                val autoCommentary = if (commentaryText.isNotBlank()) commentaryText else {
                    buildAutoCommentary(runsBat, extraRuns, extraType, isWicket, wicketType)
                }

                val newDelivery = Delivery(
                    matchId = currentMatch.id,
                    inningsNumber = currentMatch.currentInningsIndex,
                    overNumber = overNumber,
                    ballInOver = ballInOver,
                    legalBallNumber = legalBallNumber,
                    strikerId = currentMatch.currentStrikerId,
                    nonStrikerId = currentMatch.currentNonStrikerId,
                    bowlerId = bowler,
                    runsBat = runsBat.toLong(),
                    extraRuns = extraRuns.toLong(),
                    extraType = extraType,
                    isWicket = isWicket,
                    wicketType = wicketType,
                    dismissedPlayerId = dismissedPlayerId,
                    fielderId = fielderId,
                    commentary = autoCommentary
                )

                val updatedMatch = currentMatch.copy(
                    currentStrikerId = striker,
                    currentNonStrikerId = nonStriker
                )

                repository.recordDelivery(newDelivery, updatedMatch)
                _selectedMatch.value = updatedMatch
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    private fun buildAutoCommentary(runsBat: Int, extraRuns: Int, extraType: String, isWicket: Boolean, wicketType: String): String {
        return when {
            isWicket -> "OUT! Wicket fell - $wicketType!"
            extraType.uppercase() == "WIDE" -> "Wide ball called by umpire (+${extraRuns + 1})"
            extraType.uppercase() == "NO_BALL" -> "No ball signaled! Free hit opportunity"
            runsBat == 6 -> "SIX! Smacked high and long into the crowd!"
            runsBat == 4 -> "FOUR! Driven crisply to the boundary rope!"
            runsBat == 0 -> "Dot ball. Solid defense."
            runsBat == 1 -> "Quick single pushed into the gap."
            runsBat == 2 -> "Two runs taken with sharp running between wickets."
            runsBat == 3 -> "Good fielding saves the boundary, batsmen run 3."
            else -> "$runsBat runs scored."
        }
    }

    fun undoLastDelivery() {
        val currentMatch = _selectedMatch.value ?: return
        if (_userRole.value == UserRole.VIEWER) return

        viewModelScope.launch {
            try {
                val currentInningsDels = _activeDeliveries.value
                    .filter { it.inningsNumber == currentMatch.currentInningsIndex }
                    .sortedBy { it.createdAt?.seconds ?: 0L }
                val lastDelivery = currentInningsDels.lastOrNull() ?: return@launch

                repository.undoLastDelivery(currentMatch.id, lastDelivery.id)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun swapStrike() {
        val currentMatch = _selectedMatch.value ?: return
        if (_userRole.value == UserRole.VIEWER) return

        val updated = currentMatch.copy(
            currentStrikerId = currentMatch.currentNonStrikerId,
            currentNonStrikerId = currentMatch.currentStrikerId
        )
        viewModelScope.launch {
            try {
                repository.updateMatch(updated)
                _selectedMatch.value = updated
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun changeBowler(newBowlerId: String) {
        val currentMatch = _selectedMatch.value ?: return
        if (_userRole.value == UserRole.VIEWER) return

        val updated = currentMatch.copy(currentBowlerId = newBowlerId)
        viewModelScope.launch {
            try {
                repository.updateMatch(updated)
                _selectedMatch.value = updated
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun changeStriker(newStrikerId: String) {
        val currentMatch = _selectedMatch.value ?: return
        if (_userRole.value == UserRole.VIEWER) return

        val updated = currentMatch.copy(currentStrikerId = newStrikerId)
        viewModelScope.launch {
            try {
                repository.updateMatch(updated)
                _selectedMatch.value = updated
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun changeNonStriker(newNonStrikerId: String) {
        val currentMatch = _selectedMatch.value ?: return
        if (_userRole.value == UserRole.VIEWER) return

        val updated = currentMatch.copy(currentNonStrikerId = newNonStrikerId)
        viewModelScope.launch {
            try {
                repository.updateMatch(updated)
                _selectedMatch.value = updated
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun declareInnings() {
        val currentMatch = _selectedMatch.value ?: return
        if (_userRole.value == UserRole.VIEWER) return

        val nextInnings = currentMatch.currentInningsIndex + 1
        val updated = currentMatch.copy(
            isDeclared = true,
            currentInningsIndex = nextInnings
        )
        viewModelScope.launch {
            try {
                repository.updateMatch(updated)
                _selectedMatch.value = updated
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun endInnings() {
        val currentMatch = _selectedMatch.value ?: return
        if (_userRole.value == UserRole.VIEWER) return

        val nextInnings = currentMatch.currentInningsIndex + 1
        val isFinalInnings = (currentMatch.format.uppercase() == "LIMITED_OVERS" && currentMatch.currentInningsIndex >= 2) ||
                (currentMatch.format.uppercase() == "TEST" && currentMatch.currentInningsIndex >= 4)

        val updated = if (isFinalInnings) {
            currentMatch.copy(status = "COMPLETED")
        } else {
            currentMatch.copy(currentInningsIndex = nextInnings)
        }

        viewModelScope.launch {
            try {
                repository.updateMatch(updated)
                _selectedMatch.value = updated
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun enforceFollowOn() {
        val currentMatch = _selectedMatch.value ?: return
        if (_userRole.value == UserRole.VIEWER) return

        val updated = currentMatch.copy(
            followOnEnforced = true,
            currentInningsIndex = 3
        )
        viewModelScope.launch {
            try {
                repository.updateMatch(updated)
                _selectedMatch.value = updated
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun finishMatch(resultText: String) {
        val currentMatch = _selectedMatch.value ?: return
        if (_userRole.value == UserRole.VIEWER) return

        val updated = currentMatch.copy(
            status = "COMPLETED",
            resultText = resultText
        )
        viewModelScope.launch {
            try {
                repository.updateMatch(updated)
                _selectedMatch.value = updated
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    // Match creation
    fun createMatch(match: Match, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val id = repository.createMatch(match)
                onCreated(id)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Teams & Players CRUD
    fun createTeam(name: String, shortName: String) {
        viewModelScope.launch {
            try {
                repository.createTeam(name, shortName)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun deleteTeam(teamId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTeam(teamId)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun createPlayer(
        name: String,
        jersey: Long,
        role: String,
        battingStyle: String,
        bowlingStyle: String,
        teamId: String
    ) {
        viewModelScope.launch {
            try {
                repository.createPlayer(name, jersey, role, battingStyle, bowlingStyle, "", teamId)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun deletePlayer(playerId: String) {
        viewModelScope.launch {
            try {
                repository.deletePlayer(playerId)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }
}
