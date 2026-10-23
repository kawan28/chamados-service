package br.com.thindesk.chamados.service;

import br.com.thindesk.chamados.dto.AbrirChamadoRequest;
import br.com.thindesk.chamados.dto.ResolverChamadoRequest;
import br.com.thindesk.chamados.exception.AcessoNegadoException;
import br.com.thindesk.chamados.exception.RecursoNaoEncontradoException;
import br.com.thindesk.chamados.exception.RegraNegocioException;
import br.com.thindesk.chamados.model.Chamado;
import br.com.thindesk.chamados.repository.ChamadoRepository;
import br.com.thindesk.chamados.security.UsuarioLogado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Regras de negócio dos chamados.
 *
 * <ul>
 *   <li>O solicitante, a data de abertura e o status ABERTO são definidos pelo sistema.</li>
 *   <li>Um usuário comum só enxerga os próprios chamados; técnicos enxergam todos.</li>
 *   <li>Somente técnicos resolvem chamados.</li>
 *   <li>Chamado RESOLVIDO é estado final: não pode ser alterado novamente.</li>
 * </ul>
 */
@Service
public class ChamadoService {

    private static final Logger log = LoggerFactory.getLogger(ChamadoService.class);

    private final ChamadoRepository repository;

    public ChamadoService(ChamadoRepository repository) {
        this.repository = repository;
    }

    public Chamado abrir(AbrirChamadoRequest req, UsuarioLogado usuario) {
        Chamado chamado = Chamado.abrir(req.titulo().trim(), req.descricao().trim(),
                usuario.id(), usuario.nome());
        Chamado salvo = repository.save(chamado);
        log.info("Chamado {} aberto por {}", salvo.getId(), usuario.email());
        return salvo;
    }

    public List<Chamado> listarPorUsuario(String usuarioId, UsuarioLogado usuario) {
        if (!usuario.tecnico() && !usuario.id().equals(usuarioId)) {
            throw new AcessoNegadoException("Você só pode listar os seus próprios chamados");
        }
        return repository.findBySolicitanteIdOrderByDataAberturaDesc(usuarioId);
    }

    public Chamado buscar(String id, UsuarioLogado usuario) {
        Chamado chamado = buscarOuFalhar(id);
        if (!usuario.tecnico() && !usuario.id().equals(chamado.getSolicitanteId())) {
            throw new AcessoNegadoException("Você não tem acesso a este chamado");
        }
        return chamado;
    }

    public Chamado resolver(String id, ResolverChamadoRequest req, UsuarioLogado usuario) {
        // Segunda camada de proteção (a primeira está no SecurityConfig)
        if (!usuario.tecnico()) {
            throw new AcessoNegadoException("Apenas técnicos podem resolver chamados");
        }
        Chamado chamado = buscarOuFalhar(id);
        if (chamado.isResolvido()) {
            throw new RegraNegocioException("Este chamado já foi resolvido e não pode ser alterado novamente");
        }
        chamado.resolver(req.solucao().trim(), usuario.id(), usuario.nome());
        Chamado salvo = repository.save(chamado);
        log.info("Chamado {} resolvido por {}", salvo.getId(), usuario.email());
        return salvo;
    }

    private Chamado buscarOuFalhar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado"));
    }
}
