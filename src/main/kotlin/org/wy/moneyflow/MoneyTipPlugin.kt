package org.wy.moneyflow

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import org.wy.moneyflow.listener.DocumentChangeListener
import org.wy.moneyflow.model.PluginConfig
import org.wy.moneyflow.statusbar.MoneyTipStatusBarWidget
import org.wy.moneyflow.util.StockUtil
import org.wy.moneyflow.util.TimeUtil
import java.time.LocalDate
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 插件入口类
 */
class MoneyTipPlugin: ApplicationActivationListener {
    private val scheduler = Executors.newScheduledThreadPool(1)
    private val config = PluginConfig.load()

    init {
        // 1. 每日重置今日金额
        if (LocalDate.now().dayOfMonth != PluginConfig.load().retireDate.dayOfMonth) {
            PluginConfig.resetTodayAmount()
        }

        // 2. 注册编辑器监听（打字/删行）
        // 替换原来的代码：
        val editorFactory = EditorFactory.getInstance()
        val editorFactoryListener = object : EditorFactoryListener {
            override fun editorCreated(event: com.intellij.openapi.editor.event.EditorFactoryEvent) {
                DocumentChangeListener.register(event.editor)
            }

            override fun editorReleased(event: com.intellij.openapi.editor.event.EditorFactoryEvent) {
                // 处理编辑器释放事件（如果需要）
            }
        }
        editorFactory.addEditorFactoryListener(editorFactoryListener, com.intellij.openapi.Disposable { })

        // 3. 定时任务：刷新状态栏、提醒
        scheduler.scheduleAtFixedRate({
            ApplicationManager.getApplication().invokeLater {
                // 刷新所有项目的状态栏
                ProjectManager.getInstance().openProjects.forEach { project ->
                    val statusBar = WindowManager.getInstance().getStatusBar(project)
                    statusBar?.getWidget("moneyTipStatusBarWidget")?.let {
                        (it as MoneyTipStatusBarWidget).updateText()
                    }
                }

                // 时间提醒
                timeRemind()

                // 自定义提醒
                customRemind()

                // 股票异常提醒
                stockRemind()
            }
        }, 0, 1, TimeUnit.MINUTES)
    }

    // 时间提醒
    private fun timeRemind() {
        val now = java.time.LocalTime.now()
        if (now.minute % config.remindInterval == 0 && now.second < 10) {
            val offWorkCountdown = TimeUtil.getOffWorkCountdown(config.offWorkTime)
            val notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("MoneyTip Reminder")
                .createNotification(
                    "⏰ 时间提醒",
                    "距离下班还有 $offWorkCountdown | 今日已赚：${String.format("%.2f", config.todayEarned - config.todayDeducted)} 元",
                    NotificationType.INFORMATION
                )
            notification.notify(null)
        }
    }

    // 自定义提醒
    private fun customRemind() {
        if (!config.enableCustomReminder) return

        val now = java.time.LocalTime.now()

        config.reminders.forEach { reminder ->
            try {
                when (reminder.type) {
                    "TIME_POINT" -> {
                        // 时间点提醒
                        val remindTime = java.time.LocalTime.parse(reminder.value)
                        if (now.hour == remindTime.hour && now.minute == remindTime.minute && now.second < 10) {
                            val notification = NotificationGroupManager.getInstance()
                                .getNotificationGroup("MoneyTip Reminder")
                                .createNotification(
                                    "⏰ 自定义提醒",
                                    reminder.message,
                                    NotificationType.INFORMATION
                                )
                            notification.notify(null)
                        }
                    }
                    "PERIODIC" -> {
                        // 周期性提醒（分钟）
                        val interval = reminder.value.toInt()
                        if (now.minute % interval == 0 && now.second < 10) {
                            val notification = NotificationGroupManager.getInstance()
                                .getNotificationGroup("MoneyTip Reminder")
                                .createNotification(
                                    "⏰ 自定义提醒",
                                    reminder.message,
                                    NotificationType.INFORMATION
                                )
                            notification.notify(null)
                        }
                    }
                }
            } catch (e: Exception) {
                // 忽略解析错误
            }
        }
    }

    // 股票异常提醒
    private fun stockRemind() {
        val stockChange = StockUtil.getStockChange(config.stockCode)
        if (Math.abs(stockChange) >= config.stockRemindThreshold) {
            val notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("MoneyTip Stock Reminder")
                .createNotification(
                    "📈 股票提醒",
                    "股票 ${config.stockCode} 涨跌幅达到 ${String.format("%.2f", stockChange)}%，超过阈值 ${config.stockRemindThreshold}%",
                    if (stockChange > 0) NotificationType.INFORMATION else NotificationType.WARNING
                )
            notification.notify(null)
        }
    }

    // 插件销毁时关闭定时任务
    fun dispose() {
        scheduler.shutdown()
    }
}