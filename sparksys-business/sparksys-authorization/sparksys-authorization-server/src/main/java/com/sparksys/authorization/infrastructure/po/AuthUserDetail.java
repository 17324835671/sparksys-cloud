package com.sparksys.authorization.infrastructure.po;

import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * description: 授权认证用户信息
 *
 * @author zhouxinlei
 * @date 2020-05-24 12:24:25
 */
@Getter
@Setter
public class AuthUserDetail implements UserDetails {

    private final String username;
    private final String password;
    private final List<GrantedAuthority> authorities;

    public AuthUserDetail(String username, String password, List<GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
