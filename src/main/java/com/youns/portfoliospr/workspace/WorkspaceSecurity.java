package com.youns.portfoliospr.workspace;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WorkspaceSecurity {
  @Bean
  PasswordEncoder workspacePasswordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  UserDetailsService workspaceUsers(
      PasswordEncoder encoder,
      @Value("${app.editor-password:Portfolio-2026!}") String editor,
      @Value("${app.viewer-password:View-2026!}") String viewer) {
    return new InMemoryUserDetailsManager(
        User.withUsername("editor").password(encoder.encode(editor)).roles("EDITOR").build(),
        User.withUsername("viewer").password(encoder.encode(viewer)).roles("VIEWER").build());
  }

  @Bean
  @Order(1)
  SecurityFilterChain workspaceChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/workspace/**")
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/workspace/login")
                    .permitAll()
                    .requestMatchers("/workspace/logout")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/workspace/**")
                    .hasRole("EDITOR")
                    .anyRequest()
                    .authenticated())
        .formLogin(
            f ->
                f.loginPage("/workspace/login")
                    .loginProcessingUrl("/workspace/login")
                    .defaultSuccessUrl("/workspace", true)
                    .failureUrl("/workspace/login?error"))
        .logout(l -> l.logoutUrl("/workspace/logout").logoutSuccessUrl("/workspace/login?logout"));
    return http.build();
  }
}
