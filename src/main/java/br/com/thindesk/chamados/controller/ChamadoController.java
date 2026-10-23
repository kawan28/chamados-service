package br.com.thindesk.chamados.controller;

import br.com.thindesk.chamados.dto.AbrirChamadoRequest;
import br.com.thindesk.chamados.dto.ChamadoResponse;
import br.com.thindesk.chamados.dto.ResolverChamadoRequest;
import br.com.thindesk.chamados.model.Chamado;
import br.com.thindesk.chamados.security.UsuarioLogadoProvider;
import br.com.thindesk.chamados.service.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    private final ChamadoService service;
    private final UsuarioLogadoProvider usuarioLogado;

    public ChamadoController(ChamadoService service, UsuarioLogadoProvider usuarioLogado) {
        this.service = service;
        this.usuarioLogado = usuarioLogado;
    }

    /** Abre um chamado em nome do usuário logado. */
    @PostMapping
    public ResponseEntity<ChamadoResponse> abrir(@Valid @RequestBody AbrirChamadoRequest req) {
        Chamado chamado = service.abrir(req, usuarioLogado.atual());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(chamado.getId()).toUri();
        return ResponseEntity.created(location).body(ChamadoResponse.de(chamado));
    }

    /** Lista os chamados abertos por um usuário. */
    @GetMapping("/usuario/{id}")
    public List<ChamadoResponse> listarPorUsuario(@PathVariable String id) {
        return service.listarPorUsuario(id, usuarioLogado.atual()).stream()
                .map(ChamadoResponse::de)
                .toList();
    }

    /** Detalhes e status de um chamado. */
    @GetMapping("/{id}")
    public ChamadoResponse buscar(@PathVariable String id) {
        return ChamadoResponse.de(service.buscar(id, usuarioLogado.atual()));
    }

    /** Registra a solução (apenas técnicos) e muda o status para RESOLVIDO. */
    @PutMapping("/{id}/resolver")
    public ChamadoResponse resolver(@PathVariable String id, @Valid @RequestBody ResolverChamadoRequest req) {
        return ChamadoResponse.de(service.resolver(id, req, usuarioLogado.atual()));
    }
}
