package com.esportsarena.challongebracket.gui

import com.esportsarena.challongebracket.gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import org.w3c.dom.css.Rect
import java.awt.*
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.io.File
import java.io.FileWriter
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.concurrent.fixedRateTimer

class GUI {

    val frame = JFrame("Fzzy Bracket Updater")

    val lineSpacing = 50
    var line = 0

    val challongeLink = addTextField("Challonge Link",15, 15 + (line++ * lineSpacing), 460)
    val challongeApiKey = addTextField("Challonge API Key",15, 15 + (line++ * lineSpacing), 460)
    val vMixIP = addTextField("VMix IP", 15, 15 + (line++ * lineSpacing), 460, "localhost:8088")
    val vMixInputName = addTextField("VMix Input Name", 15, 15 + (line++ * lineSpacing), 460, "Top 8")

    val resetEnabled = addCheckBox("Reset Enabled", 15, 15 + (line * lineSpacing), false)
    val topName = addTextField("Top Name", 115, 15 + (line * lineSpacing), 160)
    val topScore = addTextField("Top Score", 285, 15 + (line++ * lineSpacing), 90, "0")
    val bottomName = addTextField("Bottom Name", 115, 15 + (line * lineSpacing), 160)
    val bottomScore = addTextField("Bottom Score", 285, 15 + (line++ * lineSpacing), 90, "0")

    fun addTextField(key: String, x: Int, y: Int, width: Int, defaultValue: String = "", color: Color = Color.LIGHT_GRAY): JTextField {
        val field = getCachedTextField(key, defaultValue)
        field.addFocusListener(object: FocusListener {
            override fun focusGained(p0: FocusEvent) {
                field.select(0, field.text.length)
            }
            override fun focusLost(p0: FocusEvent) {
                field.select(0, 0)
            }
        })
        field.bounds = Rectangle(x + 4, y + 21, width - 8, 17)
        frame.add(field)
        val label = JLabel(key)
        label.bounds = Rectangle(x + 4, y + 4, width - 8, 17)
        frame.add(label)
        val box = JRectangle(Rectangle(x, y, width, 41), color)
        frame.add(box)
        return field
    }
    fun addCheckBox(key: String, x: Int, y: Int, defaultValue: Boolean = false, onChange: (Boolean) -> Unit = {}): JCheckBox {
        val label = JLabel(key)
        label.bounds = Rectangle(x + 4, y + 4, label.getFontMetrics(label.font).stringWidth(key), 17)
        frame.add(label)
        val box = getCachedCheckBox(key, defaultValue, onChange)
        box.bounds = Rectangle((x - 8) + label.width / 2, y + 19, 20, 20)
        frame.add(box)
        val bg = JRectangle(Rectangle(x, y, label.width + 8, 41), Color.LIGHT_GRAY)
        frame.add(bg)
        box.background = Color.LIGHT_GRAY
        return box
    }
    private val configFile: LinkedTreeMap<String, String>
        get() {
            val f = File("config.json")
            if (!f.exists()) f.createNewFile()
            val text = f.readLines().joinToString("")
            return if (text.length < 2)
                LinkedTreeMap<String, String>()
            else
                gson.fromJson(text, object : TypeToken<Map<String, String>>() {}.type)
        }

    private fun saveToFile(key: String, value: String) {
        val save = configFile
        save[key] = value
        val writer = FileWriter("config.json")
        writer.write(gson.toJson(save))
        writer.close()
    }

    fun getCachedTextField(key: String, defaultValue: String = ""): JTextField {
        val field = JTextField(configFile.getOrDefault(key, defaultValue))
        field.preferredSize = Dimension(300, 20)

        field.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                saveToFile(key, field.text)
            }

            override fun removeUpdate(e: DocumentEvent) {
                saveToFile(key, field.text)
            }

            override fun changedUpdate(e: DocumentEvent) {
            }
        })
        return field
    }

    fun getCachedCheckBox(key: String, defaultValue: Boolean = false, onChange: (Boolean) -> Unit = {}): JCheckBox {
        val box = JCheckBox()
        box.isSelected = configFile.getOrDefault(key, if (defaultValue) "True" else "False") == "True"

        box.addItemListener {
            saveToFile(key, if (box.isSelected) "True" else "False")
            onChange(box.isSelected)
        }

        return box
    }

    val runningLabel: JLabel
    val stoppedLabel: JLabel

    init {
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.preferredSize = Dimension(505, 370)
        frame.isResizable = false
        frame.iconImages = arrayListOf(
            Toolkit.getDefaultToolkit().getImage(javaClass.getResource("/icon16.png")),
            Toolkit.getDefaultToolkit().getImage(javaClass.getResource("/icon32.png")),
            Toolkit.getDefaultToolkit().getImage(javaClass.getResource("/icon64.png")),
            Toolkit.getDefaultToolkit().getImage(javaClass.getResource("/icon128.png"))
        )
        frame.contentPane.background = Color.DARK_GRAY

        val runningIcon = ImageIcon(Toolkit.getDefaultToolkit().getImage(javaClass.getResource("/lightbulbspinning.gif")))
        runningLabel = JLabel(runningIcon)
        runningLabel.bounds = Rectangle(400, 225, runningIcon.iconWidth, runningIcon.iconHeight)
        runningLabel.isVisible = false
        frame.add(runningLabel)

        val stoppedIcon = ImageIcon(Toolkit.getDefaultToolkit().getImage(javaClass.getResource("/lightbulbstopped.png")))
        stoppedLabel = JLabel(stoppedIcon)
        stoppedLabel.bounds = Rectangle(400, 225, stoppedIcon.iconWidth, stoppedIcon.iconHeight)
        frame.add(stoppedLabel)

        val updateLeaderboardButton = JButton("Force Update")
        updateLeaderboardButton.addActionListener {
        }
        //frame.add(updateLeaderboardButton)

        frame.layout = null
        frame.setLocationRelativeTo(null)
        frame.pack()
        frame.isVisible = true
    }

}