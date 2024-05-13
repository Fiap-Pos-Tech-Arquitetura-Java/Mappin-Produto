package br.com.fiap.postech.mappin.produto.services;

import br.com.fiap.postech.mappin.produto.entities.Produto;
import br.com.fiap.postech.mappin.produto.integration.ProdutoRequest;
import br.com.fiap.postech.mappin.produto.repository.ProdutoRepository;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProdutoServiceImpl implements ProdutoService {
    
    private final ProdutoRepository
            produtoRepository;

    @Autowired
    public ProdutoServiceImpl(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public Produto save(Produto produto) {
        validaQuantidade(produto);
        validaPreco(produto);
        produtoRepository.findByNome(produto.getNome())
            .ifPresentOrElse(
                    p -> produto.setId(p.getId()),
                    () -> produto.setId(UUID.randomUUID())
            );
        return produtoRepository.save(produto);
    }

    @Override
    public Page<Produto> findAll(Pageable pageable, Produto produto) {
        Example<Produto> produtoExample = Example.of(produto);
        return produtoRepository.findAll(produtoExample, pageable);
    }

    @Override
    public Produto findById(UUID id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com o ID: " + id));
    }

    @Override
    public Produto update(UUID id, Produto produtoParam) {
        Produto produto = findById(id);
        if (StringUtils.isNotEmpty(produtoParam.getNome())) {
            produto.setNome(produtoParam.getNome());
        }
        if (produtoParam.getId() != null && !produto.getId().equals(produtoParam.getId())) {
            throw new IllegalArgumentException("Não é possível alterar o id de um produto.");
        }
        if (StringUtils.isNotEmpty(produtoParam.getNome())) {
            produto.setNome(produtoParam.getNome());
        }
        if (produtoParam.getQuantidade() != null && !produtoParam.getQuantidade().equals(produto.getQuantidade())) {
            validaQuantidade(produtoParam);
            produto.setQuantidade(produtoParam.getQuantidade());
        }
        if (produtoParam.getPreco() != null && !produtoParam.getPreco().equals(produto.getPreco())) {
            validaPreco(produtoParam);
            produto.setPreco(produtoParam.getPreco());
        }
        produto = produtoRepository.save(produto);
        return produto;
    }

    private static void validaQuantidade(Produto produto) {
        if (produto.getQuantidade() < 1) {
            throw new IllegalArgumentException("Não é possível alterar a quantidade de um produto para um valor menor ou igual a zero.");
        }
    }

    private static void validaPreco(Produto produto) {
        if (produto.getPreco().compareTo(0d) < 0) {
            throw new IllegalArgumentException("Não é possível alterar o preco de um produto para um valor menor ou igual a zero.");
        }
    }

    @Override
    public void delete(UUID id) {
        findById(id);
        produtoRepository.deleteById(id);
    }

    @Override
    public void removerDoEstoque(ProdutoRequest produtoRequest) {
        Produto produtoDb = findById(produtoRequest.getId());
        produtoDb.setQuantidade(produtoDb.getQuantidade() - produtoRequest.getQuantidade());
        validaQuantidade(produtoDb);
        produtoRepository.save(produtoDb);
    }
}