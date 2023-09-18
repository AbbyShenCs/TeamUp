package com.aoshen.usercenter.controller;

import com.aoshen.usercenter.model.domain.Team;
import com.aoshen.usercenter.model.domain.User;
import com.aoshen.usercenter.model.domain.UserTeam;
import com.aoshen.usercenter.model.dto.TeamQuery;
import com.aoshen.usercenter.model.request.*;
import com.aoshen.usercenter.model.vo.TeamUserQueryVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aoshen.usercenter.common.BaseResponse;
import com.aoshen.usercenter.common.ErrorCode;
import com.aoshen.usercenter.common.ResultUtils;
import com.aoshen.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.request.*;
import com.aoshen.usercenter.service.TeamService;
import com.aoshen.usercenter.service.UserService;
import com.aoshen.usercenter.service.UserTeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.aoshen.usercenter.contant.ServerConstant.PROD_ADDRESS;
import static com.aoshen.usercenter.contant.ServerConstant.SERVER_ADDRESS;

/**
 * 队伍控制类
 *
 * @author Aoshen
 * @date 2023/4/11 8:28
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {SERVER_ADDRESS, PROD_ADDRESS}, allowCredentials = "true")  // 允许跨域
public class TeamController {
    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    /**
     * 新建队伍
     *
     * @param teamAddRequest 队伍信息请求体
     * @param request        请求题
     * @return 返回新建队伍的队伍id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);

        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        // 调用service
        Long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    /**
     * 根据队伍id删除队伍
     *
     * @param deleteRequest 队伍id
     * @return 删除成功与否
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestBody TeamDeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        User loginUser = userService.getCurrentUser(request);
        boolean res = teamService.removeTeam(id, loginUser);
        if (!res) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 根据id 修改队伍信息
     *
     * @param teamUpdateRequest 队伍信息
     * @param request           请求体
     * @return 修改成功与否
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> update(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        boolean res = teamService.update(teamUpdateRequest, loginUser);
        if (!res) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 根据队伍id获取队伍信息
     *
     * @param id 队伍id
     * @return 返回查找的队伍
     */
    @GetMapping("/get")
    public BaseResponse<TeamUserQueryVO> get(@RequestParam long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TeamQuery teamQuery = new TeamQuery();
        teamQuery.setId(id);
        List<TeamUserQueryVO> teamList = teamService.listTeams(teamQuery, true);
        if (CollectionUtils.isEmpty(teamList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        TeamUserQueryVO team = teamList.get(0);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        List<User> joinUser = teamService.listJoinUsers(team.getId());
        team.setJoinUsers(joinUser);
        return ResultUtils.success(team);
    }

    /**
     * 根据条件查询队伍信息
     *
     * @param teamQuery 队伍查询条件
     * @param request   请求体
     * @return 返回符合条件的用户信息
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserQueryVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        Long userId = userService.getCurrentUser(request).getId();
        // 当前用户加入和创建的队伍用户关联表信息
        List<TeamUserQueryVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        if (!isAdmin) {
            List<UserTeam> userTeamList = userTeamService.list(new QueryWrapper<UserTeam>().eq("userId", userId));
            // 不查询已经加入和创建的队伍
            teamList = teamList.stream().filter(team -> {
                boolean flag = true;
                for (UserTeam userTeam : userTeamList) {
                    if (userTeam.getTeamId().longValue() == team.getId().longValue()) {
                        flag = false;
                        break;
                    }
                }
                return flag;
            }).collect(Collectors.toList());
        }

        return ResultUtils.success(teamList);
    }

    /**
     * 根据条件查询队伍信息
     *
     * @param teamQuery 队伍查询条件
     * @param request   请求体
     * @return 返回符合条件的用户信息
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserQueryVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);

        // 只查询自己创建的队伍
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserQueryVO> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamList);
    }

    /**
     * 根据条件查询参加的队伍信息
     *
     * @param teamQuery 队伍查询条件
     * @param request   请求体
     * @return 返回符合条件的用户信息
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserQueryVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = userService.getCurrentUser(request).getId();
        // 当前用户加入和创建的队伍用户关联表信息
        List<UserTeam> userTeamList = userTeamService.list(new QueryWrapper<UserTeam>().eq("userId", userId));

        List<TeamUserQueryVO> teamList = teamService.listTeams(teamQuery, true);
        List<TeamUserQueryVO> joinTeamList;

        // 过滤出自己的创建和加入的队伍
        joinTeamList = teamList.stream().filter(team -> {
            boolean flag = false;
            for (UserTeam userTeam : userTeamList) {
                if (userTeam.getTeamId().longValue() == team.getId().longValue()) {
                    flag = true;
                    break;
                }
            }
            return flag;
        }).collect(Collectors.toList());
        return ResultUtils.success(joinTeamList);
    }

    /**
     * 根据条件查询队伍信息
     *
     * @param teamQuery 查询条件
     * @return 符合条件的队伍信息
     */
    @GetMapping("/listPage")
    public BaseResponse<Page<Team>> listPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int current = teamQuery.getPageNum();
        int pageSize = teamQuery.getPageSize();

        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);

        QueryWrapper<Team> wrapper = new QueryWrapper<>(team);
        Page<Team> page = teamService.page(new Page<>(current, pageSize), wrapper);

        return ResultUtils.success(page);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest 加入队伍包含的信息
     * @param request         请求体
     * @return 返回是否加入成功
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getCurrentUser(request);
        boolean res = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * 退出队伍
     *
     * @param teamQuitRequest 包含申请退出队伍的id
     * @param request         请求体
     * @return 返回是否成功退出队伍
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        boolean res = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(res);
    }
}
