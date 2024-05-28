package com.hzboiler.core.field.util;

import com.hzboiler.core.field.annotations.ReadOnly;
import com.hzboiler.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gongshuiwen
 */
public class ReadOnlyUtil {

    private static final Map<Class<?>, Field[]> readOnlyFieldsCache = new ConcurrentHashMap<>();

    /**
     * Get all declared fields (include inherited) with annotation {@link com.hzboiler.core.field.annotations.ReadOnly} for class ,
     * a ConcurrentHashMap cache is used.
     * @param clazz the class
     * @return fields array, ensure all fields are always accessible
     */
    public static Field[] getReadOnlyFields(Class<?> clazz) {
        return readOnlyFieldsCache.computeIfAbsent(clazz, ReadOnlyUtil::_getReadOnlyFields);
    }

    private static Field[] _getReadOnlyFields(Class<?> clazz) {
        Field[] fields = ReflectUtil.getAllDeclaredFieldsWithAnnotation(clazz, ReadOnly.class);
        for (Field field : fields) field.setAccessible(true);
        return fields;
    }
}