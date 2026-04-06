package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@Api(tags = "菜品相关接口")
@RequestMapping("admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        dishService.saveWithFlavor(dishDTO);
        //清理缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
         return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> pageQuery(DishPageQueryDTO dto){
        log.info("菜品分页查询{}",dto);
        PageResult pageResult  = dishService.pageQuery(dto);
        return Result.success(pageResult);
    }

    @ApiOperation("菜品批量删除")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("正在删除id为{}的菜品",ids);
        dishService.deleteBatch(ids);
        //清理缓存数据  直接清理所有的菜品缓存数据
        cleanCache("dish_*");

        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品 和 口味")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id{}查询菜品",id);
        DishVO dishVO =  dishService.getById(id);
        return Result.success(dishVO);
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Integer categoryId){
        log.info("根据分类id{}查询菜品",categoryId);
        List<Dish> list = dishService.getByCategotyId(categoryId, null);
        return Result.success(list);
    }


    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){ //这里选择 跟新增的DishDto即可
        log.info("正在修改菜品{}",dishDTO);
        dishService.updateWithFlavors(dishDTO);
        //清理缓存数据  直接清理所有的菜品缓存数据
        cleanCache("dish_*");

        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品状态修改")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("实现对菜品{}的状态修改为{}",id,status);
        dishService.startOrStop(status,id);
        //清理缓存数据  直接清理所有的菜品缓存数据
        cleanCache("dish_*");

        return Result.success();
    }

    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
