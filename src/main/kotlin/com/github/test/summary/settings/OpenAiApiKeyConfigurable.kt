package com.github.test.summary.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.panel
import com.intellij.openapi.ui.ComboBox
import javax.swing.JComponent
import javax.swing.DefaultComboBoxModel

class OpenAiApiKeyConfigurable : Configurable {
    private var apiKeyField: JBPasswordField? = null
    private var modelComboBox: ComboBox<String>? = null
    private val credentialAttributes = CredentialAttributes("OpenAI_API_Key")

    private val availableModels = arrayOf(
        "gpt-4-turbo-preview",
        "gpt-4",
        "gpt-3.5-turbo",
        "gpt-3.5-turbo-16k"
    )

    override fun getDisplayName(): String = "OpenAI Settings"

    override fun createComponent(): JComponent {
        apiKeyField = JBPasswordField()
        modelComboBox = ComboBox(DefaultComboBoxModel(availableModels))
        
        // Load existing API key
        PasswordSafe.instance.getPassword(credentialAttributes)?.let {
            apiKeyField?.text = it
        }

        // Load existing model selection
        val settings = TestSummarySettings.getInstance()
        modelComboBox?.selectedItem = settings.openAiModel

        return panel {
            group("API Configuration") {
                row("API Key:") {
                    cell(apiKeyField!!)
                        .comment("Enter your OpenAI API key")
                }
                row("Model:") {
                    cell(modelComboBox!!)
                        .comment("Select the OpenAI model to use")
                }
            }
        }
    }

    override fun isModified(): Boolean {
        val storedKey = PasswordSafe.instance.getPassword(credentialAttributes) ?: ""
        val settings = TestSummarySettings.getInstance()
        return apiKeyField?.password?.joinToString("") != storedKey ||
               modelComboBox?.selectedItem != settings.openAiModel
    }

    override fun apply() {
        val newKey = apiKeyField?.password?.joinToString("") ?: ""
        PasswordSafe.instance.setPassword(credentialAttributes, newKey)

        val settings = TestSummarySettings.getInstance()
        settings.openAiModel = modelComboBox?.selectedItem as? String ?: "gpt-3.5-turbo"
    }

    override fun reset() {
        PasswordSafe.instance.getPassword(credentialAttributes)?.let {
            apiKeyField?.text = it
        }
        
        val settings = TestSummarySettings.getInstance()
        modelComboBox?.selectedItem = settings.openAiModel
    }

    override fun disposeUIResources() {
        apiKeyField = null
        modelComboBox = null
    }
}
