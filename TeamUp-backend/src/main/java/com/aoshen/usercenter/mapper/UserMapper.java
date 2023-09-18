package com.aoshen.usercenter.mapper;

import com.aoshen.usercenter.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;


public interface UserMapper extends BaseMapper<User> {

    List<User> searchByUserVo(String keyword);

    /**
     * 根据id 列表获取用户信息
     *
     * @param idList id 列表
     * @return 返回相应的用户信息
     */
    List<User> getUserByIdList(String idList);
}




