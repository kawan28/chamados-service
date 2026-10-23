package br.com.thindesk.chamados.controller;

import br.com.thindesk.chamados.security.UsuarioLogado;
import br.com.thindesk.chamados.security.UsuarioLogadoProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Mostra qual usuário da sessão compartilhada o microsserviço reconheceu. */
@RestController
@RequestMapping("/api/me")
public class UsuarioController {

    private final UsuarioLogadoProvider usuarioLogado;

    public UsuarioController(UsuarioLogadoProvider usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
    }

    @GetMapping
    public UsuarioLogado me() {
        return usuarioLogado.atual();
    }
}
