package com.aoshen.usercenter.model.enums;


public enum TeamStatusEnums {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private int value;
    private String text;

    // 返回值对应的是哪个枚举类
    public static TeamStatusEnums getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        TeamStatusEnums[] values = TeamStatusEnums.values();
        for (TeamStatusEnums teamStatusEnums : values) {
            if (teamStatusEnums.getValue() == value) {
                return teamStatusEnums;
            }
        }
        return null;
    }

    TeamStatusEnums(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
