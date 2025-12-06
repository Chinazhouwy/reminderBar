package org.wy.moneyflow.config

import com.intellij.ide.plugins.PluginManagerCore.getLogger
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.*
import com.intellij.ui.layout.*
import org.wy.moneyflow.model.PluginConfig
import org.wy.moneyflow.model.CustomReminder
import java.awt.event.ActionEvent
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException
import javax.swing.*
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.jetbrains.rd.util.LogLevel
import com.jetbrains.rd.util.getLogger
import com.jetbrains.rd.util.log
import org.wy.moneyflow.statusbar.MoneyTipStatusBarWidget
import java.awt.FlowLayout

/**
 * 插件自定义配置界面
 */
class MoneyTipConfigurable : Configurable {
    private val config = PluginConfig.load()

    // 配置控件
    private val hourlyWageField = JBTextField(config.hourlyWage.toString())
    private val stockCodeField = JBTextField(config.stockCode)
    private val offWorkTimeField = JBTextField(config.offWorkTime.toString())
    private val retireDateField = JBTextField(config.retireDate.toString())
    private val remindIntervalField = JBTextField(config.remindInterval.toString())
    private val stockRemindThresholdField = JBTextField(config.stockRemindThreshold.toString())

    private val stockFundUrlField = JBTextField(config.stockFundUrl)
    private val enableCustomReminderCheckBox = JBCheckBox("启用自定义提醒", config.enableCustomReminder)
    private val enableMoneyAnimationCheckBox = JBCheckBox("开启金钱四溅效果", config.enableMoneyAnimation)

    // 自定义提醒相关控件
    private val reminderPanel = JPanel()
    private val reminderControls = mutableListOf<ReminderControl>()

    // 自定义提醒控件容器类
    private class ReminderControl {
        val typeComboBox = ComboBox(arrayOf("时间点提醒", "周期性提醒"))
        val valueField = JBTextField(8)
        val messageField = JBTextField(20)
        val removeButton = JButton("删除")
        var onRemove: (() -> Unit)? = null

        init {
            removeButton.addActionListener { onRemove?.invoke() }
        }

        fun toReminder(): CustomReminder {
            return CustomReminder(
                type = if (typeComboBox.selectedItem == "时间点提醒") "TIME_POINT" else "PERIODIC",
                value = valueField.text,
                message = messageField.text
            )
        }

        fun fromReminder(reminder: CustomReminder) {
            typeComboBox.selectedItem = if (reminder.type == "TIME_POINT") "时间点提醒" else "周期性提醒"
            valueField.text = reminder.value
            messageField.text = reminder.message
        }
    }

    // 配置界面UI
    override fun createComponent(): JComponent {
        // 初始化自定义提醒控件
        reminderControls.clear()
        config.reminders.forEach { reminder ->
            val control = ReminderControl()
            control.fromReminder(reminder)
            control.onRemove = { removeReminder(control) }
            reminderControls.add(control)
        }

        // 如果没有提醒，添加一个默认的
        if (reminderControls.isEmpty()) {
            addReminder()
        }

        return panel {
            row("时薪（元/小时）：") {
                hourlyWageField()
            }
            row("股票代码（如600000）：") {
                stockCodeField()
            }
            row("下班时间（HH:mm）：") {
                offWorkTimeField()
            }
            row("退休日期（yyyy-MM-dd）：") {
                retireDateField()
            }
            row("时间提醒间隔（分钟）：") {
                remindIntervalField()
            }
            row("股票涨幅提醒阈值（%）：") {
                stockRemindThresholdField()
            }
            row("股票基金数据URL:") {
                stockFundUrlField()
            }
            row("功能设置:") {
                enableMoneyAnimationCheckBox()
            }
            row("自定义提醒:") {
                enableCustomReminderCheckBox()
            }

            // 自定义提醒配置区域
            titledRow("提醒列表") {
                row { reminderPanel() }
                updateReminderPanel()
                row {
                    button("添加提醒") { addReminder() }
                }
            }

            noteRow("提示：金钱四溅效果可动态开关 | 今日金额可手动重置（重启后自动重置）")
        }
    }

    // 更新提醒面板
    private fun updateReminderPanel() {
        reminderPanel.removeAll()
        reminderPanel.layout = BoxLayout(reminderPanel, BoxLayout.Y_AXIS)
        reminderControls.forEach { control ->
            val rowPanel = JPanel().apply {
                layout = FlowLayout(FlowLayout.LEFT)
                border = javax.swing.border.EmptyBorder(5, 0, 5, 0)
            }
            rowPanel.add(control.typeComboBox)
            rowPanel.add(JLabel(if (control.typeComboBox.selectedItem == "时间点提醒") "时间（HH:mm）：" else "周期（分钟）："))
            rowPanel.add(control.valueField)
            rowPanel.add(JLabel("消息："))
            rowPanel.add(control.messageField)
            rowPanel.add(control.removeButton)
            reminderPanel.add(rowPanel)
        }
        // 添加事件监听器，当选择类型变化时更新标签
        reminderControls.forEach { control ->
            control.typeComboBox.addActionListener {
                updateReminderPanel()
            }
        }
        reminderPanel.revalidate()
        reminderPanel.repaint()
        // 刷新整个配置面板
        reminderPanel.parent?.parent?.parent?.revalidate()
        reminderPanel.parent?.parent?.parent?.repaint()
    }

    // 添加提醒
    private fun addReminder() {
        val control = ReminderControl()
        control.onRemove = { removeReminder(control) }
        reminderControls.add(control)
        updateReminderPanel()
    }

    // 删除提醒
    private fun removeReminder(control: ReminderControl) {
        if (reminderControls.size > 1) {
            reminderControls.remove(control)
            updateReminderPanel()
        }
    }

    // 验证配置
    override fun isModified(): Boolean {
        return try {
            hourlyWageField.text.toDouble() != config.hourlyWage ||
                    stockCodeField.text != config.stockCode ||
                    LocalTime.parse(offWorkTimeField.text) != config.offWorkTime ||
                    LocalDate.parse(retireDateField.text) != config.retireDate ||
                    remindIntervalField.text.toInt() != config.remindInterval ||
                    stockRemindThresholdField.text.toDouble() != config.stockRemindThreshold ||
                    stockFundUrlField.text != config.stockFundUrl ||
                    enableCustomReminderCheckBox.isSelected != config.enableCustomReminder ||
                    enableMoneyAnimationCheckBox.isSelected != config.enableMoneyAnimation ||
                    reminderControls.map { it.toReminder() } != config.reminders
        } catch (e: Exception) {
            true
        }
    }

    // 应用配置
    @Throws(ConfigurationException::class)
    override fun apply() {
        try {
            config.hourlyWage = hourlyWageField.text.toDouble()
            config.stockCode = stockCodeField.text
            config.offWorkTime = LocalTime.parse(offWorkTimeField.text)
            config.retireDate = LocalDate.parse(retireDateField.text)
            config.remindInterval = remindIntervalField.text.toInt()
            config.stockRemindThreshold = stockRemindThresholdField.text.toDouble()
            config.stockFundUrl = stockFundUrlField.text
            config.enableCustomReminder = enableCustomReminderCheckBox.isSelected
            config.enableMoneyAnimation = enableMoneyAnimationCheckBox.isSelected
            config.reminders = reminderControls.map { it.toReminder() }

            PluginConfig.save(config)
//            getLogger().info(  PluginConfig.printData() )
            println(PluginConfig.printData())
        } catch (e: DateTimeParseException) {
            throw ConfigurationException("时间格式错误！正确格式：HH:mm（下班时间/提醒时间）、yyyy-MM-dd（退休日期）")
        } catch (e: NumberFormatException) {
            throw ConfigurationException("数字格式错误！时薪/间隔/阈值/周期需为数字")
        }
    }

    // 重置配置
    override fun reset() {
        hourlyWageField.text = config.hourlyWage.toString()
        stockCodeField.text = config.stockCode
        offWorkTimeField.text = config.offWorkTime.toString()
        retireDateField.text = config.retireDate.toString()
        remindIntervalField.text = config.remindInterval.toString()
        stockRemindThresholdField.text = config.stockRemindThreshold.toString()
        stockFundUrlField.text = config.stockFundUrl
        enableCustomReminderCheckBox.isSelected = config.enableCustomReminder
        enableMoneyAnimationCheckBox.isSelected = config.enableMoneyAnimation

        // 重置提醒列表
        reminderControls.clear()
        config.reminders.forEach { reminder ->
            val control = ReminderControl()
            control.fromReminder(reminder)
            control.onRemove = { removeReminder(control) }
            reminderControls.add(control)
        }
        updateReminderPanel()
    }

    override fun getDisplayName(): String = "MoneyTip 插件配置"

    override fun getHelpTopic(): String? = null
}
