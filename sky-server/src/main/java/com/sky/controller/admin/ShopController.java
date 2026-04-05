package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController(value = "adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;

    public static final String KEY = "SHOP_STATUS";

    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("修改店铺营业状态为{}",status);
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("查询店铺营业状态")
    public Result getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取店铺营业状态为{}",status == 1 ? "营业":"打烊");
        log.info("店铺营业状态为{}",status);
        return Result.success(status);
    }



}
