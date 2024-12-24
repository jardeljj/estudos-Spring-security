package com.mballem.curso.security.config;

import com.mballem.curso.security.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private UsuarioService service;

	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/webjars/**", "/css/**", "/image/**", "/js/**").permitAll() // Permite recursos estáticos
                        .requestMatchers("/", "/home").permitAll()

                        // acessos privados adm
                        .requestMatchers("/u/**").hasAuthority("ADMIN")

                        // acessos privados medicos
                        .requestMatchers("/medicos/**").hasAuthority("MEDICO")

                        // Permite as páginas públicas
                        .anyRequest().authenticated() // Exige autenticação para o restante
                )
                .formLogin(login -> login
                        .loginPage("/login") // Página personalizada de login
                        .defaultSuccessUrl("/", true) // Redireciona após login bem-sucedido
                        .failureUrl("/login-error") // Redireciona após falha no login
                        .permitAll() // Permite acesso à página de login
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/") // Redireciona após logout
                        .permitAll() // Permite acesso ao logout sem autenticação
                );

        return http.build();
    }

    // Define o PasswordEncoder para criptografia de senhas (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configura o AuthenticationManager com o serviço de usuários e codificador de senhas
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
	
}


