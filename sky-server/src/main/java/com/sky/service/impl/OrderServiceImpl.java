package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.controller.admin.user.OrderController;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderController orderController;

    private static final int PACK_AMOUNT = 6;

    /**
     * 用户下单的方法
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //校验收货地址  购物车是否为空   ->业务异常
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        //1 检验收货地址 为空  而建立收货地址表明 地址 手机号不为空
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //订单表 插入 1条
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(userId);
        orders.setUserName("wx_" + userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis())); //时间戳
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getDetail());
        orders.setPackAmount(PACK_AMOUNT);
        orders.setDeliveryStatus(ordersSubmitDTO.getDeliveryStatus());
        orders.setRemark(ordersSubmitDTO.getRemark());
        orders.setTablewareNumber(ordersSubmitDTO.getTablewareNumber());
        orders.setTablewareStatus(ordersSubmitDTO.getTablewareStatus());
        orders.setAmount(ordersSubmitDTO.getAmount());
        orders.setAddressBookId(ordersSubmitDTO.getAddressBookId());
        orders.setPayMethod(ordersSubmitDTO.getPayMethod());
        orders.setEstimatedDeliveryTime(ordersSubmitDTO.getEstimatedDeliveryTime());
        orderMapper.insert(orders);
        //订单明细表 插入 n条
        Long orderId = orders.getId();//主键返回
        List<OrderDetail> list2 = new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail); //发现 二者相似
            //但是购物车没 orderId
            orderDetail.setOrderId(orderId);
            list2.add(orderDetail);
        }
        orderDetailMapper.insertBatch(list2);
        //清空购物车
        shoppingCartMapper.deleteByUserId(userId);
        //返回数据
        OrderSubmitVO submitVO = OrderSubmitVO.builder()
                .id(orderId)
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
        return submitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

       /* //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );*/
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单

        //发现没有将支付时间 check_out属性赋值，所以在这里更新
        LocalDateTime check_out_time = LocalDateTime.now();

        //获取订单号码
        String orderNumber = ordersPaymentDTO.getOrderNumber();
        log.info("调用updateStatus，用于替换微信支付更新数据库状态的问题");
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderNumber);
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 历史订单查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult historyQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //先查询获得 订单
        Long userId = BaseContext.getCurrentId();
        Page<Orders> page = orderMapper.queryByUserId(ordersPageQueryDTO.getStatus(), userId);
        List<Orders> result = page.getResult();

        //再查询 订单的具体内容 查询details表  合并生产OrdersDto
       /* List<OrdersVO> newList = new ArrayList<>();
        for (Orders orders : result) {
            OrdersVO ordersVO = new OrdersVO();
            BeanUtils.copyProperties(orders,ordersVO);
            List<OrderDetail>  orderDetailList = orderDetailMapper.queryByOrderId(orders.getId());
            ordersVO.setOrderDetailList( orderDetailList);
            newList.add(ordersVO);
        }*/

        //优化 stream流获取所有的orderDetail 然后转换成Map集合
        Map<Long, List<OrderDetail>> detailMap = getOrderDetailMap(result);
        List<OrdersVO> list = result.stream().map(orders -> {
            OrdersVO ordersVO = new OrdersVO();
            BeanUtils.copyProperties(orders, ordersVO);
            ordersVO.setOrderDetailList(detailMap.get(ordersVO.getId()));
            return ordersVO;
        }).collect(Collectors.toList());
        PageResult pageResult = new PageResult(page.getTotal(), list);
        return pageResult;
    }

    /**
     * 实现历史订单具体查询
     *
     * @param orderId
     * @return
     */
    @Override
    public OrderDetailVO detailsQuery(Long orderId) {
        Orders orders = orderMapper.queryByOrderId(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.queryByOrderId(orderId);
        OrderDetailVO orderDetailVO = new OrderDetailVO();
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        BeanUtils.copyProperties(orders, orderDetailVO);
        orderDetailVO.setOrderDetailList(orderDetailList);
        return orderDetailVO;
    }

    /**
     * 取消订单
     *
     * @param orderId
     */
    @Override
    public void cancel(Long orderId) {
        //订单设置为已取消 而不是删除
        Long userId = BaseContext.getCurrentId();
        Orders orders = orderMapper.queryByUserOrderId(userId, orderId);
        orderMapper.updateStatus(Orders.CANCELLED, Orders.UN_PAID, LocalDateTime.now(), orders.getNumber());
    }

    @Override
    public void repetition(Long orderId) {
        Long userId = BaseContext.getCurrentId();
        Orders orders = orderMapper.queryByUserOrderId(userId, orderId);
        OrdersSubmitDTO ordersSubmitDTO = new OrdersSubmitDTO();
        BeanUtils.copyProperties(orders, ordersSubmitDTO);
        LocalDateTime now = LocalDateTime.now();
        now.plusHours(1);
        ordersSubmitDTO.setEstimatedDeliveryTime(now);
        //插入购物车数据
        List<OrderDetail> orderDetailList = orderDetailMapper.queryByOrderId(orderId);
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setUserId(userId);
            shoppingCartMapper.insert(shoppingCart);
        }
        orderController.submit(ordersSubmitDTO);
    }

    /**
     * 管理端实现分页查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        long total = page.getTotal();
        List<Orders> result = page.getResult();
        Map<Long, List<OrderDetail>> detailMap = getOrderDetailMap(result);
        List<OrderPageQueryVO> list = result.stream().map(orders -> {
            OrderPageQueryVO orderPageQueryVO = new OrderPageQueryVO();
            BeanUtils.copyProperties(orders, orderPageQueryVO);
            List<OrderDetail> orderDetails = detailMap.get(orderPageQueryVO.getId());
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < orderDetails.size(); i++) {
                if (i == orderDetails.size() - 1) {
                    stringBuilder.append(orderDetails.get(i).getName());
                    break;
                }
                stringBuilder.append(orderDetails.get(i).getName() + ",");
            }
            orderPageQueryVO.setOrderDishes(stringBuilder.toString());
            return orderPageQueryVO;
        }).collect(Collectors.toList());
        PageResult pageResult = new PageResult(total, list);
        return pageResult;
    }

    /**
     * 管理端实现 查询订单状态统计
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statisticsQuery() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(orderMapper.queryStatus(Orders.TO_BE_CONFIRMED));
        orderStatisticsVO.setConfirmed(orderMapper.queryStatus(Orders.CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.queryStatus(Orders.DELIVERY_IN_PROGRESS));
        return orderStatisticsVO;
    }

    /**
     * 商家接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        //根据id查询单子
        Orders orders =  orderMapper.queryByOrderId(ordersConfirmDTO.getId());
        orderMapper.updateStatus(Orders.CONFIRMED,orders.getPayStatus(),orders.getCheckoutTime(),orders.getNumber());
    }

    /**
     * 商家拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.queryByOrderId(ordersRejectionDTO.getId());
        //检验是 待接单
        if(orders.getStatus() != Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //检验用户是否支付
//        if(orders.getPayStatus() == Orders.PAID){
//            try {
//                String refund = weChatPayUtil.refund(orders.getNumber(),
//                        orders.getNumber(),
//                        new BigDecimal(0.01),
//                        new BigDecimal(0.01));
//                log.info("退款{}",refund);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
        orders.setStatus(Orders.CANCELLED);
        orders.setPayStatus(Orders.REFUND);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = orderMapper.queryByOrderId(ordersCancelDTO.getId());
        //检验是 待配送
        if(orders.getStatus() != Orders.CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //检验用户是否支付
//        if(orders.getPayStatus() == Orders.PAID){
//            try {
//                String refund = weChatPayUtil.refund(orders.getNumber(),
//                        orders.getNumber(),
//                        new BigDecimal(0.01),
//                        new BigDecimal(0.01));
//                log.info("退款{}",refund);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setPayStatus(Orders.REFUND);
        orderMapper.update(orders);
    }

    /**
     * 商家实现派送订单
     * @param orderId
     */
    @Override
    public void delivery(Long orderId) {
        Orders orders = orderMapper.queryByOrderId(orderId);
        //判断是否是 已结单
        if(orders.getStatus() != Orders.CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    @Override
    public void complete(Long orderId) {
        Orders orders = orderMapper.queryByOrderId(orderId);
        //判断是否是 配送种
        if(orders.getStatus() != Orders.DELIVERY_IN_PROGRESS){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }


    public Map<Long, List<OrderDetail>> getOrderDetailMap(List<Orders> result) {
        if(result == null || result.isEmpty()){
            return new HashMap<>();
        }
        List<Long> orderIds = new ArrayList<>();
        for (Orders orders : result) {
            orderIds.add(orders.getId());
        }
        if(orderIds.isEmpty()){
            return new HashMap<>();
        }
        List<OrderDetail> orderDetailLists = orderDetailMapper.queryByOrderIds(orderIds);
        Map<Long, List<OrderDetail>> detailMap = orderDetailLists.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId)); //stream流
        return detailMap;
    }





}
