package br.com.fiap.postech.mappin.produto.services;

import br.com.fiap.postech.mappin.produto.entities.Produto;
import br.com.fiap.postech.mappin.produto.helper.ProdutoHelper;
import br.com.fiap.postech.mappin.produto.repository.ProdutoRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProdutoServiceTest {
    private ProdutoService produtoService;

    @Mock
    private ProdutoRepository produtoRepository;

    private AutoCloseable mock;

    @BeforeEach
    void setUp() {
        mock = MockitoAnnotations.openMocks(this);
        produtoService = new ProdutoServiceImpl(produtoRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        mock.close();
    }

    @Nested
    class CadastrarProduto {
        @Test
        void devePermitirCadastrarProduto() {
            // Arrange
            var produto = ProdutoHelper.getProduto(false);
            when(produtoRepository.save(any(Produto.class))).thenAnswer(r -> r.getArgument(0));
            // Act
            var produtoSalvo = produtoService.save(produto);
            // Assert
            assertThat(produtoSalvo)
                    .isInstanceOf(Produto.class)
                    .isNotNull();
            assertThat(produtoSalvo.getNome()).isEqualTo(produto.getNome());
            assertThat(produtoSalvo.getId()).isNotNull();
            verify(produtoRepository, times(1)).save(any(Produto.class));
        }

        @Test
        void devePermitirCadastrarProduto_produtoJaExistente() {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            when(produtoRepository.findByNome(anyString())).thenReturn(Optional.of(produto));
            when(produtoRepository.save(any(Produto.class))).thenAnswer(r -> r.getArgument(0));
            // Act
            var produtoSalvo = produtoService.save(produto);
            // Assert
            assertThat(produtoSalvo)
                    .isInstanceOf(Produto.class)
                    .isNotNull();
            assertThat(produtoSalvo.getNome()).isEqualTo(produto.getNome());
            assertThat(produtoSalvo.getId()).isNotNull();
            verify(produtoRepository, times(1)).findByNome(anyString());
            verify(produtoRepository, times(1)).save(any(Produto.class));
        }
    }

    @Nested
    class BuscarProduto {
        @Test
        void devePermitirBuscarProdutoPorId() {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
            // Act
            var produtoObtido = produtoService.findById(produto.getId());
            // Assert
            assertThat(produtoObtido).isEqualTo(produto);
            verify(produtoRepository, times(1)).findById(any(UUID.class));
        }

        @Test
        void deveGerarExcecao_QuandoBuscarProdutoPorId_idNaoExiste() {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.empty());
            UUID uuid = produto.getId();
            // Act
            assertThatThrownBy(() -> produtoService.findById(uuid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Produto não encontrado com o ID: " + produto.getId());
            // Assert
            verify(produtoRepository, times(1)).findById(any(UUID.class));
        }

        @Test
        void devePermitirBuscarTodosProduto() {
            // Arrange
            Produto criteriosDeBusca = ProdutoHelper.getProduto(false);
            Page<Produto> produtos = new PageImpl<>(Arrays.asList(
                    ProdutoHelper.getProduto(true),
                    ProdutoHelper.getProduto(true),
                    ProdutoHelper.getProduto(true)
            ));
            when(produtoRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(produtos);
            // Act
            var produtosObtidos = produtoService.findAll(Pageable.unpaged(), criteriosDeBusca);
            // Assert
            assertThat(produtosObtidos).hasSize(3);
            assertThat(produtosObtidos.getContent()).asList().allSatisfy(
                    produto -> {
                        assertThat(produto)
                                .isNotNull()
                                .isInstanceOf(Produto.class);
                    }
            );
            verify(produtoRepository, times(1)).findAll(any(Example.class), any(Pageable.class));
        }
    }

    @Nested
    class AlterarProduto {
        @Test
        void devePermitirAlterarProduto() {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            var produtoReferencia = new Produto(produto.getNome(), produto.getQuantidade(), produto.getPreco());
            var novoProduto = new Produto(
                    RandomStringUtils.random(20, true, true),
                    (int) (Math.random() * 1000),
                    Math.random() * 100
            );
            novoProduto.setId(produto.getId());
            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
            when(produtoRepository.save(any(Produto.class))).thenAnswer(r -> r.getArgument(0));
            // Act
            var produtoSalvo = produtoService.update(produto.getId(), novoProduto);
            // Assert
            assertThat(produtoSalvo)
                    .isInstanceOf(Produto.class)
                    .isNotNull();
            assertThat(produtoSalvo.getNome()).isEqualTo(novoProduto.getNome());
            assertThat(produtoSalvo.getNome()).isNotEqualTo(produtoReferencia.getNome());

            verify(produtoRepository, times(1)).findById(any(UUID.class));
            verify(produtoRepository, times(1)).save(any(Produto.class));
        }

        @Test
        void devePermitirAlterarProduto_semBody() {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            var produtoReferencia = new Produto(produto.getNome(), produto.getQuantidade(), produto.getPreco());
            var novoProduto = new Produto(null, null, null );
            novoProduto.setId(produto.getId());
            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
            when(produtoRepository.save(any(Produto.class))).thenAnswer(r -> r.getArgument(0));
            // Act
            var produtoSalvo = produtoService.update(produto.getId(), novoProduto);
            // Assert
            assertThat(produtoSalvo)
                    .isInstanceOf(Produto.class)
                    .isNotNull();
            assertThat(produtoSalvo.getNome()).isEqualTo(produtoReferencia.getNome());

            verify(produtoRepository, times(1)).findById(any(UUID.class));
            verify(produtoRepository, times(1)).save(any(Produto.class));
        }

        @Test
        void deveGerarExcecao_QuandoAlterarProdutoPorId_idNaoExiste() {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.empty());
            UUID uuid = produto.getId();
            // Act && Assert
            assertThatThrownBy(() -> produtoService.update(uuid, produto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Produto não encontrado com o ID: " + produto.getId());
            verify(produtoRepository, times(1)).findById(any(UUID.class));
            verify(produtoRepository, never()).save(any(Produto.class));
        }

        @Test
        void deveGerarExcecao_QuandoAlterarClientePorId_alterandoId() {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            var produtoParam = ProdutoHelper.getProduto(true);
            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
            UUID uuid = produto.getId();
            // Act && Assert
            assertThatThrownBy(() -> produtoService.update(uuid, produtoParam))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Não é possível alterar o id de um produto.");
            verify(produtoRepository, times(1)).findById(any(UUID.class));
            verify(produtoRepository, never()).save(any(Produto.class));

        }
    }

    @Nested
    class RemoverProduto {
        @Test
        void devePermitirRemoverProduto() {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
            doNothing().when(produtoRepository).deleteById(produto.getId());
            // Act
            produtoService.delete(produto.getId());
            // Assert
            verify(produtoRepository, times(1)).findById(any(UUID.class));
            verify(produtoRepository, times(1)).deleteById(any(UUID.class));
        }

        @Test
        void deveGerarExcecao_QuandRemoverProdutoPorId_idNaoExiste() {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            doNothing().when(produtoRepository).deleteById(produto.getId());
            UUID uuid = produto.getId();
            // Act && Assert
            assertThatThrownBy(() -> produtoService.delete(uuid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Produto não encontrado com o ID: " + produto.getId());
            verify(produtoRepository, times(1)).findById(any(UUID.class));
            verify(produtoRepository, never()).deleteById(any(UUID.class));
        }
    }
}