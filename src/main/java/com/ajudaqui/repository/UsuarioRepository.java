package com.ajudaqui.repository;

import java.sql.*;
import java.util.*;

import com.ajudaqui.config.Database;
import com.ajudaqui.domain.Usuario;
import com.ajudaqui.enums.Query;

public class UsuarioRepository {

  public UUID criarUsuario(String perfil) {

    try (Connection connection = Database.getConnection();
        PreparedStatement statement = connection.prepareStatement(
            Query.REGISTAR_USUARIO.getQuery())) {

      statement.setString(1, perfil);

      try (ResultSet resultSet = statement.executeQuery()) {

        if (resultSet.next()) {
          return resultSet.getObject(1, UUID.class);
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage());
    }

    throw new RuntimeException("Não foi possível criar o usuário.");
  }

  public List<Usuario> buscarTodos(Boolean ativo) {

    List<Usuario> usuarios = new ArrayList<>();

    try (Connection connection = Database.getConnection();
        PreparedStatement statement = connection.prepareStatement(
            Query.BUSCAR_USUARIOS.getQuery())) {

      if (ativo != null) {
        statement.setBoolean(1, ativo);
      } else {
        statement.setNull(1, Types.BOOLEAN);
      }

      try (ResultSet resultSet = statement.executeQuery()) {

        while (resultSet.next()) {

          Usuario usuario = new Usuario();

          usuario.setId(resultSet.getLong("id"));
          usuario.setToken(resultSet.getObject("token", UUID.class));
          usuario.setRegistro(resultSet.getTimestamp("registro"));
          usuario.setUltimaSolicitacao(
              resultSet.getTimestamp("ultima_solicitacao"));
          usuario.setIntervalo(resultSet.getInt("intervalo"));
          usuario.setAtivo(resultSet.getBoolean("ativo"));

          usuarios.add(usuario);
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage());
    }

    return usuarios;
  }

  public void validarConsulta(String token, String estado) {
    try (Connection connection = Database.getConnection();
        PreparedStatement statement = connection.prepareStatement(
            Query.VALIDA_CONSULTA.getQuery())) {

      statement.setObject(1, UUID.fromString(token));
      statement.setString(2, estado);

      statement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public void alterarStatusUsuario(String tokenSolicitante, String tokenUsuario) {

    try (Connection connection = Database.getConnection();
        PreparedStatement statement = connection.prepareStatement(
            Query.ALTERNAR_STATUS.getQuery())) {

      statement.setObject(1, UUID.fromString(tokenSolicitante));
      statement.setObject(2, UUID.fromString(tokenUsuario));

      statement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public void mudatrPerfil(String tokenSolicitante, String tokenUsuario, String perfil) {

    try (Connection connection = Database.getConnection();
        PreparedStatement statement = connection.prepareStatement(
            Query.ALTERAR_PERFIL.getQuery())) {

      statement.setObject(1, UUID.fromString(tokenSolicitante));
      statement.setObject(2, UUID.fromString(tokenUsuario));
      statement.setObject(3, perfil);

      statement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
