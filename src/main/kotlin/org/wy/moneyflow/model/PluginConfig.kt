package org.wy.moneyflow.model

import com.intellij.ide.util.PropertiesComponent
import java.time.LocalDate
import java.time.LocalTime

/**
 * 自定义提醒数据模型
 */
data class CustomReminder(
    // 提醒类型：TIME_POINT（时间点提醒）、PERIODIC（周期性提醒）
    var type: String = "TIME_POINT",
    // 时间点（如12:00）或周期（分钟数）
    var value: String = "12:00",
    // 提醒消息
    var message: String = "提醒"
)

/**
 * 插件配置数据模型
 */
data class PluginConfig(
    // 时薪（元/小时）
    var hourlyWage: Double = 50.0,
    // 股票代码（如600000）
    var stockCode: String = "600000",
    // 下班时间（HH:mm）
    var offWorkTime: LocalTime = LocalTime.of(18, 0),
    // 退休日期（yyyy-MM-dd）
    var retireDate: LocalDate = LocalDate.of(2050, 1, 1),
    // 时间提醒间隔（分钟）
    var remindInterval: Int = 60,
    // 股票涨幅提醒阈值（%）
    var stockRemindThreshold: Double = 1.0,
    // 今日已赚金额
    var todayEarned: Double = 0.0,
    // 今日已扣金额
    var todayDeducted: Double = 0.0,
    // 股票基金数据URL
    var stockFundUrl: String = "https://example.com/api/stock",
    // 是否启用自定义提醒
    var enableCustomReminder: Boolean = false,
    // 是否开启金钱四溅效果
    var enableMoneyAnimation: Boolean = true,
    // 自定义提醒列表
    var reminders: List<CustomReminder> = emptyList()
) {
    companion object {
        private val properties = PropertiesComponent.getInstance()

        // 从持久化配置加载
        fun load(): PluginConfig {
            return PluginConfig(
                hourlyWage = properties.getValue("moneyTip.hourlyWage", "50.0")?.toDoubleOrNull() ?: 50.0,
                stockCode = properties.getValue("moneyTip.stockCode", "600000"),
                offWorkTime = LocalTime.parse(properties.getValue("moneyTip.offWorkTime", "18:00")),
                retireDate = LocalDate.parse(properties.getValue("moneyTip.retireDate", "2050-01-01")),
                remindInterval = properties.getInt("moneyTip.remindInterval", 60),
                stockRemindThreshold = properties.getValue("moneyTip.stockRemindThreshold", "1.0")?.toDoubleOrNull() ?: 1.0,
                todayEarned = properties.getValue("moneyTip.todayEarned", "0.0")?.toDoubleOrNull() ?: 0.0,
                todayDeducted = properties.getValue("moneyTip.todayDeducted", "0.0")?.toDoubleOrNull() ?: 0.0,
                stockFundUrl = properties.getValue("moneyTip.stockFundUrl", "https://example.com/api/stock"),
                enableCustomReminder = properties.getBoolean("moneyTip.enableCustomReminder", false),
                enableMoneyAnimation = properties.getBoolean("moneyTip.enableMoneyAnimation", false),
                reminders = listOf() // 目前先返回空列表，后续可以扩展为JSON序列化
            )
        }

        // 保存配置到持久化存储
        fun save(config: PluginConfig) {
            properties.setValue("moneyTip.hourlyWage", config.hourlyWage.toString())
            properties.setValue("moneyTip.stockCode", config.stockCode)
            properties.setValue("moneyTip.offWorkTime", config.offWorkTime.toString())
            properties.setValue("moneyTip.retireDate", config.retireDate.toString())
            properties.setValue("moneyTip.remindInterval", config.remindInterval.toString())
            properties.setValue("moneyTip.stockRemindThreshold", config.stockRemindThreshold.toString())
            properties.setValue("moneyTip.todayEarned", config.todayEarned.toString())
            properties.setValue("moneyTip.todayDeducted", config.todayDeducted.toString())
            properties.setValue("moneyTip.stockFundUrl", config.stockFundUrl)
            properties.setValue("moneyTip.enableCustomReminder", config.enableCustomReminder)
            properties.setValue("moneyTip.enableMoneyAnimation", config.enableMoneyAnimation)
            // 自定义提醒列表的保存可以后续扩展为JSON序列化
        }

        // 重置今日金额
        fun resetTodayAmount() {
            properties.setValue("moneyTip.todayEarned", 0.0.toString())
            properties.setValue("moneyTip.todayDeducted", 0.0.toString())
        }

        fun printData(): String {
            return "properties：${properties}"
        }
    }
}