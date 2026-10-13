package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Match(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val venue: String = "KCC Oval, Colombo",
    val matchDate: String = "",
    val format: String = "LIMITED_OVERS", // LIMITED_OVERS, TEST, CUSTOM
    val oversPerInnings: Long = 20L,
    val teamAId: String = "",
    val teamAName: String = "",
    val teamAShortName: String = "",
    val teamBId: String = "",
    val teamBName: String = "",
    val teamBShortName: String = "",
    val tossWinnerTeamId: String = "",
    val tossDecision: String = "BAT", // BAT or BOWL
    val status: String = "UPCOMING", // UPCOMING, LIVE, COMPLETED, ABANDONED
    val resultText: String = "",
    val currentInningsIndex: Long = 1L, // 1 to 4 for Test, 1 to 2 for Limited Overs
    val currentStrikerId: String = "",
    val currentNonStrikerId: String = "",
    val currentBowlerId: String = "",
    val leadTrailText: String = "",
    val followOnEnforced: Boolean = false,
    val isDeclared: Boolean = false,
    val day: Long = 1L,
    val session: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
