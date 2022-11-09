package com.esportsarena.challongebracket

import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.WindowEvent
import java.io.File
import java.io.FileWriter
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.math.min

class GUI {

    private val configFile: LinkedTreeMap<String, String>
    private val textFields = hashMapOf<String, JTextField>()
    private val checkBoxes = hashMapOf<String, JCheckBox>()

    fun getString(key: String): String? {
        return textFields[key]?.text
    }

    fun getBoolean(key: String): Boolean? {
        return checkBoxes[key]?.isSelected
    }


    init {
        val f = File("config.json")
        if (!f.exists()) f.createNewFile()
        val text = f.readLines().joinToString("")
        configFile = if (text.length < 2)
            LinkedTreeMap<String, String>()
        else
            gson.fromJson(text, object : TypeToken<Map<String, String>>() {}.type)

        val frame = JFrame("FzzyApexGraphics")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.preferredSize = Dimension(1280 / 2, 720 / 2)


        frame.add(initTextField("Challonge Tournament Link"))
        frame.add(initTextField("Challonge API Key"))
        frame.add(initTextField("Vmix IP"))
        frame.add(initTextField("Vmix Input Name"))

        frame.add(initCheckBox("Grand Finals Reset"))
        frame.add(initTextField("Reset Top Name"))
        frame.add(initTextField("Reset Top Score"))
        frame.add(initTextField("Reset Bottom Name"))
        frame.add(initTextField("Reset Bottom Score"))

        val updateLeaderboardButton = JButton("Force Update")
        updateLeaderboardButton.addActionListener {
        }
        frame.add(updateLeaderboardButton)

        frame.layout = GridLayout(14, 2)

        frame.setLocationRelativeTo(null)
        frame.pack()
        frame.isVisible = true
    }

    private fun initTextField(key: String): JPanel {
        val panel = JPanel()
        panel.layout = FlowLayout(FlowLayout.LEFT)
        panel.add(JLabel("$key: "))
        textFields[key] = JTextField(configFile.getOrDefault(key, ""))
        textFields[key]!!.preferredSize = Dimension(300, 20)

        textFields[key]!!.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                saveToFile()
            }

            override fun removeUpdate(e: DocumentEvent) {
                saveToFile()
            }

            override fun changedUpdate(e: DocumentEvent) {
            }
        })

        panel.add(textFields[key])
        return panel
    }

    private fun initCheckBox(key: String): JPanel {
        val panel = JPanel()
        panel.layout = FlowLayout(FlowLayout.LEFT)
        panel.add(JLabel("$key: "))
        checkBoxes[key] = JCheckBox()
        checkBoxes[key]!!.isSelected = configFile.getOrDefault(key, "False") == "True"

        checkBoxes[key]!!.addItemListener { saveToFile() }

        panel.add(checkBoxes[key])
        return panel
    }

    private fun saveToFile() {
        val save = hashMapOf<String, String>()
        for ((key, textfield) in textFields) {
            save[key] = textfield.text
        }
        for ((key, checkbox) in checkBoxes) {
            save[key] = if (checkbox.isSelected) "True" else "False"
        }
        val writer = FileWriter("config.json")
        writer.write(gson.toJson(save))
        writer.close()
    }

}