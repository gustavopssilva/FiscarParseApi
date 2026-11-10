package com.ajudaqui.client;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

public class FiscalLambdaClient implements AutoCloseable {

  private final LambdaClient lambda;
  private final String functionName;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public FiscalLambdaClient(String functionName, Region region) {
    this.functionName = functionName;
    this.lambda = LambdaClient.builder().region(region).build();
  }

  /**
   * Invoca a Lambda e devolve somente o campo "body" da resposta.
   * Lança exceção se houver erro de execução ou statusCode diferente de 200.
   */
  public JsonNode invocar(Map<String, Object> payload) throws IOException {
    InvokeRequest request = InvokeRequest.builder()
        .functionName(functionName)
        .payload(SdkBytes.fromUtf8String(objectMapper.writeValueAsString(payload)))
        .build();

    InvokeResponse response = lambda.invoke(request);
    String json = response.payload().asUtf8String();

    if (response.functionError() != null) {
      throw new IllegalStateException("Erro na execução da Lambda: " + json);
    }

    JsonNode envelope = objectMapper.readTree(json);
    int statusCode = envelope.path("statusCode").asInt();

    if (statusCode != 200) {
      throw new IllegalStateException("Lambda respondeu " + statusCode + ": " + envelope.path("body"));
    }

    return envelope.path("body");
  }

  @Override
  public void close() {
    lambda.close();
  }
}
