package br.com.fiap.postech.mappin.produto.integration;

import java.util.UUID;

public class ProdutoRequest {
    private final UUID id;
    private Integer quantidade;

    public ProdutoRequest(UUID id) {
        super();
        this.id = id;
    }

    public ProdutoRequest(UUID id, Integer quantidade) {
        this(id);
        this.quantidade = quantidade;
    }

    public UUID getId() {
        return id;
    }

    public Integer getQuantidade() {
        return quantidade;
    }
}
