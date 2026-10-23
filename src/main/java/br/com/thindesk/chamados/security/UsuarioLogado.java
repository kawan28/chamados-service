package br.com.thindesk.chamados.security;

import java.util.Set;

/**
 * Usuário autenticado no serviço de login, como este microsserviço o enxerga.
 *
 * @param perfis  perfis sem o prefixo ROLE_ (ex.: USER, MANAGER, ADMIN)
 * @param tecnico verdadeiro se algum perfil estiver em {@code thindesk.perfis-tecnico}
 */
public record UsuarioLogado(String id, String nome, String email, Set<String> perfis, boolean tecnico) {
}
