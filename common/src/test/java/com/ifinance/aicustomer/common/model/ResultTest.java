package com.ifinance.aicustomer.common.model;

import com.ifinance.aicustomer.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultTest {

    @Test
    void shouldBuildSuccessResult() {
        Result<String> result = Result.ok("data");

        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals("data", result.getData());
        assertEquals(ErrorCode.SUCCESS.getMessage(), result.getMessage());
    }

    @Test
    void shouldBuildFailureResult() {
        Result<Void> result = Result.fail(ErrorCode.BAD_REQUEST);

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), result.getCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), result.getMessage());
        assertNull(result.getData());
    }
}
