package org.wy.reminderBar.util

import com.alibaba.fastjson.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * 股票数据工具类（基于新浪财经接口）
 */
object StockUtil {
    private val okHttpClient = OkHttpClient()

    // 获取A股实时行情（返回：代码|名称|当前价|涨跌幅）
    fun getStockInfo(stockCode: String): String {
        return try {
            val url = "https://finance.sina.com.cn/stock/flashdata/hsgt/daily/$stockCode.json"
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful && response.body != null) {
                val json = JSONObject.parseObject(response.body!!.string())
                val data = json.getJSONObject("data")?.getJSONObject(stockCode)
                if (data != null) {
                    val name = data.getString("name") ?: "未知"
                    val price = data.getDouble("price") ?: 0.0
                    val change = data.getDouble("change") ?: 0.0
                    String.format("%s(%s): %.2f (%.2f%%)", name, stockCode, price, change)
                } else {
                    "$stockCode: 数据获取失败"
                }
            } else {
                "$stockCode: 请求失败"
            }
        } catch (e: IOException) {
            "$stockCode: 网络异常"
        }
    }

    // 获取股票涨跌幅
    fun getStockChange(stockCode: String): Double {
        return try {
            val url = "https://finance.sina.com.cn/stock/flashdata/hsgt/daily/$stockCode.json"
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful && response.body != null) {
                val json = JSONObject.parseObject(response.body!!.string())
                val data = json.getJSONObject("data")?.getJSONObject(stockCode)
                data?.getDouble("change") ?: 0.0
            } else {
                0.0
            }
        } catch (e: IOException) {
            0.0
        }
    }
}