package com.esportsarena.challongebracket.util

import com.esportsarena.challongebracket.gui
import org.xml.sax.InputSource
import java.io.StringReader
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

object VMix {

    val vmixIp: String
        get() {
            return gui.getString("Vmix IP")!!
        }

    fun getInputByName(name: String): String? {
        try {
            val vmixxml = URL("http://$vmixIp/api").openStream().bufferedReader().lines().toList().joinToString("")

            val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val inputs = builder.parse(InputSource(StringReader(vmixxml))).getElementsByTagName("input")
            for (i in 0 until inputs.length) {
                val node = inputs.item(i)
                if (node.attributes.getNamedItem("title").textContent.lowercase().replace(" ", "") == name.lowercase()
                        .replace(" ", "")
                ) {
                    return node.attributes.getNamedItem("key").textContent
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

    fun setLayer(inputName: String, index: Int, layerName: String) {
        try {
            val input = getInputByName(inputName)
            val layer = getInputByName("${layerName}.png") ?: getInputByName(layerName)
            val setLayer =
                URL("http://$vmixIp/api/?Function=SetLayer&Input=${input}&Value=${index},${layer}").openStream()
            setLayer.close()
        } catch (e: Exception) {
        }
    }

    fun setImage(inputName: String, fileName: String, selectedName: String) {
        try {
            val input = getInputByName(inputName)
            val url = if (fileName.isBlank())
                "http://$vmixIp/api/?Function=SetImage&Input=${input}&SelectedName=${selectedName}"
            else
                "http://$vmixIp/api/?Function=SetImage&Input=${input}&Value=${f(fileName)}&SelectedName=${selectedName}"
            val setLayer = URL(url).openStream()
            setLayer.close()
        } catch (e: Exception) {
        }
    }

    fun overlayInOrOutIfNotAvailable(overlayName: String, layer: Int) {
        try {
            try {
                val input = getInputByName("${overlayName}.png") ?: getInputByName(overlayName)
                if (input == null) {
                    logoOut(layer)
                } else {
                    val logoIn = URL("http://$vmixIp/api/?Function=OverlayInput${layer}In&Input=${input}").openStream()
                    logoIn.close()
                }
            } catch (_: Exception) {
                println("failed to put logo in")
            }
        } catch (e: Exception) {
        }
    }

    fun logoOut(layer: Int = 2) {
        try {
            try {
                URL("http://$vmixIp/api/?Function=OverlayInput${layer}Out").openStream().close()
            } catch (_: Exception) {
                println("failed to take logo out")
            }
        } catch (e: Exception) {
        }
    }

    fun setTitleText(value: String, inputName: String, selectedName: String) {
        try {
            val input = getInputByName(inputName) ?: return
            URL("http://$vmixIp/api/?Function=SetText&Value=${f(value)}&Input=${f(input)}&SelectedName=${f(selectedName)}").openStream()
                .close()
        } catch (_: Exception) {
            println("failed to take logo out")
        }
    }

    fun setImageVisibleOff(inputName: String, selectedName: String) {
        try {
            val input = getInputByName(inputName) ?: return
            URL("http://$vmixIp/api/?Function=SetImageVisibleOff&Input=${f(input)}&SelectedName=${f(selectedName)}").openStream()
                .close()
        } catch (_: Exception) {
            println("failed to take logo out")
        }
    }

    fun setImageVisibleOn(inputName: String, selectedName: String) {
        try {
            val input = getInputByName(inputName) ?: return
            URL("http://$vmixIp/api/?Function=SetImageVisibleOn&Input=${f(input)}&SelectedName=${f(selectedName)}").openStream()
                .close()
        } catch (_: Exception) {
            println("failed to take logo out")
        }
    }

    fun f(i: String): String {
        var s = i
        s = s.replace("#", "%23")
        return s.replace(" ", "%20")
    }
}