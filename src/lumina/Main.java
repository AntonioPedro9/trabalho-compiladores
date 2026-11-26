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
import lumina.symbols.IdentificationError;
import lumina.types.TypeError;
import lumina.types.TypeWarning;
import lumina.visitor.AstPrinterVisitor;
import lumina.visitor.IdentificationVisitor;
import lumina.visitor.SourceReprinterVisitor;
import lumina.visitor.TypeCheckerVisitor;

/**
 * Ponto de entrada do compilador Lumina. Executa as fases de análise léxica, análise sintática,
 * identificação de símbolos e verificação de tipos.
 */
public final class Main {

  private static final String SECTION_DIVIDER =
      "--------------------------------------------------------------------------------";

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
    ParseResult syntaxResult = runSyntaxAndAstAnalysis(source);
    boolean syntaxOk = isValidAstProduced(syntaxResult);
    boolean identificationOk = false;
    boolean typeCheckOk = false;

    if (syntaxOk) {
      identificationOk = runIdentificationAnalysis(syntaxResult);
      typeCheckOk = runTypeCheckingAnalysis(syntaxResult);
    }

    printCompilationSummary(lexicalOk, syntaxOk, identificationOk, typeCheckOk);

    if (!areAllPhasesApproved(lexicalOk, syntaxOk, identificationOk, typeCheckOk)) {
      System.exit(1);
    }
  }

  public static boolean runLexicalAnalysis(String source) {
    printPhaseHeader("FASE 1: ANÁLISE LÉXICA");

    LexicalErrorReporter errorReporter = new LexicalErrorReporter();
    Scanner scanner = new Scanner(source, errorReporter);
    List<Token> tokens = scanner.scanAllTokens();

    System.out.printf(
        "%-10s | %-20s | %-30s | %s%n", "POSIÇÃO", "TIPO DE TOKEN", "LEXEMA", "CATEGORIA");
    System.out.println(SECTION_DIVIDER);

    for (Token token : tokens) {
      String category = getCategoryDescription(token.getType());
      String escapedLexeme = escapeString(token.getLexeme());
      System.out.printf(
          "%-10s | %-20s | %-30s | %s%n",
          token.getPosition(), token.getType().name(), "\"" + escapedLexeme + "\"", category);
    }

    System.out.println(SECTION_DIVIDER);
    System.out.printf("Total de Tokens Identificados: %d%n", tokens.size());

    if (errorReporter.hasErrors()) {
      System.err.println("\n[!] FORAM DETECTADOS ERROS LÉXICOS:");
      errorReporter.printReport(System.err);
      return false;
    }

    System.out.println("[✓] Análise léxica concluída sem erros.");
    return true;
  }

  public static ParseResult runSyntaxAndAstAnalysis(String source) {
    printPhaseHeader("FASE 2: ANÁLISE SINTÁTICA E CONSTRUÇÃO DA AST");

    SyntaxErrorReporter errorReporter = new SyntaxErrorReporter();
    Parser parser = new Parser(new Scanner(source), errorReporter);
    ParseResult result = parser.parse();

    if (!isValidAstProduced(result)) {
      System.err.println("\n[!] FORAM DETECTADOS ERROS SINTÁTICOS (AST inválida/descartada):");
      errorReporter.printReport(System.err);
      return result;
    }

    System.out.println("[✓] Análise sintática concluída com sucesso.\n");
    System.out.println("--- ÁRVORE DE SINTAXE ABSTRATA (AstPrinterVisitor) ---");
    AstPrinterVisitor printer = new AstPrinterVisitor();
    System.out.print(printer.print(result.ast()));

    System.out.println("\n--- CÓDIGO REIMPRESSO CANÔNICO (SourceReprinterVisitor) ---");
    SourceReprinterVisitor reprinter = new SourceReprinterVisitor();
    System.out.print(reprinter.reprint(result.ast()));
    return result;
  }

  public static boolean runIdentificationAnalysis(ParseResult syntaxResult) {
    printPhaseHeader("FASE 3: PASSADA DE IDENTIFICAÇÃO E TABELA DE SÍMBOLOS");

    IdentificationVisitor identification = new IdentificationVisitor();
    identification.identify(syntaxResult.ast());

    System.out.println("--- AST DECORADA (AstPrinterVisitor com resolução nome@linha) ---");
    AstPrinterVisitor printer = new AstPrinterVisitor();
    System.out.print(printer.print(syntaxResult.ast()));

    if (identification.hasErrors()) {
      System.err.println("\n[!] FORAM DETECTADOS ERROS DE IDENTIFICAÇÃO:");
      for (IdentificationError error : identification.getErrors()) {
        System.err.println("  " + error.getMessage());
      }
      return false;
    }

    System.out.println("\n[✓] Passada de identificação concluída sem erros.");
    return true;
  }

  public static boolean runTypeCheckingAnalysis(ParseResult syntaxResult) {
    printPhaseHeader("FASE 4: VERIFICAÇÃO DE TIPOS (CP5)");

    TypeCheckerVisitor typeChecker = new TypeCheckerVisitor();
    typeChecker.check(syntaxResult.ast());

    System.out.println("--- AST TIPADA (AstPrinterVisitor com resolução nome@linha e tipos) ---");
    AstPrinterVisitor printer = new AstPrinterVisitor();
    System.out.print(printer.printTyped(syntaxResult.ast()));

    if (typeChecker.hasWarnings()) {
      printTypeWarnings(typeChecker.getWarnings());
    }

    if (typeChecker.hasErrors()) {
      printTypeErrors(typeChecker.getErrors());
      return false;
    }

    System.out.println("\n[✓] Verificação de tipos concluída sem erros.");
    return true;
  }

  private static boolean isValidAstProduced(ParseResult result) {
    return result.isSuccessful() && result.hasAst();
  }

  private static boolean areAllPhasesApproved(
      boolean lexicalOk, boolean syntaxOk, boolean identificationOk, boolean typeCheckOk) {
    return lexicalOk && syntaxOk && identificationOk && typeCheckOk;
  }

  private static void printTypeWarnings(List<TypeWarning> warnings) {
    System.out.println("\n[!] AVISOS SEMÂNTICOS (análise conservadora de inicialização):");
    for (TypeWarning warning : warnings) {
      System.out.println("  " + warning.message());
    }
  }

  private static void printTypeErrors(List<TypeError> errors) {
    System.err.println("\n[!] FORAM DETECTADOS ERROS DE TIPOS:");
    for (TypeError error : errors) {
      System.err.println("  " + error.message());
    }
  }

  private static void printCompilationSummary(
      boolean lexicalOk, boolean syntaxOk, boolean identificationOk, boolean typeCheckOk) {
    System.out.println(
        "\n================================================================================");
    System.out.println(
        "                         RESUMO DA COMPILAÇÃO                                   ");
    System.out.println(
        "================================================================================");
    System.out.printf("  Status da Análise Léxica        : %s%n", formatPhaseVerdict(lexicalOk));
    System.out.printf("  Status da Análise Sintática     : %s%n", formatPhaseVerdict(syntaxOk));
    System.out.printf(
        "  Status da Identificação (CP4)   : %s%n",
        formatDependentPhaseStatus(syntaxOk, identificationOk));
    System.out.printf(
        "  Status da Tipagem (CP5)         : %s%n",
        formatDependentPhaseStatus(syntaxOk, typeCheckOk));
    System.out.println(
        "================================================================================");
    if (areAllPhasesApproved(lexicalOk, syntaxOk, identificationOk, typeCheckOk)) {
      System.out.println(">>> PROGRAMA COMPILADO COM SUCESSO! PRONTO PARA GERAÇÃO DE CÓDIGO. <<<");
    } else {
      System.err.println(
          ">>> PROGRAMA COM ERROS. COMPILAÇÃO INTERROMPIDA ANTES DA GERAÇÃO DE CÓDIGO. <<<");
    }
  }

  private static String formatDependentPhaseStatus(boolean prerequisiteOk, boolean phaseOk) {
    if (!prerequisiteOk) {
      return "[-] NÃO EXECUTADA";
    }
    return formatPhaseVerdict(phaseOk);
  }

  private static String formatPhaseVerdict(boolean passed) {
    return passed ? "[✓] APROVADO" : "[X] REJEITADO";
  }

  private static void printPhaseHeader(String title) {
    System.out.println("\n" + SECTION_DIVIDER);
    System.out.println("  " + title);
    System.out.println(SECTION_DIVIDER);
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
    System.out.println("  make run FILE=test/programas/cp4_sombreamento_valido.lum");
  }
}
