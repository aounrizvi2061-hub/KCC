package com.example.ui.scoring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.scoring.CricketCalculationEngine
import com.example.ui.CricketViewModel
import com.example.ui.Screen
import com.example.ui.theme.KccBlackBackground
import com.example.ui.theme.KccBoundaryGreen
import com.example.ui.theme.KccExtraAmber
import com.example.ui.theme.KccGoldDark
import com.example.ui.theme.KccGoldLight
import com.example.ui.theme.KccGoldPrimary
import com.example.ui.theme.KccSixPurple
import com.example.ui.theme.KccSurfaceDark
import com.example.ui.theme.KccSurfaceVariantDark
import com.example.ui.theme.KccTextMuted
import com.example.ui.theme.KccTextSecondary
import com.example.ui.theme.KccTextWhite
import com.example.ui.theme.KccWicketRed

@Composable
fun LiveScoringScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val match by viewModel.selectedMatch.collectAsState()
    val deliveries by viewModel.activeDeliveries.collectAsState()
    val allPlayers by viewModel.players.collectAsState()

    val currentMatch = match ?: return

    val playersMap = remember(allPlayers) { allPlayers.associateBy { it.id } }

    val inningsScorecard = remember(currentMatch, deliveries, playersMap) {
        CricketCalculationEngine.calculateInningsScorecard(
            inningsNumber = currentMatch.currentInningsIndex.toInt(),
            match = currentMatch,
            deliveries = deliveries,
            allPlayers = playersMap
        )
    }

    // Recent deliveries in this over
    val recentBalls = remember(deliveries, currentMatch.currentInningsIndex) {
        deliveries
            .filter { it.inningsNumber == currentMatch.currentInningsIndex }
            .sortedBy { it.createdAt?.seconds ?: 0L }
            .takeLast(6)
    }

    var showWicketDialog by remember { mutableStateOf(false) }
    var showBowlerDialog by remember { mutableStateOf(false) }
    var showStrikerDialog by remember { mutableStateOf(false) }
    var showExtraDialog by remember { mutableStateOf(false) }
    var showEndInningsDialog by remember { mutableStateOf(false) }
    var showDeclareDialog by remember { mutableStateOf(false) }

    val striker = playersMap[currentMatch.currentStrikerId]?.name ?: "Striker"
    val nonStriker = playersMap[currentMatch.currentNonStrikerId]?.name ?: "Non-Striker"
    val bowler = playersMap[currentMatch.currentBowlerId]?.name ?: "Bowler"

    val strikerScore = inningsScorecard.batters.find { it.playerId == currentMatch.currentStrikerId }
    val nonStrikerScore = inningsScorecard.batters.find { it.playerId == currentMatch.currentNonStrikerId }
    val bowlerFigure = inningsScorecard.bowlers.find { it.playerId == currentMatch.currentBowlerId }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KccBlackBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.testTag("scoring_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = KccGoldPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${currentMatch.teamAShortName} vs ${currentMatch.teamBShortName}",
                        color = KccTextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Innings ${currentMatch.currentInningsIndex} (${currentMatch.format})",
                        color = KccGoldLight,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.undoLastDelivery() },
                    modifier = Modifier.testTag("scoring_undo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = KccGoldPrimary
                    )
                }
            }

            // Big Scoreboard Display Card
            Card(
                colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, KccGoldPrimary.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = inningsScorecard.battingTeamName.uppercase(),
                        color = KccGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${inningsScorecard.totalRuns}/${inningsScorecard.wickets}",
                            color = KccTextWhite,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${inningsScorecard.oversString} ov)",
                            color = KccTextSecondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = "CRR: ${inningsScorecard.runRate}",
                            color = KccGoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Extras: ${inningsScorecard.extras.total} (w ${inningsScorecard.extras.wides}, nb ${inningsScorecard.extras.noBalls})",
                            color = KccTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Striker & Non-Striker Cards
            Card(
                colors = CardDefaults.cardColors(containerColor = KccSurfaceVariantDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E38)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Striker Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStrikerDialog = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(KccGoldPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$striker *",
                                color = KccGoldLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${strikerScore?.runs ?: 0} (${strikerScore?.balls ?: 0}b, ${strikerScore?.fours ?: 0}x4, ${strikerScore?.sixes ?: 0}x6)",
                            color = KccTextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Non-Striker Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF555562))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = nonStriker,
                                color = KccTextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "${nonStrikerScore?.runs ?: 0} (${nonStrikerScore?.balls ?: 0}b)",
                            color = KccTextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(
                            onClick = { viewModel.swapStrike() },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KccGoldPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("swap_strike_button")
                        ) {
                            Icon(Icons.Default.ChangeCircle, contentDescription = null, tint = KccGoldPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Swap Strike", color = KccGoldLight, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Current Bowler Card
            Card(
                colors = CardDefaults.cardColors(containerColor = KccSurfaceVariantDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E38)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsCricket, contentDescription = null, tint = KccGoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = bowler,
                                color = KccTextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${bowlerFigure?.oversString ?: "0.0"} ov • ${bowlerFigure?.maidens ?: 0} M • ${bowlerFigure?.runsConceded ?: 0} R • ${bowlerFigure?.wickets ?: 0} W",
                            color = KccTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showBowlerDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444452)),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("change_bowler_button")
                    ) {
                        Text("Change", color = KccTextWhite, fontSize = 11.sp)
                    }
                }
            }

            // Recent Deliveries Strip
            Column {
                Text(
                    text = "THIS OVER",
                    color = KccTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (recentBalls.isEmpty()) {
                        item {
                            Text("Start of over", color = KccTextMuted, fontSize = 12.sp)
                        }
                    } else {
                        items(recentBalls) { del ->
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
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Fast One-Hand Scoring Keypad
            Text(
                text = "FAST SCORING KEYPAD",
                color = KccGoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            // Primary Runs Row: 0, 1, 2, 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScorepadButton(label = "0 (Dot)", onClick = {
                    viewModel.recordDelivery(0, 0, "NONE", false, "", "", "", "", "Dot ball")
                }, modifier = Modifier.weight(1f))

                ScorepadButton(label = "1", onClick = {
                    viewModel.recordDelivery(1, 0, "NONE", false, "", "", "", "", "Single taken")
                }, modifier = Modifier.weight(1f))

                ScorepadButton(label = "2", onClick = {
                    viewModel.recordDelivery(2, 0, "NONE", false, "", "", "", "", "Two runs scored")
                }, modifier = Modifier.weight(1f))

                ScorepadButton(label = "3", onClick = {
                    viewModel.recordDelivery(3, 0, "NONE", false, "", "", "", "", "Three runs taken")
                }, modifier = Modifier.weight(1f))
            }

            // Big Events Row: 4, 6, 5, WICKET
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScorepadButton(
                    label = "4 FOUR",
                    highlightColor = KccBoundaryGreen,
                    onClick = {
                        viewModel.recordDelivery(4, 0, "NONE", false, "", "", "", "", "FOUR! Driven to the fence!")
                    },
                    modifier = Modifier.weight(1f)
                )

                ScorepadButton(
                    label = "6 SIX",
                    highlightColor = KccSixPurple,
                    onClick = {
                        viewModel.recordDelivery(6, 0, "NONE", false, "", "", "", "", "SIX! Launched over the ropes!")
                    },
                    modifier = Modifier.weight(1f)
                )

                ScorepadButton(
                    label = "5",
                    onClick = {
                        viewModel.recordDelivery(5, 0, "NONE", false, "", "", "", "", "5 runs with overthrows")
                    },
                    modifier = Modifier.weight(0.7f)
                )

                ScorepadButton(
                    label = "WICKET",
                    highlightColor = KccWicketRed,
                    onClick = { showWicketDialog = true },
                    modifier = Modifier.weight(1.3f)
                )
            }

            // Extras Row: WIDE, NO-BALL, BYE, LEG-BYE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScorepadButton(
                    label = "WD (+1)",
                    highlightColor = KccExtraAmber,
                    onClick = {
                        viewModel.recordDelivery(0, 1, "WIDE", false, "", "", "", "", "Wide ball")
                    },
                    modifier = Modifier.weight(1f)
                )

                ScorepadButton(
                    label = "NB (+1)",
                    highlightColor = KccExtraAmber,
                    onClick = {
                        viewModel.recordDelivery(0, 1, "NO_BALL", false, "", "", "", "", "No ball")
                    },
                    modifier = Modifier.weight(1f)
                )

                ScorepadButton(
                    label = "BYE",
                    onClick = {
                        viewModel.recordDelivery(0, 1, "BYE", false, "", "", "", "", "Bye")
                    },
                    modifier = Modifier.weight(1f)
                )

                ScorepadButton(
                    label = "LEG-BYE",
                    onClick = {
                        viewModel.recordDelivery(0, 1, "LEG_BYE", false, "", "", "", "", "Leg bye")
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Innings Control Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeclareDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444455)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text("Declare", color = KccGoldLight, fontSize = 12.sp)
                }

                Button(
                    onClick = { showEndInningsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282834)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text("End Innings", color = KccTextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // --- Wicket Modal Dialog ---
    if (showWicketDialog) {
        var selectedWicketType by remember { mutableStateOf("BOWLED") }
        var selectedDismissedPlayer by remember { mutableStateOf(currentMatch.currentStrikerId) }
        var selectedNextBatter by remember { mutableStateOf("") }
        val wicketTypes = listOf("BOWLED", "CAUGHT", "LBW", "RUN_OUT", "STUMPED", "HIT_WICKET", "RETIRED_HURT", "RETIRED_OUT")

        AlertDialog(
            onDismissRequest = { showWicketDialog = false },
            containerColor = KccSurfaceDark,
            title = {
                Text("Record Fall of Wicket", color = KccWicketRed, fontWeight = FontWeight.Black)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Dismissal Type:", color = KccTextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    wicketTypes.chunked(3).forEach { rowTypes ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowTypes.forEach { type ->
                                val isSel = selectedWicketType == type
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) KccWicketRed else Color(0xFF22222A))
                                        .clickable { selectedWicketType = type }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = type.replace("_", " "),
                                        color = if (isSel) Color.White else KccTextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Dismissed Batter:", color = KccTextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val isStriker = selectedDismissedPlayer == currentMatch.currentStrikerId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isStriker) KccGoldPrimary else Color(0xFF22222A))
                                .clickable { selectedDismissedPlayer = currentMatch.currentStrikerId }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(striker, color = if (isStriker) Color.Black else KccTextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        val isNonStriker = selectedDismissedPlayer == currentMatch.currentNonStrikerId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isNonStriker) KccGoldPrimary else Color(0xFF22222A))
                                .clickable { selectedDismissedPlayer = currentMatch.currentNonStrikerId }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(nonStriker, color = if (isNonStriker) Color.Black else KccTextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Select Next Batter:", color = KccTextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    val availableBatters = allPlayers.filter { it.id != currentMatch.currentStrikerId && it.id != currentMatch.currentNonStrikerId }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(availableBatters) { p ->
                            val isChosen = selectedNextBatter == p.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) Color(0xFF10B981) else Color(0xFF22222A))
                                    .clickable { selectedNextBatter = p.id }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(p.name, color = if (isChosen) Color.White else KccTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordDelivery(
                            runsBat = 0,
                            extraRuns = 0,
                            extraType = "NONE",
                            isWicket = true,
                            wicketType = selectedWicketType,
                            dismissedPlayerId = selectedDismissedPlayer,
                            nextBatterId = selectedNextBatter,
                            fielderId = "",
                            commentaryText = "OUT! Wicket fell - $selectedWicketType"
                        )
                        showWicketDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KccWicketRed)
                ) {
                    Text("Confirm Wicket", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWicketDialog = false }) {
                    Text("Cancel", color = KccTextSecondary)
                }
            }
        )
    }

    // --- Change Bowler Dialog ---
    if (showBowlerDialog) {
        AlertDialog(
            onDismissRequest = { showBowlerDialog = false },
            containerColor = KccSurfaceDark,
            title = { Text("Select Bowler", color = KccGoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    allPlayers.forEach { p ->
                        if (p.id != currentMatch.currentStrikerId && p.id != currentMatch.currentNonStrikerId) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.changeBowler(p.id)
                                        showBowlerDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.SportsCricket, contentDescription = null, tint = KccGoldLight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(p.name, color = KccTextWhite, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("(${p.role})", color = KccTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBowlerDialog = false }) {
                    Text("Close", color = KccTextSecondary)
                }
            }
        )
    }

    // --- End Innings Dialog ---
    if (showEndInningsDialog) {
        AlertDialog(
            onDismissRequest = { showEndInningsDialog = false },
            containerColor = KccSurfaceDark,
            title = { Text("End Current Innings?", color = KccTextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text("This will close Innings ${currentMatch.currentInningsIndex} and transition to the next innings or finalize the match.", color = KccTextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.endInnings()
                        showEndInningsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KccGoldPrimary, contentColor = Color(0xFF171302))
                ) {
                    Text("Confirm End Innings", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndInningsDialog = false }) {
                    Text("Cancel", color = KccTextSecondary)
                }
            }
        )
    }

    // --- Declare Dialog ---
    if (showDeclareDialog) {
        AlertDialog(
            onDismissRequest = { showDeclareDialog = false },
            containerColor = KccSurfaceDark,
            title = { Text("Declare Innings?", color = KccGoldLight, fontWeight = FontWeight.Bold) },
            text = {
                Text("Declare this innings closed and hand over batting to the opposition.", color = KccTextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.declareInnings()
                        showDeclareDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KccGoldPrimary, contentColor = Color(0xFF171302))
                ) {
                    Text("Declare", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeclareDialog = false }) {
                    Text("Cancel", color = KccTextSecondary)
                }
            }
        )
    }
}

@Composable
fun ScorepadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlightColor: Color? = null
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = highlightColor ?: Color(0xFF22222B),
            contentColor = if (highlightColor != null && highlightColor == KccGoldPrimary) Color.Black else KccTextWhite
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .height(52.dp)
            .testTag("score_btn_${label.replace(" ", "_")}")
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BallBubble(
    text: String,
    isWicket: Boolean = false,
    isBoundary: Boolean = false,
    isSix: Boolean = false,
    isExtra: Boolean = false
) {
    val bgColor = when {
        isWicket -> KccWicketRed
        isSix -> KccSixPurple
        isBoundary -> KccBoundaryGreen
        isExtra -> KccExtraAmber
        else -> Color(0xFF262630)
    }

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
