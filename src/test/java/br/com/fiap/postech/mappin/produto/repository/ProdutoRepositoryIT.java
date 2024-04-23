package br.com.fiap.postech.mappin.produto.repository;

import br.com.fiap.postech.mappin.produto.entities.Produto;
import br.com.fiap.postech.mappin.produto.helper.ProdutoHelper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Transactional
public class ProdutoRepositoryIT {
    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void devePermitirCriarEstrutura() {
        var totalRegistros = produtoRepository.count();
        assertThat(totalRegistros).isEqualTo(3);
    }

    @Test
    void devePermitirCadastrarProduto() {
        // Arrange
        var produto = ProdutoHelper.getProduto(true);
        // Act
        var produtoCadastrado = produtoRepository.save(produto);
        // Assert
        assertThat(produtoCadastrado).isInstanceOf(Produto.class).isNotNull();
        assertThat(produtoCadastrado.getId()).isEqualTo(produto.getId());
        assertThat(produtoCadastrado.getNome()).isEqualTo(produto.getNome());
        assertThat(produtoCadastrado.getQuantidade()).isEqualTo(produto.getQuantidade());
        assertThat(produtoCadastrado.getPreco()).isEqualTo(produto.getPreco());
    }
    @Test
    void devePermitirBuscarProduto() {
        // Arrange
        var id = UUID.fromString("81b6b80d-e64e-41fc-9097-0f31127e2bc4");
        var nome = "london pride";
        // Act
        var produtoOpcional = produtoRepository.findById(id);
        // Assert
        assertThat(produtoOpcional).isPresent();
        produtoOpcional.ifPresent(
                produtoRecebido -> {
                    assertThat(produtoRecebido).isInstanceOf(Produto.class).isNotNull();
                    assertThat(produtoRecebido.getId()).isEqualTo(id);
                    assertThat(produtoRecebido.getNome()).isEqualTo(nome);
                }
        );
    }
    @Test
    void devePermitirRemoverProduto() {
        // Arrange
        var id = UUID.fromString("8855e7b2-77b6-448b-97f8-8a0b529f3976");
        // Act
        produtoRepository.deleteById(id);
        // Assert
        var produtoOpcional = produtoRepository.findById(id);
        assertThat(produtoOpcional).isEmpty();
    }
    @Test
    void devePermitirListarProdutos() {
        // Arrange
        // Act
        var produtosListados = produtoRepository.findAll();
        // Assert
        assertThat(produtosListados).hasSize(3);
    }
}
