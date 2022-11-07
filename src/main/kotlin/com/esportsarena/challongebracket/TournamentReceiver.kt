package com.esportsarena.challongebracket

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class TournamentReceiver {

    fun start() {
        Thread {
            while (true) {
                val url = URL("https://api.challonge.com/v1/tournaments/$tourneyId.json?api_key=$apikey&include_participants=1&include_matches=1")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                while (reader.ready()) {
                    println(reader.readLine())
                }
            }
        }.start()
    }

}