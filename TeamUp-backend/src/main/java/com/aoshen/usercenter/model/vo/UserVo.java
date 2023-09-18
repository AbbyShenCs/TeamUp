package com.aoshen.usercenter.model.vo;

import lombok.Data;

import java.io.Serializable;


@Data
public class UserVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long current;
    private Long pageSize;
    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;


    /**
     * 性别
     */
    private Integer gender;


    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0 - 正常
     */
    private Integer userStatus;

    /**
     * 用户角色 0 - 普通用户 1 - 管理员
     */
    private Integer userRole;

    /**
     * 星球编号
     */
    private String planetCode;
}
