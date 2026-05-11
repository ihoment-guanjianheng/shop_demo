package com.gjh.shopdemo.constant;

public class RedisConstant {

    public static final String TOKEN_PREFIX = "token:";

    public static final long TOKEN_EXPIRE_TIME = 24 * 60 * 60;

    public static final String PERMISSION_USER_PREFIX = "permission:user:";

    public static final long PERMISSION_EXPIRE_TIME = 24 * 60 * 60;

    public static final String PRODUCT_DETAIL_KEY = "cache:product:detail:";

    public static final long CACHE_EXPIRE_TIME = 60 * 60;

    public static final String MQ_CONSUMED_KEY_PREFIX = "mq:consumed:";
}
