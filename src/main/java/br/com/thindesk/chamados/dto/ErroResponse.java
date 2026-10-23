package br.com.thindesk.chamados.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato padrão de erro devolvido pela API. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        Map<String, String> campos
) {
    public static ErroResponse de(int status, String erro, String mensagem) {
        return new ErroResponse(LocalDateTime.now(), status, erro, mensagem, null);
    }
}
