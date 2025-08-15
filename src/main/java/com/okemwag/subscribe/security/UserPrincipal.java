package com.okemwag.subscribe.security;

import com.okemwag.subscribe.entity.Business;
import java.util.Collection;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
  private Long id;
  private String email;
  private String password;
  private String businessName;
  private Boolean active;
  private Collection<? extends GrantedAuthority> authorities;

  public static UserPrincipal create(Business business) {
    Collection<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_BUSINESS_OWNER"));

    return new UserPrincipal(
        business.getId(),
        business.getEmail(),
        business.getPassword(),
        business.getName(),
        business.getActive(),
        authorities);
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public String getPassword() {
    return password;
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
    return active;
  }
}
