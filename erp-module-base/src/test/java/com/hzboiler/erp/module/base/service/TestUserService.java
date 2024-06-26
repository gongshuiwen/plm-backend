package com.hzboiler.erp.module.base.service;

import com.hzboiler.erp.core.context.BaseContextHolder;
import com.hzboiler.erp.core.exception.BusinessException;
import com.hzboiler.erp.module.base.model.Role;
import com.hzboiler.erp.module.base.model.User;
import com.hzboiler.erp.test.annotation.WithMockAdmin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hzboiler.erp.core.security.Constants.CODE_BASE_USER;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author gongshuiwen
 */
@SpringBootTest
@Sql(scripts = {
        "/sql/ddl/user.sql",
        "/sql/ddl/role.sql",
        "/sql/ddl/user_role.sql",
        "/sql/test/data/user.sql",
        "/sql/test/data/role.sql",
        "/sql/test/data/user_role.sql",
})
class TestUserService {

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @AfterEach
    void afterEach() {
        BaseContextHolder.clearContext();
    }

    @Test
    @WithMockAdmin
    void testLoadUserByUsername() {
        User userAdmin = userService.loadUserByUsername("admin");
        assertEquals("admin", userAdmin.getUsername());

        User userDemo = userService.loadUserByUsername("demo");
        assertEquals("demo", userDemo.getUsername());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("xxx"));
    }

    @Test
    @WithMockAdmin
    void testCreateOne() {
        // Create user
        User userCreate = new User();
        userCreate.setUsername("test");
        userCreate.setNickname("test");
        userCreate.setPassword("123456");
        userService.createOne(userCreate);

        // Test user
        User user = userService.loadUserByUsername("test");
        assertNotNull(user.getId());

        // Test password
        assertTrue(passwordEncoder.matches("123456", user.getPassword()));

        // Test roles
        assertRoles(user, "BASE_USER");
    }

    private void assertRoles(User user, String... roleCodes) {
        Set<String> roles = new HashSet<>(List.of(roleCodes));
        assertEquals(roles, roleService.getRolesByUserId(user.getId()).stream().map(Role::getCode).collect(Collectors.toSet()));
    }

    @Test
    @WithMockAdmin
    void testUpdatePasswordAdmin() {
        // Get user
        User userDemo = userService.loadUserByUsername("demo");

        // Change password
        String newPassword = "12345678";
        User userUpdate = new User();
        userUpdate.setPassword(newPassword);
        userService.updateById(userDemo.getId(), userUpdate);

        // Test password
        assertTrue(passwordEncoder.matches(newPassword, userService.selectById(userDemo.getId()).getPassword()));
    }

    @Test
    @WithMockUser(username = "demo", authorities = {CODE_BASE_USER, "S:User", "U:User"})
    void testUpdatePasswordDemo() {
        String newPassword = "12345678";

        // Get user
        User userAdmin = userService.loadUserByUsername("admin");
        User userDemo = userService.loadUserByUsername("demo");

        // Change password of demo
        User userUpdate1 = new User();
        userUpdate1.setPassword(newPassword);
        assertDoesNotThrow(() -> userService.updateById(userDemo.getId(), userUpdate1));
        assertTrue(passwordEncoder.matches(newPassword, userService.selectById(userDemo.getId()).getPassword()));

        // Change password of admin
        assertThrows(BusinessException.class, () -> userService.updateById(userAdmin.getId(), userUpdate1));

        // Change nickname of demo
        User userUpdate2 = new User();
        userUpdate2.setNickname("xxx");
        assertDoesNotThrow(() -> userService.updateById(userDemo.getId(), userUpdate2));
        assertEquals("xxx", userService.selectById(userDemo.getId()).getNickname());

        // Change nickname of admin
        assertThrows(BusinessException.class, () -> userService.updateById(userAdmin.getId(), userUpdate2));
    }
}
