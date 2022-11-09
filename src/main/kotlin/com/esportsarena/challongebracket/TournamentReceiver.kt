package com.esportsarena.challongebracket

import com.esportsarena.challongebracket.challonge.Match
import com.esportsarena.challongebracket.challonge.Participant
import com.esportsarena.challongebracket.util.VMix
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class TournamentReceiver {

    val matches = hashMapOf<String, Match>()
    val participants = hashMapOf<Int, Participant>()

    fun start() {
        Thread {
            while (true) {
                try {
                    update()
                } catch (e: Exception) {
                    println("failed to update bracket")
                }
                Thread.sleep(7000)
            }
        }.start()
    }

    fun parseIdFromUrl(url: String): String {
        val prefixFinder = Regex("(?<=://)(.*)(?=.challonge.com)").find(url)
        val id = url.substringAfterLast('/')
        return if (prefixFinder != null) {
            "${prefixFinder.value}-$id"
        } else {
            id
        }
    }

    fun update() {
        val url =
            URL("https://api.challonge.com/v1/tournaments/${parseIdFromUrl(gui.getString("Challonge Tournament Link")!!)}.json" +
                    "?api_key=${gui.getString("Challonge API Key")}" +
                    "&include_participants=1" +
                    "&include_matches=1")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        val reader = BufferedReader(InputStreamReader(conn.inputStream))
        val json = JSONObject(reader.lines().toList().joinToString("")).getJSONObject("tournament")

        participants.clear()
        matches.clear()

        val p = json.getJSONArray("participants")
        for (participantJSON in p) {
            val participant = gson.fromJson(
                (participantJSON as JSONObject).getJSONObject("participant").toString(),
                Participant::class.java
            )
            participants[participant.id] = participant
        }

        val m = json.getJSONArray("matches")
        for (matchJSON in m) {
            val match = gson.fromJson((matchJSON as JSONObject).getJSONObject("match").toString(), Match::class.java)
            matches[match.identifier] = match
        }

        for (i in 0..3) {
            val losers = getLosersMatchFromRight(i)
            val conversion = when (i) {
                0 -> arrayListOf(9)
                1 -> arrayListOf(8)
                2 -> arrayListOf(6, 7)
                3 -> arrayListOf(4, 5)
                else -> arrayListOf()
            }
            insertMatchesIntoGraphic(losers, conversion)
        }
        for (i in 0..2) {
            val winners = getWinnersMatchFromRight(i)
            val conversion = when (i) {
                0 -> arrayListOf(3)
                1 -> arrayListOf(2)
                2 -> arrayListOf(1, 0)
                else -> arrayListOf()
            }
            insertMatchesIntoGraphic(winners, conversion)
        }

        val reset = gui.getBoolean("Grand Finals Reset")!!
        if (reset) {
            VMix.setImageVisibleOn(gui.getString("Vmix Input Name")!!, "Image2.Source")
            VMix.setTitleText(gui.getString("Reset Top Name")!!, gui.getString("Vmix Input Name")!!, "wP1 Name10.Text")
            VMix.setTitleText(gui.getString("Reset Bottom Name")!!, gui.getString("Vmix Input Name")!!, "wP2 Name10.Text")
            VMix.setTitleText(gui.getString("Reset Top Score")!!, gui.getString("Vmix Input Name")!!, "wP1 Score10.Text")
            VMix.setTitleText(gui.getString("Reset Bottom Score")!!, gui.getString("Vmix Input Name")!!, "wP2 Score10.Text")
        } else {
            VMix.setImageVisibleOff(gui.getString("Vmix Input Name")!!, "Image2.Source")
            VMix.setTitleText("", gui.getString("Vmix Input Name")!!, "wP1 Name10.Text")
            VMix.setTitleText("", gui.getString("Vmix Input Name")!!, "wP2 Name10.Text")
            VMix.setTitleText("", gui.getString("Vmix Input Name")!!, "wP1 Score10.Text")
            VMix.setTitleText("", gui.getString("Vmix Input Name")!!, "wP2 Score10.Text")
        }
    }

    fun insertMatchesIntoGraphic(matches: List<Match>, graphicIndices: List<Int>) {
        for ((i, graphicIndex) in graphicIndices.withIndex()) {
            val match = matches[i]
            if (match == null || match.loser_id == 0 || match.winner_id == 0) {
                VMix.setTitleText(
                    if (match.player1_id != 0) participants[match.player1_id]!!.name else "",
                    gui.getString("Vmix Input Name")!!,
                    "wP1 Name$graphicIndex.Text"
                )
                VMix.setTitleText(
                    if (match.player2_id != 0) participants[match.player2_id]!!.name else "",
                    gui.getString("Vmix Input Name")!!,
                    "wP2 Name$graphicIndex.Text"
                )
                VMix.setTitleText("0", gui.getString("Vmix Input Name")!!, "wP1 Score$graphicIndex.Text")
                VMix.setTitleText("0", gui.getString("Vmix Input Name")!!, "wP2 Score$graphicIndex.Text")
                continue
            }
            val formatIndex = graphicIndex.toString().replace("0", "")
            val scores = match.scores_csv.split("-").sortedBy { it.toInt() }
            VMix.setTitleText(participants[match.loser_id]!!.name, gui.getString("Vmix Input Name")!!, "wP1 Name$formatIndex.Text")
            VMix.setTitleText(participants[match.winner_id]!!.name, gui.getString("Vmix Input Name")!!, "wP2 Name$formatIndex.Text")
            VMix.setTitleText(scores[0], gui.getString("Vmix Input Name")!!, "wP1 Score$formatIndex.Text")
            VMix.setTitleText(scores[1], gui.getString("Vmix Input Name")!!, "wP2 Score$formatIndex.Text")
        }
    }

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

}