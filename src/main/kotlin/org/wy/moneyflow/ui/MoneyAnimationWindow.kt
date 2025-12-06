package org.wy.moneyflow.ui

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import javax.swing.*

/**
 * 金钱四溅动画窗口 - 性能优化版本
 */
class MoneyAnimationWindow(
    private val x: Int,
    private val y: Int,
    private val amount: Double,
    private val isLetterKeyPressed: Boolean = true  // 添加字母键判断参数
) : JWindow() {
    // 动画元素列表
    private val animationElements = mutableListOf<MoneyElement>()
    private val timer = Timer(33, AnimationListener()) // 优化：33ms刷新频率(约30FPS)，平衡流畅度和性能
    private lateinit var sharedImage: Image // 共享图像实例减少内存分配
    private var startTime: Long = 0 // 动画开始时间

    // 优化：限制窗口大小，只覆盖动画区域
    private val WINDOW_SIZE = 200 // 窗口大小，覆盖动画元素的最大活动范围

    // 预定义常量避免重复创建对象
    companion object {
        private val GREEN_COLOR = Color(0, 200, 0)
        private val RED_COLOR = Color(200, 0, 0)
        private val MAX_ELEMENTS = 5 // 最多同时显示5个元素
        private val ANIMATION_DURATION = 1000L // 动画持续1秒
        private val ROTATION_STEP = 15 // 优化：增大旋转步长，减少计算频率
        private val GRAVITY = 0.4 // 重力系数
    }

    init {
        // 只有字母键按下时才显示动画
        if (isLetterKeyPressed) {
            initializeAnimation()
        } else {
            dispose()
        }
    }

    /**
     * 初始化动画相关设置
     */
    private fun initializeAnimation() {
        isAlwaysOnTop = true
        background = Color(0, 0, 0, 0) // 透明背景

        // 优化：使用较小的窗口尺寸，只覆盖动画区域
        size = Dimension(WINDOW_SIZE, WINDOW_SIZE)
        // 定位窗口到点击位置附近，使动画居中显示
        location = Point(x - WINDOW_SIZE / 2, y - WINDOW_SIZE / 2)

        // 创建共享图像资源
        sharedImage = createMoneyImage()

        // 创建动画元素
        repeat(1) {
            // 检查是否超过最大数量限制
            if (animationElements.size >= MAX_ELEMENTS) {
                // 移除最早添加的元素（列表第一个元素）
                animationElements.removeAt(0)
            }

            // 添加新元素
            animationElements.add(
                MoneyElement(
                    x = WINDOW_SIZE / 2 + (Math.random() * 100 - 50).toInt(),
                    y = WINDOW_SIZE / 2 - (Math.random() * 50).toFloat(),
                    dx = (Math.random() * 8 - 4).toInt(),
                    dy = (Math.random() * 12 - 4).toDouble(),
                    color = if (amount > 0) GREEN_COLOR else RED_COLOR,
                    size = 12 + (Math.random() * 8).toInt(),
                    image = sharedImage
                )
            )
        }

        isVisible = true
        startTime = System.currentTimeMillis()
        timer.start()
    }

    // 创建金钱图像
    private fun createMoneyImage(): Image {
        // 创建一个简单的金钱图标图像
        val image = BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()

        // 启用抗锯齿提升绘制质量
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // 绘制金币形状
        g2d.color = Color.YELLOW
        g2d.fillOval(2, 2, 16, 16)
        g2d.color = Color(200, 170, 0)
        g2d.drawOval(2, 2, 16, 16)

        // 绘制$符号
        g2d.color = Color.BLACK
        g2d.font = Font("Arial", Font.BOLD, 12)
        g2d.drawString("$", 7, 14)

        g2d.dispose()
        return image
    }

    // 绘制动画 - 优化重绘逻辑
    override fun paint(g: Graphics) {
        val g2d = g as Graphics2D

        // 优化：只在需要时启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED) // 优先考虑渲染速度

        // 优化：不清除整个背景，只绘制透明背景
        g2d.composite = AlphaComposite.Clear
        g2d.fillRect(0, 0, width, height)
        g2d.composite = AlphaComposite.SrcOver

        drawAnimationElements(g2d)
    }

    /**
     * 绘制所有动画元素
     */
    private fun drawAnimationElements(g2d: Graphics2D) {
        // 检查是否超过持续时间
        if (System.currentTimeMillis() - startTime > ANIMATION_DURATION) {
            stopAnimation()
            return
        }

        for (element in animationElements) {
            // 更新物理状态
            updateElementPosition(element)

            // 绘制旋转图像
            drawRotatedImage(g2d, element)
        }
    }

    /**
     * 更新元素位置和运动状态
     */
    private fun updateElementPosition(element: MoneyElement) {
        element.x += element.dx
        element.y += element.dy.toFloat()
        element.dy += GRAVITY // 重力影响
        element.rotate = (element.rotate + ROTATION_STEP) % 360 // 旋转更新
    }

    /**
     * 绘制带旋转效果的图像 - 优化旋转性能
     */
    private fun drawRotatedImage(g2d: Graphics2D, element: MoneyElement) {
        // 优化：避免频繁的旋转变换计算
        // 只在旋转角度变化明显时重新计算变换
        val angleInRadians = Math.toRadians(element.rotate.toDouble())

        // 保存当前变换矩阵
        val originalTransform = g2d.transform

        try {
            // 执行旋转变换
            g2d.rotate(angleInRadians, element.x.toDouble(), element.y.toDouble())
            g2d.drawImage(
                element.image,
                element.x,
                element.y.toInt(),
                element.size,
                element.size,
                null
            )
        } finally {
            // 恢复原始变换矩阵
            g2d.transform = originalTransform
        }
    }

    /**
     * 停止动画并清理资源
     */
    private fun stopAnimation() {
        timer.stop()
        dispose()
    }

    // 动画元素数据类
    private data class MoneyElement(
        var x: Int,
        var y: Float,
        var dx: Int,
        var dy: Double,
        val color: Color,
        val size: Int,
        var rotate: Int = 0,
        val image: Image
    )

    // 动画监听器 - 优化重绘策略
    private inner class AnimationListener : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            // 优化：只重绘动画区域，而不是整个窗口
            repaint(0, 0, width, height)
        }
    }
}