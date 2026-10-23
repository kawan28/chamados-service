package br.com.thindesk.chamados.repository;

import br.com.thindesk.chamados.model.Chamado;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChamadoRepository extends MongoRepository<Chamado, String> {

    /** Chamados de um usuário, do mais recente para o mais antigo. */
    List<Chamado> findBySolicitanteIdOrderByDataAberturaDesc(String solicitanteId);
}
