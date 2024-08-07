package com.hzboiler.erp.core.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.hzboiler.erp.core.field.*;
import com.hzboiler.erp.core.field.annotations.OnDelete;
import com.hzboiler.erp.core.field.util.RelationFieldUtil;
import com.hzboiler.erp.core.mapper.BaseMapper;
import com.hzboiler.erp.core.mapper.BaseMapperRegistry;
import com.hzboiler.erp.core.mapper.RelationMapper;
import com.hzboiler.erp.core.model.BaseModel;
import com.hzboiler.erp.core.protocal.query.Condition;
import com.hzboiler.erp.core.protocal.query.OrderBys;
import com.hzboiler.erp.core.security.model.ModelAccessCheckUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author gongshuiwen
 */
@Slf4j
public abstract class AbstractBaseService<T extends BaseModel>
        implements BaseService<T>, InitializingBean {

    private static final int DEFAULT_BATCH_SIZE = 1000;

    protected final Log mybatisLog = LogFactory.getLog(getClass());

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    @Qualifier("sqlSessionTemplate")
    private SqlSession sqlSession;

    // cache for modelClass
    private volatile Class<T> modelClass;
    private final Object modelClassLock = new Object();

    // cache for baseMapperClass
    private volatile Class<BaseMapper<T>> baseMapperClass;
    private final Object baseMapperClassLock = new Object();

    // cache for BaseMapper<T>
    private volatile BaseMapper<T> baseMapper;
    private final Object baseMapperLock = new Object();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public T selectById(Long id) {
        if (id == null) return null;

        List<T> records = getBaseMapper().selectBatchIds(List.of(id));
        return records.isEmpty() ? null : records.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<T> selectByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        ModelAccessCheckUtil.checkSelect(getModelClass());
        return getBaseMapper().selectBatchIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IPage<T> page(Long pageNum, Long pageSize, Condition condition, OrderBys orderBys) {
        ModelAccessCheckUtil.checkSelect(getModelClass());
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        if (condition != null)
            condition.applyToQueryWrapper(queryWrapper);
        if (orderBys != null)
            orderBys.applyToQueryWrapper(queryWrapper);
        return getBaseMapper().selectPage(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long count(QueryWrapper<T> queryWrapper) {
        ModelAccessCheckUtil.checkSelect(getModelClass());
        return SqlHelper.retCount(getBaseMapper().selectCount(queryWrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<T> selectList(QueryWrapper<T> queryWrapper) {
        ModelAccessCheckUtil.checkSelect(getModelClass());
        return getBaseMapper().selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<T> nameSearch(String name) {
        ModelAccessCheckUtil.checkSelect(getModelClass());
        Page<T> page = new Page<>(1, 7);

        if (name == null || name.isEmpty() || name.isBlank()) return getBaseMapper().selectList(page, Wrappers.emptyWrapper());

        // TODO: optimize performance by cache
        try {
            getModelClass().getDeclaredField("name");
        } catch (NoSuchFieldException e) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<T> queryWrapper = new LambdaQueryWrapper<>(getModelClass());
        queryWrapper.like(T::getName, name.trim());
        return getBaseMapper().selectList(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOne(T record) {
        if (record == null) return false;
        return createBatch(List.of(record));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createBatch(List<T> records) {
        if (records == null || records.isEmpty()) return false;

        // check authority for create
        ModelAccessCheckUtil.checkCreate(getModelClass());

        // do create
        boolean res = saveBatch(records);

        // process one2many fields
        processOne2ManyForCreate(records);

        // process many2many fields
        processMany2ManyForCreate(records);
        return res;
    }

    @Transactional(rollbackFor = Exception.class)
    protected boolean saveBatch(Collection<T> entityList) {
        String sqlStatement = SqlHelper.getSqlStatement(getBaseMapperClass(), SqlMethod.INSERT_ONE);
        return SqlHelper.executeBatch(sqlSessionFactory, mybatisLog, entityList, DEFAULT_BATCH_SIZE,
                (sqlSession, entity) -> sqlSession.insert(sqlStatement, entity));
    }

    @SneakyThrows(IllegalAccessException.class)
    @SuppressWarnings("unchecked")
    private void processOne2ManyForCreate(List<T> records) {
        for (Field field : RelationFieldUtil.getOne2ManyFields(getModelClass())) {
            Class<BaseModel> targetClass = RelationFieldUtil.getTargetModelClass(getModelClass(), field);
            Field inverseField = RelationFieldUtil.getInverseField(getModelClass(), field);
            BaseService<BaseModel> targetService = getService(targetClass);
            for (T record : records) {
                One2Many<BaseModel> filedValue;
                try {
                    filedValue = (One2Many<BaseModel>) field.get(record);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (filedValue == null) break;

                List<Command<BaseModel>> commands = filedValue.getCommands();
                if (commands == null) break;
                for (Command<BaseModel> command : commands) {
                    if (command == null) break;
                    if (Objects.requireNonNull(command.getCommandType()) == CommandType.CREATE) {
                        if (command.getRecords() == null || command.getRecords().isEmpty()) {
                            throw new IllegalArgumentException("The records of Command CREATE cannot be null or empty");
                        }
                        for (BaseModel targetRecord : command.getRecords()) {
                            inverseField.set(targetRecord, Many2One.ofId(record.getId()));
                        }
                        targetService.createBatch(command.getRecords());
                    } else {
                        throw new IllegalArgumentException("The Command " + command.getCommandType()
                                + " is not supported for one2many field in create method");
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processMany2ManyForCreate(List<T> records) {
        for (Field field : RelationFieldUtil.getMany2ManyFields(getModelClass())) {
            Class<? extends BaseModel> targetClass = RelationFieldUtil.getTargetModelClass(getModelClass(), field);
            for (T record : records) {
                Many2Many<BaseModel> filedValue;
                try {
                    filedValue = (Many2Many<BaseModel>) field.get(record);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (filedValue == null) break;

                List<Command<BaseModel>> commands = filedValue.getCommands();
                if (commands == null) break;

                for (Command<BaseModel> command : commands) {
                    if (command == null) break;
                    if (Objects.requireNonNull(command.getCommandType()) == CommandType.ADD) {
                        if (command.getIds() == null || command.getIds().isEmpty()) {
                            throw new IllegalArgumentException("The ids of Command ADD cannot be null or empty");
                        }
                        RelationMapper mapper = getRelationMapper(targetClass);
                        mapper.add(getModelClass(), record.getId(), command.getIds());
                    } else {
                        throw new IllegalArgumentException("The Command " + command.getCommandType()
                                + " is not supported for many2many field in create method");
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(Long id, T updateValues) {
        if (id == null || updateValues == null) return false;
        return updateByIds(List.of(id), updateValues);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByIds(List<Long> ids, T updateValues) {
        if (ids == null || ids.isEmpty() || updateValues == null) return false;
        if (updateValues.getId() != null)
            throw new IllegalArgumentException("record's id must be null");

        // check authority for update
        ModelAccessCheckUtil.checkUpdate(getModelClass());

        // construct LambdaUpdateWrapper
        LambdaUpdateWrapper<T> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setEntityClass(getModelClass());
        updateWrapper.in(T::getId, ids);

        // do update
        boolean res = SqlHelper.retBool(getBaseMapper().update(updateValues, updateWrapper));

        // process one2many fields
        processOne2ManyForUpdate(ids, updateValues);

        // process many2many fields
        processMany2ManyForUpdate(ids, updateValues);

        return res;
    }

    @SneakyThrows(IllegalAccessException.class)
    @SuppressWarnings("unchecked")
    private void processOne2ManyForUpdate(List<Long> ids, T record) {
        for (Field field : RelationFieldUtil.getOne2ManyFields(getModelClass())) {
            Class<BaseModel> targetClass = RelationFieldUtil.getTargetModelClass(getModelClass(), field);
            Field inverseField = RelationFieldUtil.getInverseField(getModelClass(), field);
            BaseService<BaseModel> targetService = getService(targetClass);
            One2Many<BaseModel> filedValue;
            try {
                filedValue = (One2Many<BaseModel>) field.get(record);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (filedValue == null) break;

            for (Long sourceId : ids) {
                List<Command<BaseModel>> commands = filedValue.getCommands();
                if (commands == null) break;

                for (Command<BaseModel> command : commands) {
                    if (command == null) break;
                    switch (command.getCommandType()) {
                        case CREATE: {
                            for (BaseModel targetRecord : command.getRecords()) {
                                inverseField.set(targetRecord, Many2One.ofId(sourceId));
                            }
                            targetService.createBatch(command.getRecords());
                        }
                        case DELETE: {
                            targetService.deleteByIds(command.getIds());
                            break;
                        }
                        case UPDATE: {
                            targetService.updateByIds(command.getIds(), command.getRecords().get(0));
                            break;
                        }
                        default: {
                            throw new RuntimeException();
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processMany2ManyForUpdate(List<Long> ids, T record) {
        for (Field field : RelationFieldUtil.getMany2ManyFields(getModelClass())) {
            Class<? extends BaseModel> targetClass = RelationFieldUtil.getTargetModelClass(getModelClass(), field);
            Many2Many<BaseModel> filedValue;
            try {
                filedValue = (Many2Many<BaseModel>) field.get(record);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (filedValue == null) break;

            for (Long sourceId : ids) {
                List<Command<BaseModel>> commands = filedValue.getCommands();
                if (commands == null) break;

                for (Command<BaseModel> command : commands) {
                    if (command == null) break;
                    switch (command.getCommandType()) {
                        case ADD: {
                            RelationMapper mapper = getRelationMapper(targetClass);
                            mapper.add(getModelClass(), sourceId, command.getIds());
                            break;
                        }
                        case REMOVE: {
                            RelationMapper mapper = getRelationMapper(targetClass);
                            mapper.remove(getModelClass(), sourceId, command.getIds());
                            break;
                        }
                        case REPLACE: {
                            RelationMapper mapper = getRelationMapper(targetClass);
                            mapper.replace(getModelClass(), sourceId, command.getIds());
                            break;
                        }
                        default: {
                            throw new RuntimeException();
                        }
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        if (id == null) return false;
        return deleteByIds(List.of(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return false;

        // check authority for delete
        ModelAccessCheckUtil.checkDelete(getModelClass());

        // do delete
        boolean res = SqlHelper.retBool(getBaseMapper().deleteBatchIds(ids));

        // process one2many fields
        processOne2manyForDelete(ids);

        // process many2many fields
        processMany2manyForDelete(ids);
        return res;
    }

    private void processOne2manyForDelete(List<Long> ids) {
        for (Field field : RelationFieldUtil.getOne2ManyFields(getModelClass())) {
            Class<BaseModel> targetClass = RelationFieldUtil.getTargetModelClass(getModelClass(), field);
            Field inverseField = RelationFieldUtil.getInverseField(getModelClass(), field);
            AbstractBaseService<BaseModel> targetService = getService(targetClass);
            OnDelete.Type onDeleteType = OnDelete.Type.RESTRICT;
            OnDelete onDelete = inverseField.getDeclaredAnnotation(OnDelete.class);
            if (onDelete != null) onDeleteType = onDelete.value();
            switch (onDeleteType) {
                case RESTRICT: {
                    if (targetService.countByMany2OneIds(inverseField, ids) > 0) {
                        // TODO: optimize exception type
                        throw new RuntimeException("Records can't be deleted, because the " + field.getName() + " field is not empty.");
                    }
                }
                case CASCADE: {
                    List<Long> targetIds = targetService.selectIdsByMany2OneIds(inverseField, ids);
                    if (!targetIds.isEmpty()) {
                        targetService.deleteByIds(targetIds);
                    }
                    break;
                }
                case SET_NULL: {
                    List<Long> targetIds = targetService.selectIdsByMany2OneIds(inverseField, ids);
                    if (!targetIds.isEmpty()) {
                        targetService.updateMany2OneToNullByIds(inverseField, targetIds);
                    }
                    break;
                }
                default: {
                    throw new RuntimeException();
                }
            }
        }
    }

    protected long countByMany2OneIds(Field field, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(toColumnName(field), ids);
        return count(queryWrapper);
    }

    protected List<Long> selectIdsByMany2OneIds(Field field, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(toColumnName(field), ids);
        return getBaseMapper().selectList(queryWrapper).stream().map(BaseModel::getId).toList();
    }

    protected void updateMany2OneToNullByIds(Field field, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", ids);
        updateWrapper.set(toColumnName(field), null);
        getBaseMapper().update(null, updateWrapper);
    }

    private String toColumnName(Field field) {
        return field.getName().replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    private void processMany2manyForDelete(List<Long> ids) {
        for (Field field : RelationFieldUtil.getMany2ManyFields(getModelClass())) {
            Class<? extends BaseModel> targetClass = RelationFieldUtil.getTargetModelClass(getModelClass(), field);
            for (Long sourceId : ids) {
                RelationMapper mapper = getRelationMapper(targetClass);
                mapper.removeAll(getModelClass(), sourceId);
            }
        }
    }

    @Override
    public SqlSession getSqlSession() {
        return sqlSession;
    }

    @Override
    public Class<T> getModelClass() {
        // Double-checked locking for thread-safely lazy loading
        if (modelClass == null) {
            synchronized (modelClassLock) {
                if (modelClass == null) {
                    @SuppressWarnings("unchecked")
                    Class<T> clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                    modelClass = clazz;
                }
            }
        }
        return modelClass;
    }

    @Override
    public Class<BaseMapper<T>> getBaseMapperClass() {
        // Double-checked locking for thread-safely lazy loading
        if (baseMapperClass == null) {
            synchronized (baseMapperClassLock) {
                if (baseMapperClass == null) {
                    @SuppressWarnings("unchecked")
                    Class<BaseMapper<T>> baseMapperClass1 = (Class<BaseMapper<T>>) getBaseMapper().getClass().getInterfaces()[0];
                    baseMapperClass = baseMapperClass1;
                }
            }
        }

        return baseMapperClass;
    }

    @Override
    public BaseMapper<T> getBaseMapper() {
        // Double-checked locking for thread-safely lazy loading
        if (baseMapper == null) {
            synchronized (baseMapperLock) {
                if (baseMapper == null) {
                    baseMapper = BaseMapperRegistry.getBaseMapper(sqlSession, getModelClass());
                }
            }
        }
        return baseMapper;
    }

    @Override
    public <AT extends BaseModel> BaseMapper<AT> getBaseMapper(Class<AT> modelClass) {
        return BaseMapperRegistry.getBaseMapper(sqlSession, modelClass);
    }

    @Override
    public <M extends BaseMapper<T>> M getMapper(Class<M> mapperInterface) {
        return getSqlSession().getMapper(mapperInterface);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends BaseService<AT>, AT extends BaseModel> S getService(Class<AT> modelClass) {
        return (S) BaseServiceRegistry.getByModelClass(modelClass);
    }

    @Override
    public LambdaQueryChainWrapper<T> lambdaQuery() {
        ModelAccessCheckUtil.checkSelect(getModelClass());
        return ChainWrappers.lambdaQueryChain(getBaseMapper(), getModelClass());
    }

    @Override
    public void afterPropertiesSet() {
        BaseServiceRegistry.register(this);
    }
}
