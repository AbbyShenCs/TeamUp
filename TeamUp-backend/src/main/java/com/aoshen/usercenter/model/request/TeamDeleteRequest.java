package com.aoshen.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamDeleteRequest implements Serializable {

    private static final long serialVersionUID = 1787902631969457554L;

    private long id;
}