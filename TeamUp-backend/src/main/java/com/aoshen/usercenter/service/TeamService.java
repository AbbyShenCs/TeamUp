package com.aoshen.usercenter.service;

import com.aoshen.usercenter.model.domain.Team;
import com.aoshen.usercenter.model.domain.User;
import com.aoshen.usercenter.model.dto.TeamQuery;
import com.aoshen.usercenter.model.vo.TeamUserQueryVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aoshen.usercenter.model.request.TeamJoinRequest;
import com.aoshen.usercenter.model.request.TeamQuitRequest;
import com.aoshen.usercenter.model.request.TeamUpdateRequest;

import java.util.List;


public interface TeamService extends IService<Team> {

    /**
     * 新增队伍
     *
     * @param team      队伍基本信息
     * @param loginUser 当前登录用户
     * @return 队伍创建后的id
     */
    Long addTeam(Team team, User loginUser);

    /**
     * 根据是否是管理员查询符合要求的队伍信息
     *
     * @param teamQuery 队伍查询条件
     * @param isAdmin   是否为管理员
     * @return 队伍和队伍所包含的用户信息
     */
    List<TeamUserQueryVO> listTeams(TeamQuery teamQuery, boolean isAdmin);


    /**
     * 更新队伍信息
     *
     * @param teamUpdateRequest 包含新的队伍信息请求体
     * @param loginUser         当前登录用户
     * @return 是否更新成功
     */
    boolean update(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     *
     * @param teamJoinRequest 加入队伍包含的信息
     * @param loginUser       当前登录用户信息
     * @return 返回是否加入成功
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     *
     * @param teamQuitRequest 包含申请退出队伍的id
     * @param loginUser       当前登录用户
     * @return 返回是否成功退出队伍
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除队伍，并删除所有的关联关系
     *
     * @param id        队伍id
     * @param loginUser 当前登录用户
     * @return 是否删除成功
     */
    boolean removeTeam(Long id, User loginUser);

    /**
     * 根据队伍id 获取所有加入队伍的用户信息
     *
     * @param id 队伍 id
     * @return 加入队伍的用户信息列表
     */
    List<User> listJoinUsers(Long id);
}
