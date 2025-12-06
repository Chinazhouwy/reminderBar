package org.wy.reminderBar.listener

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import org.wy.reminderBar.model.PluginConfig
import org.wy.reminderBar.ui.MoneyAnimationWindow
import org.wy.reminderBar.util.TimeUtil
import java.awt.Point

/**
 * 文档编辑监听器（监听打字/删行）
 */
class DocumentChangeListener(private val editor: Editor) : DocumentListener {
    private var lastLineCount = editor.document.lineCount
    private var config = PluginConfig.load()

    // 文档内容变更监听
    override fun documentChanged(event: DocumentEvent) {
        val document = event.document
        val offset = event.offset
        val newText = event.newFragment.toString()
        val oldText = event.oldFragment.toString()

        // 1. 打字赚钱（新增字符）
        config = PluginConfig.load()
        if (newText.isNotEmpty() && oldText.isEmpty()) {
            val charEarn = TimeUtil.getCharEarn(config.hourlyWage)
            val earnAmount = newText.length * charEarn
            config.todayEarned += earnAmount
            PluginConfig.save(config)

            // 显示赚钱动画
            showAnimation(earnAmount, offset)
        }

        // 2. 删除行扣钱
        val currentLineCount = document.lineCount
        val lineDiff = lastLineCount - currentLineCount
        if (lineDiff > 0) {
            val lineDeduct = TimeUtil.getLineDeduct(config.hourlyWage)
            val deductAmount = lineDiff * lineDeduct
            config.todayDeducted += deductAmount
            PluginConfig.save(config)

            // 显示扣钱动画
            showAnimation(-deductAmount, offset)
        }
        lastLineCount = currentLineCount
    }

    // 显示动画
    private fun showAnimation(amount: Double, offset: Int) {
        // 每次都加载最新的配置，实现动态开关效果
        val latestConfig = PluginConfig.load()
        // 检查是否启用金钱四溅效果
        if (latestConfig.enableMoneyAnimation) {
            // 获取光标位置的屏幕坐标
            val visualPosition = editor.offsetToVisualPosition(offset)
            val point = editor.visualPositionToXY(visualPosition)
            val screenPoint = Point(
                editor.component.locationOnScreen.x + point.x,
                editor.component.locationOnScreen.y + point.y
            )

            // 启动动画
            MoneyAnimationWindow(screenPoint.x, screenPoint.y, amount).isVisible = true
        }
    }

    companion object {
        // 注册监听器
        fun register(editor: Editor) {
            editor.document.addDocumentListener(DocumentChangeListener(editor))
        }
    }
}