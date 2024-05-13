package br.com.fiap.postech.mappin.produto.services;

import br.com.fiap.postech.mappin.produto.entities.Produto;
import br.com.fiap.postech.mappin.produto.helper.ProdutoHelper;
import br.com.fiap.postech.mappin.produto.integration.ProdutoRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Transactional
public class ProdutoServiceIT {
    @Autowired
    private ProdutoService produtoService;

    @Nested
    class CadastrarProduto {
        @Test
        void devePermitirCadastrarProduto() {
            // Arrange
            var produto = ProdutoHelper.getProduto(false);
            // Act
            var produtoSalvo = produtoService.save(produto);
            // Assert
            assertThat(produtoSalvo)
                    .isInstanceOf(Produto.class)
                    .isNotNull();
            assertThat(produtoSalvo.getNome()).isEqualTo(produto.getNome());
            assertThat(produtoSalvo.getQuantidade()).isEqualTo(produto.getQuantidade());
            assertThat(produtoSalvo.getPreco()).isEqualTo(produto.getPreco());
            assertThat(produtoSalvo.getId()).isNotNull();
        }
    }

    @Nested
    class BuscarProduto {
        @Test
        void devePermitirBuscarProdutoPorId() {
            // Arrange
            var id = UUID.fromString("81b6b80d-e64e-41fc-9097-0f31127e2bc4");
            var nome = "london pride";
            // Act
            var produtoObtido = produtoService.findById(id);
            // Assert
            assertThat(produtoObtido).isNotNull().isInstanceOf(Produto.class);
            assertThat(produtoObtido.getNome()).isEqualTo(nome);
            assertThat(produtoObtido.getId()).isNotNull();
            assertThat(produtoObtido.getId()).isEqualTo(id);
        }

        @Test
        void deveGerarExcecao_QuandoBuscarProdutoPorId_idNaoExiste() {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            UUID uuid = produto.getId();
            // Act &&  Assert
            assertThatThrownBy(() -> produtoService.findById(uuid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Produto não encontrado com o ID: " + produto.getId());
        }

        @Test
        void devePermitirBuscarTodosProduto() {
            // Arrange
            Produto criteriosDeBusca = new Produto(null,null,null);
            criteriosDeBusca.setId(null);
            // Act
            var listaProdutosObtidos = produtoService.findAll(Pageable.unpaged(), criteriosDeBusca);
            // Assert
            assertThat(listaProdutosObtidos).isNotNull().isInstanceOf(Page.class);
            assertThat(listaProdutosObtidos.getContent()).asList().hasSize(38);
            assertThat(listaProdutosObtidos.getContent()).asList().allSatisfy(
                    produtoObtido -> {
                        assertThat(produtoObtido).isNotNull();
                    }
            );
        }
    }

    @Nested
    class AlterarProduto {

        @Test
        void devePermitirAlterarProduto() {
            // Arrange
            var id = UUID.fromString("cccf34c8-c57d-4612-aed9-edbeda2dc38f");
            var nome = "london pride IPA";
            var quantidade = 5281680;
            var preco = 1.00;

            var produto = new Produto(nome, quantidade, preco);
            produto.setId(null);
            // Act
            var produtoAtualizada = produtoService.update(id, produto);
            // Assert
            assertThat(produtoAtualizada).isNotNull().isInstanceOf(Produto.class);
            assertThat(produtoAtualizada.getId()).isNotNull();
            assertThat(produtoAtualizada.getNome()).isEqualTo(nome);
        }

        @Test
        void deveGerarExcecao_QuandoAlterarProdutoPorId_idNaoExiste() {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            var uuid = produto.getId();
            // Act &&  Assert
            assertThatThrownBy(() -> produtoService.update(uuid, produto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Produto não encontrado com o ID: " + produto.getId());
        }
    }

    @Nested
    class RemoverProduto {
        @Test
        void devePermitirRemoverProduto() {
            // Arrange
            var id = UUID.fromString("3fe252d3-1bce-4e8f-9f9f-15f143003c3a");
            // Act
            produtoService.delete(id);
            // Assert
            assertThatThrownBy(() -> produtoService.findById(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Produto não encontrado com o ID: " + id);
        }

        @Test
        void deveGerarExcecao_QuandRemoverProdutoPorId_idNaoExiste() {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            var uuid = produto.getId();
            // Act &&  Assert
            assertThatThrownBy(() -> produtoService.delete(uuid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Produto não encontrado com o ID: " + produto.getId());
        }
    }

    @Nested
    class RemoverProdutoEstoque {
        @Test
        void devePermitirRemoverProdutoEstoque() {
            var id = UUID.fromString("81b6b80d-e64e-41fc-9097-0f31127e2bc4");
            var quantidadeOriginal = produtoService.findById(id).getQuantidade();
            var quantidadeARemoverDoEstoque = 23;
            var quantidadeEsperada = quantidadeOriginal - quantidadeARemoverDoEstoque;

            var produtoRequest = new ProdutoRequest(id, quantidadeARemoverDoEstoque);
            produtoService.removerDoEstoque(produtoRequest);

            var produto = produtoService.findById(id);
            assertThat(produto).isNotNull().isInstanceOf(Produto.class);
            assertThat(produto.getQuantidade()).isEqualTo(quantidadeEsperada);
        }

        @Test
        void deveGerarExcecao_QuandoAlterarClientePorId_alterandoId() {
            var id = UUID.fromString("81b6b80d-e64e-41fc-9097-0f31127e2bc4");
            var quantidadeARemoverDoEstoque = 50;

            var produtoRequest = new ProdutoRequest(id, quantidadeARemoverDoEstoque);

            // Act &&  Assert
            assertThatThrownBy(() -> produtoService.removerDoEstoque(produtoRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Não é possível alterar a quantidade de um produto para um valor menor ou igual a zero.");
        }
    }
}
