package br.com.thindesk.chamados.exception;

/** Usuário autenticado, mas sem permissão para a operação. HTTP 403. */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
