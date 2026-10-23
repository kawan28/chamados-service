package br.com.thindesk.chamados.exception;

import br.com.thindesk.chamados.dto.ErroResponse;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Converte as exceções da aplicação em respostas JSON padronizadas. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ErroResponse corpo = new ErroResponse(LocalDateTime.now(), 400, "Bad Request",
                "Dados inválidos", campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou mal formatado");
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponse> acessoNegado(AcessoNegadoException ex) {
        return resposta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex) {
        return resposta(HttpStatus.NOT_FOUND, "Rota não encontrada");
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErroResponse> concorrencia(OptimisticLockingFailureException ex) {
        return resposta(HttpStatus.CONFLICT,
                "O chamado foi alterado por outra requisição. Consulte-o novamente.");
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status)
                .body(ErroResponse.de(status.value(), status.getReasonPhrase(), mensagem));
    }
}
