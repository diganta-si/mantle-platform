package com.acuityspace.mantle.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test-only auto-configuration that provides a fully permissive SecurityFilterChain
 * when no real SecurityFilterChain is present in the context (i.e. in @WebMvcTest slices
 * where SecurityConfig is not included). In full @SpringBootTest contexts, SecurityConfig
 * already provides a SecurityFilterChain, so this bean is never created.
 *
 * SpringBootWebSecurityConfiguration (which creates the default HTTP-basic chain) is
 * annotated @AutoConfiguration(after = SecurityAutoConfiguration.class), so running
 * before SecurityAutoConfiguration ensures we run before both.
 */
@AutoConfiguration(before = SecurityAutoConfiguration.class)
class WebMvcTestSecurityConfig {

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain webMvcTestPermitAllChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
