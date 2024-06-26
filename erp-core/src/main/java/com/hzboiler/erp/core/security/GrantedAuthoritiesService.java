package com.hzboiler.erp.core.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

/**
 * @author gongshuiwen
 */
public interface GrantedAuthoritiesService {

    /**
     * Get the set of {@link GrantedAuthority} by user id.
     * @param userId id of user
     * @return set of {@link GrantedAuthority}
     */
    Set<? extends GrantedAuthority> getAuthoritiesByUserId(Long userId);
}
