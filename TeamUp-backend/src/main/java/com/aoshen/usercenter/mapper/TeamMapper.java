package com.aoshen.usercenter.mapper;

import com.aoshen.usercenter.model.domain.Team;
import com.aoshen.usercenter.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;


public interface TeamMapper extends BaseMapper<Team> {

    /**
     * 根据队伍id 查询加入队伍的用户信息列表
     *
     * @param id 队伍id
     * @return 用户信息列表
     */
    List<User> selectJoinUsers(Long id);
}
