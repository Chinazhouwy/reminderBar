package org.wy.moneyflow.ui

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

/**
 * 金钱四溅动画窗口
 */
class MoneyAnimationWindow(private val x: Int, private val y: Int, private val amount: Double) : JWindow() {
    // 动画元素列表
    private val animationElements = mutableListOf<MoneyElement>()
    private val timer = Timer(30, AnimationListener())

    init {
        isAlwaysOnTop = true
        background = Color(0, 0, 0, 0) // 透明背景
        size = Toolkit.getDefaultToolkit().screenSize
        location = Point(0, 0)

        // 创建动画元素（8个金钱文字）
        repeat(8) {
            animationElements.add(MoneyElement(
                x = x + (Math.random() * 100 - 50).toInt(),
                y = y - (Math.random() * 50).toFloat(),
                dx = (Math.random() * 4 - 2).toInt(),
                dy = (Math.random() * -6 - 2).toDouble(),
                text = if (amount > 0) "💰+${String.format("%.3f", amount)}" else "💸${String.format("%.3f", amount)}",
                color = if (amount > 0) Color.GREEN else Color.RED,
                size = 12 + (Math.random() * 8).toInt()
            ))
        }

        timer.start()
    }

    // 绘制动画
    override fun paint(g: Graphics) {
        super.paint(g)
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // 遍历绘制元素
        val iterator = animationElements.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            // 更新位置
            element.x += element.dx
            element.y += element.dy.toFloat()
            // 增加重力
            element.dy += 0.2
            // 旋转
            element.rotate += 5

            // 超出屏幕则移除
            if (element.y > size.height || element.x < 0 || element.x > size.width) {
                iterator.remove()
                continue
            }

            // 绘制文字
            g2d.color = element.color
            g2d.font = Font("微软雅黑", Font.BOLD, element.size)
            g2d.rotate(Math.toRadians(element.rotate.toDouble()), element.x.toDouble(), element.y.toDouble())
            g2d.drawString(element.text, element.x.toFloat(), element.y)
            g2d.rotate(-Math.toRadians(element.rotate.toDouble()), element.x.toDouble(), element.y.toDouble())
        }

        // 无元素时关闭窗口
        if (animationElements.isEmpty()) {
            timer.stop()
            dispose()
        }
    }

    // 动画元素数据类
    private data class MoneyElement(
        var x: Int,
        var y: Float,
        var dx: Int,
        var dy: Double,
        val text: String,
        val color: Color,
        val size: Int,
        var rotate: Int = 0
    )

    // 动画监听器
    private inner class AnimationListener : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            repaint()
        }
    }
}