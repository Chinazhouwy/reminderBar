package org.wy.moneyflow.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import org.wy.moneyflow.model.PluginConfig
import org.wy.moneyflow.util.TimeUtil
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException
import javax.swing.JComponent

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

    // 配置界面UI
    override fun createComponent(): JComponent {
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
            noteRow("提示：配置修改后需重启IDEA生效 | 今日金额可手动重置（重启后自动重置）")
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
                    stockRemindThresholdField.text.toDouble() != config.stockRemindThreshold
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

            PluginConfig.save(config)
        } catch (e: DateTimeParseException) {
            throw ConfigurationException("时间格式错误！正确格式：HH:mm（下班时间）、yyyy-MM-dd（退休日期）")
        } catch (e: NumberFormatException) {
            throw ConfigurationException("数字格式错误！时薪/间隔/阈值需为数字")
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
    }

    override fun getDisplayName(): String = "MoneyTip 插件配置"

    override fun getHelpTopic(): String? = null
}