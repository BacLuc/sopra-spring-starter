package ch.uzh.ifi.hase.soprafs22.service;

import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs22.security.Authorities;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Autowired
  public UserDetailServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String id) {
    return userRepository
        .findById(Long.parseLong(id))
        .map(
            user ->
                new UserDetails() {
                  @Override
                  public Collection<? extends GrantedAuthority> getAuthorities() {
                    return List.of(new SimpleGrantedAuthority(Authorities.ROLE_USER.name()));
                  }

                  @Override
                  public String getPassword() {
                    return user.getPasswordHash();
                  }

                  @Override
                  public String getUsername() {
                    return user.getId().toString();
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
                })
        .orElseThrow(
            () -> new UsernameNotFoundException("User not found with username %s".formatted(id)));
  }
}
