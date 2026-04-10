package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid = #{openid}")
   User getByOpenId(String openid);

    void insert(User user);

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    List<Map<String, Object>> sumUserByMapAndDate(Map map);

    Integer countByMap(Map map);
}
