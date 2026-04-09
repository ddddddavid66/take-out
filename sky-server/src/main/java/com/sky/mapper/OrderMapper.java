package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    public void insert(Orders orders);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{checkOutTime} " +
            "where number = #{orderNumber}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime checkOutTime, String orderNumber);

    Page<Orders> queryByUserId(Integer status, Long userId);

    @Select("select * from orders where user_id  = #{userId} and id = #{orderId}")
    Orders queryDetail(Long userId, Long orderId);

    @Delete("delete from orders where id = #{orderId}")
    void deleteByOrderId(Long orderId);

    @Select("select * from orders where id = #{orderId} and user_id = #{userId}")
    Orders  queryByUserOrderId(Long userId, Long orderId);

    /**
     * 管理端实现分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select count(*) from orders where status = #{Status}")
    Integer queryStatus(Integer Status);

    @Select("select * from orders where  id = #{orderId}")
    Orders queryByOrderId(Long orderId);

    @Select("select * from orders where status = #{status} and order_time < #{minus}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime minus);

    void updateBatch(List<Orders> list);

    Double sumByMap(Map map);

    List<Map<String, Object>> sumByMapAndDate(Map map);
}
