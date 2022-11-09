package com.esportsarena.challongebracket.gui

import java.awt.Color
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.JLabel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class FzzyTextField(val gui: GUI, key: String, x: Int, y: Int, width: Int, defaultValue: String = "", color: Color = Color.LIGHT_GRAY) {

    val box: JRectangle
    val label: JLabel
    val field: JTextField

    init {
        field = getCachedTextField(key, defaultValue)
        field.addFocusListener(object: FocusListener {
            override fun focusGained(p0: FocusEvent) {
                field.select(0, field.text.length)
            }
            override fun focusLost(p0: FocusEvent) {
                field.select(0, 0)
            }
        })
        field.bounds = Rectangle(x + 4, y + 21, width - 8, 17)
        gui.frame.add(field)

        label = JLabel(key)
        label.bounds = Rectangle(x + 4, y + 4, width - 8, 17)
        gui.frame.add(label)

        box = JRectangle(Rectangle(x, y, width, 41), color)
        gui.frame.add(box)
    }

    fun getCachedTextField(key: String, defaultValue: String = ""): JTextField {
        val field = JTextField(gui.configFile.getOrDefault(key, defaultValue))
        field.preferredSize = Dimension(300, 20)

        field.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                gui.saveToFile(key, field.text)
            }

            override fun removeUpdate(e: DocumentEvent) {
                gui.saveToFile(key, field.text)
            }

            override fun changedUpdate(e: DocumentEvent) {
            }
        })
        return field
    }

}