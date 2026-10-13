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
import lumina.parser.ParseResult;
import lumina.parser.Parser;
import lumina.parser.error.SyntaxErrorReporter;

/**
 * Ponto de entrada do compilador Lumina. Executa a análise léxica e a análise sintática de um
 * arquivo-fonte.
 */
public final class Main {

  private Main() {}

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
      compileAndReport(source, filePathString);
    } catch (IOException e) {
      System.err.printf("Erro de leitura no arquivo '%s': %s%n", filePathString, e.getMessage());
      System.exit(1);
    }
  }

  public static void compileAndReport(String source, String fileName) {
    printBanner("COMPILADOR LUMINA: " + fileName);

    boolean lexicalOk = runLexicalAnalysis(source);
    boolean syntaxOk = runSyntaxAnalysis(source);

    printCompilationSummary(lexicalOk, syntaxOk);

    if (!lexicalOk || !syntaxOk) {
      System.exit(1);
    }
  }

  public static boolean runLexicalAnalysis(String source) {
    System.out.println(
        "\n--------------------------------------------------------------------------------");
    System.out.println("  FASE 1: ANÁLISE LÉXICA");
    System.out.println(
        "--------------------------------------------------------------------------------");

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
      System.err.println("\n[!] FORAM DETECTADOS ERROS LÉXICOS:");
      errorReporter.printReport(System.err);
      return false;
    }

    System.out.println("[✓] Análise léxica concluída sem erros.");
    return true;
  }

  public static boolean runSyntaxAnalysis(String source) {
    System.out.println(
        "\n--------------------------------------------------------------------------------");
    System.out.println("  FASE 2: ANÁLISE SINTÁTICA");
    System.out.println(
        "--------------------------------------------------------------------------------");

    SyntaxErrorReporter errorReporter = new SyntaxErrorReporter();
    Parser parser = new Parser(new Scanner(source), errorReporter);
    ParseResult result = parser.parse();

    if (result.hasErrors()) {
      System.err.println("\n[!] FORAM DETECTADOS ERROS SINTÁTICOS:");
      errorReporter.printReport(System.err);
      return false;
    }

    System.out.println("[✓] Análise sintática concluída com sucesso.");
    return true;
  }

  private static void printCompilationSummary(boolean lexicalOk, boolean syntaxOk) {
    System.out.println(
        "\n================================================================================");
    System.out.println(
        "                         RESUMO DA COMPILAÇÃO                                   ");
    System.out.println(
        "================================================================================");
    System.out.printf(
        "  Status da Análise Léxica   : %s%n", lexicalOk ? "[✓] APROVADO" : "[X] REJEITADO");
    System.out.printf(
        "  Status da Análise Sintática: %s%n", syntaxOk ? "[✓] APROVADO" : "[X] REJEITADO");
    System.out.println(
        "================================================================================");
    if (lexicalOk && syntaxOk) {
      System.out.println(">>> PROGRAMA COMPILADO COM SUCESSO! <<<");
    } else {
      System.err.println(">>> PROGRAMA COM ERROS. REVISE OS PROBLEMAS ACIMA. <<<");
    }
  }

  private static void printBanner(String title) {
    System.out.println(
        "================================================================================");
    System.out.printf("  %s%n", title);
    System.out.println(
        "================================================================================");
  }

  private static String getCategoryDescription(TokenType type) {
    if (type.isKeyword()) return "Palavra Reservada";
    if (type.isLiteral()) return "Literal";
    if (type.isOperator()) return "Operador";
    if (type.isDelimiter()) return "Delimitador";
    if (type == TokenType.EOF) return "Fim de Arquivo";
    if (type == TokenType.ERROR) return "ERRO LÉXICO";
    return "Identificador";
  }

  private static String escapeString(String text) {
    return text.replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
        .replace("\"", "\\\"");
  }

  private static void printUsage() {
    System.out.println("Uso:");
    System.out.println("  java lumina.Main <caminho-do-arquivo.lum>");
    System.out.println("\nExemplos:");
    System.out.println("  make run FILE=exemplos/exemplo_fatorial.lum");
    System.out.println("  make run FILE=exemplos/exemplo_com_erros.lum");
  }
}
