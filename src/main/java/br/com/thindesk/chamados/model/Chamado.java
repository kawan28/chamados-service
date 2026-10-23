package br.com.thindesk.chamados.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento de chamado técnico persistido na coleção {@code chamados} do MongoDB.
 *
 * <p>Os dados de solicitante, data de abertura e status são preenchidos pelo próprio
 * sistema ({@link #abrir}); o cliente da API informa apenas título e descrição.</p>
 */
@Document(collection = "chamados")
public class Chamado {

    @Id
    private String id;

    private String titulo;
    private String descricao;

    /** Id do usuário no serviço de login (quem abriu o chamado). */
    @Indexed
    private String solicitanteId;
    private String solicitanteNome;

    private StatusChamado status;
    private LocalDateTime dataAbertura;

    private String solucao;
    private String tecnicoId;
    private String tecnicoNome;
    private LocalDateTime dataResolucao;

    /** Controle de concorrência: impede que duas resoluções simultâneas sobrescrevam uma à outra. */
    @Version
    private Long versao;

    public static Chamado abrir(String titulo, String descricao, String solicitanteId, String solicitanteNome) {
        Chamado chamado = new Chamado();
        chamado.titulo = titulo;
        chamado.descricao = descricao;
        chamado.solicitanteId = solicitanteId;
        chamado.solicitanteNome = solicitanteNome;
        chamado.status = StatusChamado.ABERTO;
        chamado.dataAbertura = LocalDateTime.now();
        return chamado;
    }

    public void resolver(String solucao, String tecnicoId, String tecnicoNome) {
        this.solucao = solucao;
        this.tecnicoId = tecnicoId;
        this.tecnicoNome = tecnicoNome;
        this.status = StatusChamado.RESOLVIDO;
        this.dataResolucao = LocalDateTime.now();
    }

    public boolean isResolvido() {
        return status == StatusChamado.RESOLVIDO;
    }

    // ---------------- getters ----------------

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getSolicitanteId() { return solicitanteId; }
    public String getSolicitanteNome() { return solicitanteNome; }
    public StatusChamado getStatus() { return status; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public String getSolucao() { return solucao; }
    public String getTecnicoId() { return tecnicoId; }
    public String getTecnicoNome() { return tecnicoNome; }
    public LocalDateTime getDataResolucao() { return dataResolucao; }
    public Long getVersao() { return versao; }
}
