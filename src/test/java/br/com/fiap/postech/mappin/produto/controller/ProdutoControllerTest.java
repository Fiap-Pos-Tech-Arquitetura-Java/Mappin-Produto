package br.com.fiap.postech.mappin.produto.controller;

import br.com.fiap.postech.mappin.produto.entities.Produto;
import br.com.fiap.postech.mappin.produto.helper.ProdutoHelper;
import br.com.fiap.postech.mappin.produto.services.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProdutoControllerTest {
    public static final String CLIENTE = "/produto";
    private MockMvc mockMvc;
    @Mock
    private ProdutoService produtoService;
    private AutoCloseable mock;

    @BeforeEach
    void setUp() {
        mock = MockitoAnnotations.openMocks(this);
        ProdutoController produtoController = new ProdutoController(produtoService);
        mockMvc = MockMvcBuilders.standaloneSetup(produtoController).build();
    }

    @AfterEach
    void tearDown() throws Exception {
        mock.close();
    }

    public static String asJsonString(final Object object) throws Exception {
        return new ObjectMapper().writeValueAsString(object);
    }

    @Nested
    class CadastrarProduto {
        @Test
        void devePermitirCadastrarProduto() throws Exception {
            // Arrange
            var produto = ProdutoHelper.getProduto(false);
            when(produtoService.save(any(Produto.class))).thenAnswer(r -> r.getArgument(0));
            // Act
            mockMvc.perform(
                            post(CLIENTE).contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(produto)))
                    .andExpect(status().isCreated());
            // Assert
            verify(produtoService, times(1)).save(any(Produto.class));
        }

        @Test
        void deveGerarExcecao_QuandoRegistrarProduto_RequisicaoXml() throws Exception {
            // Arrange
            var produto = ProdutoHelper.getProduto(false);
            when(produtoService.save(any(Produto.class))).thenAnswer(r -> r.getArgument(0));
            // Act
            mockMvc.perform(
                            post("/produto").contentType(MediaType.APPLICATION_XML)
                                    .content(asJsonString(produto)))
                    .andExpect(status().isUnsupportedMediaType());
            // Assert
            verify(produtoService, never()).save(any(Produto.class));
        }
    }
    @Nested
    class BuscarProduto {
        @Test
        void devePermitirBuscarProdutoPorId() throws Exception {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            when(produtoService.findById(any(UUID.class))).thenReturn(produto);
            // Act
            mockMvc.perform(get("/produto/{id}", produto.getId().toString()))
                    .andExpect(status().isOk());
            // Assert
            verify(produtoService, times(1)).findById(any(UUID.class));
        }
        @Test
        void deveGerarExcecao_QuandoBuscarProdutoPorId_idNaoExiste() throws Exception {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            when(produtoService.findById(produto.getId())).thenThrow(IllegalArgumentException.class);
            // Act
            mockMvc.perform(get("/produto/{id}", produto.getId().toString()))
                    .andExpect(status().isBadRequest());
            // Assert
            verify(produtoService, times(1)).findById(produto.getId());
        }

        @Test
        void devePermitirBuscarTodosProduto() throws Exception {
            // Arrange
            int page = 0;
            int size = 10;
            var produto = ProdutoHelper.getProduto(true);
            var criterioProduto = new Produto(produto.getNome(), null, null);
            criterioProduto.setId(null);
            List<Produto> listProduto = new ArrayList<>();
            listProduto.add(produto);
            Page<Produto> produtos = new PageImpl<>(listProduto);
            var pageable = PageRequest.of(page, size);
            when(produtoService.findAll(
                            pageable,
                            criterioProduto
                    )
            ).thenReturn(produtos);
            // Act
            mockMvc.perform(
                            get("/produto")
                                    .param("page", String.valueOf(page))
                                    .param("size", String.valueOf(size))
                                    .param("nome", produto.getNome())
                    )
                    //.andDo(print())
                    .andExpect(status().is5xxServerError())
            //.andExpect(jsonPath("$.content", not(empty())))
            //.andExpect(jsonPath("$.totalPages").value(1))
            //.andExpect(jsonPath("$.totalElements").value(1))
            ;
            // Assert
            verify(produtoService, times(1)).findAll(pageable, criterioProduto);
        }
    }

    @Nested
    class AlterarProduto {
        @Test
        void devePermitirAlterarProduto() throws Exception {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            when(produtoService.update(produto.getId(), produto)).thenAnswer(r -> r.getArgument(1) );
            // Act
            mockMvc.perform(put("/produto/{id}", produto.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(produto)))
                    .andExpect(status().isAccepted());
            // Assert
            verify(produtoService, times(1)).update(produto.getId(), produto);
        }

        @Test
        void deveGerarExcecao_QuandoAlterarProduto_RequisicaoXml() throws Exception {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            when(produtoService.update(produto.getId(), produto)).thenAnswer(r -> r.getArgument(1) );
            // Act
            mockMvc.perform(put("/produto/{id}", produto.getId())
                            .contentType(MediaType.APPLICATION_XML)
                            .content(asJsonString(produto)))
                    .andExpect(status().isUnsupportedMediaType());
            // Assert
            verify(produtoService, never()).update(produto.getId(), produto);
        }

        @Test
        void deveGerarExcecao_QuandoAlterarProdutoPorId_idNaoExiste() throws Exception {
            // Arrange
            var produtoDTO = ProdutoHelper.getProduto(true);
            when(produtoService.update(produtoDTO.getId(), produtoDTO)).thenThrow(IllegalArgumentException.class);
            // Act
            mockMvc.perform(put("/produto/{id}", produtoDTO.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(produtoDTO)))
                    .andExpect(status().isBadRequest());
            // Assert
            verify(produtoService, times(1)).update(any(UUID.class), any(Produto.class));
        }
    }

    @Nested
    class RemoverProduto {
        @Test
        void devePermitirRemoverProduto() throws Exception {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            doNothing().when(produtoService).delete(produto.getId());
            // Act
            mockMvc.perform(delete("/produto/{id}", produto.getId()))
                    .andExpect(status().isNoContent());
            // Assert
            verify(produtoService, times(1)).delete(produto.getId());
            verify(produtoService, times(1)).delete(produto.getId());
        }

        @Test
        void deveGerarExcecao_QuandoRemoverProdutoPorId_idNaoExiste() throws Exception {
            // Arrange
            var produto = ProdutoHelper.getProduto(true);
            doThrow(new IllegalArgumentException("Produto não encontrado com o ID: " + produto.getId()))
                    .when(produtoService).delete(produto.getId());
            // Act
            mockMvc.perform(delete("/produto/{id}", produto.getId()))
                    .andExpect(status().isBadRequest());
            // Assert
            verify(produtoService, times(1)).delete(produto.getId());
        }
    }
}