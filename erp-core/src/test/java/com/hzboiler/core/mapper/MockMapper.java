package com.hzboiler.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hzboiler.core.model.Mock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MockMapper extends BaseMapper<Mock> {
}