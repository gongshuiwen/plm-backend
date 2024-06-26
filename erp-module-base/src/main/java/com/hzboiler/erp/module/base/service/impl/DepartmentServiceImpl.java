package com.hzboiler.erp.module.base.service.impl;

import com.hzboiler.erp.core.service.AbstractBaseService;
import com.hzboiler.erp.module.base.mapper.DepartmentMapper;
import com.hzboiler.erp.module.base.model.Department;
import com.hzboiler.erp.module.base.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DepartmentServiceImpl extends AbstractBaseService<DepartmentMapper, Department> implements DepartmentService {
}
