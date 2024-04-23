package br.com.fiap.postech.mappin.produto.services;

import br.com.fiap.postech.mappin.produto.entities.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProdutoService {
    Produto save(Produto produto);

    Page<Produto> findAll(Pageable pageable, Produto produto);

    Produto findById(UUID id);

    Produto update(UUID id, Produto produto);

    void delete(UUID id);
}
