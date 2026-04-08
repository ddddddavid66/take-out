package com.sky.utils;

import java.util.List;

public class CalcDistanceUtil {
    /**
     * 坐标位置相关util
     */

        /**
         * 赤道半径（单位：米）
         */
        private static final double EQUATOR_RADIUS = 6378137;
        /**
         * 方法一：（反余弦计算方式）
         *
         * @return 返回距离，单位m
         */
        public static double getDistance1(List<Double> admin, List<Double> user){
            // 纬度
            double lat1 = Math.toRadians(admin.get(1));
            double lat2 = Math.toRadians(user.get(1));
            // 经度
            double lon1 = Math.toRadians(admin.get(0));
            double lon2 = Math.toRadians(user.get(0));
            // 纬度之差
            double a = lat1 - lat2;
            // 经度之差
            double b = lon1 - lon2;
            // 计算两点距离的公式
            double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2), 2)));
            // 弧长乘赤道半径, 返回单位: 米
            s = s * EQUATOR_RADIUS;
            return s;
        }
}
