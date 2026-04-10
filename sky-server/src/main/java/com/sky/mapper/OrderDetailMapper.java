package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {
    public void insertBatch(List<OrderDetail> list2);

    List<OrderDetail> queryByOrderId(Long orderId);

    List<OrderDetail> queryByOrderIds(List<Long> orderIds);

    @Delete("delete from order_detail where order_id =#{orderId}")
    void deleteByOrderId(Long orderId);

    List<Map<String, Object>> countSaleTop10(Map map);
}
