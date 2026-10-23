package br.com.thindesk.chamados.security;

import br.com.securelogin.security.SecurityUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Obtém o usuário da sessão compartilhada com o serviço de login. */
@Component
public class UsuarioLogadoProvider {

    private final Set<String> perfisTecnico;

    public UsuarioLogadoProvider(@Value("${thindesk.perfis-tecnico}") List<String> perfisTecnico) {
        this.perfisTecnico = Set.copyOf(perfisTecnico);
    }

    public UsuarioLogado atual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUser user)) {
            throw new AuthenticationCredentialsNotFoundException("Usuário não autenticado");
        }
        Set<String> perfis = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .collect(Collectors.toUnmodifiableSet());
        boolean tecnico = perfis.stream().anyMatch(perfisTecnico::contains);
        return new UsuarioLogado(user.getId(), user.getName(), user.getUsername(), perfis, tecnico);
    }
}
