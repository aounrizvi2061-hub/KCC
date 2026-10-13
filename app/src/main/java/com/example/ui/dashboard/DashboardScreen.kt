package com.example.ui.dashboard

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Match
import com.example.ui.CricketViewModel
import com.example.ui.Screen
import com.example.ui.UserRole
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
fun DashboardScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.matches.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val players by viewModel.players.collectAsState()
    val role by viewModel.userRole.collectAsState()

    val liveMatches = matches.filter { it.status.uppercase() == "LIVE" }
    val upcomingMatches = matches.filter { it.status.uppercase() == "UPCOMING" }
    val completedMatches = matches.filter { it.status.uppercase() == "COMPLETED" }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KccBlackBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.kcc_logo),
                            contentDescription = "KCC Logo",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, KccGoldPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "KCC CRICKET",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = KccGoldPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Official Match Centre",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = KccTextSecondary
                            )
                        }
                    }

                    // Role Switcher Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF22222A))
                            .border(1.dp, KccGoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .clickable {
                                val nextRole = when (role) {
                                    UserRole.SCORER -> UserRole.ADMIN
                                    UserRole.ADMIN -> UserRole.VIEWER
                                    UserRole.VIEWER -> UserRole.SCORER
                                }
                                viewModel.setUserRole(nextRole)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("role_switcher_chip")
                    ) {
                        Text(
                            text = "ROLE: ${role.name}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = KccGoldLight
                        )
                    }
                }
            }

            // Quick Actions Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(Screen.CreateMatch) },
                        colors = ButtonDefaults.buttonColors(containerColor = KccGoldPrimary, contentColor = Color(0xFF161202)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_create_match_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "New Match", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(Screen.Teams) },
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF383842)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_manage_teams_button")
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = KccGoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Teams", color = KccTextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(Screen.Players) },
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF383842)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_manage_players_button")
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = KccGoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Players", color = KccTextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Overview Metric Stats
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282832)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KCC CLUB OVERVIEW",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = KccGoldPrimary,
                                letterSpacing = 1.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.navigateTo(Screen.Statistics) }
                            ) {
                                Icon(Icons.Default.BarChart, contentDescription = null, tint = KccGoldLight, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "All Records",
                                    fontSize = 11.sp,
                                    color = KccGoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatBox(label = "Matches", value = matches.size.toString())
                            StatBox(label = "Live", value = liveMatches.size.toString(), isHighlight = liveMatches.isNotEmpty())
                            StatBox(label = "Teams", value = teams.size.toString())
                            StatBox(label = "Players", value = players.size.toString())
                            StatBox(label = "KCC Wins", value = "${matches.count { it.resultText.contains("KCC", ignoreCase = true) }}")
                        }
                    }
                }
            }

            // Live Matches Section
            item {
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
                                .background(KccWicketRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE MATCHES (${liveMatches.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = KccTextWhite,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            if (liveMatches.isEmpty()) {
                item {
                    EmptyCard(message = "No live match in progress right now. Start a new match to score ball-by-ball.")
                }
            } else {
                items(liveMatches) { match ->
                    MatchCard(
                        match = match,
                        isLive = true,
                        role = role,
                        onOpenMatchCentre = {
                            viewModel.selectMatch(match)
                            viewModel.navigateTo(Screen.MatchCentre(match.id))
                        },
                        onOpenScoreLive = {
                            viewModel.selectMatch(match)
                            viewModel.navigateTo(Screen.LiveScoring)
                        }
                    )
                }
            }

            // Upcoming Matches Section
            item {
                Text(
                    text = "UPCOMING MATCHES (${upcomingMatches.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = KccTextWhite,
                    letterSpacing = 1.sp
                )
            }

            if (upcomingMatches.isEmpty()) {
                item {
                    EmptyCard(message = "No upcoming matches scheduled.")
                }
            } else {
                items(upcomingMatches) { match ->
                    MatchCard(
                        match = match,
                        isLive = false,
                        role = role,
                        onOpenMatchCentre = {
                            viewModel.selectMatch(match)
                            viewModel.navigateTo(Screen.MatchCentre(match.id))
                        },
                        onOpenScoreLive = {
                            viewModel.selectMatch(match)
                            viewModel.navigateTo(Screen.LiveScoring)
                        }
                    )
                }
            }

            // Completed Matches Section
            item {
                Text(
                    text = "COMPLETED MATCHES (${completedMatches.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = KccTextWhite,
                    letterSpacing = 1.sp
                )
            }

            if (completedMatches.isEmpty()) {
                item {
                    EmptyCard(message = "No completed matches recorded yet.")
                }
            } else {
                items(completedMatches) { match ->
                    MatchCard(
                        match = match,
                        isLive = false,
                        role = role,
                        onOpenMatchCentre = {
                            viewModel.selectMatch(match)
                            viewModel.navigateTo(Screen.MatchCentre(match.id))
                        },
                        onOpenScoreLive = {}
                    )
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, isHighlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = if (isHighlight) KccWicketRed else KccGoldPrimary
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = KccTextSecondary
        )
    }
}

@Composable
fun MatchCard(
    match: Match,
    isLive: Boolean,
    role: UserRole,
    onOpenMatchCentre: () -> Unit,
    onOpenScoreLive: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KccSurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isLive) 1.5.dp else 1.dp,
            color = if (isLive) KccGoldPrimary.copy(alpha = 0.8f) else Color(0xFF282832)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenMatchCentre() }
            .testTag("match_card_${match.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(KccWicketRed)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF22222A))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = match.format,
                            color = KccGoldLight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = match.venue,
                    color = KccTextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = match.title,
                color = KccTextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Teams display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.teamAName,
                        color = KccTextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = match.teamBName,
                        color = KccTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (match.resultText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E261E))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = match.resultText,
                            color = Color(0xFF4ADE80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (match.leadTrailText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = match.leadTrailText,
                    color = KccGoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onOpenMatchCentre,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF383844)),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "Match Centre", color = KccTextWhite, fontSize = 12.sp)
                }

                if (isLive && role != UserRole.VIEWER) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenScoreLive,
                        colors = ButtonDefaults.buttonColors(containerColor = KccGoldPrimary, contentColor = Color(0xFF171302)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.SportsCricket, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Score Ball", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16161B)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24242C)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SportsCricket,
                contentDescription = null,
                tint = KccGoldPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = KccTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
