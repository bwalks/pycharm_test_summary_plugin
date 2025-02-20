package com.github.test.summary.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.ui.content.ContentFactory
import com.intellij.openapi.diagnostic.Logger
import javax.swing.*
import javax.swing.JButton
import javax.swing.table.DefaultTableModel
import java.awt.*
import java.awt.Dialog
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import javax.swing.table.DefaultTableCellRenderer
import com.intellij.ui.components.JBTextArea
import java.util.Vector

class TestSummaryToolWindow : ToolWindowFactory {
    private var tableModel: DefaultTableModel? = null
    private var detailsArea: JBTextArea? = null
    private val LOG = Logger.getInstance(TestSummaryToolWindow::class.java)

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        LOG.warn("Creating tool window content")
        instance = this
        tableModel = DefaultTableModel(Vector<String>().apply {
            add("Test Name")
            add("Summary")
        }, 0)
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(
            createToolWindowPanel(),
            "",
            false
        )
        toolWindow.contentManager.addContent(content)
        LOG.warn("Tool window content created")
    }

    private fun createToolWindowPanel(): JPanel {
        val mainPanel = JPanel(BorderLayout())
        
        // Header with title and clear button
        val headerPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(5)
            
            // Title on the left
            add(JBLabel("Test Summary").apply {
                font = font.deriveFont(Font.BOLD, 16f)
            }, BorderLayout.WEST)
            
            // Clear button on the right
            add(JButton("Clear Results").apply {
                addActionListener {
                    clear()
                }
            }, BorderLayout.EAST)
        }
        mainPanel.add(headerPanel, BorderLayout.NORTH)

        // Split pane for table and details
        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)
        splitPane.border = JBUI.Borders.empty(10)

        // Table
        val table = JBTable(tableModel).apply {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            columnModel.getColumn(0).preferredWidth = 200
            columnModel.getColumn(1).preferredWidth = 600
            autoCreateRowSorter = true
            rowHeight = 36
            
            // Custom renderer for the summary column to handle word wrap
            columnModel.getColumn(1).apply {
                cellRenderer = object : DefaultTableCellRenderer() {
                    private val textArea = JTextArea()
                    init {
                        textArea.lineWrap = true
                        textArea.wrapStyleWord = true
                        textArea.border = JBUI.Borders.empty(5)
                    }
                    
                    override fun getTableCellRendererComponent(
                        table: JTable,
                        value: Any?,
                        isSelected: Boolean,
                        hasFocus: Boolean,
                        row: Int,
                        column: Int
                    ): Component {
                        textArea.text = value?.toString()?.let {
                            if (it.length > 100) it.substring(0, 97) + "..." else it
                        } ?: ""
                        textArea.background = if (isSelected) table.selectionBackground else table.background
                        textArea.foreground = if (isSelected) table.selectionForeground else table.foreground
                        return textArea
                    }
                }
            }

            // Add mouse listener for expanding summary
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    val row = rowAtPoint(e.point)
                    val col = columnAtPoint(e.point)
                    if (col == 1 && row >= 0) { // Summary column
                        val testName = getValueAt(row, 0) as String
                        val summary = getValueAt(row, 1) as String
                        showExpandedSummary(testName, summary)
                    }
                }
            })

            // Add tooltip for summary column
            columnModel.getColumn(1).apply {
                headerRenderer = object : DefaultTableCellRenderer() {
                    init {
                        toolTipText = "Click on a summary to expand"
                        horizontalAlignment = CENTER
                    }
                }
            }

            // Add selection listener
            selectionModel.addListSelectionListener { e ->
                if (!e.valueIsAdjusting) {
                    val selectedRow = selectedRow
                    if (selectedRow >= 0) {
                        val testName = getValueAt(selectedRow, 0) as String
                        val summary = getValueAt(selectedRow, 1) as String
                        detailsArea?.text = """
                            Test: $testName
                            
                            Summary:
                            $summary
                        """.trimIndent()
                    }
                }
            }
        }

        val tableScrollPane = JBScrollPane(table)
        splitPane.topComponent = tableScrollPane

        // Details panel
        detailsArea = JBTextArea().apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            border = JBUI.Borders.empty(10)
            font = Font("JetBrains Mono", Font.PLAIN, 12)
        }
        
        val detailsScrollPane = JBScrollPane(detailsArea)
        detailsScrollPane.preferredSize = Dimension(0, 150)
        splitPane.bottomComponent = detailsScrollPane

        splitPane.dividerLocation = 300
        mainPanel.add(splitPane, BorderLayout.CENTER)

        return mainPanel
    }

    private fun showExpandedSummary(testName: String, summary: String) {
        val dialog = JDialog(SwingUtilities.getWindowAncestor(detailsArea), "Test Summary", Dialog.ModalityType.MODELESS)
        dialog.layout = BorderLayout()

        val summaryArea = JTextArea().apply {
            text = """
                Test: $testName
                
                Summary:
                $summary
            """.trimIndent()
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            font = Font("JetBrains Mono", Font.PLAIN, 12)
            border = JBUI.Borders.empty(10)
        }

        dialog.add(JBScrollPane(summaryArea), BorderLayout.CENTER)
        dialog.preferredSize = Dimension(600, 400)
        dialog.pack()
        dialog.setLocationRelativeTo(detailsArea)
        dialog.isVisible = true
    }

    companion object {
        private var instance: TestSummaryToolWindow? = null
        private val LOG = Logger.getInstance(TestSummaryToolWindow::class.java)
        
        fun addTestResult(testName: String, status: String, summary: String) {
            LOG.warn("Adding test result: $testName, $status")
            SwingUtilities.invokeLater {
                instance?.let { window ->
                    window.tableModel?.addRow(Vector<String>().apply {
                        add(testName)
                        add(summary)
                    })
                    window.detailsArea?.text = """
                        Most Recent Test:
                        Test: $testName
                        
                        Summary:
                        $summary
                    """.trimIndent()
                }
            }
        }

        fun clear() {
            LOG.warn("Clearing test results")
            SwingUtilities.invokeLater {
                instance?.let { window ->
                    window.tableModel?.rowCount = 0
                    window.detailsArea?.text = ""
                }
            }
        }

        private fun TestSummaryToolWindow.clear() {
            LOG.warn("Clearing test results from instance")
            SwingUtilities.invokeLater {
                tableModel?.rowCount = 0
                detailsArea?.text = ""
            }
        }
    }
}
