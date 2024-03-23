package com.hzhg.plm.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hzhg.plm.core.annotations.FetchName;
import com.hzhg.plm.core.entity.BaseEntity;
import com.hzhg.plm.core.annotations.AllowedForRoles;
import com.hzhg.plm.mapper.DepartmentMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Schema(description = "用户信息")
public class User extends BaseEntity implements Serializable, UserDetails {

    private static final long serialVersionUID = 1L;

    private static final String ROLE_PREFIX = "ROLE_";

    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Size(max = 30, message = "用户名长度不能超过30个字符")
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = "用户密码不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Schema(description = "昵称")
    @Size(max = 30, message = "昵称长度不能超过30个字符")
    private String nickname;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    @Schema(description = "手机号码")
    @Size(max = 11, message = "手机号码长度不能超过11个字符")
    private String phone;

    @Schema(description = "用户性别 0=未知,1=女,2=男")
    private Integer sex;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "状态 1=正常,0=停用")
    private Integer status;

    @AllowedForRoles(value = {"SYS_ADMIN"})
    @Schema(description = "最后登录IP")
    private String loginIp;

    @AllowedForRoles(value = {"SYS_ADMIN"})
    @Schema(description = "最后登录时间")
    private LocalDateTime loginTime;

    @Schema(description = "部门ID")
    private Long departmentId;

    @TableField(exist = false)
    @Schema(description = "部门名称")
    @FetchName(idField = "departmentId", mapper = DepartmentMapper.class)
    private String departmentName;

    @TableField(exist = false)
    private Set<Role> roles;

    @TableField(exist = false)
    private Set<GrantedAuthority> authorities;

    public void addAuthoritiesWithRoles(Set<Role> roles) {
        if (authorities == null) {
            authorities = new HashSet<>();
        }
        roles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.getCode()))
                .forEach(authorities::add);
    }

    public void addAuthoritiesWithPermissions(Set<Permission> permissions) {
        if (authorities == null) {
            authorities = new HashSet<>();
        }
        permissions.stream()
                .map(perm -> new SimpleGrantedAuthority(perm.getCode()))
                .forEach(authorities::add);
    }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            return new HashSet<>();
        }
        return authorities;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return status == 1;
    }

    @Override
    public String getDisplayName() {
        return getNickname();
    }
}
