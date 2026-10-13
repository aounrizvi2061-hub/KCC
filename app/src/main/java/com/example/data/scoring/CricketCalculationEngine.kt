package com.example.data.scoring

import com.example.data.model.Delivery
import com.example.data.model.Match
import com.example.data.model.Player
import java.util.Locale

data class BatterScorecardItem(
    val playerId: String,
    val playerName: String,
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val strikeRate: Double = 0.0,
    val isOut: Boolean = false,
    val dismissalText: String = "not out"
)

data class BowlerScorecardItem(
    val playerId: String,
    val playerName: String,
    val oversString: String = "0.0",
    val legalBalls: Int = 0,
    val maidens: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val economyRate: Double = 0.0,
    val dots: Int = 0
)

data class FallOfWicketItem(
    val wicketNumber: Int,
    val scoreAtFall: Int,
    val oversAtFall: String,
    val batterName: String
)

data class PartnershipItem(
    val wicketNumber: Int,
    val batter1Name: String,
    val batter1Runs: Int,
    val batter2Name: String,
    val batter2Runs: Int,
    val totalRuns: Int,
    val totalBalls: Int
)

data class ExtrasBreakdown(
    val total: Int = 0,
    val wides: Int = 0,
    val noBalls: Int = 0,
    val byes: Int = 0,
    val legByes: Int = 0,
    val penalty: Int = 0
)

data class InningsScorecard(
    val inningsNumber: Int,
    val battingTeamName: String,
    val bowlingTeamName: String,
    val totalRuns: Int = 0,
    val wickets: Int = 0,
    val oversString: String = "0.0",
    val legalBalls: Int = 0,
    val runRate: Double = 0.0,
    val extras: ExtrasBreakdown = ExtrasBreakdown(),
    val batters: List<BatterScorecardItem> = emptyList(),
    val bowlers: List<BowlerScorecardItem> = emptyList(),
    val fallOfWickets: List<FallOfWicketItem> = emptyList(),
    val partnerships: List<PartnershipItem> = emptyList(),
    val currentPartnership: PartnershipItem? = null,
    val isDeclared: Boolean = false
)

data class TestMatchContext(
    val teamAFirstInningsRuns: Int = 0,
    val teamBFirstInningsRuns: Int = 0,
    val teamASecondInningsRuns: Int = 0,
    val teamBSecondInningsRuns: Int = 0,
    val leadOrTrailText: String = "",
    val followOnEligible: Boolean = false,
    val followOnThreshold: Int = 200,
    val targetForFourthInnings: Int? = null,
    val currentStatusSummary: String = ""
)

data class PlayerStats(
    val playerId: String,
    val playerName: String,
    val matches: Int = 0,
    val inningsBatted: Int = 0,
    val totalRuns: Int = 0,
    val ballsFaced: Int = 0,
    val highestScore: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val fifties: Int = 0,
    val hundreds: Int = 0,
    val battingAverage: Double = 0.0,
    val battingStrikeRate: Double = 0.0,
    val oversBowledDecimal: String = "0.0",
    val ballsBowled: Int = 0,
    val maidens: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val bowlingAverage: Double = 0.0,
    val bowlingEconomy: Double = 0.0,
    val bestBowlingFigures: String = "0/0"
)

object CricketCalculationEngine {

    fun formatOvers(legalBalls: Int): String {
        val completedOvers = legalBalls / 6
        val remainingBalls = legalBalls % 6
        return "$completedOvers.$remainingBalls"
    }

    fun calculateInningsScorecard(
        inningsNumber: Int,
        match: Match,
        deliveries: List<Delivery>,
        allPlayers: Map<String, Player>
    ): InningsScorecard {
        val inningsDeliveries = deliveries
            .filter { it.inningsNumber.toInt() == inningsNumber }
            .sortedBy { it.createdAt?.seconds ?: 0L }

        var totalRuns = 0
        var totalWickets = 0
        var totalLegalBalls = 0

        var wides = 0
        var noBalls = 0
        var byes = 0
        var legByes = 0
        var penalty = 0

        // Batting stats tracking
        class BatRecord(
            val id: String,
            var runs: Int = 0,
            var balls: Int = 0,
            var fours: Int = 0,
            var sixes: Int = 0,
            var isOut: Boolean = false,
            var dismissalText: String = "not out"
        )
        val batMap = linkedMapOf<String, BatRecord>()

        // Bowling stats tracking
        class BowlRecord(
            val id: String,
            var legalBalls: Int = 0,
            var runsConceded: Int = 0,
            var wickets: Int = 0,
            var dots: Int = 0,
            val oversMap: MutableMap<Int, MutableList<Delivery>> = mutableMapOf()
        )
        val bowlMap = linkedMapOf<String, BowlRecord>()

        val fowList = mutableListOf<FallOfWicketItem>()
        val partnerships = mutableListOf<PartnershipItem>()

        var currentPshipRuns = 0
        var currentPshipBalls = 0
        var pshipBatter1 = ""
        var pshipBatter2 = ""
        var pshipBatter1Runs = 0
        var pshipBatter2Runs = 0

        for (del in inningsDeliveries) {
            val rBat = del.runsBat.toInt()
            val rExtra = del.extraRuns.toInt()
            val extraType = del.extraType.uppercase()
            val isLegal = extraType != "WIDE" && extraType != "NO_BALL"

            val ballRunsTotal = rBat + rExtra
            totalRuns += ballRunsTotal

            when (extraType) {
                "WIDE" -> wides += (rExtra + (if (del.extraRuns == 0L) 1 else 0))
                "NO_BALL" -> noBalls += (rExtra + (if (del.extraRuns == 0L) 1 else 0))
                "BYE" -> byes += rExtra
                "LEG_BYE" -> legByes += rExtra
                "PENALTY" -> penalty += rExtra
            }

            if (isLegal) {
                totalLegalBalls++
            }

            // Striker batting update
            if (del.strikerId.isNotBlank()) {
                val b = batMap.getOrPut(del.strikerId) { BatRecord(del.strikerId) }
                b.runs += rBat
                if (extraType != "WIDE") {
                    b.balls++
                }
                if (rBat == 4) b.fours++
                if (rBat == 6) b.sixes++
            }

            // Ensure non-striker in batting record
            if (del.nonStrikerId.isNotBlank()) {
                batMap.getOrPut(del.nonStrikerId) { BatRecord(del.nonStrikerId) }
            }

            // Partnership tracking
            if (pshipBatter1.isBlank()) pshipBatter1 = del.strikerId
            if (pshipBatter2.isBlank()) pshipBatter2 = del.nonStrikerId
            currentPshipRuns += ballRunsTotal
            if (extraType != "WIDE") {
                currentPshipBalls++
            }
            if (del.strikerId == pshipBatter1) {
                pshipBatter1Runs += rBat
            } else if (del.strikerId == pshipBatter2) {
                pshipBatter2Runs += rBat
            }

            // Bowler update
            if (del.bowlerId.isNotBlank()) {
                val bowl = bowlMap.getOrPut(del.bowlerId) { BowlRecord(del.bowlerId) }
                if (isLegal) {
                    bowl.legalBalls++
                }
                // Bowler runs conceded: bat runs + wide + no balls (byes & leg byes not charged to bowler)
                var bowlerCharged = rBat
                if (extraType == "WIDE" || extraType == "NO_BALL") {
                    bowlerCharged += rExtra
                }
                bowl.runsConceded += bowlerCharged

                if (ballRunsTotal == 0) {
                    bowl.dots++
                }

                val overIdx = del.overNumber.toInt()
                bowl.oversMap.getOrPut(overIdx) { mutableListOf() }.add(del)

                // Wickets: Bowler credited unless Run out, Retired
                if (del.isWicket) {
                    val wType = del.wicketType.uppercase()
                    if (wType != "RUN_OUT" && wType != "RETIRED_HURT" && wType != "RETIRED_OUT") {
                        bowl.wickets++
                    }
                }
            }

            // Wicket event
            if (del.isWicket) {
                totalWickets++
                val dismissedId = if (del.dismissedPlayerId.isNotBlank()) del.dismissedPlayerId else del.strikerId
                val dismissedRecord = batMap.getOrPut(dismissedId) { BatRecord(dismissedId) }
                dismissedRecord.isOut = true

                val bowlerName = allPlayers[del.bowlerId]?.name ?: "Bowler"
                val fielderName = allPlayers[del.fielderId]?.name ?: "Fielder"
                val dismissalDesc = when (del.wicketType.uppercase()) {
                    "BOWLED" -> "b $bowlerName"
                    "CAUGHT" -> "c $fielderName b $bowlerName"
                    "LBW" -> "lbw b $bowlerName"
                    "RUN_OUT" -> "run out ($fielderName)"
                    "STUMPED" -> "st $fielderName b $bowlerName"
                    "HIT_WICKET" -> "hit wicket b $bowlerName"
                    "RETIRED_HURT" -> "retired hurt"
                    "RETIRED_OUT" -> "retired out"
                    else -> "b $bowlerName"
                }
                dismissedRecord.dismissalText = dismissalDesc

                val dismissedName = allPlayers[dismissedId]?.name ?: "Batter"
                fowList.add(
                    FallOfWicketItem(
                        wicketNumber = totalWickets,
                        scoreAtFall = totalRuns,
                        oversAtFall = formatOvers(totalLegalBalls),
                        batterName = dismissedName
                    )
                )

                // Close partnership
                partnerships.add(
                    PartnershipItem(
                        wicketNumber = totalWickets,
                        batter1Name = allPlayers[pshipBatter1]?.name ?: "Batter 1",
                        batter1Runs = pshipBatter1Runs,
                        batter2Name = allPlayers[pshipBatter2]?.name ?: "Batter 2",
                        batter2Runs = pshipBatter2Runs,
                        totalRuns = currentPshipRuns,
                        totalBalls = currentPshipBalls
                    )
                )
                // Reset partnership
                currentPshipRuns = 0
                currentPshipBalls = 0
                pshipBatter1Runs = 0
                pshipBatter2Runs = 0
                pshipBatter1 = if (dismissedId == pshipBatter1) "" else pshipBatter1
                pshipBatter2 = if (dismissedId == pshipBatter2) "" else pshipBatter2
            }
        }

        // Active ongoing partnership
        val currentPship = if (currentPshipRuns > 0 || currentPshipBalls > 0 || inningsDeliveries.isNotEmpty()) {
            PartnershipItem(
                wicketNumber = totalWickets + 1,
                batter1Name = allPlayers[pshipBatter1]?.name ?: (allPlayers[match.currentStrikerId]?.name ?: "Striker"),
                batter1Runs = pshipBatter1Runs,
                batter2Name = allPlayers[pshipBatter2]?.name ?: (allPlayers[match.currentNonStrikerId]?.name ?: "Non-Striker"),
                batter2Runs = pshipBatter2Runs,
                totalRuns = currentPshipRuns,
                totalBalls = currentPshipBalls
            )
        } else null

        val batterItems = batMap.values.map { b ->
            val pName = allPlayers[b.id]?.name ?: "Batter ${b.id.take(4)}"
            val sr = if (b.balls > 0) (b.runs.toDouble() / b.balls.toDouble()) * 100.0 else 0.0
            BatterScorecardItem(
                playerId = b.id,
                playerName = pName,
                runs = b.runs,
                balls = b.balls,
                fours = b.fours,
                sixes = b.sixes,
                strikeRate = String.format(Locale.US, "%.1f", sr).toDoubleOrNull() ?: 0.0,
                isOut = b.isOut,
                dismissalText = b.dismissalText
            )
        }

        val bowlerItems = bowlMap.values.map { bowl ->
            val pName = allPlayers[bowl.id]?.name ?: "Bowler ${bowl.id.take(4)}"
            // Maidens calculation: completed overs (6 legal balls) where 0 runs were conceded
            var maidens = 0
            bowl.oversMap.forEach { (_, overDeliveries) ->
                val legalInOver = overDeliveries.count { it.extraType.uppercase() != "WIDE" && it.extraType.uppercase() != "NO_BALL" }
                val runsInOver = overDeliveries.sumOf { it.runsBat.toInt() + (if (it.extraType.uppercase() == "WIDE" || it.extraType.uppercase() == "NO_BALL") it.extraRuns.toInt() else 0) }
                if (legalInOver == 6 && runsInOver == 0) {
                    maidens++
                }
            }

            val oversCompleted = bowl.legalBalls / 6
            val remainder = bowl.legalBalls % 6
            val decimalOvers = oversCompleted + (remainder / 6.0)
            val econ = if (decimalOvers > 0.0) bowl.runsConceded / decimalOvers else 0.0

            BowlerScorecardItem(
                playerId = bowl.id,
                playerName = pName,
                oversString = "$oversCompleted.$remainder",
                legalBalls = bowl.legalBalls,
                maidens = maidens,
                runsConceded = bowl.runsConceded,
                wickets = bowl.wickets,
                economyRate = String.format(Locale.US, "%.2f", econ).toDoubleOrNull() ?: 0.0,
                dots = bowl.dots
            )
        }

        val extrasTotal = wides + noBalls + byes + legByes + penalty
        val oversDecimal = (totalLegalBalls / 6) + ((totalLegalBalls % 6) / 6.0)
        val runRate = if (oversDecimal > 0.0) totalRuns / oversDecimal else 0.0

        // Batting / Bowling team name resolution
        val isOdd = (inningsNumber % 2 != 0)
        val team1BatsFirst = (match.tossDecision.uppercase() == "BAT" && match.tossWinnerTeamId == match.teamAId) ||
                (match.tossDecision.uppercase() == "BOWL" && match.tossWinnerTeamId == match.teamBId)

        val battingTeam = if (isOdd) {
            if (team1BatsFirst) match.teamAName else match.teamBName
        } else {
            if (team1BatsFirst) match.teamBName else match.teamAName
        }

        val bowlingTeam = if (isOdd) {
            if (team1BatsFirst) match.teamBName else match.teamAName
        } else {
            if (team1BatsFirst) match.teamAName else match.teamBName
        }

        return InningsScorecard(
            inningsNumber = inningsNumber,
            battingTeamName = battingTeam.ifBlank { "Batting Team" },
            bowlingTeamName = bowlingTeam.ifBlank { "Bowling Team" },
            totalRuns = totalRuns,
            wickets = totalWickets,
            oversString = formatOvers(totalLegalBalls),
            legalBalls = totalLegalBalls,
            runRate = String.format(Locale.US, "%.2f", runRate).toDoubleOrNull() ?: 0.0,
            extras = ExtrasBreakdown(
                total = extrasTotal,
                wides = wides,
                noBalls = noBalls,
                byes = byes,
                legByes = legByes,
                penalty = penalty
            ),
            batters = batterItems,
            bowlers = bowlerItems,
            fallOfWickets = fowList,
            partnerships = partnerships,
            currentPartnership = currentPship,
            isDeclared = match.isDeclared && match.currentInningsIndex.toInt() == inningsNumber
        )
    }

    fun computeTestMatchContext(
        match: Match,
        innings1: InningsScorecard,
        innings2: InningsScorecard,
        innings3: InningsScorecard,
        innings4: InningsScorecard
    ): TestMatchContext {
        val r1 = innings1.totalRuns
        val r2 = innings2.totalRuns
        val r3 = innings3.totalRuns
        val r4 = innings4.totalRuns

        val followOnThreshold = 200
        val deficit1stInnings = r1 - r2
        val followOnEligible = deficit1stInnings >= followOnThreshold

        var leadOrTrail = ""
        var targetFor4: Int? = null

        when (match.currentInningsIndex.toInt()) {
            1 -> {
                leadOrTrail = "${innings1.battingTeamName} 1st Innings"
            }
            2 -> {
                val diff = r2 - r1
                leadOrTrail = if (diff >= 0) {
                    "${innings2.battingTeamName} leads by $diff runs"
                } else {
                    "${innings2.battingTeamName} trails by ${-diff} runs"
                }
            }
            3 -> {
                if (match.followOnEnforced) {
                    val remainingTrail = (r1 - (r2 + r3))
                    leadOrTrail = if (remainingTrail > 0) {
                        "${innings3.battingTeamName} (f/o) trails by $remainingTrail runs"
                    } else {
                        "${innings3.battingTeamName} (f/o) leads by ${-remainingTrail} runs"
                    }
                } else {
                    val totalLead = (r1 + r3) - r2
                    leadOrTrail = "${innings3.battingTeamName} leads by $totalLead runs"
                }
            }
            4 -> {
                val totalLeadTeamA = (r1 + r3) - r2
                val target = totalLeadTeamA + 1
                targetFor4 = target
                val needed = target - r4
                leadOrTrail = if (needed > 0) {
                    "Target: $target. ${innings4.battingTeamName} needs $needed runs"
                } else {
                    "${innings4.battingTeamName} won by ${10 - innings4.wickets} wickets"
                }
            }
        }

        return TestMatchContext(
            teamAFirstInningsRuns = r1,
            teamBFirstInningsRuns = r2,
            teamASecondInningsRuns = r3,
            teamBSecondInningsRuns = r4,
            leadOrTrailText = leadOrTrail,
            followOnEligible = followOnEligible,
            followOnThreshold = followOnThreshold,
            targetForFourthInnings = targetFor4,
            currentStatusSummary = leadOrTrail
        )
    }

    fun computeCareerStatistics(
        players: List<Player>,
        allDeliveries: List<Delivery>
    ): List<PlayerStats> {
        val playerMap = players.associateBy { it.id }

        return players.map { player ->
            val pId = player.id
            val deliveriesFaced = allDeliveries.filter { it.strikerId == pId }
            val deliveriesBowled = allDeliveries.filter { it.bowlerId == pId }

            val totalRuns = deliveriesFaced.sumOf { it.runsBat.toInt() }
            val ballsFaced = deliveriesFaced.count { it.extraType.uppercase() != "WIDE" }
            val fours = deliveriesFaced.count { it.runsBat.toInt() == 4 }
            val sixes = deliveriesFaced.count { it.runsBat.toInt() == 6 }

            // Group by match and innings for highest score and 50s/100s
            val inningsScores = deliveriesFaced
                .groupBy { "${it.matchId}_${it.inningsNumber}" }
                .map { (_, dels) -> dels.sumOf { it.runsBat.toInt() } }

            val inningsBatted = inningsScores.size
            val highestScore = inningsScores.maxOrNull() ?: 0
            val fifties = inningsScores.count { it in 50..99 }
            val hundreds = inningsScores.count { it >= 100 }

            val dismissals = allDeliveries.count {
                it.isWicket && (it.dismissedPlayerId == pId || (it.dismissedPlayerId.isBlank() && it.strikerId == pId))
            }
            val battingAvg = if (dismissals > 0) totalRuns.toDouble() / dismissals else totalRuns.toDouble()
            val battingSr = if (ballsFaced > 0) (totalRuns.toDouble() / ballsFaced) * 100.0 else 0.0

            // Bowling stats
            val legalBallsBowled = deliveriesBowled.count {
                it.extraType.uppercase() != "WIDE" && it.extraType.uppercase() != "NO_BALL"
            }
            val runsConceded = deliveriesBowled.sumOf {
                it.runsBat.toInt() + (if (it.extraType.uppercase() == "WIDE" || it.extraType.uppercase() == "NO_BALL") it.extraRuns.toInt() else 0)
            }
            val wickets = deliveriesBowled.count {
                it.isWicket && it.wicketType.uppercase() !in listOf("RUN_OUT", "RETIRED_HURT", "RETIRED_OUT")
            }

            // Best bowling
            val matchBowlings = deliveriesBowled.groupBy { it.matchId }.map { (_, dels) ->
                val wk = dels.count { it.isWicket && it.wicketType.uppercase() !in listOf("RUN_OUT", "RETIRED_HURT", "RETIRED_OUT") }
                val r = dels.sumOf { it.runsBat.toInt() + (if (it.extraType.uppercase() == "WIDE" || it.extraType.uppercase() == "NO_BALL") it.extraRuns.toInt() else 0) }
                Pair(wk, r)
            }
            val best = matchBowlings.maxWithOrNull(compareBy({ it.first }, { -it.second }))
            val bbi = if (best != null && best.first > 0) "${best.first}/${best.second}" else "0/0"

            val oversComp = legalBallsBowled / 6
            val oversRem = legalBallsBowled % 6
            val decOvers = oversComp + (oversRem / 6.0)
            val bowlingEcon = if (decOvers > 0.0) runsConceded / decOvers else 0.0
            val bowlingAvg = if (wickets > 0) runsConceded.toDouble() / wickets else 0.0

            val matchesPlayed = allDeliveries
                .filter { it.strikerId == pId || it.nonStrikerId == pId || it.bowlerId == pId }
                .map { it.matchId }
                .distinct()
                .size

            PlayerStats(
                playerId = pId,
                playerName = player.name,
                matches = if (matchesPlayed > 0) matchesPlayed else 1,
                inningsBatted = inningsBatted,
                totalRuns = totalRuns,
                ballsFaced = ballsFaced,
                highestScore = highestScore,
                fours = fours,
                sixes = sixes,
                fifties = fifties,
                hundreds = hundreds,
                battingAverage = String.format(Locale.US, "%.2f", battingAvg).toDoubleOrNull() ?: 0.0,
                battingStrikeRate = String.format(Locale.US, "%.1f", battingSr).toDoubleOrNull() ?: 0.0,
                oversBowledDecimal = "$oversComp.$oversRem",
                ballsBowled = legalBallsBowled,
                maidens = 0,
                runsConceded = runsConceded,
                wickets = wickets,
                bowlingAverage = String.format(Locale.US, "%.2f", bowlingAvg).toDoubleOrNull() ?: 0.0,
                bowlingEconomy = String.format(Locale.US, "%.2f", bowlingEcon).toDoubleOrNull() ?: 0.0,
                bestBowlingFigures = bbi
            )
        }
    }
}
