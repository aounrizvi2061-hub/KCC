package com.example.ui.match

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.ui.CricketViewModel
import com.example.ui.Screen
import com.example.ui.theme.KccBlackBackground
import com.example.ui.theme.KccGoldLight
import com.example.ui.theme.KccGoldPrimary
import com.example.ui.theme.KccSurfaceDark
import com.example.ui.theme.KccTextMuted
import com.example.ui.theme.KccTextSecondary
import com.example.ui.theme.KccTextWhite

@Composable
fun CreateMatchScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val teams by viewModel.teams.collectAsState()
    val players by viewModel.players.collectAsState()

    var title by remember { mutableStateOf("KCC Championship Cup") }
    var venue by remember { mutableStateOf("KCC Main Ground, Oval") }
    var matchDate by remember { mutableStateOf("Today, 14:00") }
    var format by remember { mutableStateOf("LIMITED_OVERS") } // LIMITED_OVERS, TEST, CUSTOM
    var overs by remember { mutableLongStateOf(20L) }

    var selectedTeamAId by remember { mutableStateOf(teams.firstOrNull()?.id ?: "") }
    var selectedTeamBId by remember { mutableStateOf(teams.getOrNull(1)?.id ?: "") }

    var tossWinnerId by remember { mutableStateOf(selectedTeamAId) }
    var tossDecision by remember { mutableStateOf("BAT") } // BAT or BOWL
    var status by remember { mutableStateOf("LIVE") } // LIVE or UPCOMING

    val formats = listOf("LIMITED_OVERS", "TEST", "CUSTOM")

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = KccGoldPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "CREATE NEW MATCH",
                        color = KccGoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Match Setup & Configuration",
                        color = KccTextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // General Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C36)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("MATCH DETAILS", color = KccGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Match Title") },
                        modifier = Modifier.fillMaxWidth().testTag("match_title_input"),
                        colors = outlinedTextFieldColors()
                    )

                    OutlinedTextField(
                        value = venue,
                        onValueChange = { venue = it },
                        label = { Text("Venue / Ground") },
                        modifier = Modifier.fillMaxWidth().testTag("match_venue_input"),
                        colors = outlinedTextFieldColors()
                    )

                    OutlinedTextField(
                        value = matchDate,
                        onValueChange = { matchDate = it },
                        label = { Text("Date & Time") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors()
                    )

                    Text("Format", color = KccTextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        formats.forEach { f ->
                            val isSel = format == f
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) KccGoldPrimary else Color(0xFF24242E))
                                    .clickable {
                                        format = f
                                        if (f == "TEST") overs = 90L else if (f == "LIMITED_OVERS") overs = 20L
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = f.replace("_", " "),
                                    color = if (isSel) Color.Black else KccTextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (format != "TEST") {
                        OutlinedTextField(
                            value = overs.toString(),
                            onValueChange = { overs = it.toLongOrNull() ?: 20L },
                            label = { Text("Overs per Innings") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = outlinedTextFieldColors()
                        )
                    }
                }
            }

            // Teams Selection Card
            Card(
                colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C36)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SELECT TEAMS", color = KccGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Text("Team A:", color = KccTextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        teams.forEach { team ->
                            val isSel = selectedTeamAId == team.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) KccGoldPrimary else Color(0xFF24242E))
                                    .clickable {
                                        selectedTeamAId = team.id
                                        tossWinnerId = team.id
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(team.name, color = if (isSel) Color.Black else KccTextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("Team B:", color = KccTextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        teams.filter { it.id != selectedTeamAId }.forEach { team ->
                            val isSel = selectedTeamBId == team.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) KccGoldPrimary else Color(0xFF24242E))
                                    .clickable { selectedTeamBId = team.id }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(team.name, color = if (isSel) Color.Black else KccTextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Toss Card
            Card(
                colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C36)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("TOSS & DECISION", color = KccGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    val teamAName = teams.find { it.id == selectedTeamAId }?.name ?: "Team A"
                    val teamBName = teams.find { it.id == selectedTeamBId }?.name ?: "Team B"

                    Text("Toss Winner:", color = KccTextWhite, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(Pair(selectedTeamAId, teamAName), Pair(selectedTeamBId, teamBName)).forEach { (tId, tName) ->
                            val isSel = tossWinnerId == tId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) KccGoldLight else Color(0xFF24242E))
                                    .clickable { tossWinnerId = tId }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(tName, color = if (isSel) Color.Black else KccTextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("Elected to:", color = KccTextWhite, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("BAT", "BOWL").forEach { dec ->
                            val isSel = tossDecision == dec
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) KccGoldPrimary else Color(0xFF24242E))
                                    .clickable { tossDecision = dec }
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(dec, color = if (isSel) Color.Black else KccTextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Create Button
            Button(
                onClick = {
                    val teamA = teams.find { it.id == selectedTeamAId } ?: teams.firstOrNull()
                    val teamB = teams.find { it.id == selectedTeamBId } ?: teams.getOrNull(1)

                    val teamAPlayers = players.filter { it.teamId == teamA?.id }
                    val teamBPlayers = players.filter { it.teamId == teamB?.id }

                    val defaultStriker = teamAPlayers.firstOrNull()?.id ?: players.firstOrNull()?.id ?: ""
                    val defaultNonStriker = teamAPlayers.getOrNull(1)?.id ?: players.getOrNull(1)?.id ?: ""
                    val defaultBowler = teamBPlayers.firstOrNull()?.id ?: players.getOrNull(2)?.id ?: ""

                    val newMatch = Match(
                        title = title,
                        venue = venue,
                        matchDate = matchDate,
                        format = format,
                        oversPerInnings = overs,
                        teamAId = teamA?.id ?: "",
                        teamAName = teamA?.name ?: "Team A",
                        teamAShortName = teamA?.shortName ?: "TMA",
                        teamBId = teamB?.id ?: "",
                        teamBName = teamB?.name ?: "Team B",
                        teamBShortName = teamB?.shortName ?: "TMB",
                        tossWinnerTeamId = tossWinnerId,
                        tossDecision = tossDecision,
                        status = status,
                        currentInningsIndex = 1,
                        currentStrikerId = defaultStriker,
                        currentNonStrikerId = defaultNonStriker,
                        currentBowlerId = defaultBowler,
                        day = 1,
                        session = 1
                    )

                    viewModel.createMatch(newMatch) { matchId ->
                        viewModel.selectMatch(newMatch.copy(id = matchId))
                        viewModel.navigateTo(Screen.LiveScoring)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KccGoldPrimary, contentColor = Color(0xFF171302)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_create_match_button")
            ) {
                Text("Start Match & Begin Scoring", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = KccTextWhite,
    unfocusedTextColor = KccTextWhite,
    focusedBorderColor = KccGoldPrimary,
    unfocusedBorderColor = Color(0xFF333340),
    focusedLabelColor = KccGoldPrimary,
    unfocusedLabelColor = KccTextSecondary
)
