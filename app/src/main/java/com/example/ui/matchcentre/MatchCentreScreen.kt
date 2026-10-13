package com.example.ui.matchcentre

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.scoring.CricketCalculationEngine
import com.example.ui.CricketViewModel
import com.example.ui.Screen
import com.example.ui.UserRole
import com.example.ui.scoring.BallBubble
import com.example.ui.theme.KccBlackBackground
import com.example.ui.theme.KccGoldDark
import com.example.ui.theme.KccGoldLight
import com.example.ui.theme.KccGoldPrimary
import com.example.ui.theme.KccSurfaceDark
import com.example.ui.theme.KccSurfaceVariantDark
import com.example.ui.theme.KccTextMuted
import com.example.ui.theme.KccTextSecondary
import com.example.ui.theme.KccTextWhite
import com.example.ui.theme.KccWicketRed

@Composable
fun MatchCentreScreen(
    matchId: String,
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.matches.collectAsState()
    val deliveries by viewModel.activeDeliveries.collectAsState()
    val allPlayers by viewModel.players.collectAsState()
    val role by viewModel.userRole.collectAsState()

    val match = matches.find { it.id == matchId } ?: viewModel.selectedMatch.collectAsState().value ?: return

    val playersMap = remember(allPlayers) { allPlayers.associateBy { it.id } }

    val tabs = listOf(
        "Overview",
        "Live Score",
        "Scorecard",
        "Commentary",
        "Partnerships",
        "Fall of Wickets",
        "Playing XI",
        "Summary"
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedInningsNumber by remember { mutableIntStateOf(match.currentInningsIndex.toInt().coerceAtLeast(1)) }

    val currentInningsScorecard = remember(match, deliveries, playersMap, selectedInningsNumber) {
        CricketCalculationEngine.calculateInningsScorecard(
            inningsNumber = selectedInningsNumber,
            match = match,
            deliveries = deliveries,
            allPlayers = playersMap
        )
    }

    val testContext = remember(match, deliveries, playersMap) {
        val inn1 = CricketCalculationEngine.calculateInningsScorecard(1, match, deliveries, playersMap)
        val inn2 = CricketCalculationEngine.calculateInningsScorecard(2, match, deliveries, playersMap)
        val inn3 = CricketCalculationEngine.calculateInningsScorecard(3, match, deliveries, playersMap)
        val inn4 = CricketCalculationEngine.calculateInningsScorecard(4, match, deliveries, playersMap)
        CricketCalculationEngine.computeTestMatchContext(match, inn1, inn2, inn3, inn4)
    }

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = KccGoldPrimary)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "KCC MATCH CENTRE",
                        color = KccGoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = match.title,
                        color = KccTextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (match.status.uppercase() == "LIVE" && role != UserRole.VIEWER) {
                    IconButton(onClick = {
                        viewModel.selectMatch(match)
                        viewModel.navigateTo(Screen.LiveScoring)
                    }) {
                        Icon(Icons.Default.SportsCricket, contentDescription = "Score", tint = KccGoldLight)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF141419),
                contentColor = KccGoldPrimary,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = KccGoldPrimary,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) KccGoldLight else KccTextSecondary,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> OverviewTab(match, currentInningsScorecard, testContext)
                    1 -> LiveScoreTab(match, currentInningsScorecard, deliveries, playersMap, viewModel, role)
                    2 -> ScorecardTab(
                        match = match,
                        scorecard = currentInningsScorecard,
                        selectedInnings = selectedInningsNumber,
                        onSelectInnings = { selectedInningsNumber = it }
                    )
                    3 -> CommentaryTab(match, deliveries, playersMap)
                    4 -> PartnershipsTab(currentInningsScorecard)
                    5 -> FallOfWicketsTab(currentInningsScorecard)
                    6 -> PlayingXITab(match, allPlayers)
                    7 -> MatchSummaryTab(match, currentInningsScorecard, testContext)
                }
            }
        }
    }
}

@Composable
fun OverviewTab(
    match: com.example.data.model.Match,
    scorecard: com.example.data.scoring.InningsScorecard,
    testContext: com.example.data.scoring.TestMatchContext
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C36)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "STATUS: ${match.status}", color = if (match.status.uppercase() == "LIVE") KccWicketRed else KccGoldPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text(text = match.format, color = KccGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "${match.teamAName} vs ${match.teamBName}", color = KccTextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Venue: ${match.venue}", color = KccTextSecondary, fontSize = 13.sp)
                    Text(text = "Toss: ${match.tossWinnerTeamId} elected to ${match.tossDecision}", color = KccTextSecondary, fontSize = 13.sp)
                    if (match.resultText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = match.resultText, color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        if (match.format.uppercase() == "TEST") {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B22)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, KccGoldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TEST MATCH SESSION & LEAD DETAILS", color = KccGoldPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(testContext.leadOrTrailText, color = KccTextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Day: ${match.day} • Session: ${match.session}", color = KccTextSecondary, fontSize = 13.sp)
                        if (testContext.followOnEligible) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Follow-on Eligible: Deficit >= 200 runs", color = KccGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveScoreTab(
    match: com.example.data.model.Match,
    scorecard: com.example.data.scoring.InningsScorecard,
    deliveries: List<com.example.data.model.Delivery>,
    playersMap: Map<String, com.example.data.model.Player>,
    viewModel: CricketViewModel,
    role: UserRole
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, KccGoldPrimary.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(scorecard.battingTeamName, color = KccGoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${scorecard.totalRuns}/${scorecard.wickets}", color = KccTextWhite, fontSize = 36.sp, fontWeight = FontWeight.Black)
                    Text("(${scorecard.oversString} Overs) • CRR: ${scorecard.runRate}", color = KccTextSecondary, fontSize = 13.sp)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CURRENT BATSMEN", color = KccGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (match.status.uppercase() == "LIVE" && role != UserRole.VIEWER) {
                    Text("Open Scorepad ->", color = KccGoldLight, fontSize = 12.sp, modifier = Modifier.clickable {
                        viewModel.selectMatch(match)
                        viewModel.navigateTo(Screen.LiveScoring)
                    })
                }
            }
        }

        items(scorecard.batters.take(2)) { b ->
            Card(
                colors = CardDefaults.cardColors(containerColor = KccSurfaceVariantDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(b.playerName, color = KccTextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${b.runs} (${b.balls}) • ${b.fours}x4 ${b.sixes}x6 • SR: ${b.strikeRate}", color = KccGoldLight, fontSize = 13.sp)
                }
            }
        }

        item {
            Text("CURRENT BOWLER", color = KccGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        items(scorecard.bowlers.take(1)) { bowl ->
            Card(
                colors = CardDefaults.cardColors(containerColor = KccSurfaceVariantDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(bowl.playerName, color = KccTextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${bowl.oversString} ov, ${bowl.runsConceded} runs, ${bowl.wickets} wkts (Econ ${bowl.economyRate})", color = KccTextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ScorecardTab(
    match: com.example.data.model.Match,
    scorecard: com.example.data.scoring.InningsScorecard,
    selectedInnings: Int,
    onSelectInnings: (Int) -> Unit
) {
    val totalInningsCount = if (match.format.uppercase() == "TEST") 4 else 2

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Innings Selector
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..totalInningsCount).forEach { inn ->
                    val isSelected = selectedInnings == inn
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) KccGoldPrimary else Color(0xFF22222A))
                            .clickable { onSelectInnings(inn) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Innings $inn",
                            color = if (isSelected) Color.Black else KccTextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Batting Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C35)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "BATTING — ${scorecard.battingTeamName}",
                        color = KccGoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Batter", color = KccTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(2.5f))
                        Text("R", color = KccTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(0.7f))
                        Text("B", color = KccTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(0.7f))
                        Text("4s", color = KccTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(0.7f))
                        Text("6s", color = KccTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(0.7f))
                        Text("SR", color = KccTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(color = Color(0xFF2A2A35), modifier = Modifier.padding(vertical = 6.dp))

                    scorecard.batters.forEach { b ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(2.5f)) {
                                Text(b.playerName, color = KccTextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(b.dismissalText, color = KccTextMuted, fontSize = 10.sp)
                            }
                            Text("${b.runs}", color = KccGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(0.7f))
                            Text("${b.balls}", color = KccTextSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.7f))
                            Text("${b.fours}", color = KccTextSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.7f))
                            Text("${b.sixes}", color = KccTextSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.7f))
                            Text("${b.strikeRate}", color = KccTextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2A2A35), modifier = Modifier.padding(vertical = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL (${scorecard.oversString} Ov, RR ${scorecard.runRate})", color = KccTextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("${scorecard.totalRuns}/${scorecard.wickets}", color = KccGoldPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                }
            }
        }

        // Bowling Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C35)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "BOWLING — ${scorecard.bowlingTeamName}",
                        color = KccGoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bowler", color = KccTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(2.5f))
                        Text("O", color = KccTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(0.7f))
                        Text("M", color = KccTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(0.7f))
                        Text("R", color = KccTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(0.7f))
                        Text("W", color = KccTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(0.7f))
                        Text("Econ", color = KccTextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(color = Color(0xFF2A2A35), modifier = Modifier.padding(vertical = 6.dp))

                    scorecard.bowlers.forEach { bowl ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(bowl.playerName, color = KccTextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.5f))
                            Text(bowl.oversString, color = KccTextSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.7f))
                            Text("${bowl.maidens}", color = KccTextSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.7f))
                            Text("${bowl.runsConceded}", color = KccTextSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.7f))
                            Text("${bowl.wickets}", color = KccGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(0.7f))
                            Text("${bowl.economyRate}", color = KccTextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentaryTab(
    match: com.example.data.model.Match,
    deliveries: List<com.example.data.model.Delivery>,
    playersMap: Map<String, com.example.data.model.Player>
) {
    val sorted = deliveries.sortedByDescending { it.createdAt?.seconds ?: 0L }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (sorted.isEmpty()) {
            item {
                Text("No deliveries logged yet.", color = KccTextSecondary, fontSize = 13.sp)
            }
        } else {
            items(sorted) { del ->
                val bowlerName = playersMap[del.bowlerId]?.name ?: "Bowler"
                val strikerName = playersMap[del.strikerId]?.name ?: "Batter"
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF17171E)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        BallBubble(
                            text = when {
                                del.isWicket -> "W"
                                del.extraType.uppercase() == "WIDE" -> "Wd"
                                del.extraType.uppercase() == "NO_BALL" -> "Nb"
                                del.runsBat == 4L -> "4"
                                del.runsBat == 6L -> "6"
                                del.runsBat == 0L -> "•"
                                else -> "${del.runsBat}"
                            },
                            isWicket = del.isWicket,
                            isBoundary = del.runsBat == 4L,
                            isSix = del.runsBat == 6L,
                            isExtra = del.extraType.uppercase() != "NONE"
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Over ${del.overNumber}.${del.ballInOver} • $bowlerName to $strikerName",
                                color = KccGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = del.commentary,
                                color = KccTextWhite,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PartnershipsTab(scorecard: com.example.data.scoring.InningsScorecard) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (scorecard.currentPartnership != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, KccGoldPrimary.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("CURRENT PARTNERSHIP", color = KccGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        val p = scorecard.currentPartnership
                        Text("${p.totalRuns} runs (${p.totalBalls} balls)", color = KccTextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${p.batter1Name} (${p.batter1Runs}) & ${p.batter2Name} (${p.batter2Runs})", color = KccTextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }

        items(scorecard.partnerships) { p ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181820)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Wicket ${p.wicketNumber} Partnership", color = KccGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${p.batter1Name} & ${p.batter2Name}", color = KccTextWhite, fontSize = 13.sp)
                    }
                    Text("${p.totalRuns} (${p.totalBalls}b)", color = KccGoldPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun FallOfWicketsTab(scorecard: com.example.data.scoring.InningsScorecard) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (scorecard.fallOfWickets.isEmpty()) {
            item {
                Text("No wickets have fallen in this innings.", color = KccTextSecondary, fontSize = 13.sp)
            }
        } else {
            items(scorecard.fallOfWickets) { fow ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181820)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(KccWicketRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${fow.wicketNumber}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(fow.batterName, color = KccTextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("at ${fow.oversAtFall} overs", color = KccTextSecondary, fontSize = 11.sp)
                            }
                        }

                        Text("${fow.scoreAtFall} runs", color = KccGoldPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PlayingXITab(
    match: com.example.data.model.Match,
    players: List<com.example.data.model.Player>
) {
    val teamAPlayers = players.filter { it.teamId == match.teamAId }
    val teamBPlayers = players.filter { it.teamId == match.teamBId }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("SQUAD: ${match.teamAName}", color = KccGoldPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
        items(teamAPlayers) { p ->
            SquadPlayerItem(p)
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text("SQUAD: ${match.teamBName}", color = KccGoldPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
        items(teamBPlayers) { p ->
            SquadPlayerItem(p)
        }
    }
}

@Composable
fun SquadPlayerItem(player: com.example.data.model.Player) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF17171F)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF282834)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${player.jerseyNumber}", color = KccGoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(player.name, color = KccTextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Text(player.role, color = KccTextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun MatchSummaryTab(
    match: com.example.data.model.Match,
    scorecard: com.example.data.scoring.InningsScorecard,
    testContext: com.example.data.scoring.TestMatchContext
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            // Shareable Match Presentation Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111116)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, KccGoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shareable_summary_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.kcc_logo),
                        contentDescription = "KCC Logo",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(1.dp, KccGoldPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("KCC CRICKET — OFFICIAL RESULT", color = KccGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(match.title, color = KccTextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(match.teamAShortName, color = KccGoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text(match.teamAName, color = KccTextSecondary, fontSize = 11.sp)
                        }

                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = KccGoldLight, modifier = Modifier.size(36.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(match.teamBShortName, color = KccGoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text(match.teamBName, color = KccTextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF222818))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (match.resultText.isNotBlank()) match.resultText else "Match In Progress",
                            color = Color(0xFF86EFAC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Venue: ${match.venue} • Format: ${match.format}", color = KccTextMuted, fontSize = 11.sp)
                }
            }
        }
    }
}
