package com.github.test.summary

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.ProjectManager

class SummaryPresenter {
    fun showSummary(testName: String, summary: String) {
        val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return
        
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Test Summary")
            .createNotification(
                "Test Failure Summary",
                "$testName: $summary",
                NotificationType.INFORMATION
            )
            .notify(project)
    }
}