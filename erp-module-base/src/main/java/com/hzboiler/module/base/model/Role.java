package com.hzboiler.module.base.model;

import com.hzboiler.core.model.BaseModel;
import com.hzboiler.core.validation.CreateValidationGroup;
import com.hzboiler.core.validation.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Size;

import java.io.Serial;

@Getter
@Setter
@Schema(description = "角色信息")
public class Role extends BaseModel {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色标识")
    @NotNull(groups = {CreateValidationGroup.class})
    @NullOrNotBlank
    @Size(min = 2, max = 18)
    private String code;

    @Schema(description = "角色名称")
    @NotNull(groups = {CreateValidationGroup.class})
    @NullOrNotBlank
    @Size(min = 2, max = 18)
    private String name;

    @Schema(description = "显示顺序")
    private Integer orderNum;

    @Schema(description = "状态 1=正常,0=停用")
    private Integer status;
}