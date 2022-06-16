package com.power.base.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * 作者：Gongsensen
 * 日期：2022/6/14
 * 说明：显示金额格式化
 */
public class MoneyFormatter {

    /**
     * float
     */
    public static String format(float money) {
        return format(money, RoundingMode.HALF_UP); //默认四舍五入
    }

    public static String format(float money, RoundingMode mode) {
        float temp = money - (int) money;
        if (temp == 0f) {
            return String.valueOf((int) money);
        }

        DecimalFormat format = new DecimalFormat("#.##");
        format.setRoundingMode(mode);
        return format.format(money);
    }

    /**
     * double
     */
    public static String format(double money) {
        return format(money, RoundingMode.HALF_UP); //默认四舍五入
    }

    public static String format(double money, RoundingMode mode) {
        double temp = money - (int) money;
        if (temp == 0f) {
            return String.valueOf((int) money);
        }

        DecimalFormat format = new DecimalFormat("#.##");
        format.setRoundingMode(mode);
        return format.format(money);
    }

    /**
     * String
     */
    public static String format(String money) {
        return format(money, RoundingMode.HALF_UP); //默认四舍五入
    }

    public static String format(String money, RoundingMode mode) {
        if (money == null) {
            return "";
        }

        try {
            float moneyFloat = Float.parseFloat(money);
            return format(moneyFloat, mode);
        } catch (NumberFormatException e) {
            return money;
        }
    }
}