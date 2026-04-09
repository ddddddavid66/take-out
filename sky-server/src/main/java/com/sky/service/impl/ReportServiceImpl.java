package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate start, LocalDate end) {
        //创建时间列表
        List<LocalDate> list = new ArrayList<>();
        list.add(start);
        while(!start.equals(end)){
            start = start.plusDays(1L);
            list.add(start);
        }
        list.add(end);

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
        List<Map<String,Object>> result = orderMapper.sumByMapAndDate(map);
        // 构建日期对应的营业额Map
        Map<String, Double> turnoverMap = new HashMap<>();
        for (Map<String, Object> m : result) {
            String dateStr = m.get("date").toString();
            Double turnover = ((Number) m.get("turnover")).doubleValue();
            turnoverMap.put(dateStr, turnover);
        }
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
}
