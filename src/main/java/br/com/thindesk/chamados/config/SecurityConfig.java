package br.com.thindesk.chamados.config;

import br.com.thindesk.chamados.dto.ErroResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Segurança do microsserviço.
 *
 * <h2>Reutilização da autenticação existente</h2>
 * <p>Este serviço NÃO tem login próprio. O usuário se autentica no serviço de login
 * (porta 8080), que grava a sessão na coleção {@code sessions} do MongoDB. Este
 * microsserviço lê essa mesma sessão (Spring Session + cookie {@code SESSION}) e
 * recupera o usuário e seus perfis.</p>
 *
 * <h2>Mapa de rotas x perfis</h2>
 * <pre>
 *  PUT  /api/chamados/{id}/resolver ... técnico (thindesk.perfis-tecnico)
 *  /api/**  ........................... qualquer usuário autenticado
 * </pre>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ObjectMapper objectMapper,
                                                   @Value("${thindesk.perfis-tecnico}") List<String> perfisTecnico)
            throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/chamados/*/resolver")
                    .hasAnyRole(perfisTecnico.toArray(String[]::new))
                .anyRequest().authenticated()
            )

            // Usa a sessão existente do serviço de login, mas nunca cria uma nova:
            // sem login prévio a requisição é recusada com 401.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.NEVER))

            // API REST consumida por clientes HTTP (Postman/ThinDesk). A proteção contra
            // requisições forjadas fica a cargo do cookie SameSite=Lax do serviço de login.
            .csrf(AbstractHttpConfigurer::disable)

            // Sem tela de login, Basic Auth ou logout: isso é responsabilidade do serviço de login.
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .requestCache(AbstractHttpConfigurer::disable)

            // Erros de autenticação/autorização também em JSON
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> escreverErro(res, objectMapper, HttpStatus.UNAUTHORIZED,
                        "Faça login no ThinDesk (serviço de login) para acessar os chamados"))
                .accessDeniedHandler((req, res, e) -> escreverErro(res, objectMapper, HttpStatus.FORBIDDEN,
                        "Apenas técnicos podem resolver chamados"))
            );

        return http.build();
    }

    private static void escreverErro(HttpServletResponse res, ObjectMapper mapper,
                                     HttpStatus status, String mensagem) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        mapper.writeValue(res.getOutputStream(),
                ErroResponse.de(status.value(), status.getReasonPhrase(), mensagem));
    }
}
