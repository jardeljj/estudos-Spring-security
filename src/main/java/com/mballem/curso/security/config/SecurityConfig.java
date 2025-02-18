package com.mballem.curso.security.config;

import com.mballem.curso.security.domain.PerfilTipo;
import com.mballem.curso.security.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final String ADMIN = PerfilTipo.ADMIN.getDesc();
    private static final String MEDICO = PerfilTipo.MEDICO.getDesc();
    private static final String PACIENTE = PerfilTipo.PACIENTE.getDesc();

    @Autowired
    private UsuarioService service;

	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/webjars/**", "/css/**", "/image/**", "/js/**").permitAll() // Permite recursos estáticos
                        .requestMatchers("/", "/home", "/expired").permitAll()
                        .requestMatchers("/u/novo/cadastro","/u/cadastro/realizado","/u/cadastro/paciente/salvar").permitAll()
                        .requestMatchers("/u/confirmacao/cadastro").permitAll()
                        .requestMatchers("/u/p/**").permitAll()

                        // acessos privados adm
                        .requestMatchers("/u/editar/senha", "/u/confirmar/senha").hasAnyAuthority(PACIENTE,MEDICO)
                        .requestMatchers("/u/**").hasAuthority(ADMIN)


                        // acessos privados medicos
                        .requestMatchers("/medicos/especialidade/titulo/*").hasAnyAuthority(MEDICO,PACIENTE)
                        .requestMatchers("/medicos/dados", "/medicos/salvar", "/medicos/editar").hasAnyAuthority(MEDICO, ADMIN)
                        .requestMatchers("/medicos/**").hasAuthority(MEDICO)

                        // acessos privados pacientes
                        .requestMatchers("/pacientes/**").hasAuthority(PACIENTE)

                        // acessos privados especialidades
                        .requestMatchers("/especialidades/datatables/server/medico/*").hasAnyAuthority(MEDICO, ADMIN)
                        .requestMatchers("/especialidades/titulo").hasAnyAuthority(MEDICO, ADMIN, PACIENTE)
                        .requestMatchers("/especialidades/**").hasAuthority(ADMIN)


                        // Permite as páginas
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
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/acesso-negado")
                )
                .rememberMe(remember -> remember
                        .key("chave-secreta-remember-me") // Chave usada para assinar os tokens de remember me
                        .tokenValiditySeconds(604800) // Tempo de expiração do cookie (7 dias)
                        .userDetailsService(service) // Serviço de usuários para carregar as credenciais
                );
                http.sessionManagement(session -> session
                    .maximumSessions(1)
                    .expiredUrl("/expired")
                    .maxSessionsPreventsLogin(false)
                    .sessionRegistry(sessionRegistry())
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

    @Bean
    public SessionRegistry sessionRegistry(){
        return new SessionRegistryImpl();
    }

    @Bean
    public ServletListenerRegistrationBean<?> servletListenerRegistrationBean(){
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }
	
}


