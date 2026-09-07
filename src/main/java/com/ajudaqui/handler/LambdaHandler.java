package com.ajudaqui.handler;

import java.util.HashMap;
import java.util.Map;

import com.ajudaqui.repository.UsuarioRepository;
import com.ajudaqui.resource.CuponFiscalResource;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LambdaHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
    try {
      context.getLogger().log("Recebido evento: " + event);

      String httpMethod = (String) event.get("httpMethod");
      String path = (String) event.get("path");
      String body = (String) event.get("body");

      context.getLogger().log("Método: " + httpMethod + " | Path: " + path);

      if (body == null || body.isEmpty()) {
        return erro(400, "Body é obrigatório");
      }

      Map<String, Object> payload = objectMapper.readValue(body, Map.class);

      String operacao = (String) payload.get("operacao");

      if (operacao == null || operacao.isEmpty()) {
        return erro(400, "Campo 'operacao' é obrigatório");
      }

      context.getLogger().log("Operação: " + operacao);

      CuponFiscalResource resource = new CuponFiscalResource(new UsuarioRepository());

      Object resultado = null;

      switch (operacao) {
        case "buscarCupon":
          resultado = resource.buscaCupon(
              (String) payload.get("token"),
              (String) payload.get("url"));
          break;

        case "registrar":
          resultado = resource.register(
              (String) payload.get("perfil"));
          break;

        case "mudarStatus":
          resultado = resource.mudarStatus(
              (String) payload.get("tokenSolicitante"),
              (String) payload.get("tokenUsuario"));
          break;

        case "mudatrPerfil":
          resultado = resource.mudatrPerfil(
              (String) payload.get("tokenSolicitante"),
              (String) payload.get("tokenUsuario"),
              (String) payload.get("perfil"));
          break;

        case "getAllUsers":
          Boolean ativo = payload.containsKey("ativo") ? (Boolean) payload.get("ativo") : null;
          resultado = resource.getAllUsers(ativo);
          break;

        default:
          return erro(400, "Operação desconhecida: " + operacao);
      }

      return sucesso(200, resultado);

    } catch (IllegalArgumentException e) {
      return erro(400, "Argumento inválido: " + e.getMessage());
    } catch (com.fasterxml.jackson.core.JsonParseException e) {
      return erro(400, "JSON inválido: " + e.getMessage());
    } catch (java.io.IOException e) {
      return erro(400, "Erro ao processar JSON: " + e.getMessage());
    } catch (RuntimeException e) {
      return erro(500, "Erro ao processar: " + e.getMessage());
    } catch (Exception e) {
      return erro(500, "Erro interno: " + e.getMessage());
    }
  }

  private Map<String, Object> sucesso(int statusCode, Object body) {
    Map<String, Object> response = new HashMap<>();
    response.put("statusCode", statusCode);
    response.put("body", body);
    response.put("headers", new HashMap<String, String>() {
      {
        put("Content-Type", "application/json");
      }
    });
    return response;
  }

  private Map<String, Object> erro(int statusCode, String mensagem) {
    Map<String, Object> response = new HashMap<>();
    response.put("statusCode", statusCode);
    response.put("body", "{\"error\": \"" + mensagem + "\"}");
    response.put("headers", new HashMap<String, String>() {
      {
        put("Content-Type", "application/json");
      }
    });
    return response;
  }
}
