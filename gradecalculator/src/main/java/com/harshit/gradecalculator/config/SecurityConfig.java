package com.harshit.gradecalculator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // This handles standard hashing
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/index.html", "/login.html", "/register.html", "/css/**", "/js/**").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login.html")
            .defaultSuccessUrl("/index.html", true)
            .permitAll()
        )
        // 👇 ADD THIS BLOCK 👇
        .rememberMe(remember -> remember
            .key("superSecretKey")
            .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
        )
        .logout(logout -> logout.permitAll());

        return http.build();
    }

}

