package com.ajudaqui.support;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ajudaqui.domain.Usuario;
import com.ajudaqui.repository.UsuarioRepository;

/**
 * Repositorio falso: grava as chamadas recebidas e devolve valores configurados,
 * sem acessar o banco.
 */
public class FakeUsuarioRepository extends UsuarioRepository {

  public final List<String> chamadas = new ArrayList<>();
  public UUID tokenCriado = UUID.randomUUID();
  public List<Usuario> usuarios = new ArrayList<>();
  public RuntimeException erro;

  @Override
  public UUID criarUsuario(String perfil) {
    chamadas.add("criarUsuario:" + perfil);
    lancarErroSeConfigurado();
    return tokenCriado;
  }

  @Override
  public List<Usuario> buscarTodos(Boolean ativo) {
    chamadas.add("buscarTodos:" + ativo);
    lancarErroSeConfigurado();
    return usuarios;
  }

  @Override
  public void validarConsulta(String token, String estado) {
    chamadas.add("validarConsulta:" + token + ":" + estado);
    lancarErroSeConfigurado();
  }

  @Override
  public void alterarStatusUsuario(String tokenSolicitante, String tokenUsuario) {
    chamadas.add("alterarStatusUsuario:" + tokenSolicitante + ":" + tokenUsuario);
    lancarErroSeConfigurado();
  }

  @Override
  public void mudatrPerfil(String tokenSolicitante, String tokenUsuario, String perfil) {
    chamadas.add("mudatrPerfil:" + tokenSolicitante + ":" + tokenUsuario + ":" + perfil);
    lancarErroSeConfigurado();
  }

  private void lancarErroSeConfigurado() {
    if (erro != null) {
      throw erro;
    }
  }
}
