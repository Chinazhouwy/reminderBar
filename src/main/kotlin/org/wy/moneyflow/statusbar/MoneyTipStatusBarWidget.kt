package org.wy.moneyflow.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer
import org.wy.moneyflow.model.PluginConfig
import org.wy.moneyflow.util.StockUtil
import org.wy.moneyflow.util.TimeUtil
import java.awt.Component
import java.awt.event.MouseEvent
import java.time.LocalTime
import javax.swing.JLabel

/**
 * 修复后：适配 2022.2.4 SDK 的 StatusBarWidget 接口
 */
class MoneyTipStatusBarWidget(private val project: Project) : StatusBarWidget {
    private val label = JLabel()
    private val config = PluginConfig.load()
    private val presentation = MyPresentation()

    init {
        updateText()
    }

    fun updateText() {
        val offWorkCountdown = TimeUtil.getOffWorkCountdown(config.offWorkTime)
        val retireDays = TimeUtil.getRetireCountdown(config.retireDate)
        val todayProfit = config.todayEarned - config.todayDeducted
        val stockInfo = StockUtil.getStockInfo(config.stockCode)
        var text = String.format(
            " 🕒下班：%s | 🎯退休：%d天 | 💰今日：%.2f | 📈%s ",
            offWorkCountdown, retireDays, todayProfit, stockInfo
        )
        val config = PluginConfig.load()
        if(config.enableCustomReminder){
            config.reminders.forEach { reminder ->
                val currentTime = LocalTime.now()
                when (reminder.type) {
                    "TIME_POINT" -> {
                        val reminderTime = LocalTime.parse(reminder.value)
                        if (currentTime.isAfter(reminderTime)) {
                            // 提示消息
                            text += " | ⏰${reminder.message}"
                        }
                    }
                    "PERIODIC" -> {
                        val reminderInterval = reminder.value.toInt()
                        // 提示消息
                        text += " | ⏰${reminder.message}"
                    }
                }
            }
        }

        label.text = text
    }

    override fun ID(): String = "moneyTipStatusBarWidget"

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = presentation

    override fun install(statusBar: StatusBar) {
//        statusBar.addWidget(this)
    }


    override fun dispose() {}

    // 内部类实现 Presentation，避免接口兼容问题
    private inner class MyPresentation : StatusBarWidget.TextPresentation {
        override fun getText(): String = label.text ?: ""

        override fun getTooltipText(): String = "MoneyTip 插件：点击刷新数据"

        override fun getClickConsumer(): Consumer<MouseEvent> {
            return Consumer { updateText() }
        }

        override fun getAlignment(): Float = Component.CENTER_ALIGNMENT

//        override fun getTextColor(): StatusBarWidget.TextColor? = StatusBarWidget.TextColor.NORMAL
    }

    class Factory : StatusBarWidgetFactory {
        override fun getId(): String = "moneyTipStatusBarWidget"

        override fun getDisplayName(): String = "MoneyTip"

        override fun isAvailable(project: Project): Boolean = true

        override fun createWidget(project: Project): StatusBarWidget = MoneyTipStatusBarWidget(project)

        override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true

        // 添加这个方法
        override fun disposeWidget(widget: StatusBarWidget) {
            // 通常可以留空，或者调用 widget 的 dispose 方法（如果有的话）
            if (widget is MoneyTipStatusBarWidget) {
                widget.dispose()
            }
        }
    }
}