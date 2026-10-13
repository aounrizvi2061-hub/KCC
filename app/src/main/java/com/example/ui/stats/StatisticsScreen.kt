package com.example.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.scoring.CricketCalculationEngine
import com.example.ui.CricketViewModel
import com.example.ui.theme.KccBlackBackground
import com.example.ui.theme.KccGoldDark
import com.example.ui.theme.KccGoldLight
import com.example.ui.theme.KccGoldPrimary
import com.example.ui.theme.KccSurfaceDark
import com.example.ui.theme.KccTextMuted
import com.example.ui.theme.KccTextSecondary
import com.example.ui.theme.KccTextWhite

@Composable
fun StatisticsScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val players by viewModel.players.collectAsState()
    val deliveries by viewModel.activeDeliveries.collectAsState()
    val matches by viewModel.matches.collectAsState()

    val statsList = remember(players, deliveries) {
        CricketCalculationEngine.computeCareerStatistics(players, deliveries)
    }

    val topRuns = remember(statsList) { statsList.sortedByDescending { it.totalRuns } }
    val topWickets = remember(statsList) { statsList.sortedByDescending { it.wickets } }
    val topScores = remember(statsList) { statsList.sortedByDescending { it.highestScore } }
    val mostSixes = remember(statsList) { statsList.sortedByDescending { it.sixes } }
    val bestAvg = remember(statsList) { statsList.filter { it.totalRuns > 0 }.sortedByDescending { it.battingAverage } }

    val categories = listOf("Top Runs", "Top Wickets", "High Scores", "Most Sixes", "Batting Avg", "Head to Head")
    var selectedCat by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KccBlackBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = KccGoldPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "KCC RECORDS & ARCHIVES",
                        color = KccGoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Club Hall of Fame & Statistics",
                        color = KccTextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedCat,
                containerColor = Color(0xFF141419),
                contentColor = KccGoldPrimary,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedCat]),
                        color = KccGoldPrimary,
                        height = 3.dp
                    )
                }
            ) {
                categories.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedCat == index,
                        onClick = { selectedCat = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedCat == index) KccGoldLight else KccTextSecondary,
                                fontWeight = if (selectedCat == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (selectedCat) {
                    0 -> { // Top Runs
                        items(topRuns.take(10)) { s ->
                            LeaderboardRow(
                                rank = topRuns.indexOf(s) + 1,
                                title = s.playerName,
                                subtitle = "${s.inningsBatted} Inns • HS: ${s.highestScore} • Avg: ${s.battingAverage}",
                                primaryMetric = "${s.totalRuns} Runs"
                            )
                        }
                    }
                    1 -> { // Top Wickets
                        items(topWickets.take(10)) { s ->
                            LeaderboardRow(
                                rank = topWickets.indexOf(s) + 1,
                                title = s.playerName,
                                subtitle = "${s.oversBowledDecimal} Ov • Econ: ${s.bowlingEconomy} • BBI: ${s.bestBowlingFigures}",
                                primaryMetric = "${s.wickets} Wkts"
                            )
                        }
                    }
                    2 -> { // Highest Scores
                        items(topScores.take(10)) { s ->
                            LeaderboardRow(
                                rank = topScores.indexOf(s) + 1,
                                title = s.playerName,
                                subtitle = "${s.fours} Fours • ${s.sixes} Sixes • SR: ${s.battingStrikeRate}",
                                primaryMetric = "${s.highestScore}*"
                            )
                        }
                    }
                    3 -> { // Most Sixes
                        items(mostSixes.take(10)) { s ->
                            LeaderboardRow(
                                rank = mostSixes.indexOf(s) + 1,
                                title = s.playerName,
                                subtitle = "${s.fours} Fours • Total Runs: ${s.totalRuns}",
                                primaryMetric = "${s.sixes} Sixes"
                            )
                        }
                    }
                    4 -> { // Batting Avg
                        items(bestAvg.take(10)) { s ->
                            LeaderboardRow(
                                rank = bestAvg.indexOf(s) + 1,
                                title = s.playerName,
                                subtitle = "${s.totalRuns} Runs in ${s.inningsBatted} Inns",
                                primaryMetric = "${s.battingAverage}"
                            )
                        }
                    }
                    5 -> { // Head to Head
                        val completed = matches.filter { it.status.uppercase() == "COMPLETED" }
                        if (completed.isEmpty()) {
                            item {
                                Text("No head-to-head match results yet.", color = KccTextSecondary)
                            }
                        } else {
                            items(completed) { m ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("${m.teamAName} vs ${m.teamBName}", color = KccTextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(m.resultText.ifBlank { "Result archived" }, color = Color(0xFF4ADE80), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Venue: ${m.venue} • Format: ${m.format}", color = KccTextMuted, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(
    rank: Int,
    title: String,
    subtitle: String,
    primaryMetric: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C36)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (rank <= 3) KccGoldPrimary else Color(0xFF24242E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$rank",
                        color = if (rank <= 3) Color.Black else KccTextWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, color = KccTextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = KccTextSecondary, fontSize = 11.sp)
                }
            }

            Text(
                text = primaryMetric,
                color = KccGoldLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
