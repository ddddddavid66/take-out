package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderDetailVO;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController(value = "adminOrderController")
@RequestMapping("admin/order")
@Slf4j
@Api(tags = "订单相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @ApiOperation("订单搜索")
    @GetMapping("/conditionSearch")
    public Result<PageResult> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单查询{}",ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("各个状态的订单数量统计")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statisticsQuery(){
        log.info("查询各个状态的订单数量统计");
        OrderStatisticsVO orderStatisticsVO = orderService.statisticsQuery();
        return Result.success(orderStatisticsVO);
    }

    @ApiOperation("查询订单详情")
    @GetMapping("/details/{id}")
    public Result<OrderDetailVO> detailQuery(@PathVariable Long id){
        log.info("查询订单{}详情",id);
        OrderDetailVO orderDetailVO = orderService.detailsQuery(id);
        return Result.success(orderDetailVO);
    }

    @ApiOperation("接单")
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("商家接单{}",ordersConfirmDTO.getId());
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    @ApiOperation("拒单")
    @PutMapping("/rejection")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("商家拒单{}",ordersRejectionDTO.getId());
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    @ApiOperation("拒单")
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("商家取消单{}",ordersCancelDTO.getId());
        orderService.cancelOrder(ordersCancelDTO);
        return Result.success();
    }

}
