package com.esportsarena.challongebracket

import com.esportsarena.challongebracket.gui.GUI
import com.google.gson.Gson

val gson = Gson()

lateinit var gui: GUI

fun main(args: Array<String>) {
    gui = GUI()
    val receiver = TournamentReceiver()
    receiver.start()
    while (true) {
    }
}

