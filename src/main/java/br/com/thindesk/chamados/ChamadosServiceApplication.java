package br.com.thindesk.chamados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Sem usuários próprios: a autenticação vem do serviço de login (sessão compartilhada)
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ChamadosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChamadosServiceApplication.class, args);
    }
}
