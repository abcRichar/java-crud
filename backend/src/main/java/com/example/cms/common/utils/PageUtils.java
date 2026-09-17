package com.example.cms.common.utils;

public final class PageUtils {

    private static final int MAX_PAGE_SIZE = 100;

    private PageUtils() {
    }

    public static Page normalize(Integer page, Integer pageSize) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedPageSize = pageSize == null || pageSize < 1
                ? 10
                : Math.min(pageSize, MAX_PAGE_SIZE);
        return new Page(normalizedPage, normalizedPageSize, (normalizedPage - 1) * normalizedPageSize);
    }

    public record Page(int page, int pageSize, int offset) {
    }
}
