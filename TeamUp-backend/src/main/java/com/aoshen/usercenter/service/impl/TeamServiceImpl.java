package com.aoshen.usercenter.service.impl;

import com.aoshen.usercenter.exception.BusinessException;
import com.aoshen.usercenter.model.domain.Team;
import com.aoshen.usercenter.model.domain.User;
import com.aoshen.usercenter.model.domain.UserTeam;
import com.aoshen.usercenter.model.dto.TeamQuery;
import com.aoshen.usercenter.model.enums.TeamStatusEnums;
import com.aoshen.usercenter.model.vo.TeamUserQueryVO;
import com.aoshen.usercenter.model.vo.UserQueryVo;
import com.aoshen.usercenter.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aoshen.usercenter.common.ErrorCode;
import com.aoshen.usercenter.mapper.TeamMapper;
import com.aoshen.usercenter.model.request.TeamJoinRequest;
import com.aoshen.usercenter.model.request.TeamQuitRequest;
import com.aoshen.usercenter.model.request.TeamUpdateRequest;
import com.aoshen.usercenter.service.TeamService;
import com.aoshen.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Transactional
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    private static final String USER_ID = "userId";

    @Override
    public Long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final Long userId = loginUser.getId();
        // 3. 校验信息
        //      1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //      2. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isEmpty(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不符合要求");
        }
        //      3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");

        }
        // 4. status 是否公开（int）不传默认为 0（公开）
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnums statusEnums = TeamStatusEnums.getEnumByValue(status);
        if (statusEnums == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态异常");
        }
        // 5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnums.SECRET.equals(statusEnums) && (StringUtils.isBlank(password) || password.length() > 32)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");

        }
        // 6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "失效时间必须大于当前时间");
        }
        // 7. 校验用户最多创建 5 个队伍
        // todo 有bug，高并发状态下可能创建很多队伍
        QueryWrapper<Team> wrapper = new QueryWrapper<Team>().eq(USER_ID, userId);
        long count = this.count(wrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建五个队伍");
        }
        // 4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);

        boolean res = this.save(team);
        if (!res) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        Long teamId = team.getId();
        // 5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        long currentTime = System.currentTimeMillis() + 8 * 60 * 60 * 1000;
        userTeam.setJoinTime(new Date(currentTime));
        res = userTeamService.save(userTeam);
        if (!res) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserQueryVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        // 查询条件为空默认返回空数据
        if (teamQuery == null) {
            return new ArrayList<>();
        }
        QueryWrapper<Team> wrapper = new QueryWrapper<>();
        // 检验 teamQuery 中的数据，使用mybatis-plus的方式来模糊查询
        Long teamId = teamQuery.getId();
        if (teamId != null && teamId > 0) {
            wrapper.eq("id", teamId);
        }

        // 根据关键词搜索
        String searchText = teamQuery.getSearchText();
        if (StringUtils.isNotBlank(searchText)) {
            wrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
        }

        String name = teamQuery.getName();
        if (StringUtils.isNotBlank(name)) {
            wrapper.like("name", name);
        }

        String description = teamQuery.getDescription();
        if (StringUtils.isNotBlank(description)) {
            wrapper.like("description", description);
        }

        Integer maxNum = teamQuery.getMaxNum();
        if (maxNum != null && maxNum > 0) {
            wrapper.eq("maxNUm", maxNum);
        }

        // 根据创建人来查询
        Long userId = teamQuery.getUserId();
        if (userId != null && userId > 0) {
            wrapper.eq(USER_ID, userId);
        }

        // 根据状态来查询：是否为管理员，只有管理员能查询不公开的队伍
        Integer status = teamQuery.getStatus();
        TeamStatusEnums statusEnums = TeamStatusEnums.getEnumByValue(status);
        if (statusEnums == null) {
            // 如果没设置状态就默认为公开的
            statusEnums = TeamStatusEnums.PUBLIC;
        }
        if (!isAdmin && statusEnums.equals(TeamStatusEnums.PRIVATE)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

//        if (!isAdmin) {
//            // 不显示已经过期的队伍
//            wrapper.gt("expireTime", new Date()).and(qw -> qw.isNotNull("expireTime"));
//        }

        if (!isAdmin) {
            wrapper.and(qw ->
                    qw.and(q -> q.eq("status", TeamStatusEnums.PUBLIC.getValue()).or().eq("status", TeamStatusEnums.SECRET.getValue()))
                            .isNotNull("expireTime")
                            .gt("expireTime", new Date())
            );
        }

        // 根据条件查询
        List<Team> teamList = this.list(wrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }

        List<TeamUserQueryVO> teamUserQueryVOList = new ArrayList<>();
        // 关联用户查询
        for (Team team : teamList) {
            Long id = team.getUserId();
            if (id == null) {
                continue;
            }
            User user = userService.getById(id);
            long hasJoinNum = userTeamService.count(new QueryWrapper<UserTeam>().eq("teamId", team.getId()));
            TeamUserQueryVO teamUserQueryVO = new TeamUserQueryVO();
            BeanUtils.copyProperties(team, teamUserQueryVO);
            teamUserQueryVO.setHasJoinNum(hasJoinNum);
            // 脱敏
            if (user != null) {
                UserQueryVo userQueryVo = new UserQueryVo();
                BeanUtils.copyProperties(user, userQueryVo);

                teamUserQueryVO.setCreateUser(userQueryVo);
            }
            teamUserQueryVOList.add(teamUserQueryVO);
        }
        return teamUserQueryVOList;
    }

    @Override
    public boolean update(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        Long id = teamUpdateRequest.getId();
        Team oldTeam = getTeamById(id);

        // 获取当前用户id
        Long userId = loginUser.getId();

        // 管理员和队伍创建者才可以修改队伍信息
        if (!userService.isAdmin(loginUser) && userId.longValue() != oldTeam.getUserId()) {
            throw new BusinessException(ErrorCode.NO_AUTH, "不是创建者！");
        }

        // 如果设置了队伍状态是加密状态，密码不能为空
        Integer status = teamUpdateRequest.getStatus();
        TeamStatusEnums statusEnums = TeamStatusEnums.getEnumByValue(status);
        if (statusEnums == null) {
            statusEnums = TeamStatusEnums.PUBLIC;
        }

        if (statusEnums.equals(TeamStatusEnums.SECRET) && (StringUtils.isBlank(teamUpdateRequest.getPassword()))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须有密码");

        }

        Team newTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, newTeam);

        return this.updateById(newTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 需要加入的队伍id
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);

        // 队伍是否超时
        Date expireTime = team.getExpireTime();
        if (expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已失效");
        }

        // 加入的队伍是否是私有的
        Integer status = team.getStatus();
        if (TeamStatusEnums.PRIVATE.equals(TeamStatusEnums.getEnumByValue(status))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍是私有的，不允许加入");
        }

        // 加密的队伍是否是加密的，验证密码是否正确
        String inputPassword = teamJoinRequest.getPassword();
        if (TeamStatusEnums.SECRET.equals(TeamStatusEnums.getEnumByValue(status)) &&
                (StringUtils.isBlank(inputPassword) || !inputPassword.equals(team.getPassword()))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 只有一个线程能获取到锁
        RLock lock = redissonClient.getLock("yupao:join_team");
        try {
            // 抢到锁并执行
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    // 数据校验通过，下面是逻辑校验
                    QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
                    Long userId = loginUser.getId();

                    // 加入和创建的队伍不能超过五个
                    wrapper.eq(USER_ID, userId);
                    List<UserTeam> teamList = userTeamService.list(wrapper);

                    if (teamList.size() >= 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入五个队伍");
                    }

                    // 如果队伍id相同且队伍里面的userid相同，则提示不能重复加入队伍
                    for (UserTeam userTeam : teamList) {
                        if (teamId.longValue() == userTeam.getTeamId() && userId.longValue() == userTeam.getUserId()) {
                            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能重复加入队伍");
                        }
                    }

                    // 获取当前队伍人数信息，验证是否还能加入
                    int teamUsers = 0;
                    for (UserTeam userTeam : teamList) {
                        if (teamId.longValue() == userTeam.getTeamId()) {
                            teamUsers++;
                        }
                    }
                    if (teamUsers >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数已满");
                    }

                    //修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
            return false;
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                log.debug("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检测退出的队伍是否存在
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);

        // 校验登录用户是否已经加入改队伍
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<UserTeam>().eq("teamId", teamId);
        List<UserTeam> userTeamList = userTeamService.list(wrapper);
        Long userId = loginUser.getId();
        boolean res = userTeamList.stream().anyMatch(userTeam -> userTeam.getUserId() == userId.longValue());
        if (!res) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "没加入此队伍");
        }

        long teamHasJoinNum = userTeamList.size();  // 这个队伍的人数
        // 队伍只剩下一个人， 直接解散
        if (teamHasJoinNum == 1) {
            this.removeById(teamId);
        } else {
            // 如果退出的是队长
            if (team.getUserId() == userId.longValue()) {
                // 把队伍转交给最早加入的用户
                UserTeam nextTeamLeader = userTeamList.get(1);
                Long nextTeamLeaderUserId = nextTeamLeader.getUserId();
                // 修改表关系
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderUserId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        // 移除关系
        wrapper.eq(USER_ID, userId);
        return userTeamService.remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeTeam(Long id, User loginUser) {
        if (id == null || id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = getTeamById(id);
        Long userId = loginUser.getId();

        // 检验是否有权限
        if (team.getUserId().longValue() != userId.longValue() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH,"无访问权限");
        }
        //删除关联关系
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<UserTeam>().eq("teamId", id);
        boolean res = userTeamService.remove(wrapper);
        if (!res) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联关系失败");
        }
        return this.removeById(id);
    }

    @Override
    public List<User> listJoinUsers(Long id) {
        if (id == null || id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = baseMapper.selectJoinUsers(id);
        return userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());

    }

    /**
     * 根据队伍id 获取队伍信息
     *
     * @param id 队伍id
     * @return 队伍信息
     */
    private Team getTeamById(Long id) {
        if (id == null || id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team team = this.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        return team;
    }


}
