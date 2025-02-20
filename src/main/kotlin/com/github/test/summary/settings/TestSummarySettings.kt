package com.github.test.summary.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "TestSummarySettings",
    storages = [Storage("testSummarySettings.xml")]
)
@Service
class TestSummarySettings : PersistentStateComponent<TestSummarySettings> {
    var openAiModel: String = "gpt-3.5-turbo"
    var systemPrompt: String = "You are a helpful assistant that analyzes test failures and provides concise summaries."
    var userPromptTemplate: String = "Summarize this test failure in one sentence: {error_message}"
    var maxTokens: Int = 150
    var temperature: Double = 0.7

    override fun getState(): TestSummarySettings = this

    override fun loadState(state: TestSummarySettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): TestSummarySettings =
            service<TestSummarySettings>()
    }
}
