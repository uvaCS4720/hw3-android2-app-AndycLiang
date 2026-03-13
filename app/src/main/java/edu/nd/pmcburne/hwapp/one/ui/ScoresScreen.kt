package edu.nd.pmcburne.hwapp.one.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import edu.nd.pmcburne.hwapp.one.GameEntity
import edu.nd.pmcburne.hwapp.one.Sport
import edu.nd.pmcburne.hwapp.one.ui.theme.HwAppDimens
import edu.nd.pmcburne.hwapp.one.ui.theme.winnerHighlightTextStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ScoresScreen(
    viewModel: ScoresViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Basketball Scores") },
                actions = {
                    if (uiState.isRefreshing) {
                        CircularProgressIndicator()
                    }
                    IconButton(
                        onClick = viewModel::refresh,
                        enabled = !uiState.isRefreshing,
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Controls(
                    selectedDate = uiState.selectedDate,
                    selectedSport = uiState.selectedSport,
                    isOnline = uiState.isOnline,
                    onSelectDate = viewModel::setDate,
                    onSelectSport = viewModel::setSport,
                )

                if (uiState.isRefreshing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    Spacer(modifier = Modifier.height(HwAppDimens.LoadingSpacerHeight))
                }

                HorizontalDivider()

                if (uiState.games.isEmpty() && uiState.isRefreshing) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading...", style = MaterialTheme.typography.bodyLarge)
                    }
                } else if (uiState.games.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No games saved for this date.", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    GamesList(games = uiState.games)
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Controls(
    selectedDate: LocalDate,
    selectedSport: Sport,
    isOnline: Boolean,
    onSelectDate: (LocalDate) -> Unit,
    onSelectSport: (Sport) -> Unit,
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    if (showDatePicker) {
        val millis = remember(selectedDate) {
            selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = millis)
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val date = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            onSelectDate(date)
                        }
                        showDatePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(HwAppDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(HwAppDimens.SectionSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { showDatePicker = true }) {
                Text(selectedDate.format(dateFormatter))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(HwAppDimens.ItemSpacing)) {
                FilterChip(
                    selected = selectedSport == Sport.Men,
                    onClick = { onSelectSport(Sport.Men) },
                    label = { Text("Men") },
                    colors = FilterChipDefaults.filterChipColors(),
                )
                FilterChip(
                    selected = selectedSport == Sport.Women,
                    onClick = { onSelectSport(Sport.Women) },
                    label = { Text("Women") },
                    colors = FilterChipDefaults.filterChipColors(),
                )
            }
        }

        if (!isOnline) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = "No internet connection. Showing last saved scores.",
                    modifier = Modifier.padding(
                        horizontal = HwAppDimens.ScreenPadding,
                        vertical = HwAppDimens.ItemSpacing,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun GamesList(
    games: List<GameEntity>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(HwAppDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(HwAppDimens.SectionSpacing),
    ) {
        items(
            items = games,
            key = { it.sport + ":" + it.gameId },
        ) { game ->
            GameCard(game = game)
        }
    }
}

@Composable
private fun GameCard(
    game: GameEntity,
) {
    val isFinal = game.gameState.equals("final", ignoreCase = true)
    val isLive = game.gameState.equals("live", ignoreCase = true)
    val isPre = game.gameState.equals("pre", ignoreCase = true)

    val statusText = when {
        isFinal -> "Final"
        isLive -> listOfNotNull(game.currentPeriod, game.contestClock)
            .joinToString(" • ")
            .ifBlank { "In progress" }
        isPre -> game.startTime?.let { "Starts $it" } ?: "Upcoming"
        else -> game.gameState
    }

    val highlightStyle = winnerHighlightTextStyle()
    val awayNameStyle =
        if (isFinal && game.awayWinner) highlightStyle else MaterialTheme.typography.titleMedium
    val homeNameStyle =
        if (isFinal && game.homeWinner) highlightStyle else MaterialTheme.typography.titleMedium

    val awayScoreStyle =
        if (isFinal && game.awayWinner) highlightStyle else MaterialTheme.typography.titleMedium
    val homeScoreStyle =
        if (isFinal && game.homeWinner) highlightStyle else MaterialTheme.typography.titleMedium

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(HwAppDimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(HwAppDimens.ItemSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Away: ${game.awayTeamName}",
                            style = awayNameStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Home: ${game.homeTeamName}",
                            style = homeNameStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = scoreText(game.awayScore, isPre), style = awayScoreStyle)
                    Text(text = scoreText(game.homeScore, isPre), style = homeScoreStyle)
                }
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

fun scoreText(score: Int?, isPre: Boolean): String {
    if (isPre) return "—"
    return score?.toString() ?: "—"
}
