package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrederTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理 超时 订单
     */
    @Scheduled(cron = "0 0/1 * * * ? ") //每分钟执行一次
    public void processTimeOutOrder(){
        log.info("处理超时订单 :{}", LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now().minusMinutes(15L);
        //计算下单时间与当前时间 超过15min取消订单
        List<Orders> list = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,now);
        if(list != null && list.size() > 0){
            for (Orders orders : list) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason(MessageConstant.ORDER_OUT_TIME);
                orders.setCancelTime(LocalDateTime.now());
            }
            orderMapper.updateBatch(list);
            log.info("处理超时订单{}个",list.size());
        }
        log.info("没有要处理的超时订单");
    }

    @Scheduled(cron = "0 0 1 * * ? ") //每天凌晨1点 处理未派送订单
    public void processDeliverOrder(){
        log.info("处理处于派送中的订单{}",LocalDateTime.now());
        LocalDateTime yesterday = LocalDateTime.now().minusHours(1L);
        List<Orders> list = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,yesterday);
        if(list != null && list.size() > 0){
            for (Orders orders : list) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
            log.info("处理未派送订单{}个",list.size());
        }
        log.info("没有要处理的未派送订单");
    }
}
