package com.ajudaqui.service;

import java.util.List;

import com.ajudaqui.domain.Usuario;
import com.ajudaqui.model.UrlInput;

public class CuponFiscalService {

  private UsuarioRepository repository = new UsuarioRepository();
  private final FiscalParseService fiscalParseService = new FiscalParseService();

  public String buscaCupon(String token, String url) {
    repository.validarConsulta(token, "PE");
    return fiscalParseService.parseToJson(new UrlInput(url));
  }

  public List<Usuario> getAllUsers(Boolean ativo) {

    return repository.buscarTodos(ativo);
  }

  public String register(String perfil) {
    return repository.criarUsuario(perfil).toString();
  }

  public void setDelay(int delay) {
  };

  public void mudarStatus(boolean ativo) {
  }

}
