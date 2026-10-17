package com.ajudaqui.enums;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Garante que cada funcao/procedure chamada pelo {@link Query} esta definida
 * nos scripts de database/, evitando divergencia de nomes entre codigo e banco.
 */
class QueryTest {

  private static final Path DATABASE_DIR = Paths.get("database");
  private static final Pattern DEFINICAO = Pattern.compile(
      "(?i)create\\s+(?:or\\s+replace\\s+)?(?:function|procedure)\\s+(\\w+)");
  private static final Pattern CHAMADA = Pattern.compile("(?i)(?:from|select|call)\\s+(\\w+)\\s*\\(");

  @Test
  void todasAsChamadasExistemNosScriptsDeDatabase() throws IOException {
    Set<String> definidas = new HashSet<>();
    try (Stream<Path> arquivos = Files.list(DATABASE_DIR)) {
      for (Path arquivo : arquivos.filter(p -> p.toString().endsWith(".sql")).toList()) {
        Matcher m = DEFINICAO.matcher(Files.readString(arquivo));
        while (m.find()) {
          definidas.add(m.group(1).toLowerCase());
        }
      }
    }

    for (Query query : Query.values()) {
      Matcher m = CHAMADA.matcher(query.getQuery());
      assertTrue(m.find(), "Query sem chamada de funcao/procedure: " + query);
      String nome = m.group(1).toLowerCase();
      assertTrue(definidas.contains(nome),
          "Query " + query + " chama '" + nome + "' que nao esta definida em database/");
    }
  }
}
