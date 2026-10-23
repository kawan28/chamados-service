package br.com.thindesk.chamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo do PUT /api/chamados/{id}/resolver. */
public record ResolverChamadoRequest(
        @NotBlank(message = "A solução é obrigatória")
        @Size(min = 10, max = 2000, message = "A solução deve ter entre 10 e 2000 caracteres")
        String solucao
) {
}
