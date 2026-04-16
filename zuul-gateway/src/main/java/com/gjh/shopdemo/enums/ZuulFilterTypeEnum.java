package com.gjh.shopdemo.enums;

public enum ZuulFilterTypeEnum {
    PRE,
    ROUTING,
    POST,
    ERROR
    ;
    public String toString() {
        return this.name().toLowerCase();
    }
}
