package br.com.securelogin.security;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

/**
 * Contrato compartilhado com o serviço de login.
 *
 * <p>O serviço de login guarda o usuário autenticado dentro da sessão, no MongoDB
 * (coleção {@code sessions}), serializado como {@code br.com.securelogin.security.SecurityUser}.
 * Para que este microsserviço consiga ler essa mesma sessão — e assim reutilizar a
 * autenticação existente sem um novo login — a classe precisa existir aqui com o
 * <b>mesmo pacote, os mesmos campos e o mesmo serialVersionUID</b>.</p>
 *
 * <p>Não altere os campos sem alterar também o serviço de login.</p>
 */
public class SecurityUser implements UserDetails, CredentialsContainer {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final String email;
    private String password;
    private final List<SimpleGrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonLocked;

    public SecurityUser(String id, String name, String email, List<SimpleGrantedAuthority> authorities) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.authorities = List.copyOf(authorities);
        this.enabled = true;
        this.accountNonLocked = true;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    @Override
    public String getUsername() { return email; }

    @Override
    public String getPassword() { return password; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public boolean isAccountNonLocked() { return accountNonLocked; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
