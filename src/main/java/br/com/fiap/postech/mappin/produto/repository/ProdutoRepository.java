package br.com.fiap.postech.mappin.produto.repository;

import br.com.fiap.postech.mappin.produto.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
    Optional<Produto> findByNome(String nome);
}
