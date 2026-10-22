package com.ajudaqui.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ajudaqui.domain.Usuario;
import com.ajudaqui.resource.CuponFiscalResource;
import com.ajudaqui.support.FakeUsuarioRepository;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class LambdaHandlerTest {

  private static final String TOKEN_ADMIN = "550e8400-e29b-41d4-a716-446655440002";
  private static final String TOKEN_USUARIO = "f8b9e295-bbd8-4cfb-8795-9dc70d1b32f5";

  private FakeUsuarioRepository repository;
  private LambdaHandler handler;
  private Context context;

  @BeforeEach
  void setUp() {
    repository = new FakeUsuarioRepository();
    handler = new LambdaHandler(new CuponFiscalResource(repository));
    context = contextoFake();
  }

  @Test
  void semBodyNemOperacaoRetornaErro400() {
    Map<String, Object> resposta = handler.handleRequest(evento(null), context);

    assertEquals(400, resposta.get("statusCode"));
    assertTrue(resposta.get("body").toString().contains("'operacao' é obrigatório"));
  }

  @Test
  void bodyVazioRetornaErro400() {
    Map<String, Object> resposta = handler.handleRequest(evento(""), context);

    assertEquals(400, resposta.get("statusCode"));
    assertTrue(resposta.get("body").toString().contains("Body é obrigatório"));
  }

  @Test
  void semOperacaoRetornaErro400() {
    Map<String, Object> resposta = handler.handleRequest(evento("{\"perfil\":\"gratuito\"}"), context);

    assertEquals(400, resposta.get("statusCode"));
    assertTrue(resposta.get("body").toString().contains("'operacao' é obrigatório"));
  }

  @Test
  void operacaoDesconhecidaRetornaErro400() {
    Map<String, Object> resposta = handler.handleRequest(evento("{\"operacao\":\"nao_existe\"}"), context);

    assertEquals(400, resposta.get("statusCode"));
    assertTrue(resposta.get("body").toString().contains("Operação desconhecida"));
  }

  @Test
  void jsonInvalidoRetornaErro400() {
    Map<String, Object> resposta = handler.handleRequest(evento("{operacao:"), context);

    assertEquals(400, resposta.get("statusCode"));
  }

  @Test
  void registrarRoteiaParaRepositorioERetorna200() {
    repository.tokenCriado = UUID.fromString(TOKEN_USUARIO);

    Map<String, Object> resposta = handler.handleRequest(
        evento("{\"operacao\":\"registrar\",\"perfil\":\"gratuito\"}"), context);

    assertEquals(200, resposta.get("statusCode"));
    assertEquals(TOKEN_USUARIO, resposta.get("body"));
    assertEquals(List.of("criarUsuario:gratuito"), repository.chamadas);
  }

  @Test
  void mudarStatusRoteiaComOsDoisTokens() {
    Map<String, Object> resposta = handler.handleRequest(evento(
        "{\"operacao\":\"mudarStatus\",\"tokenSolicitante\":\"" + TOKEN_ADMIN
            + "\",\"tokenUsuario\":\"" + TOKEN_USUARIO + "\"}"),
        context);

    assertEquals(200, resposta.get("statusCode"));
    assertEquals(List.of("alterarStatusUsuario:" + TOKEN_ADMIN + ":" + TOKEN_USUARIO), repository.chamadas);
  }

  @Test
  void mudatrPerfilRoteiaComTokensEPerfil() {
    Map<String, Object> resposta = handler.handleRequest(evento(
        "{\"operacao\":\"mudatrPerfil\",\"tokenSolicitante\":\"" + TOKEN_ADMIN
            + "\",\"tokenUsuario\":\"" + TOKEN_USUARIO + "\",\"perfil\":\"pago_02\"}"),
        context);

    assertEquals(200, resposta.get("statusCode"));
    assertEquals(
        List.of("mudatrPerfil:" + TOKEN_ADMIN + ":" + TOKEN_USUARIO + ":pago_02"),
        repository.chamadas);
  }

  @Test
  void getAllUsersRepassaAtivoQuandoInformado() {
    repository.usuarios = List.of(new Usuario());

    Map<String, Object> resposta = handler.handleRequest(
        evento("{\"operacao\":\"getAllUsers\",\"ativo\":false}"), context);

    assertEquals(200, resposta.get("statusCode"));
    assertEquals(List.of("buscarTodos:false"), repository.chamadas);
  }

  @Test
  void getAllUsersDevolveBodyJson() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    usuario.setPerfil("admin");
    usuario.setAtivo(true);
    repository.usuarios = List.of(usuario);

    Map<String, Object> resposta = handler.handleRequest(
        evento("{\"operacao\":\"getAllUsers\"}"), context);

    assertEquals(200, resposta.get("statusCode"));
    String body = (String) resposta.get("body");
    JsonNode json = new ObjectMapper().readTree(body);
    assertEquals("admin", json.get(0).get("perfil").asText());
    assertFalse(body.contains("Usuario@"));
  }

  @Test
  void getAllUsersSemAtivoPassaNulo() {
    handler.handleRequest(evento("{\"operacao\":\"getAllUsers\"}"), context);

    assertEquals(List.of("buscarTodos:null"), repository.chamadas);
  }

  @Test
  void erroDoRepositorioRetornaErro500() {
    repository.erro = new RuntimeException("falha no banco");

    Map<String, Object> resposta = handler.handleRequest(
        evento("{\"operacao\":\"registrar\",\"perfil\":\"gratuito\"}"), context);

    assertEquals(500, resposta.get("statusCode"));
    assertTrue(resposta.get("body").toString().contains("falha no banco"));
  }

  @Test
  void invocacaoDiretaAceitaOperacaoEParamsNoTopo() {
    Map<String, Object> evento = new HashMap<>();
    evento.put("operacao", "getAllUsers");
    evento.put("params", Map.of("ativo", "true"));

    Map<String, Object> resposta = handler.handleRequest(evento, context);

    assertEquals(200, resposta.get("statusCode"));
    assertEquals(List.of("buscarTodos:true"), repository.chamadas);
  }

  private static Map<String, Object> evento(String body) {
    Map<String, Object> evento = new HashMap<>();
    evento.put("httpMethod", "POST");
    evento.put("path", "/");
    if (body != null) {
      evento.put("body", body);
    }
    return evento;
  }

  private static Context contextoFake() {
    LambdaLogger logger = new LambdaLogger() {
      @Override
      public void log(String message) {
      }

      @Override
      public void log(byte[] message) {
      }
    };
    return (Context) Proxy.newProxyInstance(
        LambdaHandlerTest.class.getClassLoader(),
        new Class<?>[] { Context.class },
        (proxy, method, args) -> method.getName().equals("getLogger") ? logger : null);
  }
}
