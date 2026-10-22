package com.ajudaqui.resource;

import java.util.List;

import com.ajudaqui.domain.Usuario;
import com.ajudaqui.model.UrlInput;
import com.ajudaqui.repository.UsuarioRepository;
import com.ajudaqui.service.FiscalParseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CuponFiscalResource {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private UsuarioRepository repository = new UsuarioRepository();
  private final FiscalParseService fiscalParseService = new FiscalParseService();

  public CuponFiscalResource(UsuarioRepository repository) {
    this.repository = repository;
  }

  public String buscaCupon(String token, String url) {
    repository.validarConsulta(token, "PE");
    return fiscalParseService.parseToJson(new UrlInput(url));
  }

  public String getAllUsers(Boolean ativo) {
    List<Usuario> usuarios = repository.buscarTodos(ativo);
    try {
      return objectMapper.writeValueAsString(usuarios);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Erro ao serializar usuários: " + e.getMessage(), e);
    }
  }

  public String register(String perfil) {
    return repository.criarUsuario(perfil).toString();
  }

  public String mudatrPerfil(String tokenSolicitante, String tokenUsuario, String perfil) {
    repository.mudatrPerfil(tokenSolicitante, tokenUsuario, perfil);
    return String.format("Usuario %s teve o perfil alteraro para %s com sucesso.", tokenUsuario, perfil);
  };

  public String mudarStatus( String tokenSolicitante, String tokenUsuario) {

    repository.alterarStatusUsuario(tokenSolicitante, tokenUsuario);
    return String.format("Modificação de status para o usuario %s  foi realizadao com sucesso", tokenUsuario);
  }

}
