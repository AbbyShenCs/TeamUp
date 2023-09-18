package com.aoshen.usercenter.model.vo;

import com.aoshen.usercenter.model.domain.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
public class TeamUserQueryVO implements Serializable {

    private static final long serialVersionUID = 163478861968488713L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人用户信息
     */
    private UserQueryVo createUser;

    /**
     * 加入队伍的用户信息
     */
    private List<User> joinUsers;

    /**
     * 已加入的用户数
     */
    private Long hasJoinNum;

    /**
     * 是否已加入队伍
     */
    private boolean hasJoin = false;
}