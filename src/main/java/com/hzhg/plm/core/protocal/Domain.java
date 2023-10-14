package com.hzhg.plm.core.protocal;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.enums.SqlLike;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Setter
@Getter
public class Domain<T> {

    private static final Method generalMethod;
    private static final Method likeMethod;
    static {
        try {
            generalMethod = AbstractWrapper.class.getDeclaredMethod("addCondition",
                    boolean.class, Object.class, SqlKeyword.class, Object.class);
            likeMethod = AbstractWrapper.class.getDeclaredMethod("likeValue",
                    boolean.class, SqlKeyword.class, Object.class, Object.class, SqlLike.class);
            generalMethod.setAccessible(true);
            likeMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Set<String> generalSet = new HashSet<>();
    static {
        generalSet.add("=");
        generalSet.add("!=");
        generalSet.add(">");
        generalSet.add(">=");
        generalSet.add("<");
        generalSet.add("<=");
    }

    private static final Map<String, SqlKeyword> sqlKeywordMap = new HashMap<>();
    static {
        sqlKeywordMap.put("=", SqlKeyword.EQ);
        sqlKeywordMap.put("!=", SqlKeyword.NE);
        sqlKeywordMap.put(">", SqlKeyword.GT);
        sqlKeywordMap.put(">=", SqlKeyword.GE);
        sqlKeywordMap.put("<", SqlKeyword.LT);
        sqlKeywordMap.put("<=", SqlKeyword.LE);
        sqlKeywordMap.put("like", SqlKeyword.LIKE);
        sqlKeywordMap.put("like left", SqlKeyword.LIKE);
        sqlKeywordMap.put("like right", SqlKeyword.LIKE);
        sqlKeywordMap.put("not like", SqlKeyword.NOT_LIKE);
        sqlKeywordMap.put("not like left", SqlKeyword.NOT_LIKE);
        sqlKeywordMap.put("not like right", SqlKeyword.NOT_LIKE);
    }

    private static final Map<String, SqlLike> sqlLikeMap = new HashMap<>();
    static {
        sqlLikeMap.put("like", SqlLike.DEFAULT);
        sqlLikeMap.put("like left", SqlLike.LEFT);
        sqlLikeMap.put("like right", SqlLike.RIGHT);
        sqlLikeMap.put("not like", SqlLike.DEFAULT);
        sqlLikeMap.put("not like left", SqlLike.LEFT);
        sqlLikeMap.put("not like right", SqlLike.RIGHT);
    }

    String column;
    String operator;
    Object value;

    public void applyToQueryWrapper(QueryWrapper<T> queryWrapper) {
        if (isGeneral()) {
            applyGeneral(queryWrapper);
        } else if (isLike()) {
            applyLike(queryWrapper);
        } else {
            throw new IllegalArgumentException();
        }
    };

    private boolean isGeneral() {
        return generalSet.contains(operator);
    }

    private boolean isLike() {
        return sqlLikeMap.containsKey(operator);
    }

    private void applyGeneral(QueryWrapper<T> queryWrapper) {
        try {
            generalMethod.invoke(queryWrapper, true, column, getSqlKeyword(), getValue());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void applyLike(QueryWrapper<T> queryWrapper) {
        try {
            likeMethod.invoke(queryWrapper, true, getSqlKeyword(), column, getValue(), getSqlLike());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private SqlKeyword getSqlKeyword() {
        if (operator == null) {
            throw new IllegalArgumentException();
        }
        return sqlKeywordMap.get(operator);
    }

    private SqlLike getSqlLike() {
        if (operator == null) {
            throw new IllegalArgumentException();
        }
        return sqlLikeMap.get(operator);
    }
}