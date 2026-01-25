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
        // 1. Disable CSRF (Common cause of "403" on POST requests)
        .csrf(csrf -> csrf.disable()) 
        
        // 2. Allow access to static files and the Registration API
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/index.html", "/login.html", "/register.html", "/css/**", "/js/**").permitAll()
            .requestMatchers("/api/auth/**").permitAll()  // <--- CRITICAL LINE: Allows Registering!
            .anyRequest().authenticated()
        )
        
        // 3. Configure Login
        .formLogin(form -> form
            .loginPage("/login.html")
            .defaultSuccessUrl("/index.html", true)
            .permitAll()
        )
        .logout(logout -> logout.permitAll());

    return http.build();
}

}
