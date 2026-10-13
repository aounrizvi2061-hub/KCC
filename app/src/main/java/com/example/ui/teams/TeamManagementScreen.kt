package com.example.ui.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import com.example.ui.CricketViewModel
import com.example.ui.match.outlinedTextFieldColors
import com.example.ui.theme.KccBlackBackground
import com.example.ui.theme.KccGoldLight
import com.example.ui.theme.KccGoldPrimary
import com.example.ui.theme.KccSurfaceDark
import com.example.ui.theme.KccTextMuted
import com.example.ui.theme.KccTextSecondary
import com.example.ui.theme.KccTextWhite
import com.example.ui.theme.KccWicketRed

@Composable
fun TeamManagementScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val teams by viewModel.teams.collectAsState()
    val players by viewModel.players.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var teamName by remember { mutableStateOf("") }
    var teamShortName by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KccBlackBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = KccGoldPrimary,
                contentColor = Color(0xFF171302),
                modifier = Modifier.testTag("add_team_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Team")
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
                        text = "TEAM ROSTERS",
                        color = KccGoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "KCC Registered Teams (${teams.size})",
                        color = KccTextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (teams.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No teams registered. Tap + to add a team.", color = KccTextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(teams) { team ->
                        val teamPlayers = players.filter { it.teamId == team.id }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2C36)),
                            modifier = Modifier.fillMaxWidth().testTag("team_card_${team.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF22222A))
                                                .border(1.dp, KccGoldPrimary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(team.shortName, color = KccGoldLight, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(team.name, color = KccTextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            Text("${teamPlayers.size} Registered Players", color = KccTextSecondary, fontSize = 12.sp)
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteTeam(team.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF71717A))
                                    }
                                }

                                if (teamPlayers.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Players: " + teamPlayers.take(4).joinToString(", ") { it.name } + if (teamPlayers.size > 4) "..." else "", color = KccTextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = KccSurfaceDark,
            title = { Text("Create New Team", color = KccGoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Team Name (e.g. KCC Knights)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors()
                    )
                    OutlinedTextField(
                        value = teamShortName,
                        onValueChange = { teamShortName = it },
                        label = { Text("Abbreviation (e.g. KCC)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teamName.isNotBlank() && teamShortName.isNotBlank()) {
                            viewModel.createTeam(teamName, teamShortName)
                            teamName = ""
                            teamShortName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KccGoldPrimary, contentColor = Color(0xFF171302))
                ) {
                    Text("Save Team", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = KccTextSecondary)
                }
            }
        )
    }
}
