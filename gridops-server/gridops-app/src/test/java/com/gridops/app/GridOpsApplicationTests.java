package com.gridops.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class GridOpsApplicationTests {

    @Test
    void applicationClassLoads() {
        assertNotNull(GridOpsApplication.class);
    }
}
