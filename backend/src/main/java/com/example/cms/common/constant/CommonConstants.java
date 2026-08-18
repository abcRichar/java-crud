package com.example.cms.common.constant;

public final class CommonConstants {

    private CommonConstants() {
    }

    public static final String NOT_DELETED = "0";
    public static final String DELETED = "1";

    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 0;

    public static final String REDIS_TOKEN_PREFIX = "cms:token:";
    public static final String REDIS_USER_PERMISSIONS_PREFIX = "cms:perms:";

    public static final String DEFAULT_PAGE = "1";
    public static final String DEFAULT_PAGE_SIZE = "10";
}
