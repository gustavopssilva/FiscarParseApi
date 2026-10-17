package com.ajudaqui.resource;

import java.util.List;
import java.util.stream.Collectors;

import com.ajudaqui.domain.Usuario;
import com.ajudaqui.model.UrlInput;
import com.ajudaqui.repository.UsuarioRepository;
import com.ajudaqui.service.FiscalParseService;

public class CuponFiscalResource {

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
    if (usuarios.isEmpty()) {
      return "Nenhum usuário encontrado.";
    }
    return usuarios.stream()
        .map(CuponFiscalResource::formatarUsuario)
        .collect(Collectors.joining("\n"));
  }

  private static String formatarUsuario(Usuario usuario) {
    return String.format(
        "id=%s token=%s perfil=%s intervalo=%s ativo=%s registro=%s ultima_solicitacao=%s",
        usuario.getId(), usuario.getToken(), usuario.getPerfil(), usuario.getIntervalo(),
        usuario.getAtivo(), usuario.getRegistro(), usuario.getUltimaSolicitacao());
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
