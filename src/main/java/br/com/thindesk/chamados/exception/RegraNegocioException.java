package br.com.thindesk.chamados.exception;

/** Violação de regra de negócio (ex.: alterar chamado já resolvido). HTTP 409. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
