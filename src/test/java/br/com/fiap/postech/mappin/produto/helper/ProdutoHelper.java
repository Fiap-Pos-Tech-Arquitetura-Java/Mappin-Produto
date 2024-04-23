package br.com.fiap.postech.mappin.produto.helper;

import br.com.fiap.postech.mappin.produto.entities.Produto;

import java.util.UUID;

public class ProdutoHelper {
    public static Produto getProduto(boolean geraId) {
        var produto = new Produto(
                "Geladeira de Cerveja",
                123,
                123d
        );
        if (geraId) {
            produto.setId(UUID.randomUUID());
        }
        return produto;
    }
}
