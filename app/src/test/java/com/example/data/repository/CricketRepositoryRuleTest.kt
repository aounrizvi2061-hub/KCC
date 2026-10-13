package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Delivery
import com.example.data.model.Match
import com.example.data.model.Player
import com.example.data.model.Team
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CricketRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun testCreateAndObserveTeam() = runBlocking {
        val uid = signInTestUser("scorer_alice@kcccricket.org")
        val repository = CricketRepository(firestore)

        val teamId = repository.createTeam("KCC Lions", "LIO")
        assertNotNull(teamId)

        val teams = repository.observeTeams().first()
        assertTrue(teams.any { it.id == teamId && it.name == "KCC Lions" && it.userId == uid })
    }

    @Test
    fun testCreateMatchAndRecordDelivery() = runBlocking {
        val uid = signInTestUser("scorer_bob@kcccricket.org")
        val repository = CricketRepository(firestore)

        val match = Match(
            userId = uid,
            title = "KCC League Match",
            format = "LIMITED_OVERS",
            teamAId = "team_a",
            teamAName = "KCC Kings",
            teamBId = "team_b",
            teamBName = "KCC Titans",
            status = "LIVE"
        )
        val matchId = repository.createMatch(match)
        assertNotNull(matchId)

        val delivery = Delivery(
            userId = uid,
            matchId = matchId,
            inningsNumber = 1,
            strikerId = "player_1",
            bowlerId = "player_2",
            runsBat = 4,
            isWicket = false
        )
        val deliveryId = repository.recordDelivery(delivery)
        assertNotNull(deliveryId)

        val deliveries = repository.observeDeliveries(matchId).first()
        assertEquals(1, deliveries.size)
        assertEquals(4L, deliveries[0].runsBat)
    }
}
