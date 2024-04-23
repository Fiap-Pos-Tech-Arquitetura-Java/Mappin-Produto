package br.com.fiap.postech.mappin.produto.controller;

import br.com.fiap.postech.mappin.produto.entities.Produto;
import br.com.fiap.postech.mappin.produto.helper.ProdutoHelper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
public class ProdutoControllerIT {

    public static final String CLIENTE = "/mappin/produto";
    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Nested
    class CadastrarProduto {
        @Test
        void devePermitirCadastrarProduto() {
            var produto = ProdutoHelper.getProduto(false);
            given()
                .contentType(MediaType.APPLICATION_JSON_VALUE).body(produto)
            .when()
                .post(CLIENTE)
            .then()
                .statusCode(HttpStatus.CREATED.value())
                .body(matchesJsonSchemaInClasspath("schemas/produto.schema.json"));
            // TODO VERIFICAR A OBRIGATORIEDADE DO ID
        }

        @Test
        void deveGerarExcecao_QuandoCadastrarProduto_RequisicaoXml() {
            /*
              Na aula o professor instanciou uma string e enviou no .body()
              Mas como o teste valida o contentType o body pode ser enviado com qualquer conteudo
              ou nem mesmo ser enviado como ficou no teste abaixo.
             */
            given()
                .contentType(MediaType.APPLICATION_XML_VALUE)
            .when()
                .post(CLIENTE)
            .then()
                .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .body(matchesJsonSchemaInClasspath("schemas/error.schema.json"));
        }
    }

    @Nested
    class BuscarProduto {
        @Test
        void devePermitirBuscarProdutoPorId() {
            var id = "81b6b80d-e64e-41fc-9097-0f31127e2bc4";
            given()
                //.header(HttpHeaders.AUTHORIZATION, ProdutoHelper.getToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .get(CLIENTE + "/{id}", id)
            .then()
                .statusCode(HttpStatus.OK.value())
                .body(matchesJsonSchemaInClasspath("schemas/produto.schema.json"));
            // TODO VERIFICAR A OBRIGATORIEDADE DO ID
        }
        @Test
        void deveGerarExcecao_QuandoBuscarProdutoPorId_idNaoExiste() {
            var id = ProdutoHelper.getProduto(true).getId();
            given()
                //.header(HttpHeaders.AUTHORIZATION, ProdutoHelper.getToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .get(CLIENTE + "/{id}", id)
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void devePermitirBuscarTodosProduto() {
            given()
                //.header(HttpHeaders.AUTHORIZATION, ProdutoHelper.getToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .get(CLIENTE)
            .then()
                .statusCode(HttpStatus.OK.value())
                .body(matchesJsonSchemaInClasspath("schemas/produto.page.schema.json"));
        }

        @Test
        void devePermitirBuscarTodosProduto_ComPaginacao() {
            given()
                //.header(HttpHeaders.AUTHORIZATION, ProdutoHelper.getToken())
                .queryParam("page", "1")
                .queryParam("size", "1")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .get(CLIENTE)
            .then()
                .statusCode(HttpStatus.OK.value())
                .body(matchesJsonSchemaInClasspath("schemas/produto.page.schema.json"));
        }
    }

    @Nested
    class AlterarProduto {
        @Test
        void devePermitirAlterarProduto(){
            var produto = new Produto(
                    "Kaiby o mestre do miro !!!",
                    16804046,
                    123.44
            );
            produto.setId(UUID.fromString("cccf34c8-c57d-4612-aed9-edbeda2dc38f"));
            given()
                //.header(HttpHeaders.AUTHORIZATION, ProdutoHelper.getToken())
                .body(produto).contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .put(CLIENTE + "/{id}", produto.getId())
            .then()
                .statusCode(HttpStatus.ACCEPTED.value())
                .body(matchesJsonSchemaInClasspath("schemas/produto.schema.json"));
        }

        @Test
        void deveGerarExcecao_QuandoAlterarProduto_RequisicaoXml() {
            var produto = ProdutoHelper.getProduto(true);
            given()
                //.header(HttpHeaders.AUTHORIZATION, ProdutoHelper.getToken())
                .body(produto).contentType(MediaType.APPLICATION_XML_VALUE)
            .when().log().all()
                .put(CLIENTE + "/{id}", produto.getId())
            .then().log().all()
                .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        }

        @Test
        void deveGerarExcecao_QuandoAlterarProdutoPorId_idNaoExiste() {
            var produto = ProdutoHelper.getProduto(true);
            given()
                //.header(HttpHeaders.AUTHORIZATION, ProdutoHelper.getToken())
                .body(produto).contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .put(CLIENTE + "/{id}", produto.getId())
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(equalTo("Produto não encontrado com o ID: " + produto.getId()));
        }
    }

    @Nested
    class RemoverProduto {
        @Test
        void devePermitirRemoverProduto() {
            var produto = new Produto(
                    "Janaina",
                    1235,
                    1234.45
            );
            produto.setId(UUID.fromString("3fe252d3-1bce-4e8f-9f9f-15f143003c3a"));
            given()
                //.header(HttpHeaders.AUTHORIZATION, ProdutoHelper.getToken())
            .when()
                .delete(CLIENTE + "/{id}", produto.getId())
            .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void deveGerarExcecao_QuandoRemoverProdutoPorId_idNaoExiste() {
            var produto = ProdutoHelper.getProduto(true);
            given()
                //.header(HttpHeaders.AUTHORIZATION, ProdutoHelper.getToken())
            .when()
                .delete(CLIENTE + "/{id}", produto.getId())
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(equalTo("Produto não encontrado com o ID: " + produto.getId()));
        }
    }
}
