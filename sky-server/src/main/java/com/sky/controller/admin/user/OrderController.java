package com.sky.controller.admin.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.impl.OrderServiceImpl;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController(value = "UserOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "C端订单相关接口")
public class OrderController {
    @Autowired
    private OrderServiceImpl orderService;

    @PostMapping("/submit")
    @ApiOperation("用户端提交接口")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("C端用户提交订单");
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success(orderPaymentVO);
    }

    @ApiOperation("历史订单查询")
    @GetMapping("/historyOrders")
    public Result<PageResult> historyOrdersQuery(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("历史订单查询{}",ordersPageQueryDTO);
        PageResult pageResult= orderService.historyQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("C端用户历史订单查询")
    @GetMapping("/orderDetail/{id}")
    public Result<OrderDetailVO> orderDetailsQuery(@PathVariable Long id){
        log.info("查询C端用户{}的历史订单",id);
        OrderDetailVO ordersVO = orderService.detailsQuery(id);
        return Result.success(ordersVO);
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id){
        log.info("正在取消订单{}",id);
        orderService.cancel(id);
        return Result.success();
    }

    @ApiOperation("再来一单")
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id){
        log.info("再来一个订单{}",id);
        orderService.repetition(id);
        return Result.success();
    }


}
