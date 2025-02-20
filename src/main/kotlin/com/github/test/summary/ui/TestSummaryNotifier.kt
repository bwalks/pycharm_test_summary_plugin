package com.github.test.summary.ui

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import javax.swing.SwingUtilities
import com.intellij.openapi.project.DumbAwareAction

class TestSummaryNotifier {
    fun showSummary(project: Project, testName: String, status: String, summary: String) {
        // Update tool window
        SwingUtilities.invokeLater {
            TestSummaryToolWindow.addTestResult(testName, status, summary)
            
            // Ensure tool window is visible
            ToolWindowManager.getInstance(project)
                .getToolWindow("Test Summary")
                ?.show()
        }

        // Show notification
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Test Summary")
            .createNotification(
                "Test Failure Summary",
                "$testName: $summary",
                NotificationType.INFORMATION
            )
            .addAction(object : DumbAwareAction("Open Summary") {
                override fun actionPerformed(e: AnActionEvent) {
                    ToolWindowManager.getInstance(project)
                        .getToolWindow("Test Summary")
                        ?.activate(null)
                }
            })
            .notify(project)
    }
}
