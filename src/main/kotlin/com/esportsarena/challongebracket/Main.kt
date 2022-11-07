package com.esportsarena.challongebracket

import com.esportsarena.challongebracket.challonge.Match
import com.esportsarena.challongebracket.challonge.Participant
import com.esportsarena.challongebracket.util.VMix
import com.google.gson.Gson
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

val tourneyId = "SeriesE_Multiversus_qual2"
val apikey = "eTkK65FzozLn8Q1PhBPCMmv8WdMG7jGKwCLY8oh2"
val matches = hashMapOf<String, Match>()
val participants = hashMapOf<Int, Participant>()

fun getWinnersMatchFromRight(index: Int): List<Match> {
    var highestRound = 0
    for (m in matches.values) {
        if (m.round > highestRound) highestRound = m.round
    }
    val consolidate = arrayListOf<Match>()
    for (m in matches.values) {
        if (m.round == highestRound - index) consolidate.add(m)
    }
    return consolidate
}

fun getLosersMatchFromRight(index: Int): List<Match> {
    var lowestRound = 0
    for (m in matches.values) {
        if (m.round < lowestRound) lowestRound = m.round
    }
    val consolidate = arrayListOf<Match>()
    for (m in matches.values) {
        if (m.round == lowestRound + index) consolidate.add(m)
    }
    return consolidate
}

val gson = Gson()

fun main(args: Array<String>) {
    val url =
        URL("https://api.challonge.com/v1/tournaments/$tourneyId.json?api_key=$apikey&include_participants=1&include_matches=1")
    val conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "GET"
    val reader = BufferedReader(InputStreamReader(conn.inputStream))
    val json = JSONObject(reader.lines().toList().joinToString("")).getJSONObject("tournament")

    val p = json.getJSONArray("participants")
    for (participantJSON in p) {
        val participant = gson.fromJson((participantJSON as JSONObject).getJSONObject("participant").toString(), Participant::class.java)
        participants[participant.id] = participant
    }

    val m = json.getJSONArray("matches")
    for (matchJSON in m) {
        val match = gson.fromJson((matchJSON as JSONObject).getJSONObject("match").toString(), Match::class.java)
        matches[match.identifier] = match
    }

    val grandfinals = getWinnersMatchFromRight(0).first()
    for (i in 0..3) {
        when (i) {
            0 -> {
                val losersFinals = getLosersMatchFromRight(i).first()

            }
        }
    }
}

fun insertMatchIntoGraphic(index: Int, match: Match) {
    VMix.setTitleText(participants[match.winner_id]!!.name, "Top 8", "wP1 Name$index.Text")
    VMix.setTitleText(participants[match.winner_id]!!.name, "Top 8", "wP2 Name$index.Text")
    VMix.setTitleText(participants[match.winner_id]!!.name, "Top 8", "wP1 Name$index.Text")
    VMix.setTitleText(participants[match.winner_id]!!.name, "Top 8", "wP1 Name$index.Text")
}