package com.aoshen.usercenter.service.impl;

import java.util.ArrayList;

import com.aoshen.usercenter.exception.BusinessException;
import com.aoshen.usercenter.model.domain.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.aoshen.usercenter.common.ErrorCode;
import com.aoshen.usercenter.service.UserService;
import com.aoshen.usercenter.mapper.UserMapper;
import com.aoshen.usercenter.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.aoshen.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.aoshen.usercenter.contant.UserConstant.USER_LOGIN_STATE;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "aoshen";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode, String avatarUrl) {
        if (StringUtils.isEmpty(avatarUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请上传头像");
        }
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        user.setAvatarUrl(avatarUrl);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser 原始用户信息
     * @return 脱敏后的用户信息
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setProfile(originUser.getProfile());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request 请求体
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public List<User> searchUserCondition(String keyword) {
        return baseMapper.searchByUserVo(keyword);
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 标签列表
     * @return 符合标签的用户
     */
    @Override
    public IPage<User> searchUsersByTags(long pageNum, long pageSize, List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> wrapper = new QueryWrapper<User>().ne("tags", "[]");
        List<User> userList = baseMapper.selectList(wrapper);
        Gson gson = new Gson();

        Page<User> page = new Page<>(pageNum, pageSize);
        return page.setRecords(userList.stream().filter(user -> {
            String tags = user.getTags();
            // 使用gson 将字符串转为json
            Set<String> tempTagNameSet = gson.fromJson(tags, new TypeToken<Set<String>>() {}.getType());

            // 判断tempTagNameSet 是不是空
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());

            for (String tagName : tagNameList) {
                // 如果用户不存在这个标签，就直接return false
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList()));
    }


    @Override
    public User getCurrentUser(HttpServletRequest request) {
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObject == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) userObject;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        Long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果不是管理员的同时，登录id与修改用户id不一致
        if (!isAdmin(loginUser) && user.getId().longValue() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = baseMapper.selectById(user.getId());
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return baseMapper.updateById(user);
    }

    @Override
    public IPage<User> getMatchedUsersPage(long pageNum, long pageSize, User loginUser) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<User>()
                .select("id", "tags")
                .ne("tags", "[]")
                .isNotNull("tags");

        Gson gson = new Gson();
        // 计算所有用户的相似度，并将用户及其相似度保存在一个 Map 中
        String loginUserTags = loginUser.getTags();
        List<String> tagList = gson.fromJson(loginUserTags, new TypeToken<List<String>>() {
        }.getType());

        HashMap<User, Integer> userScoreMap = new HashMap<>();
        this.list(userQueryWrapper).stream()
                .filter(user -> !user.getId().equals(loginUser.getId())) // 排除当前登录用户
                .forEach(user -> userScoreMap.put(user,
                        AlgorithmUtils.minDistance(gson.fromJson(user.getTags(), new TypeToken<List<String>>() {}.getType())
                        , tagList)));

        // 对 Map 中的用户进行排序
        List<User> sortedUserList = sortMap(userScoreMap);
        List<Long> validUserIdData = sortedUserList.stream().map(User::getId).collect(Collectors.toList());

        // 构造分页参数，从数据库中查询指定范围的数据，并保持顺序
        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .in("id", validUserIdData)
                .orderByAsc("FIELD(id, " + StringUtils.join(validUserIdData, ",") + ")");
        IPage<User> userPage = this.page(page, queryWrapper);

        // 将查询出来的数据进行安全处理，并返回分页结果
        return userPage.convert(this::getSafetyUser);
    }

    /**
     * map 排序
     *
     * @param map 需要排序的map 集合
     * @return 排好序的list
     */
    public static List<User> sortMap(Map<User, Integer> map) {
        //利用Map的entrySet方法，转化为list进行排序
        List<Map.Entry<User, Integer>> entryList = new ArrayList<>(map.entrySet());
        //利用Collections的sort方法对list排序
        entryList.sort(Comparator.comparingInt(Map.Entry::getValue));

        List<User> userList = new ArrayList<>();
        for (Map.Entry<User, Integer> e : entryList) {
            userList.add(e.getKey());
        }
        return userList;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 根据标签查询用户sql 版
     *
     * @param tagNameList
     * @return
     */
    @Deprecated
    public List<User> searchUsersByTagsSql(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //拼接 and 查询
        //like '%Java%' and like '%Python%'
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tag : tagNameList) {
            queryWrapper.like("tags", tag);
        }
        List<User> userList = baseMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }
}




