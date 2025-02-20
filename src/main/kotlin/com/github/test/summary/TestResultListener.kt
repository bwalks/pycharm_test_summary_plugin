package com.github.test.summary

import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsListener
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.ProjectManager
import com.github.test.summary.ui.TestSummaryNotifier
import com.github.test.summary.ui.TestSummaryToolWindow
import com.intellij.execution.testframework.TestStatusListener
import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.sm.runner.SMTestProxy.SMRootTestProxy

class TestResultListener : TestStatusListener(), SMTRunnerEventsListener {
    private val openAiService = OpenAiService()
    private val notifier = TestSummaryNotifier()
    private val LOG = Logger.getInstance(TestResultListener::class.java)

    override fun onTestingStarted(testsRoot: SMRootTestProxy) {
        LOG.warn("Testing started")
        TestSummaryToolWindow.clear()
    }

    override fun onTestingFinished(testsRoot: SMRootTestProxy) {
        LOG.warn("Testing finished")
        val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return
        
        LOG.warn("Number of tests: ${testsRoot.allTests.size}")
        testsRoot.allTests.filter { it.isLeaf }.forEach { test ->
            val status = when {
                test.isDefect -> "Failed"
                test.isPassed -> "Passed"
                else -> "Skipped"
            }
            
            LOG.warn("Test: ${test.name}, Status: $status")

            if (test.isDefect) {
                val errorMessage = test.errorMessage
                LOG.warn("Error message: $errorMessage")
                if (errorMessage != null) {
                    val summary = openAiService.getSummary(errorMessage)
                    LOG.warn("Generated summary: $summary")
                    TestSummaryToolWindow.addTestResult(test.name, status, summary)
                }
            }
        }
    }

    // TestStatusListener overrides
    override fun testSuiteFinished(root: AbstractTestProxy?) {
        LOG.warn("Test suite finished")
        if (root is SMRootTestProxy) {
            onTestingFinished(root)
        }
    }

    override fun onTestsCountInSuite(count: Int) {
        LOG.warn("Tests count in suite: $count")
    }
    
    override fun onSuiteFinished(suite: SMTestProxy) {
        LOG.warn("Suite finished: ${suite.name}")
    }
    
    override fun onSuiteStarted(suite: SMTestProxy) {
        LOG.warn("Suite started: ${suite.name}")
    }
    
    override fun onTestStarted(test: SMTestProxy) {
        LOG.warn("Test started: ${test.name}")
    }
    
    override fun onTestFinished(test: SMTestProxy) {
        LOG.warn("Test finished: ${test.name}")
        if (test.isPassed) {
            LOG.warn("Adding passed test to tool window: ${test.name}")
            TestSummaryToolWindow.addTestResult(test.name, "Passed", "Test completed successfully")
        }
    }
    
    override fun onTestFailed(test: SMTestProxy) {
        LOG.warn("Test failed: ${test.name}")
        val errorMessage = test.errorMessage
        if (errorMessage != null) {
            val summary = openAiService.getSummary(errorMessage)
            LOG.warn("Adding test result to tool window: ${test.name}, Failed, $summary")
            TestSummaryToolWindow.addTestResult(test.name, "Failed", summary)
        }
    }
    
    override fun onTestIgnored(test: SMTestProxy) {
        LOG.warn("Test ignored: ${test.name}")
    }

    override fun onCustomProgressTestStarted() {}
    override fun onCustomProgressTestFinished() {}
    override fun onCustomProgressTestFailed() {}
    override fun onSuiteTreeNodeAdded(testProxy: SMTestProxy) {}
    override fun onSuiteTreeStarted(suite: SMTestProxy) {}
    override fun onCustomProgressTestsCategory(categoryName: String?, testCount: Int) {}
}
