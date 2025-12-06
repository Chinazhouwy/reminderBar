package org.wy.moneyflow.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 时间计算工具类
 */
object TimeUtil {
    // 计算距离下班的时间（返回：小时:分钟:秒）
    fun getOffWorkCountdown(offWorkTime: LocalTime): String {
        val now = LocalDateTime.now()
        val todayOffWork = LocalDateTime.of(now.toLocalDate(), offWorkTime)
        val tomorrowOffWork = todayOffWork.plusDays(1)

        val target = if (now.isBefore(todayOffWork)) todayOffWork else tomorrowOffWork
        val duration = Duration.between(now, target)

        val hours = duration.toHours()
//        val minutes = duration.toMinutes() % 60
//        val seconds = duration.seconds % 60

        return String.format("%02d", hours)
    }

    // 计算距离退休的天数
    fun getRetireCountdown(retireDate: LocalDate): Long {
        val now = LocalDate.now()
        return Duration.between(now.atStartOfDay(), retireDate.atStartOfDay()).toDays()
    }

    // 计算每字符的收益（时薪/小时总字符数，假设每分钟打字60字符）
    fun getCharEarn(hourlyWage: Double): Double {
        val charsPerHour = 60 * 60.0 // 每小时3600字符（基准值）
        return hourlyWage / charsPerHour
    }

    // 计算每删除一行的扣钱数
    fun getLineDeduct(hourlyWage: Double): Double {
        return hourlyWage / 120.0 // 每删一行扣1/120小时工资（约0.5小时/60行）
    }
}