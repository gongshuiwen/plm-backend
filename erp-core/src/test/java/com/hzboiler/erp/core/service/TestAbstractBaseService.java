package com.hzboiler.erp.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hzboiler.erp.core.context.BaseContextHolder;
import com.hzboiler.erp.core.model.Mock;
import com.hzboiler.erp.core.mapper.MockMapper;
import com.hzboiler.erp.core.protocal.query.Condition;
import com.hzboiler.erp.core.protocal.query.SimpleCondition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static com.hzboiler.erp.core.security.Constants.*;

/**
 * @author gongshuiwen
 */
@SpringBootTest
public class TestAbstractBaseService {

    static final String MOCK_ENTITY_NAME = "Mock";
    static final String MOCK_AUTHORITY_SELECT = MOCK_ENTITY_NAME + ":" + AUTHORITY_SELECT;
    static final String MOCK_AUTHORITY_CREATE = MOCK_ENTITY_NAME + ":" + AUTHORITY_CREATE;
    static final String MOCK_AUTHORITY_UPDATE = MOCK_ENTITY_NAME + ":" + AUTHORITY_UPDATE;
    static final String MOCK_AUTHORITY_DELETE = MOCK_ENTITY_NAME + ":" + AUTHORITY_DELETE;

    @Autowired
    MockMapper mockMapper;

    @Autowired
    MockService mockService;

    @AfterEach
    void afterEach() {
        BaseContextHolder.clearContext();
    }

    @Test
    @Sql(scripts = {"/sql/test/ddl/mock.sql", "/sql/test/data/mock.sql"})
    @WithMockUser(authorities = {MOCK_AUTHORITY_SELECT})
    void testSelectById() {
        Mock result = mockService.selectById(1L);
        Assertions.assertEquals(1L, result.getId());
        Assertions.assertEquals("mock1", result.getName());
    }

    @Test
    @Sql(scripts = {"/sql/test/ddl/mock.sql", "/sql/test/data/mock.sql"})
    @WithMockUser(authorities = {MOCK_AUTHORITY_SELECT})
    void testSelectByIds() {
        List<Long> ids = List.of(1L, 2L);
        List<Mock> results = mockService.selectByIds(ids);
        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals(1L, results.get(0).getId());
        Assertions.assertEquals("mock1", results.get(0).getName());
        Assertions.assertEquals(2L, results.get(1).getId());
        Assertions.assertEquals("mock2", results.get(1).getName());
    }

    @Test
    @Sql(scripts = {"/sql/test/ddl/mock.sql", "/sql/test/data/mock.sql"})
    @WithMockUser(authorities = {MOCK_AUTHORITY_SELECT})
    void testPage() {
        IPage<Mock> pageResult = mockService.page(1L, 20L);
        Assertions.assertEquals(1, pageResult.getPages());
        Assertions.assertEquals(2, pageResult.getTotal());
        Assertions.assertEquals(1, pageResult.getCurrent());
        Assertions.assertEquals(20, pageResult.getSize());

        List<Mock> results = pageResult.getRecords();
        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals(1L, results.get(0).getId());
        Assertions.assertEquals("mock1", results.get(0).getName());
        Assertions.assertEquals(2L, results.get(1).getId());
        Assertions.assertEquals("mock2", results.get(1).getName());

        pageResult = mockService.page(1L, 20L,"_id");
        Assertions.assertEquals(1, pageResult.getPages());
        Assertions.assertEquals(2, pageResult.getTotal());
        Assertions.assertEquals(1, pageResult.getCurrent());
        Assertions.assertEquals(20, pageResult.getSize());

        results = pageResult.getRecords();
        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals(2L, results.get(0).getId());
        Assertions.assertEquals("mock2", results.get(0).getName());
        Assertions.assertEquals(1L, results.get(1).getId());
        Assertions.assertEquals("mock1", results.get(1).getName());

        Condition condition = SimpleCondition.of("id", "=", 1L);

        pageResult = mockService.page(1L, 20L, "_id", condition);
        Assertions.assertEquals(1, pageResult.getPages());
        Assertions.assertEquals(1, pageResult.getTotal());
        Assertions.assertEquals(1, pageResult.getCurrent());
        Assertions.assertEquals(20, pageResult.getSize());

        results = pageResult.getRecords();
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(1L, results.get(0).getId());
        Assertions.assertEquals("mock1", results.get(0).getName());

        pageResult = mockService.page(1L, 20L, null, condition);
        Assertions.assertEquals(1, pageResult.getPages());
        Assertions.assertEquals(1, pageResult.getTotal());
        Assertions.assertEquals(1, pageResult.getCurrent());
        Assertions.assertEquals(20, pageResult.getSize());

        results = pageResult.getRecords();
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(1L, results.get(0).getId());
        Assertions.assertEquals("mock1", results.get(0).getName());
    }

    @Test
    @Sql(scripts = {"/sql/test/ddl/mock.sql", "/sql/test/data/mock.sql"})
    @WithMockUser(authorities = {MOCK_AUTHORITY_SELECT})
    void testCount() {
        Long count = mockService.count((Condition) null);
        Assertions.assertEquals(2, count);

        Condition condition = SimpleCondition.of("id", "=", 1L);
        count = mockService.count(condition);
        Assertions.assertEquals(1, count);
    }

    @Test
    @Sql(scripts = {"/sql/test/ddl/mock.sql"})
    @WithMockUser(authorities = {MOCK_AUTHORITY_CREATE})
    void testCreateOne() {
        Mock mock = new Mock("mock");
        mockService.createOne(mock);
        Assertions.assertEquals(1, mockMapper.selectList(null).size());

        Assertions.assertEquals(1L, mock.getId());
        Assertions.assertEquals("mock", mock.getName());
        Assertions.assertEquals(0, mock.getCreateUser());
        Assertions.assertEquals(0, mock.getUpdateUser());
        Assertions.assertNotNull(mock.getCreateTime());
        Assertions.assertNotNull(mock.getUpdateTime());

        Mock result = mockMapper.selectById(mock.getId());
        Assertions.assertEquals(mock.getId(), result.getId());
        Assertions.assertEquals(mock.getName(), result.getName());
        Assertions.assertEquals(mock.getCreateUser(), result.getCreateUser());
        Assertions.assertEquals(mock.getUpdateUser(), result.getUpdateUser());
        Assertions.assertNotNull(result.getCreateTime());
        Assertions.assertNotNull(result.getUpdateTime());
    }

    @Test
    @Sql(scripts = {"/sql/test/ddl/mock.sql"})
    @WithMockUser(authorities = {MOCK_AUTHORITY_CREATE})
    void testCreateBatch() {
        List<Mock> mocks = List.of(new Mock("mock1"), new Mock("mock2"));
        mockService.createBatch(mocks);
        Assertions.assertEquals(2, mockMapper.selectList(null).size());

        for (int i = 0; i < mocks.size(); i++) {
            Mock mock = mocks.get(i);
            Assertions.assertEquals(i + 1, mock.getId());
            Assertions.assertEquals("mock" + (i + 1), mock.getName());
            Assertions.assertEquals(0, mock.getCreateUser());
            Assertions.assertEquals(0, mock.getUpdateUser());
            Assertions.assertNotNull(mock.getCreateTime());
            Assertions.assertNotNull(mock.getUpdateTime());

            Mock result = mockMapper.selectById(mock.getId());
            Assertions.assertEquals(mock.getId(), result.getId());
            Assertions.assertEquals(mock.getName(), result.getName());
            Assertions.assertEquals(mock.getCreateUser(), result.getCreateUser());
            Assertions.assertEquals(mock.getUpdateUser(), result.getUpdateUser());
            Assertions.assertNotNull(result.getCreateTime());
            Assertions.assertNotNull(result.getUpdateTime());
        }
    }

    @Test
    @Sql(scripts = {"/sql/test/ddl/mock.sql", "/sql/test/data/mock.sql"})
    @WithMockUser(authorities = {MOCK_AUTHORITY_UPDATE})
    void testUpdateById() {
        Long id = 1L;
        Mock mock = new Mock("mock");
        mockService.updateById(id, mock);

        Mock result = mockMapper.selectById(id);
        Assertions.assertEquals(mock.getName(), result.getName());
    }

    @Test
    @Sql(scripts = {"/sql/test/ddl/mock.sql", "/sql/test/data/mock.sql"})
    @WithMockUser(authorities = {MOCK_AUTHORITY_UPDATE})
    void testUpdateByIds() {
        List<Long> ids = List.of(1L, 2L);
        Mock mock = new Mock("mock");
        mockService.updateByIds(ids, mock);

        List<Mock> results = mockMapper.selectBatchIds(ids);
        for (Mock result : results) {
            Assertions.assertEquals(mock.getName(), result.getName());
        }
    }

    @Test
    @Sql(scripts = {"/sql/test/ddl/mock.sql", "/sql/test/data/mock.sql"})
    @WithMockUser(authorities = {MOCK_AUTHORITY_DELETE})
    void testDeleteById() {
        Long id = 1L;
        Assertions.assertNotNull(mockMapper.selectById(id));
        mockService.deleteById(id);
        Assertions.assertNull(mockMapper.selectById(id));
    }

    @Test
    @Sql(scripts = {"/sql/test/ddl/mock.sql", "/sql/test/data/mock.sql"})
    @WithMockUser(authorities = {MOCK_AUTHORITY_DELETE})
    void testDeleteByIds() {
        List<Long> ids = List.of(1L, 2L);
        Assertions.assertEquals(2, mockMapper.selectBatchIds(ids).size());
        mockService.deleteByIds(ids);
        Assertions.assertEquals(0, mockMapper.selectBatchIds(ids).size());
    }
}
