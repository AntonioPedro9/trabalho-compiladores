package lumina;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lumina.lexer.Scanner;
import lumina.lexer.error.LexicalErrorReporter;
import lumina.lexer.token.Token;
import lumina.lexer.token.TokenType;

/**
 * Ponto de entrada CLI principal da linguagem Lumina. Recebe o caminho de um arquivo-fonte .lum,
 * realiza a varredura léxica e imprime a tabela de tokens e o relatório de erros com formatação
 * clara e alinhada.
 */
public final class Main {

  /** Construtor privado para evitar instanciação da classe utilitária. */
  private Main() {}

  /**
   * Ponto de entrada da aplicação via linha de comando.
   *
   * @param args argumentos de linha de comando contendo o caminho do arquivo-fonte
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      printUsage();
      return;
    }

    String filePathString = args[0];
    Path path = Paths.get(filePathString);

    if (!Files.exists(path)) {
      System.err.printf("Erro: Arquivo não encontrado: '%s'%n", filePathString);
      System.exit(1);
    }

    try {
      String source = Files.readString(path, StandardCharsets.UTF_8);
      tokenizeAndPrint(source, filePathString);
    } catch (IOException e) {
      System.err.printf("Erro de leitura no arquivo '%s': %s%n", filePathString, e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Executa a análise léxica do código-fonte e imprime a tabela de tokens e diagnósticos.
   *
   * @param source conteúdo textual do código-fonte
   * @param fileName nome ou caminho do arquivo analisado
   */
  public static void tokenizeAndPrint(String source, String fileName) {
    System.out.println(
        "================================================================================");
    System.out.printf("  ANALISADOR LÉXICO LUMINA — ARQUIVO: %s%n", fileName);
    System.out.println(
        "================================================================================");

    LexicalErrorReporter errorReporter = new LexicalErrorReporter();
    Scanner scanner = new Scanner(source, errorReporter);
    List<Token> tokens = scanner.scanAllTokens();

    System.out.printf(
        "%-10s | %-20s | %-30s | %s%n", "POSIÇÃO", "TIPO DE TOKEN", "LEXEMA", "CATEGORIA");
    System.out.println(
        "--------------------------------------------------------------------------------");

    for (Token token : tokens) {
      String category = getCategoryDescription(token.getType());
      String escapedLexeme = escapeString(token.getLexeme());
      System.out.printf(
          "%-10s | %-20s | %-30s | %s%n",
          token.getPosition(), token.getType().name(), "\"" + escapedLexeme + "\"", category);
    }

    System.out.println(
        "--------------------------------------------------------------------------------");
    System.out.printf("Total de Tokens Identificados: %d%n", tokens.size());

    if (errorReporter.hasErrors()) {
      System.out.println("\n[!] FORAM DETECTADOS ERROS LÉXICOS:");
      errorReporter.printReport(System.err);
      System.exit(1);
    } else {
      System.out.println(
          "[✓] Varredura léxica concluída com 100% de sucesso. Nenhum erro detectado.");
    }
  }

  /**
   * Retorna a descrição amigável da categoria léxica de um tipo de token.
   *
   * @param type tipo do token a ser categorizado
   * @return descrição textual da categoria do token
   */
  private static String getCategoryDescription(TokenType type) {
    if (type.isKeyword()) return "Palavra Reservada";
    if (type.isLiteral()) return "Literal";
    if (type.isOperator()) return "Operador";
    if (type.isDelimiter()) return "Delimitador";
    if (type == TokenType.EOF) return "Fim de Arquivo";
    if (type == TokenType.ERROR) return "ERRO LÉXICO";
    return "Identificador";
  }

  /**
   * Escapa caracteres de controle para exibição formatada.
   *
   * @param text texto a ser escapado
   * @return texto com caracteres de escape formatados
   */
  private static String escapeString(String text) {
    return text.replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
        .replace("\"", "\\\"");
  }

  /** Imprime as instruções de uso e exemplos de execução na saída padrão. */
  private static void printUsage() {
    System.out.println("Uso:");
    System.out.println("  java lumina.Main <caminho-do-arquivo.lum>");
    System.out.println("  java lumina.lexer.Main <caminho-do-arquivo.lum>");
    System.out.println("\nExemplos:");
    System.out.println("  make run FILE=exemplo_fatorial.lum");
    System.out.println("  make run FILE=exemplo_com_erros.lum");
  }
}
