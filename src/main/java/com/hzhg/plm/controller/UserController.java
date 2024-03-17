package com.hzhg.plm.controller;


import com.hzhg.plm.core.controller.BaseController;
import com.hzhg.plm.core.protocal.R;
import com.hzhg.plm.entity.User;
import com.hzhg.plm.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController extends BaseController<UserService, User> {

    @Autowired
    PasswordEncoder passwordEncoder;

    public static final String EXPRESSION_AUTHORITY_USER =
            "hasRole('" + ROLE_ADMIN + "') or #id == authentication.principal.id";

    @Override
    @GetMapping("/{id}")
    @PreAuthorize(EXPRESSION_AUTHORITY_USER)
    public R<User> get(@PathVariable Long id) throws NoSuchFieldException, IllegalAccessException {
        return super.get(id);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize(EXPRESSION_AUTHORITY_USER)
    public R<Boolean> update(@PathVariable Long id, @RequestBody User roleDto) {
        return super.update(id, roleDto);
    }

    @Override
    @PostMapping
    @PreAuthorize(value = EXPRESSION_AUTHORITY_CREATE)
    public R<User> create(@RequestBody User roleDto) {
        if (roleDto.getId() != null) {
            roleDto.setId(null);
        }
        roleDto.setPassword(passwordEncoder.encode(roleDto.getPassword()));
        return super.create(roleDto);
    }
}
