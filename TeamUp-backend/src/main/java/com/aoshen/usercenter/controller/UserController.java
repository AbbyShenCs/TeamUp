package com.aoshen.usercenter.controller;

import com.aoshen.usercenter.model.domain.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.aoshen.usercenter.common.BaseResponse;
import com.aoshen.usercenter.common.ErrorCode;
import com.aoshen.usercenter.common.ResultUtils;
import com.aoshen.usercenter.exception.BusinessException;
import com.aoshen.usercenter.model.request.UserLoginRequest;
import com.aoshen.usercenter.model.request.UserRegisterRequest;
import com.aoshen.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.aoshen.usercenter.contant.ServerConstant.PROD_ADDRESS;
import static com.aoshen.usercenter.contant.ServerConstant.SERVER_ADDRESS;


@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {SERVER_ADDRESS, PROD_ADDRESS}, allowCredentials = "true")  // 允许跨域
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 注册
     *
     * @param userRegisterRequest 注册请求体
     * @return 统一返回
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        String avatarUrl = userRegisterRequest.getAvatarUrl();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode, avatarUrl)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode, avatarUrl);
        return ResultUtils.success(result);
    }

    /**
     * 登录
     *
     * @param userLoginRequest 用户信息请求体
     * @param request          请求体
     * @return 统一返回
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 登出
     *
     * @param request 请求体
     * @return 统一返回
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户信息
     *
     * @param request 请求体
     * @return 统一返回
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 获取用户信息列表
     *
     * @param username 前端传来的查询数据
     * @param request  请求体
     * @return 统一返回
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);

    }

    /**
     * 根据id 删除用户信息
     *
     * @param id      用户id
     * @param request 请求体
     * @return 统一返回
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@PathParam("id") long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 条件查询
     *
     * @param keyword 前端传来的查询数据
     * @return 统一返回体
     */
    @GetMapping("/getByCondition")
    public BaseResponse<List<User>> searchUserCondition(@PathParam("keyword") String keyword, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        List<User> userList = null;
        if (StringUtils.isEmpty(keyword)) {
            userList = userService.list();
        } else {
            userList = userService.searchUserCondition(keyword);
        }
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/search/tags")
    public BaseResponse<IPage<User>> searchUserByTags(long pageSize, long pageNum, @RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        IPage<User> pageResult = userService.searchUsersByTags(pageNum, pageSize, tagNameList);
        return ResultUtils.success(pageResult);
    }

    @GetMapping("/updateTags")
    public BaseResponse<Integer> updateTags(@RequestParam Long id, @RequestParam(required = false) List<String> tagNameList, HttpServletRequest request) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择至少一个标签");
        }
        Gson gson = new Gson();
        String tags = gson.toJson(tagNameList);

        User loginUser = userService.getCurrentUser(request);
        User user = new User();
        user.setTags(tags);
        user.setId(id);
        int i = userService.updateUser(user, loginUser);
        return ResultUtils.success(i);
    }

    /**
     * 更新用户信息，
     *
     * @param user    修改用户信息
     * @param request 登录用户信息
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        int i = userService.updateUser(user, loginUser);
        return ResultUtils.success(i);
    }


    /**
     * 首页展示推荐用户
     *
     * @param pageSize 页面大小
     * @param pageNum  数据起始条数
     * @param request  请求体
     * @return 推荐的用户列表
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getCurrentUser(request);
        String redisKey = String.format("yupao:user:recommend:%s:%s", loginUser.getId(), pageSize);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        //如果有缓存，直接读缓存
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        //无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>().ne("id", loginUser.getId());
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        //写缓存
        try {
            valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userPage);
    }

    /**
     * 获取最匹配的用户
     * @param pageNum 数据条数
     * @param pageSize 页面大小
     * @param request 请求体
     * @return 返回匹配的用户列表
     */
    @GetMapping("/match")
    public BaseResponse<IPage<User>> matchUsers(long pageNum,long pageSize, HttpServletRequest request) {
        User user = userService.getCurrentUser(request);
        return ResultUtils.success(userService.getMatchedUsersPage(pageNum, pageSize, user));
    }
}
