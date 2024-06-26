package br.com.fiap.postech.mappin.produto.integration;

import br.com.fiap.postech.mappin.produto.services.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.function.Consumer;

@Component
public class ProdutoConsumer {
    private final ProdutoService produtoService;

    @Autowired
    public ProdutoConsumer(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Bean(name = "consumer-remover-do-estoque")
    @PostMapping
    Consumer<ProdutoRequest> consumerRemoverDoEstoque() {
        return produtoService::removerDoEstoque;
    }
}
