package com.ajudaqui.domain;

import java.sql.Timestamp;
import java.util.UUID;

public class Usuario {
  private Long id;
  private UUID token;
  private Timestamp registro;
  private Timestamp ultimaSolicitacao;
  private Integer intervalo;
  private Boolean ativo;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UUID getToken() {
    return token;
  }

  public void setToken(UUID token) {
    this.token = token;
  }

  public Timestamp getRegistro() {
    return registro;
  }

  public void setRegistro(Timestamp registro) {
    this.registro = registro;
  }

  public Timestamp getUltimaSolicitacao() {
    return ultimaSolicitacao;
  }

  public void setUltimaSolicitacao(Timestamp ultimaSolicitacao) {
    this.ultimaSolicitacao = ultimaSolicitacao;
  }

  public Integer getIntervalo() {
    return intervalo;
  }

  public void setIntervalo(Integer intervalo) {
    this.intervalo = intervalo;
  }

  public Boolean getAtivo() {
    return ativo;
  }

  public void setAtivo(Boolean ativo) {
    this.ativo = ativo;
  }

}
