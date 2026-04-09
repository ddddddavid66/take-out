package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate start, LocalDate end) {
        //创建时间列表
        List<LocalDate> list = getDateList(start,end);

/*        //创建订单列表
        List<Double> ordersList = new ArrayList<>();
        for (LocalDate date : list) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date,LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status",Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            ordersList.add(turnover);
        }*/

        //第二种方式获取 日期 只需要一次sql
        //一次性查询所有日期
        Map map = new HashMap();
        map.put("begin",start);
        map.put("end",end);
        map.put("status",Orders.COMPLETED);
        // 构建日期对应的营业额Map
        Map<String, Double> turnoverMap = getMap2(map,Number::doubleValue);
        // 遍历日期列表获取营业额
        List<Double> ordersList = new ArrayList<>();
        for (LocalDate date : list) {
            Double turnover = turnoverMap.get(date.toString());
            ordersList.add(turnover == null ? 0.0 : turnover);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(list,","))
                .turnoverList(StringUtils.join(ordersList,","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //获取时间列表
        List<LocalDate> dateList = getDateList(begin, end);
        //获取 用户  总的 和今天新增的
        Map map = new HashMap();
        map.put("end",end);
        //获取每日的 总用户
        Map<String, Integer> turnoverTotalMap = getMap(map,Number::intValue);
        //获取每日的新用户
        map.put("begin",begin);
        Map<String, Integer> turnoverNewMap = getMap(map,Number::intValue);
        // 遍历日期列表获取 数据
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            Integer turnover = turnoverTotalMap.get(date.toString());
            totalUserList.add(turnover == null ? 0 : turnover);
        }

        List<Integer> newUserList  = new ArrayList<>();
        for (LocalDate date : dateList) {
            Integer turnover = turnoverNewMap.get(date.toString());
            newUserList.add(turnover == null ? 0 : turnover);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    @NonNullDecl
    private <T> Map<String, T> getMap(Map map, Function<Number, T> converter) {
        List<Map<String,Object>> totalResult = userMapper.sumUserByMapAndDate(map);
        Map<String, T> turnoverTotalMap = new HashMap<>();
        for (Map<String, Object> m : totalResult) {
            String dateStr = m.get("date").toString();
            Object turnoverVal = m.get("turnover");
            T turnover = (turnoverVal instanceof Number) ? converter.apply((Number) turnoverVal) : null;
            turnoverTotalMap.put(dateStr, turnover);
        }
        return turnoverTotalMap;
    }

    @NonNullDecl
    private <T> Map<String, T> getMap2(Map map, Function<Number, T> converter) {
        List<Map<String,Object>> totalResult = orderMapper.sumByMapAndDate(map);
        Map<String, T> turnoverTotalMap = new HashMap<>();
        for (Map<String, Object> m : totalResult) {
            String dateStr = m.get("date").toString();
            Object turnoverVal = m.get("turnover");
            T turnover = (turnoverVal instanceof Number) ? converter.apply((Number) turnoverVal) : null;
            turnoverTotalMap.put(dateStr, turnover);
        }
        return turnoverTotalMap;
    }

    public List<LocalDate> getDateList(LocalDate start, LocalDate end){
        List<LocalDate> list = new ArrayList<>();
        list.add(start);
        while(!start.equals(end)){
            start = start.plusDays(1L);
            list.add(start);
        }
        list.add(end);
        return list;
    }

}
