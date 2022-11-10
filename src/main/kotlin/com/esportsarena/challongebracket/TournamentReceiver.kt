package com.esportsarena.challongebracket

import com.esportsarena.challongebracket.challonge.Match
import com.esportsarena.challongebracket.challonge.Participant
import com.esportsarena.challongebracket.util.VMix
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
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
                    gui.runningLabel.isVisible = true
                    gui.stoppedLabel.isVisible = false
                    gui.errorText.text = ""
                } catch (e: Exception) {
                    gui.stoppedLabel.isVisible = true
                    gui.runningLabel.isVisible = false
                    gui.errorText.text = e.message
                    val lastError = File("lastError.txt")
                    val writer = FileWriter(lastError, false)
                    writer.write(e.stackTraceToString())
                    writer.close()
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
            URL(
                "https://api.challonge.com/v1/tournaments/${parseIdFromUrl(gui.challongeLink.text)}.json" +
                        "?api_key=${gui.challongeApiKey.text}" +
                        "&include_participants=1" +
                        "&include_matches=1"
            )
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36")
        conn.readTimeout = 5000
        conn.connectTimeout = 5000
        val reader = BufferedReader(InputStreamReader(conn.inputStream))
        val json = JSONObject(reader.readText()).getJSONObject("tournament")
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

        val reset = gui.resetEnabled.isSelected
        if (reset) {
            VMix.setImageVisibleOn(gui.vMixInputName.text, "Image2.Source")
            VMix.setTitleText(gui.topName.text, gui.vMixInputName.text, "wP1 Name10.Text")
            VMix.setTitleText(gui.bottomName.text, gui.vMixInputName.text, "wP2 Name10.Text")
            VMix.setTitleText(gui.topScore.text, gui.vMixInputName.text, "wP1 Score10.Text")
            VMix.setTitleText(gui.bottomScore.text, gui.vMixInputName.text, "wP2 Score10.Text")
        } else {
            VMix.setImageVisibleOff(gui.vMixInputName.text, "Image2.Source")
            VMix.setTitleText("", gui.vMixInputName.text, "wP1 Name10.Text")
            VMix.setTitleText("", gui.vMixInputName.text, "wP2 Name10.Text")
            VMix.setTitleText("", gui.vMixInputName.text, "wP1 Score10.Text")
            VMix.setTitleText("", gui.vMixInputName.text, "wP2 Score10.Text")
        }
    }

    fun insertMatchesIntoGraphic(matches: List<Match>, graphicIndices: List<Int>) {
        for ((i, graphicIndex) in graphicIndices.withIndex()) {
            val match = matches[i]
            if (match == null || match.loser_id == 0 || match.winner_id == 0) {
                VMix.setTitleText(
                    if (match.player1_id != 0) participants[match.player1_id]!!.name else "",
                    gui.vMixInputName.text,
                    "wP1 Name$graphicIndex.Text"
                )
                VMix.setTitleText(
                    if (match.player2_id != 0) participants[match.player2_id]!!.name else "",
                    gui.vMixInputName.text,
                    "wP2 Name$graphicIndex.Text"
                )
                VMix.setTitleText("0", gui.vMixInputName.text, "wP1 Score$graphicIndex.Text")
                VMix.setTitleText("0", gui.vMixInputName.text, "wP2 Score$graphicIndex.Text")
                continue
            }
            val formatIndex = graphicIndex.toString().replace("0", "")
            val scores = match.scores_csv.split("-").sortedBy { it.toInt() }
            VMix.setTitleText(participants[match.loser_id]!!.name, gui.vMixInputName.text, "wP1 Name$formatIndex.Text")
            VMix.setTitleText(participants[match.winner_id]!!.name, gui.vMixInputName.text, "wP2 Name$formatIndex.Text")
            VMix.setTitleText(scores[0], gui.vMixInputName.text, "wP1 Score$formatIndex.Text")
            VMix.setTitleText(scores[1], gui.vMixInputName.text, "wP2 Score$formatIndex.Text")
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