package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.error.OperationType
import com.example.data.error.handleFirestoreError
import com.example.data.model.Delivery
import com.example.data.model.Match
import com.example.data.model.Player
import com.example.data.model.Team
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CricketRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun currentUserIdOrNull(): String? = auth.currentUser?.uid

    // Teams
    fun observeTeams(): Flow<List<Team>> = flow {
        val path = "teams"
        emitAll(
            db.collection("teams")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Team::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createTeam(name: String, shortName: String, logoUrl: String = "", captainId: String = ""): String {
        val uid = requireUserId()
        val docRef = db.collection("teams").document()
        val payload = mapOf(
            "userId" to uid,
            "name" to name,
            "shortName" to shortName,
            "logoUrl" to logoUrl,
            "captainPlayerId" to captainId,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateTeam(team: Team) {
        require(team.id.isNotBlank()) { "Team ID must not be blank" }
        val docRef = db.collection("teams").document(team.id)
        try {
            docRef.set(team.copy(updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteTeam(teamId: String) {
        val docRef = db.collection("teams").document(teamId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // Players
    fun observePlayers(): Flow<List<Player>> = flow {
        val path = "players"
        emitAll(
            db.collection("players")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Player::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createPlayer(
        name: String,
        jerseyNumber: Long,
        role: String,
        battingStyle: String,
        bowlingStyle: String,
        photoUrl: String = "",
        teamId: String = ""
    ): String {
        val uid = requireUserId()
        val docRef = db.collection("players").document()
        val payload = mapOf(
            "userId" to uid,
            "name" to name,
            "jerseyNumber" to jerseyNumber,
            "role" to role,
            "battingStyle" to battingStyle,
            "bowlingStyle" to bowlingStyle,
            "photoUrl" to photoUrl,
            "teamId" to teamId,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updatePlayer(player: Player) {
        require(player.id.isNotBlank()) { "Player ID must not be blank" }
        val docRef = db.collection("players").document(player.id)
        try {
            docRef.set(player.copy(updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deletePlayer(playerId: String) {
        val docRef = db.collection("players").document(playerId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // Matches
    fun observeMatches(): Flow<List<Match>> = flow {
        val path = "matches"
        emitAll(
            db.collection("matches")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Match::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createMatch(match: Match): String {
        val uid = requireUserId()
        val docRef = db.collection("matches").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to match.title,
            "venue" to match.venue,
            "matchDate" to match.matchDate,
            "format" to match.format,
            "oversPerInnings" to match.oversPerInnings,
            "teamAId" to match.teamAId,
            "teamAName" to match.teamAName,
            "teamAShortName" to match.teamAShortName,
            "teamBId" to match.teamBId,
            "teamBName" to match.teamBName,
            "teamBShortName" to match.teamBShortName,
            "tossWinnerTeamId" to match.tossWinnerTeamId,
            "tossDecision" to match.tossDecision,
            "status" to match.status,
            "resultText" to match.resultText,
            "currentInningsIndex" to match.currentInningsIndex,
            "currentStrikerId" to match.currentStrikerId,
            "currentNonStrikerId" to match.currentNonStrikerId,
            "currentBowlerId" to match.currentBowlerId,
            "leadTrailText" to match.leadTrailText,
            "followOnEnforced" to match.followOnEnforced,
            "isDeclared" to match.isDeclared,
            "day" to match.day,
            "session" to match.session,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateMatch(match: Match) {
        require(match.id.isNotBlank()) { "Match ID must not be blank" }
        val docRef = db.collection("matches").document(match.id)
        try {
            docRef.set(match.copy(updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMatch(matchId: String) {
        val docRef = db.collection("matches").document(matchId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // Deliveries
    fun observeDeliveries(matchId: String): Flow<List<Delivery>> = flow {
        val path = "matches/$matchId/deliveries"
        emitAll(
            db.collection("matches").document(matchId).collection("deliveries")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Delivery::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun recordDelivery(delivery: Delivery, updatedMatch: Match? = null): String {
        val uid = requireUserId()
        val delDoc = db.collection("matches").document(delivery.matchId).collection("deliveries").document()
        val payload = mapOf(
            "userId" to uid,
            "matchId" to delivery.matchId,
            "inningsNumber" to delivery.inningsNumber,
            "overNumber" to delivery.overNumber,
            "ballInOver" to delivery.ballInOver,
            "legalBallNumber" to delivery.legalBallNumber,
            "strikerId" to delivery.strikerId,
            "nonStrikerId" to delivery.nonStrikerId,
            "bowlerId" to delivery.bowlerId,
            "runsBat" to delivery.runsBat,
            "extraRuns" to delivery.extraRuns,
            "extraType" to delivery.extraType,
            "isWicket" to delivery.isWicket,
            "wicketType" to delivery.wicketType,
            "dismissedPlayerId" to delivery.dismissedPlayerId,
            "fielderId" to delivery.fielderId,
            "commentary" to delivery.commentary,
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            delDoc.set(payload).await()
            if (updatedMatch != null) {
                updateMatch(updatedMatch)
            }
            return delDoc.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, delDoc.path)
            throw e
        }
    }

    suspend fun undoLastDelivery(matchId: String, deliveryId: String, updatedMatch: Match? = null) {
        val docRef = db.collection("matches").document(matchId).collection("deliveries").document(deliveryId)
        try {
            docRef.delete().await()
            if (updatedMatch != null) {
                updateMatch(updatedMatch)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun seedInitialKccDataIfEmpty() {
        val uid = requireUserId()
        val existingTeams = db.collection("teams").limit(1).get().await()
        if (!existingTeams.isEmpty) return

        // Seed Teams
        val team1Id = createTeam("KCC Kings", "KCC", "", "")
        val team2Id = createTeam("Royal Challengers KCC", "RCK", "", "")
        val team3Id = createTeam("KCC Titans", "TIT", "", "")

        // Seed Players for Team 1
        val p1 = createPlayer("Aoun Rizvi", 7, "All-Rounder", "Right Hand Bat", "Right-arm Fast", "", team1Id)
        val p2 = createPlayer("Kamran Khan", 18, "Batter", "Right Hand Bat", "Right-arm Medium", "", team1Id)
        val p3 = createPlayer("Tariq Mahmood", 10, "Wicketkeeper", "Right Hand Bat", "None", "", team1Id)
        val p4 = createPlayer("Zeeshan Ali", 99, "Bowler", "Right Hand Bat", "Right-arm Fast", "", team1Id)
        val p5 = createPlayer("Bilal Ahmed", 23, "Bowler", "Left Hand Bat", "Left-arm Spin", "", team1Id)
        val p6 = createPlayer("Usman Farooq", 12, "Batter", "Left Hand Bat", "None", "", team1Id)

        // Seed Players for Team 2
        val p7 = createPlayer("Hamza Malik", 45, "Batter", "Right Hand Bat", "Right-arm Off Break", "", team2Id)
        val p8 = createPlayer("Sohail Rauf", 8, "Bowler", "Right Hand Bat", "Right-arm Fast", "", team2Id)
        val p9 = createPlayer("Fahad Qureshi", 17, "All-Rounder", "Left Hand Bat", "Right-arm Medium", "", team2Id)
        val p10 = createPlayer("Salman Butt", 1, "Wicketkeeper", "Right Hand Bat", "None", "", team2Id)
        val p11 = createPlayer("Imran Nazir", 14, "Batter", "Right Hand Bat", "Right-arm Leg Spin", "", team2Id)

        // Seed Live Match
        val liveMatch = Match(
            userId = uid,
            title = "KCC Super League 2026 - Semi Final",
            venue = "KCC International Cricket Stadium",
            matchDate = "Today, 14:00",
            format = "LIMITED_OVERS",
            oversPerInnings = 20,
            teamAId = team1Id,
            teamAName = "KCC Kings",
            teamAShortName = "KCC",
            teamBId = team2Id,
            teamBName = "Royal Challengers KCC",
            teamBShortName = "RCK",
            tossWinnerTeamId = team1Id,
            tossDecision = "BAT",
            status = "LIVE",
            resultText = "KCC Kings batting first",
            currentInningsIndex = 1,
            currentStrikerId = p1,
            currentNonStrikerId = p2,
            currentBowlerId = p8,
            day = 1,
            session = 1
        )
        val liveMatchId = createMatch(liveMatch)

        // Add initial deliveries to live match
        recordDelivery(Delivery(matchId = liveMatchId, inningsNumber = 1, overNumber = 0, ballInOver = 1, legalBallNumber = 1, strikerId = p1, nonStrikerId = p2, bowlerId = p8, runsBat = 1, commentary = "Single pushed to mid-on by Aoun Rizvi"))
        recordDelivery(Delivery(matchId = liveMatchId, inningsNumber = 1, overNumber = 0, ballInOver = 2, legalBallNumber = 2, strikerId = p2, nonStrikerId = p1, bowlerId = p8, runsBat = 4, commentary = "FOUR! Glorious cover drive through the gap by Kamran Khan"))
        recordDelivery(Delivery(matchId = liveMatchId, inningsNumber = 1, overNumber = 0, ballInOver = 3, legalBallNumber = 3, strikerId = p2, nonStrikerId = p1, bowlerId = p8, runsBat = 0, commentary = "Good length, defended solidly back to the bowler"))
        recordDelivery(Delivery(matchId = liveMatchId, inningsNumber = 1, overNumber = 0, ballInOver = 4, legalBallNumber = 4, strikerId = p2, nonStrikerId = p1, bowlerId = p8, runsBat = 6, commentary = "SIX! Lofted beautifully over long-off into the stands!"))
        recordDelivery(Delivery(matchId = liveMatchId, inningsNumber = 1, overNumber = 0, ballInOver = 5, legalBallNumber = 5, strikerId = p2, nonStrikerId = p1, bowlerId = p8, runsBat = 1, commentary = "Quick single taken to mid-wicket"))
        recordDelivery(Delivery(matchId = liveMatchId, inningsNumber = 1, overNumber = 0, ballInOver = 6, legalBallNumber = 6, strikerId = p1, nonStrikerId = p2, bowlerId = p8, runsBat = 2, commentary = "Whipped off the pads for a brace. Over completed!"))

        // Seed Completed Test Match
        val completedTest = Match(
            userId = uid,
            title = "KCC Annual Trophy - 4-Day Test",
            venue = "KCC Oval Grounds",
            matchDate = "Last Weekend",
            format = "TEST",
            oversPerInnings = 90,
            teamAId = team1Id,
            teamAName = "KCC Kings",
            teamAShortName = "KCC",
            teamBId = team3Id,
            teamBName = "KCC Titans",
            teamBShortName = "TIT",
            tossWinnerTeamId = team1Id,
            tossDecision = "BAT",
            status = "COMPLETED",
            resultText = "KCC Kings won by 82 runs",
            currentInningsIndex = 4,
            leadTrailText = "Match completed"
        )
        val compMatchId = createMatch(completedTest)
        recordDelivery(Delivery(matchId = compMatchId, inningsNumber = 1, overNumber = 0, ballInOver = 1, legalBallNumber = 1, strikerId = p1, nonStrikerId = p2, bowlerId = p4, runsBat = 4, commentary = "Boundary to start the first innings"))
        recordDelivery(Delivery(matchId = compMatchId, inningsNumber = 1, overNumber = 0, ballInOver = 2, legalBallNumber = 2, strikerId = p1, nonStrikerId = p2, bowlerId = p4, runsBat = 6, commentary = "Magnificent six!"))
    }
}
