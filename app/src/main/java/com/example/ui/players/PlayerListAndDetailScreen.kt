package com.example.ui.players

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Player
import com.example.data.scoring.CricketCalculationEngine
import com.example.data.scoring.PlayerStats
import com.example.ui.CricketViewModel
import com.example.ui.match.outlinedTextFieldColors
import com.example.ui.theme.KccBlackBackground
import com.example.ui.theme.KccGoldDark
import com.example.ui.theme.KccGoldLight
import com.example.ui.theme.KccGoldPrimary
import com.example.ui.theme.KccSurfaceDark
import com.example.ui.theme.KccSurfaceVariantDark
import com.example.ui.theme.KccTextMuted
import com.example.ui.theme.KccTextSecondary
import com.example.ui.theme.KccTextWhite

@Composable
fun PlayerListAndDetailScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val players by viewModel.players.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val deliveries by viewModel.activeDeliveries.collectAsState()

    val teamsMap = remember(teams) { teams.associateBy { it.id } }

    val careerStatsList = remember(players, deliveries) {
        CricketCalculationEngine.computeCareerStatistics(players, deliveries)
    }
    val statsMap = remember(careerStatsList) { careerStatsList.associateBy { it.playerId } }

    var selectedRoleFilter by remember { mutableStateOf("ALL") }
    var selectedPlayerForDetail by remember { mutableStateOf<Player?>(null) }
    var showAddPlayerDialog by remember { mutableStateOf(false) }

    val roles = listOf("ALL", "Batter", "Bowler", "All-Rounder", "Wicketkeeper")

    val filteredPlayers = if (selectedRoleFilter == "ALL") {
        players
    } else {
        players.filter { it.role.equals(selectedRoleFilter, ignoreCase = true) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KccBlackBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPlayerDialog = true },
                containerColor = KccGoldPrimary,
                contentColor = Color(0xFF171302),
                modifier = Modifier.testTag("add_player_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Player")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = KccGoldPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "PLAYER PROFILES & STATS",
                        color = KccGoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "KCC Player Database (${players.size})",
                        color = KccTextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Role Filters
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(roles) { r ->
                    val isSel = selectedRoleFilter == r
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) KccGoldPrimary else Color(0xFF22222A))
                            .clickable { selectedRoleFilter = r }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = r,
                            color = if (isSel) Color.Black else KccTextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredPlayers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No players found in this category.", color = KccTextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredPlayers) { player ->
                        val pStats = statsMap[player.id]
                        val teamName = teamsMap[player.teamId]?.name ?: "KCC Pool"
                        Card(
                            colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C36)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPlayerForDetail = player }
                                .testTag("player_item_${player.id}")
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
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF22222C))
                                            .border(1.5.dp, KccGoldPrimary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (player.jerseyNumber > 0) "#${player.jerseyNumber}" else player.name.take(2).uppercase(),
                                            color = KccGoldLight,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(player.name, color = KccTextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text("${player.role} • $teamName", color = KccTextSecondary, fontSize = 12.sp)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${pStats?.totalRuns ?: 0} Runs",
                                        color = KccGoldLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${pStats?.wickets ?: 0} Wkts",
                                        color = KccTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Player Detail Modal Dialog ---
    if (selectedPlayerForDetail != null) {
        val p = selectedPlayerForDetail!!
        val stats = statsMap[p.id]
        val teamName = teamsMap[p.teamId]?.name ?: "KCC Squad"

        AlertDialog(
            onDismissRequest = { selectedPlayerForDetail = null },
            containerColor = KccSurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(KccGoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (p.jerseyNumber > 0) "#${p.jerseyNumber}" else p.name.take(2),
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(p.name, color = KccTextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${p.role} • $teamName", color = KccGoldLight, fontSize = 12.sp)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Styles: ${p.battingStyle} | ${p.bowlingStyle}", color = KccTextSecondary, fontSize = 12.sp)

                    HorizontalDivider(color = Color(0xFF2E2E38))

                    Text("BATTING CAREER STATS", color = KccGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatPill("Mat", "${stats?.matches ?: 0}")
                        StatPill("Inns", "${stats?.inningsBatted ?: 0}")
                        StatPill("Runs", "${stats?.totalRuns ?: 0}")
                        StatPill("HS", "${stats?.highestScore ?: 0}")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatPill("Avg", "${stats?.battingAverage ?: 0.0}")
                        StatPill("SR", "${stats?.battingStrikeRate ?: 0.0}")
                        StatPill("50s", "${stats?.fifties ?: 0}")
                        StatPill("100s", "${stats?.hundreds ?: 0}")
                    }

                    HorizontalDivider(color = Color(0xFF2E2E38))

                    Text("BOWLING CAREER STATS", color = KccGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatPill("Wkts", "${stats?.wickets ?: 0}")
                        StatPill("Econ", "${stats?.bowlingEconomy ?: 0.0}")
                        StatPill("Avg", "${stats?.bowlingAverage ?: 0.0}")
                        StatPill("BBI", stats?.bestBowlingFigures ?: "0/0")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedPlayerForDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = KccGoldPrimary, contentColor = Color(0xFF171302))
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.deletePlayer(p.id)
                    selectedPlayerForDetail = null
                }) {
                    Text("Delete Player", color = Color(0xFFEF4444))
                }
            }
        )
    }

    // --- Add Player Dialog ---
    if (showAddPlayerDialog) {
        var name by remember { mutableStateOf("") }
        var jersey by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("Batter") }
        var battingStyle by remember { mutableStateOf("Right Hand Bat") }
        var bowlingStyle by remember { mutableStateOf("Right-arm Fast") }
        var selectedTeamId by remember { mutableStateOf(teams.firstOrNull()?.id ?: "") }

        AlertDialog(
            onDismissRequest = { showAddPlayerDialog = false },
            containerColor = KccSurfaceDark,
            title = { Text("Register Player", color = KccGoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Player Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = jersey,
                        onValueChange = { jersey = it },
                        label = { Text("Jersey Number") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors()
                    )
                    Text("Role:", color = KccTextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("Batter", "Bowler", "All-Rounder", "Wicketkeeper")) { r ->
                            val isSel = role == r
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) KccGoldPrimary else Color(0xFF22222A))
                                    .clickable { role = r }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(r, color = if (isSel) Color.Black else KccTextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("Team Assignment:", color = KccTextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(teams) { t ->
                            val isSel = selectedTeamId == t.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) KccGoldPrimary else Color(0xFF22222A))
                                    .clickable { selectedTeamId = t.id }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(t.name, color = if (isSel) Color.Black else KccTextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.createPlayer(
                                name = name,
                                jersey = jersey.toLongOrNull() ?: 0L,
                                role = role,
                                battingStyle = battingStyle,
                                bowlingStyle = bowlingStyle,
                                teamId = selectedTeamId
                            )
                            showAddPlayerDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KccGoldPrimary, contentColor = Color(0xFF171302))
                ) {
                    Text("Save Player", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlayerDialog = false }) {
                    Text("Cancel", color = KccTextSecondary)
                }
            }
        )
    }
}

@Composable
fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = KccTextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(label, color = KccTextSecondary, fontSize = 10.sp)
    }
}
