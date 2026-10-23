package br.com.thindesk.chamados.dto;

import br.com.thindesk.chamados.model.Chamado;
import br.com.thindesk.chamados.model.StatusChamado;

import java.time.LocalDateTime;

public record ChamadoResponse(
        String id,
        String titulo,
        String descricao,
        StatusChamado status,
        String solicitanteId,
        String solicitanteNome,
        LocalDateTime dataAbertura,
        String solucao,
        String tecnicoId,
        String tecnicoNome,
        LocalDateTime dataResolucao
) {
    public static ChamadoResponse de(Chamado c) {
        return new ChamadoResponse(
                c.getId(), c.getTitulo(), c.getDescricao(), c.getStatus(),
                c.getSolicitanteId(), c.getSolicitanteNome(), c.getDataAbertura(),
                c.getSolucao(), c.getTecnicoId(), c.getTecnicoNome(), c.getDataResolucao());
    }
}
