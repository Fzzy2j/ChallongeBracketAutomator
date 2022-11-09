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

val gson = Gson()

lateinit var gui: GUI

fun main(args: Array<String>) {
    gui = GUI()
    val receiver = TournamentReceiver()
    receiver.start()
}

