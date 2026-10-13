package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Delivery(
    @DocumentId val id: String = "",
    val userId: String = "",
    val matchId: String = "",
    val inningsNumber: Long = 1L,
    val overNumber: Long = 0L,
    val ballInOver: Long = 1L,
    val legalBallNumber: Long = 1L,
    val strikerId: String = "",
    val nonStrikerId: String = "",
    val bowlerId: String = "",
    val runsBat: Long = 0L,
    val extraRuns: Long = 0L,
    val extraType: String = "NONE", // NONE, WIDE, NO_BALL, BYE, LEG_BYE, PENALTY
    val isWicket: Boolean = false,
    val wicketType: String = "", // BOWLED, CAUGHT, LBW, RUN_OUT, STUMPED, HIT_WICKET, RETIRED_HURT, RETIRED_OUT
    val dismissedPlayerId: String = "",
    val fielderId: String = "",
    val commentary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
