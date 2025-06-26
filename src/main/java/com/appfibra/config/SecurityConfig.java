package com.appfibra.config;

import com.appfibra.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // PasswordEncoder para encriptar contraseñas
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configurar AuthenticationManager para login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Configuración de seguridad para Spring Security
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // 🔹 Definir qué rutas son públicas y cuáles requieren autenticación
            	.authorizeHttpRequests(auth -> auth
                // 🔓 Permitir APIs públicas sin autenticación
                .requestMatchers("/api/clients").permitAll()
                .requestMatchers("/api/clients/***").permitAll()
                .requestMatchers("/api/civil-works/**").permitAll()
                .requestMatchers("/api/village/all-civil-works/**").permitAll()
                .requestMatchers("/api/projects").permitAll()
                .requestMatchers("/api/chat").permitAll()
                .requestMatchers("/api/qgis-projects").permitAll()
                .requestMatchers("/api/qgis-projects/**").permitAll()
                .requestMatchers("/api/qgis/**").permitAll()
                .requestMatchers("/api/qgis-projects/trench").permitAll()
                .requestMatchers("/api/qgis-projects/geojson").permitAll()
                .requestMatchers("/qgis-projects-map").permitAll()
                .requestMatchers("/qgis-projects").permitAll()
                .requestMatchers("/qgis-projects/**").permitAll()
                .requestMatchers("/village/**").permitAll()
                .requestMatchers("/api/village/**").permitAll()
                .requestMatchers("/api/qgis-projects/trench/**").permitAll()
                .requestMatchers("/api/cataster/layer").permitAll()
                .requestMatchers("/api/cataster/layer/**").permitAll()
                .requestMatchers("/api/cataster/**").permitAll()
                .requestMatchers("/api/reports/**").permitAll()
                .requestMatchers("/api/wochenmeldung/**").permitAll()
                // 🔓 Permitir acceso a archivos estáticos
                .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()

                // 🔓 Permitir acceso sin login a la página de login
                .requestMatchers("/login").permitAll()

                // 🔒 El resto de rutas requieren autenticación
                .anyRequest().authenticated()
            )

            // 🔹 Configurar el formulario de login
            .formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )

            // 🔹 Configurar logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )

            // 🔹 Deshabilitar CSRF para APIs (para evitar problemas con POST y PUT)
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

            // 🔹 Habilitar CORS para evitar problemas con QGIS
            .cors();

        return http.build();
    }
}
