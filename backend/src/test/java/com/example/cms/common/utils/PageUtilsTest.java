package com.example.cms.common.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageUtilsTest {

    @Test
    void normalizesInvalidValues() {
        PageUtils.Page page = PageUtils.normalize(0, 0);

        assertThat(page.page()).isEqualTo(1);
        assertThat(page.pageSize()).isEqualTo(10);
        assertThat(page.offset()).isZero();
    }

    @Test
    void capsPageSizeAndCalculatesOffset() {
        PageUtils.Page page = PageUtils.normalize(3, 1000);

        assertThat(page.page()).isEqualTo(3);
        assertThat(page.pageSize()).isEqualTo(100);
        assertThat(page.offset()).isEqualTo(200);
    }
}
