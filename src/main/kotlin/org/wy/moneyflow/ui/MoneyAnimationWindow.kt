package org.wy.moneyflow.ui

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import javax.swing.*

/**
 * 金钱四溅动画窗口 - 优化版本
 */
class MoneyAnimationWindow(
    private val x: Int,
    private val y: Int,
    private val amount: Double,
    private val isLetterKeyPressed: Boolean = true  // 添加字母键判断参数
) : JWindow() {
    // 动画元素列表
    private val animationElements = mutableListOf<MoneyElement>()
    private val timer = Timer(50, AnimationListener()) // 50ms刷新频率
    private lateinit var sharedImage: Image // 共享图像实例减少内存分配
    private var startTime: Long = 0 // 动画开始时间

    // 预定义常量避免重复创建对象
    companion object {
        private val GREEN_COLOR = Color(0, 200, 0)
        private val RED_COLOR = Color(200, 0, 0)
        private val MAX_ELEMENTS = 5 // 最多同时显示5个元素
        private val ANIMATION_DURATION = 1000L // 动画持续1秒
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
        if (animationElements.size < MAX_ELEMENTS) {
            isAlwaysOnTop = true
            background = Color(0, 0, 0, 0) // 透明背景
            size = Toolkit.getDefaultToolkit().screenSize
            location = Point(0, 0)

            // 创建共享图像资源
            sharedImage = createMoneyImage()

            // 创建动画元素（最多5个）
            // 在 initializeAnimation 方法中调整元素创建参数
            repeat(1) {
                animationElements.add(
                    MoneyElement(
                        x = x + (Math.random() * 100 - 50).toInt(),
                        y = y - (Math.random() * 50).toFloat(),
                        dx = (Math.random() * 8 - 4).toInt(),     // 增大水平速度范围 (-4到4)
                        dy = (Math.random() * 12 - 4).toDouble(), // 增大垂直初速度 (-16到-4)
                        color = if (amount > 0) GREEN_COLOR else RED_COLOR,
                        size = 12 + (Math.random() * 8).toInt(),
                        image = sharedImage
                    )
                )
            }
        }

        isVisible = true
        startTime = System.currentTimeMillis()
        timer.start()
    }

    // 在类中添加创建图像的方法
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

    // 绘制动画
    override fun paint(g: Graphics) {
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // 清除背景为透明
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
    // 在 updateElementPosition 方法中调整重力系数
    private fun updateElementPosition(element: MoneyElement) {
        element.x += element.dx
        element.y += element.dy.toFloat()
        element.dy += 0.4 // 增加重力系数从0.2到0.4，使下落更快
        element.rotate = (element.rotate + 10) % 360 // 增加旋转速度
    }

    /**
     * 绘制带旋转效果的图像
     */
    private fun drawRotatedImage(g2d: Graphics2D, element: MoneyElement) {
        val angleInRadians = Math.toRadians(element.rotate.toDouble())

        // 保存当前变换矩阵
        val originalTransform = g2d.transform

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

        // 恢复原始变换矩阵
        g2d.transform = originalTransform
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

    // 动画监听器
    private inner class AnimationListener : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            repaint()
        }
    }
}
