package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Player(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val jerseyNumber: Long = 0L,
    val role: String = "Batter", // Batter, Bowler, All-Rounder, Wicketkeeper
    val battingStyle: String = "Right Hand Bat",
    val bowlingStyle: String = "Right-arm Medium",
    val photoUrl: String = "",
    val teamId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
