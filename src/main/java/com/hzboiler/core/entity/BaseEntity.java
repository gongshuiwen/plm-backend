package com.hzboiler.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hzboiler.core.annotations.FetchName;
import com.hzboiler.core.jackson2.AllowedForAdmin;
import com.hzboiler.core.jackson2.SecurityBeanPropertyFilter;
import com.hzboiler.core.validation.CreateValidationGroup;
import com.hzboiler.core.validation.UpdateValidationGroup;
import com.hzboiler.core.utils.SpringContextUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Getter
@Setter
@JsonFilter(SecurityBeanPropertyFilter.FILTER_ID)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseEntity implements Serializable {

    @Schema(description = "ID")
    @TableId(type = IdType.AUTO)
    @Null(groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    private Long id;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @AllowedForAdmin
    @Null(groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @AllowedForAdmin
    @Null(groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    private LocalDateTime updateTime;

    @Schema(description = "创建用户")
    @TableField(fill = FieldFill.INSERT)
    @AllowedForAdmin
    @Null(groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    private Long createUser;

    @Schema(description = "更新用户")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @AllowedForAdmin
    @Null(groups = {CreateValidationGroup.class, UpdateValidationGroup.class})
    private Long updateUser;

    public String getName() {
        return "";
    }

    public String getDisplayName() {
        return getName();
    }

    public static <T extends BaseEntity> void fetchNames(List<T> entities)
            throws NoSuchFieldException, IllegalAccessException {
        // Do noting if empty
        if ( entities == null  || entities.isEmpty() ) return;

        // Get entity class
        Class<?> clazz = entities.get(0).getClass();
        List<Field> fetchNameFields = getFetchNameFields(clazz);
        for (Field fetchNameField : fetchNameFields) {
            // Get annotation
            FetchName annotation = fetchNameField.getAnnotation(FetchName.class);

            // Get id field
            Field idField = clazz.getDeclaredField(annotation.idField());
            idField.setAccessible(true);

            // Get target mapper from application context
            Class<? extends BaseMapper<? extends BaseEntity>> mapperClazz = annotation.mapper();
            BaseMapper<? extends BaseEntity> mapper = SpringContextUtils.getApplicationContext().getBean(mapperClazz);
            for (T entity : entities) {
                // Use selectById to fetch target entity and get this display name
                // TODO: Optimize performance by cache
                BaseEntity t = mapper.selectById((Serializable) idField.get(entity));
                if (t != null) {
                    fetchNameField.set(entity, t.getDisplayName());
                }
            }
        }
    }

    private static List<Field> getFetchNameFields(Class<?> clazz) {
        // TODO: Optimize performance by cache
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getAnnotation(FetchName.class) != null)
                .peek(field -> field.setAccessible(true))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getClass().getName() + "@" + getId() + "[" + getDisplayName() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BaseEntity)) return false;
        return Objects.equals(this.id, ((BaseEntity) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }
}