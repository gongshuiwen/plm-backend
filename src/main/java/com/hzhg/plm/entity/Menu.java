package com.hzhg.plm.entity;

import com.hzhg.plm.core.entity.TreeBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@Schema(description = "菜单信息")
public class Menu extends TreeBaseEntity<Menu> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "显示顺序")
    private Integer orderNum;

    @Schema(description = "状态 1=正常,0=停用")
    private Integer status;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "菜单标题")
    private String title;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "路由参数")
    private String query;

    @Schema(description = "外链地址")
    private String url;

    @Schema(description = "菜单图标")
    private String icon;

    @Override
    public String getDisplayName() {
        return getName();
    }

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    public String getName() {
        return name;
    }

    @NotNull(message = "显示顺序不能为空")
    public Integer getOrderNum() {
        return orderNum;
    }

    @Size(max = 200, message = "路由地址不能超过200个字符")
    public String getPath() {
        return path;
    }

    @Size(max = 200, message = "组件路径不能超过255个字符")
    public String getComponent() {
        return component;
    }
}
