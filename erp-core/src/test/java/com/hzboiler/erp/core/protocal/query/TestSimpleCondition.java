package com.hzboiler.erp.core.protocal.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hzboiler.erp.core.model.Mock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

class TestSimpleCondition {

    static final LocalDateTime CREATE_TIME = LocalDateTime.of(2020, 1, 1, 0, 0, 0);

    @Test
    void testApplyToQueryWrapperGeneral() {
        QueryWrapper<Mock> mockQueryWrapper;
        Condition condition;

        mockQueryWrapper = new QueryWrapper<>();
        condition = SimpleCondition.of("id", "=", 100);
        condition.applyToQueryWrapper(mockQueryWrapper);
        Assertions.assertEquals("(id = #{ew.paramNameValuePairs.MPGENVAL1})", mockQueryWrapper.getSqlSegment());
        Assertions.assertEquals(100, mockQueryWrapper.getParamNameValuePairs().get("MPGENVAL1"));

        mockQueryWrapper = new QueryWrapper<>();
        condition = SimpleCondition.of("id", "<", 100);
        condition.applyToQueryWrapper(mockQueryWrapper);
        Assertions.assertEquals("(id < #{ew.paramNameValuePairs.MPGENVAL1})", mockQueryWrapper.getSqlSegment());
        Assertions.assertEquals(100, mockQueryWrapper.getParamNameValuePairs().get("MPGENVAL1"));

        mockQueryWrapper = new QueryWrapper<>();
        condition = SimpleCondition.of("id", ">", 100);
        condition.applyToQueryWrapper(mockQueryWrapper);
        Assertions.assertEquals("(id > #{ew.paramNameValuePairs.MPGENVAL1})", mockQueryWrapper.getSqlSegment());
        Assertions.assertEquals(100, mockQueryWrapper.getParamNameValuePairs().get("MPGENVAL1"));

        mockQueryWrapper = new QueryWrapper<>();
        condition = SimpleCondition.of("id", "!=", 100);
        condition.applyToQueryWrapper(mockQueryWrapper);
        Assertions.assertEquals("(id <> #{ew.paramNameValuePairs.MPGENVAL1})", mockQueryWrapper.getSqlSegment());
        Assertions.assertEquals(100, mockQueryWrapper.getParamNameValuePairs().get("MPGENVAL1"));

        mockQueryWrapper = new QueryWrapper<>();
        condition = SimpleCondition.of("id", "<=", 100);
        condition.applyToQueryWrapper(mockQueryWrapper);
        Assertions.assertEquals("(id <= #{ew.paramNameValuePairs.MPGENVAL1})", mockQueryWrapper.getSqlSegment());
        Assertions.assertEquals(100, mockQueryWrapper.getParamNameValuePairs().get("MPGENVAL1"));

        mockQueryWrapper = new QueryWrapper<>();
        condition = SimpleCondition.of("id", ">=", 100);
        condition.applyToQueryWrapper(mockQueryWrapper);
        Assertions.assertEquals("(id >= #{ew.paramNameValuePairs.MPGENVAL1})", mockQueryWrapper.getSqlSegment());
        Assertions.assertEquals(100, mockQueryWrapper.getParamNameValuePairs().get("MPGENVAL1"));
    }

    @Test
    void testApplyToQueryWrapperLike() {
        QueryWrapper<Mock> mockQueryWrapper;
        Condition condition;

        mockQueryWrapper = new QueryWrapper<>();
        condition = SimpleCondition.of("name", "like", "mock");
        condition.applyToQueryWrapper(mockQueryWrapper);
        Assertions.assertEquals("(name LIKE #{ew.paramNameValuePairs.MPGENVAL1})", mockQueryWrapper.getSqlSegment());
        Assertions.assertEquals("%mock%", mockQueryWrapper.getParamNameValuePairs().get("MPGENVAL1"));

        mockQueryWrapper = new QueryWrapper<>();
        condition = SimpleCondition.of("name", "likeLeft", "mock");
        condition.applyToQueryWrapper(mockQueryWrapper);
        Assertions.assertEquals("(name LIKE #{ew.paramNameValuePairs.MPGENVAL1})", mockQueryWrapper.getSqlSegment());
        Assertions.assertEquals("%mock", mockQueryWrapper.getParamNameValuePairs().get("MPGENVAL1"));

        mockQueryWrapper = new QueryWrapper<>();
        condition = SimpleCondition.of("name", "likeRight", "mock");
        condition.applyToQueryWrapper(mockQueryWrapper);
        Assertions.assertEquals("(name LIKE #{ew.paramNameValuePairs.MPGENVAL1})", mockQueryWrapper.getSqlSegment());
        Assertions.assertEquals("mock%", mockQueryWrapper.getParamNameValuePairs().get("MPGENVAL1"));

        mockQueryWrapper = new QueryWrapper<>();
        condition = SimpleCondition.of("name", "notLike", "mock");
        condition.applyToQueryWrapper(mockQueryWrapper);
        Assertions.assertEquals("(name NOT LIKE #{ew.paramNameValuePairs.MPGENVAL1})", mockQueryWrapper.getSqlSegment());
        Assertions.assertEquals("%mock%", mockQueryWrapper.getParamNameValuePairs().get("MPGENVAL1"));

        mockQueryWrapper = new QueryWrapper<>();
        condition = SimpleCondition.of("name", "notLikeLeft", "mock");
        condition.applyToQueryWrapper(mockQueryWrapper);
        Assertions.assertEquals("(name NOT LIKE #{ew.paramNameValuePairs.MPGENVAL1})", mockQueryWrapper.getSqlSegment());
        Assertions.assertEquals("%mock", mockQueryWrapper.getParamNameValuePairs().get("MPGENVAL1"));

        mockQueryWrapper = new QueryWrapper<>();
        condition = SimpleCondition.of("name", "notLikeRight", "mock");
        condition.applyToQueryWrapper(mockQueryWrapper);
        Assertions.assertEquals("(name NOT LIKE #{ew.paramNameValuePairs.MPGENVAL1})", mockQueryWrapper.getSqlSegment());
        Assertions.assertEquals("mock%", mockQueryWrapper.getParamNameValuePairs().get("MPGENVAL1"));
    }
}