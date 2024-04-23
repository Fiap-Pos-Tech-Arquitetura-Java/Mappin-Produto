package br.com.fiap.postech.mappin.produto.repository;

import br.com.fiap.postech.mappin.produto.entities.Produto;
import br.com.fiap.postech.mappin.produto.helper.ProdutoHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProdutoRepositoryTest {
    @Mock
    private ProdutoRepository produtoRepository;

    AutoCloseable openMocks;
    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void devePermitirCadastrarProduto() {
        // Arrange
        var produto = ProdutoHelper.getProduto(false);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        // Act
        var savedProduto = produtoRepository.save(produto);
        // Assert
        assertThat(savedProduto).isNotNull().isEqualTo(produto);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void devePermitirBuscarProduto() {
        // Arrange
        var produto = ProdutoHelper.getProduto(true);
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
        // Act
        var produtoOpcional = produtoRepository.findById(produto.getId());
        // Assert
        assertThat(produtoOpcional).isNotNull().containsSame(produto);
        produtoOpcional.ifPresent(
                produtoRecebido -> {
                    assertThat(produtoRecebido).isInstanceOf(Produto.class).isNotNull();
                    assertThat(produtoRecebido.getId()).isEqualTo(produto.getId());
                    assertThat(produtoRecebido.getNome()).isEqualTo(produto.getNome());
                }
        );
        verify(produtoRepository, times(1)).findById(produto.getId());
    }
    @Test
    void devePermitirRemoverProduto() {
        //Arrange
        var id = UUID.randomUUID();
        doNothing().when(produtoRepository).deleteById(id);
        //Act
        produtoRepository.deleteById(id);
        //Assert
        verify(produtoRepository, times(1)).deleteById(id);
    }
    @Test
    void devePermitirListarProdutos() {
        // Arrange
        var produto1 = ProdutoHelper.getProduto(true);
        var produto2 = ProdutoHelper.getProduto(true);
        var listaProdutos = Arrays.asList(
                produto1,
                produto2
        );
        when(produtoRepository.findAll()).thenReturn(listaProdutos);
        // Act
        var produtosListados = produtoRepository.findAll();
        assertThat(produtosListados)
                .hasSize(2)
                .containsExactlyInAnyOrder(produto1, produto2);
        verify(produtoRepository, times(1)).findAll();
    }
}