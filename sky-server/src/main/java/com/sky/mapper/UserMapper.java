package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     */
    User getByOpenid(String openid);
    /**
     * 插入数据
     */

    void insert(User user);
}
