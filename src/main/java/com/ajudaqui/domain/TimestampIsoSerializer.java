package com.ajudaqui.domain;

import java.io.IOException;
import java.sql.Timestamp;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

// Mantém a data e hora que o banco devolveu, sem conversão de fuso (ex.: 2026-10-17T02:29:04.249609)
public class TimestampIsoSerializer extends JsonSerializer<Timestamp> {

  @Override
  public void serialize(Timestamp value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeString(value.toLocalDateTime().toString());
  }
}
