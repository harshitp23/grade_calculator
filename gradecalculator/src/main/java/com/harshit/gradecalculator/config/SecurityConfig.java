package com.harshit.gradecalculator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 1. Allow POST requests
            .authorizeHttpRequests(auth -> auth
                // 👇 THIS IS THE CRITICAL LINE TO STOP THE LOOP 👇
                .requestMatchers("/", "/index.html", "/login.html", "/register.html", "/css/**", "/js/**", "/error").permitAll()
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html") // Tells Spring where the custom page is
                .loginProcessingUrl("/login") // Tells Spring where to submit the form
                .defaultSuccessUrl("/index.html", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login.html?logout") // Go back to login after logout
                .permitAll()
            );

        return http.build();
    }
}
