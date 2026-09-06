package com.ajudaqui.enums;

public enum Query {

  BUSCAR_USUARIOS("SELECT busca_usuarios(?)"),
  REGISTAR_USUARIO("SELECT criar_usuario(?)"),
  VALIDA_CONSULTA("CALL validador_consulta(?,?);"),

  ALTERNAR_STATUS("SELECT alternar_status(?,?)"),
  ALTERNAR_PERFIL("SELECT alternar_perfil(?,?,?)");

  private final String query;

  Query(String query) {
    this.query = query;
  }

  public String getQuery() {
    return query;
  }
}
