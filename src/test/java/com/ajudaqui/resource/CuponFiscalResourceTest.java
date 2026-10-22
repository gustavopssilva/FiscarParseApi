package com.ajudaqui.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ajudaqui.domain.Usuario;
import com.ajudaqui.support.FakeUsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class CuponFiscalResourceTest {

  private static final String TOKEN_ADMIN = "550e8400-e29b-41d4-a716-446655440002";
  private static final String TOKEN_USUARIO = "f8b9e295-bbd8-4cfb-8795-9dc70d1b32f5";

  private FakeUsuarioRepository repository;
  private CuponFiscalResource resource;

  @BeforeEach
  void setUp() {
    repository = new FakeUsuarioRepository();
    resource = new CuponFiscalResource(repository);
  }

  @Test
  void getAllUsersRepassaFiltroAtivoAoRepositorio() {
    repository.usuarios = List.of(usuario(1L, "admin", true));

    String resultado = resource.getAllUsers(true);

    assertEquals(List.of("buscarTodos:true"), repository.chamadas);
    assertTrue(resultado.startsWith("[{"));
    assertTrue(resultado.contains("\"perfil\":\"admin\""));
  }

  @Test
  void getAllUsersAceitaFiltroNulo() {
    resource.getAllUsers(null);

    assertEquals(List.of("buscarTodos:null"), repository.chamadas);
  }

  @Test
  void getAllUsersDevolveArrayJson() throws Exception {
    repository.usuarios = List.of(usuario(1L, "admin", true), usuario(2L, "gratuito", false));

    String resultado = resource.getAllUsers(null);

    JsonNode json = new ObjectMapper().readTree(resultado);
    assertTrue(json.isArray());
    assertEquals(2, json.size());
    assertEquals("admin", json.get(0).get("perfil").asText());
    assertEquals("gratuito", json.get(1).get("perfil").asText());
    assertFalse(json.get(1).get("ativo").asBoolean());
  }

  @Test
  void getAllUsersFormataDatasEmIso8601() throws Exception {
    Usuario usuario = usuario(1L, "admin", true);
    usuario.setRegistro(Timestamp.valueOf("2026-10-17 02:29:04.249609"));
    repository.usuarios = List.of(usuario);

    JsonNode json = new ObjectMapper().readTree(resource.getAllUsers(null));

    assertEquals("2026-10-17T02:29:04.249609", json.get(0).get("registro").asText());
    assertTrue(json.get(0).get("ultimaSolicitacao").isNull());
  }

  @Test
  void getAllUsersSemResultadosDevolveArrayVazio() {
    assertEquals("[]", resource.getAllUsers(null));
  }

  private static Usuario usuario(Long id, String perfil, boolean ativo) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setToken(UUID.fromString(TOKEN_USUARIO));
    usuario.setPerfil(perfil);
    usuario.setIntervalo(1000);
    usuario.setAtivo(ativo);
    return usuario;
  }

  @Test
  void registerDevolveTokenCriadoComoString() {
    repository.tokenCriado = UUID.fromString(TOKEN_USUARIO);

    String token = resource.register("gratuito");

    assertEquals(List.of("criarUsuario:gratuito"), repository.chamadas);
    assertEquals(TOKEN_USUARIO, token);
  }

  @Test
  void mudarStatusDelegaAoRepositorioEInformaSucesso() {
    String mensagem = resource.mudarStatus(TOKEN_ADMIN, TOKEN_USUARIO);

    assertEquals(List.of("alterarStatusUsuario:" + TOKEN_ADMIN + ":" + TOKEN_USUARIO), repository.chamadas);
    assertTrue(mensagem.contains(TOKEN_USUARIO));
  }

  @Test
  void mudatrPerfilDelegaAoRepositorioEInformaSucesso() {
    String mensagem = resource.mudatrPerfil(TOKEN_ADMIN, TOKEN_USUARIO, "pago_01");

    assertEquals(
        List.of("mudatrPerfil:" + TOKEN_ADMIN + ":" + TOKEN_USUARIO + ":pago_01"),
        repository.chamadas);
    assertTrue(mensagem.contains("pago_01"));
  }

  @Test
  void buscaCupomValidaConsultaAntesDeChamarOParser() {
    repository.erro = new RuntimeException("Aguarde o intervalo minimo");

    RuntimeException e = assertThrows(RuntimeException.class,
        () -> resource.buscaCupon(TOKEN_USUARIO, "https://exemplo.com/cupom"));

    assertEquals("Aguarde o intervalo minimo", e.getMessage());
    assertEquals(List.of("validarConsulta:" + TOKEN_USUARIO + ":PE"), repository.chamadas);
  }
}
