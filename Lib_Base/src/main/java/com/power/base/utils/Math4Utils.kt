package com.power.base.utils

import java.math.BigDecimal
import java.math.BigDecimal.ROUND_DOWN

/**
 * 作者：Gongsensen
 * 日期：2022/3/3
 * 说明：简单的加减乘除工具类，默认保留两位小数
 */
object Math4Utils {
    // 需要精确至小数点后几位
    private const val DECIMAL_POINT_NUMBER: Int = 2

    // 加法运算
    @JvmStatic
    fun add(d1: Double, d2: Double, decimalPoint: Int = DECIMAL_POINT_NUMBER): Double =
        BigDecimal(d1).add(BigDecimal(d2)).setScale(decimalPoint, ROUND_DOWN)
            .toDouble()

    // 减法运算
    @JvmStatic
    fun sub(d1: Double, d2: Double, decimalPoint: Int = DECIMAL_POINT_NUMBER): Double =
        BigDecimal(d1).subtract(BigDecimal(d2))
            .setScale(decimalPoint, ROUND_DOWN).toDouble()

    // 乘法运算
    @JvmStatic
    fun mul(d1: Double, d2: Double, decimalPoint: Int = DECIMAL_POINT_NUMBER): Double =
        BigDecimal(d1).multiply(BigDecimal(d2)).setScale(decimalPoint, ROUND_DOWN)
            .toDouble()

    // 除法运算
    @JvmStatic
    fun div(d1: Double, d2: Double, decimalPoint: Int = DECIMAL_POINT_NUMBER): Double =
        BigDecimal(d1).divide(BigDecimal(d2)).setScale(decimalPoint, ROUND_DOWN)
            .toDouble()

}