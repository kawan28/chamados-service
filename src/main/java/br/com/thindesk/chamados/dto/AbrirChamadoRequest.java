package br.com.thindesk.chamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo do POST /api/chamados. Apenas título e descrição são aceitos:
 * solicitante, data e status são definidos pelo sistema.
 */
public record AbrirChamadoRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(min = 5, max = 100, message = "O título deve ter entre 5 e 100 caracteres")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(min = 10, max = 2000, message = "A descrição deve ter entre 10 e 2000 caracteres")
        String descricao
) {
}
