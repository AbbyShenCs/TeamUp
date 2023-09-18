package com.aoshen.usercenter.service;

import com.aoshen.usercenter.model.domain.User;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @param avatarUrl     头像
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode, String avatarUrl);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 按条件查询用户信息
     *
     * @param keyword 用户信息体
     * @return 返回统一数据
     */
    List<User> searchUserCondition(String keyword);

    /**
     * 根据标签列表搜索用户
     *
     * @param tagNameList 标签列表
     * @return list
     */
    IPage<User> searchUsersByTags(long pageSize, long pageNum, List<String> tagNameList);

    /**
     * 获取当前用户信息
     *
     * @param request
     * @return
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 更新用户信息
     *
     * @param user      修改用户
     * @param loginUser 登录用户
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 是否是管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 是否是管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);


    /**
     * 根据相似度匹配用户
     *
     * @param pageNum   数据条数
     * @param pageSize  页面大小
     * @param loginUser 当前登录用户
     * @return 匹配的用户列表
     */
    IPage<User> getMatchedUsersPage(long pageNum, long pageSize, User loginUser);

}
