package com.sky.service.impl;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.NameList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

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

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        //查询每天的 订单总数
        Map map = new HashMap();
        map.put("end",end);
        map.put("begin",begin);
        Map<String, Integer> turnoverTotalMap = getMap3(map,Number::intValue);
        //查询 每天的 有效订单数
        map.put("status",Orders.COMPLETED);
        Map<String, Integer> turnoverVaildMap = getMap3(map,Number::intValue);
        // 遍历 日期列表 获取 数据
        List<Integer> totalOrderList = new ArrayList<>();
        for (LocalDate date : dateList) {
            Integer turnover = turnoverTotalMap.get(date.toString());
            totalOrderList .add(turnover == null ? 0 : turnover);
        }
        List<Integer> validOrderList  = new ArrayList<>();
        for (LocalDate date : dateList) {
            Integer turnover = turnoverVaildMap.get(date.toString());
            validOrderList.add(turnover == null ? 0 : turnover);
        }
        BigDecimal totalOrderCount = BigDecimal.valueOf(totalOrderList.size());
        BigDecimal validOrderCount = BigDecimal.valueOf(validOrderList.size());
        BigDecimal rate = validOrderCount.divide(totalOrderCount,2,BigDecimal.ROUND_HALF_UP);
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(totalOrderList,","))
                .validOrderCountList(StringUtils.join(validOrderList,","))
                .totalOrderCount(totalOrderCount.intValue())
                .validOrderCount(validOrderCount.intValue())
                .orderCompletionRate(rate.doubleValue())
                .build();
    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        Map map = new HashMap();
        map.put("end",end);
        map.put("begin",begin);
        map.put("status",Orders.COMPLETED);
        Map<String, Integer> turnoverTotalMap = getMap4(map,Number::intValue);
        List<Integer> countList  = new ArrayList<>();
        List<String> nameList  = new ArrayList<>();
        Set<String> keys = turnoverTotalMap.keySet();
        for (Object o : keys) {
            System.out.println(o);
            Integer turnover = turnoverTotalMap.get(o.toString());
            countList.add(turnover == null ? 0 : turnover);
            nameList.add(o.toString());
        }
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(countList,","))
                .build();
    }

    private <T> Map<String, T> getMap4(Map map, Function<Number, T> converter) {
        List<Map<String,Object>> totalResult = orderDetailMapper.countSaleTop10(map);
        Map<String, T> turnoverTotalMap = new HashMap<>();
        for (Map<String, Object> m : totalResult) {
            String dateStr = m.get("name").toString();
            Object turnoverVal = m.get("turnover");
            T turnover = (turnoverVal instanceof Number) ? converter.apply((Number) turnoverVal) : null;
            turnoverTotalMap.put(dateStr, turnover);
        }
        return turnoverTotalMap;
    }


    @NonNullDecl
    private <T> Map<String, T> getMap(Map map, Function<Number, T> converter) {
        List<Map<String,Object>> totalResult = userMapper.sumUserByMapAndDate(map);
        Map<String, T> turnoverTotalMap = getTotalMap(totalResult, converter);
        return turnoverTotalMap;
    }

    @NonNullDecl
    private <T> Map<String, T> getMap2(Map map, Function<Number, T> converter) {
        List<Map<String,Object>> totalResult = orderMapper.sumByMapAndDate(map);
        Map<String, T> turnoverTotalMap = getTotalMap(totalResult, converter);
        return turnoverTotalMap;
    }

    @NonNullDecl
    private <T> Map<String, T> getMap3(Map map, Function<Number, T> converter) {
        List<Map<String,Object>> totalResult = orderMapper.countByMapAndDateAndStatus(map);
        Map<String, T> turnoverTotalMap = getTotalMap(totalResult, converter);
        return turnoverTotalMap;
    }


    public <T> Map<String, T> getTotalMap(List<Map<String,Object>> totalResult,Function<Number, T> converter){
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
