package com.github.test.summary.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.JLabel
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.Insets

class TestSummarySettingsConfigurable : Configurable {
    private var settingsComponent: TestSummarySettingsComponent? = null

    override fun getDisplayName(): String = "Test Summary Settings"

    override fun createComponent(): JComponent {
        settingsComponent = TestSummarySettingsComponent()
        return settingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings = TestSummarySettings.getInstance()
        return settingsComponent?.let { component ->
            component.openAiModel != settings.openAiModel ||
            component.systemPrompt != settings.systemPrompt ||
            component.userPromptTemplate != settings.userPromptTemplate
        } ?: false
    }

    override fun apply() {
        val settings = TestSummarySettings.getInstance()
        settingsComponent?.let { component ->
            settings.openAiModel = component.openAiModel
            settings.systemPrompt = component.systemPrompt
            settings.userPromptTemplate = component.userPromptTemplate
        }
    }

    override fun reset() {
        val settings = TestSummarySettings.getInstance()
        settingsComponent?.let { component ->
            component.openAiModel = settings.openAiModel
            component.systemPrompt = settings.systemPrompt
            component.userPromptTemplate = settings.userPromptTemplate
        }
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}

private class TestSummarySettingsComponent {
    val panel: JPanel = JPanel(GridBagLayout())
    private val modelField = JTextField()
    private val systemPromptField = JTextField()
    private val userPromptField = JTextField()

    init {
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(5, 5, 5, 5)
            gridx = 0
            weightx = 0.0
        }

        panel.add(JLabel("OpenAI Model:"), gbc.apply { gridy = 0 })
        panel.add(modelField, gbc.apply { 
            gridx = 1
            weightx = 1.0 
        })

        panel.add(JLabel("System Prompt:"), gbc.apply { 
            gridx = 0
            gridy = 1
            weightx = 0.0
        })
        panel.add(systemPromptField, gbc.apply { 
            gridx = 1
            weightx = 1.0
        })

        panel.add(JLabel("User Prompt Template:"), gbc.apply { 
            gridx = 0
            gridy = 2
            weightx = 0.0
        })
        panel.add(userPromptField, gbc.apply { 
            gridx = 1
            weightx = 1.0
        })
    }

    var openAiModel: String
        get() = modelField.text
        set(value) { modelField.text = value }

    var systemPrompt: String
        get() = systemPromptField.text
        set(value) { systemPromptField.text = value }

    var userPromptTemplate: String
        get() = userPromptField.text
        set(value) { userPromptField.text = value }
}