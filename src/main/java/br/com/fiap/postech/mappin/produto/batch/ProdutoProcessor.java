package br.com.fiap.postech.mappin.produto.batch;

import br.com.fiap.postech.mappin.produto.entities.Produto;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDateTime;

public class ProdutoProcessor implements ItemProcessor<Produto, Produto> {

    @Override
    public Produto process(Produto produto) throws Exception {
        produto.setDataAtualizacao(LocalDateTime.now());
        return produto;
    }
}
