package lumina.visitor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lumina.ast.BinNode;
import lumina.ast.BlockNode;
import lumina.ast.CallExprNode;
import lumina.ast.DoubleLitNode;
import lumina.ast.FunctionDeclNode;
import lumina.ast.IdNode;
import lumina.ast.ParamNode;
import lumina.ast.ProgramNode;
import lumina.ast.ReturnNode;
import lumina.ast.TypeNode;
import lumina.ast.VarDeclNode;
import lumina.common.source.SourcePosition;
import lumina.lexer.token.TokenType;
import lumina.parser.ParseResult;
import lumina.parser.Parser;
import lumina.symbols.IdentificationError;
import lumina.types.LuminaType;
import lumina.types.TypeError;
import lumina.types.TypeWarning;

/**
 * Suíte de testes do verificador de tipos, comparando os programas em {@code test/programas/*.txt}
 * com {@code test/esperado/*.txt} e validando a anotação de tipos e casos estruturais na AST.
 */
public final class TypeCheckerTest {

  private int testsPassed = 0;
  private int testsFailed = 0;

  public static void main(String[] args) throws IOException {
    if (args.length == 1) {
      String source = Files.readString(Paths.get(args[0]), StandardCharsets.UTF_8);
      System.out.print(runSemanticVerification(source));
      return;
    }
    TypeCheckerTest suite = new TypeCheckerTest();
    suite.runAllTests();
  }

  /** Executa a identificação e a verificação de tipos sobre o código-fonte informado. */
  public static String runSemanticVerification(String source) {
    ParseResult parsed = new Parser(source).parse();
    if (!hasValidAst(parsed)) {
      return "ERRO SINTÁTICO\n";
    }
    IdentificationVisitor identification = new IdentificationVisitor();
    identification.identify(parsed.ast());

    TypeCheckerVisitor typeChecker = new TypeCheckerVisitor();
    typeChecker.check(parsed.ast());

    if (!identification.hasErrors()) {
      return typeChecker.formatOutput();
    }
    return formatOrderedDiagnostics(collectSortedDiagnostics(identification, typeChecker));
  }

  public void runAllTests() {
    printHeader();

    testProgramAndExpectedTxtPairs();
    testAllExistingValidProgramsPassTypeChecking();
    testExpressionTypeAnnotationOnAstNodes();
    testNestedFunctionAstUsingEnclosingParameter();
    testNullSafetyAndNoDuplicateErrorOnUndeclaredIdentifiers();

    printSummary();
    evaluateExitStatus();
  }

  private void testProgramAndExpectedTxtPairs() {
    Path programsDir = Paths.get("test/programas");
    Path expectedDir = Paths.get("test/esperado");
    if (!Files.isDirectory(programsDir)) {
      return;
    }

    for (Path programFile : listSortedTxtFiles(programsDir)) {
      verifyProgramAgainstExpectedFile(programFile, expectedDir);
    }
  }

  private void verifyProgramAgainstExpectedFile(Path programFile, Path expectedDir) {
    String fileName = programFile.getFileName().toString();
    Path expectedFile = expectedDir.resolve(fileName);
    boolean expectedExists = Files.exists(expectedFile);
    assertTrue(
        "Arquivo esperado deve existir para " + fileName + " em test/esperado/" + fileName,
        expectedExists);
    if (!expectedExists) {
      return;
    }

    String source = readFile(programFile);
    String expected = normalizeNewlines(readFile(expectedFile)).trim();
    String actual = normalizeNewlines(runSemanticVerification(source)).trim();

    assertEqual(
        "Saída do verificador para test/programas/"
            + fileName
            + " deve coincidir com test/esperado/"
            + fileName,
        expected,
        actual);
  }

  private void testAllExistingValidProgramsPassTypeChecking() {
    String[] validFiles = {
      "exemplos/exemplo_fatorial.lum",
      "exemplos/exemplo_valido_tipos.lum",
      "exemplos/exemplo_valido_controle.lum",
      "exemplos/exemplo_valido_funcoes.lum",
      "test/programas/cp4_sombreamento_valido.lum",
      "test/programas/cp4_procedimento_recursivo.lum"
    };
    for (String filePath : validFiles) {
      TypeCheckerVisitor checker = parseIdentifyAndCheck(readFile(Paths.get(filePath)));
      assertFalse(
          "Programa válido '" + filePath + "' não deve ter erros de tipos", checker.hasErrors());
      assertEqual("Saída limpa para '" + filePath + "'", "OK\n", checker.formatOutput());
    }
  }

  private void testExpressionTypeAnnotationOnAstNodes() {
    String source = readFile(Paths.get("test/programas/cp5_valido_completo.txt"));
    ProgramNode ast = new Parser(source).parse().ast();
    new IdentificationVisitor().identify(ast);
    TypeCheckerVisitor checker = new TypeCheckerVisitor();
    checker.check(ast);

    assertFalse("Programa válido completo deve passar sem erros", checker.hasErrors());

    FunctionDeclNode calcDecl = (FunctionDeclNode) ast.getDeclarations().get(5);
    VarDeclNode acumuladoDecl = (VarDeclNode) calcDecl.getBody().getStatements().get(0);
    VarDeclNode bonusRestoDecl = (VarDeclNode) calcDecl.getBody().getStatements().get(1);
    assertEqual(
        "Expressão aritmética mista deve gravar tipo DOUBLE no nó",
        LuminaType.DOUBLE,
        acumuladoDecl.getInitializer().getType());
    assertEqual(
        "Expressão pontos % 3 deve gravar tipo INT no nó",
        LuminaType.INT, bonusRestoDecl.getInitializer().getType());
  }

  private void testNestedFunctionAstUsingEnclosingParameter() {
    SourcePosition line1 = new SourcePosition(1, 1);
    SourcePosition line2 = new SourcePosition(2, 5);
    SourcePosition line3 = new SourcePosition(3, 9);
    SourcePosition line5 = new SourcePosition(5, 5);

    ParamNode outerParam = new ParamNode(line1, new TypeNode(line1, TokenType.INT), "fatorExterno");
    ParamNode innerParam = new ParamNode(line2, new TypeNode(line2, TokenType.DOUBLE), "valor");
    BinNode innerMultiplication =
        new BinNode(
            line3, TokenType.STAR, new IdNode(line3, "fatorExterno"), new IdNode(line3, "valor"));
    FunctionDeclNode innerFunction =
        new FunctionDeclNode(
            line2,
            new TypeNode(line2, TokenType.DOUBLE),
            "escalarInterna",
            List.of(innerParam),
            new BlockNode(line2, List.of(new ReturnNode(line3, innerMultiplication))));

    CallExprNode callInner =
        new CallExprNode(line5, "escalarInterna", List.of(new DoubleLitNode(line5, 2.5, "2.5")));
    FunctionDeclNode outerFunction =
        new FunctionDeclNode(
            line1,
            new TypeNode(line1, TokenType.DOUBLE),
            "calcularExterna",
            List.of(outerParam),
            new BlockNode(line1, List.of(innerFunction, new ReturnNode(line5, callInner))));

    ProgramNode nestedProgram = new ProgramNode(line1, List.of(outerFunction));
    IdentificationVisitor idVisitor = new IdentificationVisitor();
    idVisitor.identify(nestedProgram);
    assertFalse("Identificação da função aninhada não deve ter erros", idVisitor.hasErrors());

    TypeCheckerVisitor nestedChecker = new TypeCheckerVisitor();
    nestedChecker.check(nestedProgram);
    assertFalse(
        "Tipagem da função aninhada usando variável do nível acima deve ser válida",
        nestedChecker.hasErrors());
    assertEqual(
        "fatorExterno (int) * valor (double) na função aninhada deve resultar em DOUBLE",
        LuminaType.DOUBLE,
        innerMultiplication.getType());
  }

  private void testNullSafetyAndNoDuplicateErrorOnUndeclaredIdentifiers() {
    TypeCheckerVisitor nullChecker = new TypeCheckerVisitor();
    assertEqual("check(null) deve retornar VOID sem NPE", LuminaType.VOID, nullChecker.check(null));

    String undeclaredSource = readFile(Paths.get("test/programas/cp4_nao_declarado.lum"));
    ProgramNode ast = new Parser(undeclaredSource).parse().ast();
    IdentificationVisitor idVisitor = new IdentificationVisitor();
    idVisitor.identify(ast);
    assertTrue("CP4 reporta os identificadores não declarados", idVisitor.hasErrors());

    TypeCheckerVisitor checker = new TypeCheckerVisitor();
    checker.check(ast);
    assertFalse(
        "CP5 não deve reportar novamente nem gerar cascata para identificadores não declarados",
        checker.hasErrors());
  }

  private TypeCheckerVisitor parseIdentifyAndCheck(String source) {
    ParseResult parsed = new Parser(source).parse();
    assertTrue("Programa de teste deve ser sintaticamente válido", parsed.isSuccessful());
    IdentificationVisitor identification = new IdentificationVisitor();
    identification.identify(parsed.ast());
    TypeCheckerVisitor checker = new TypeCheckerVisitor();
    checker.check(parsed.ast());
    return checker;
  }

  private static boolean hasValidAst(ParseResult parsed) {
    return parsed.isSuccessful() && parsed.hasAst();
  }

  private static List<OrderedDiagnostic> collectSortedDiagnostics(
      IdentificationVisitor identification, TypeCheckerVisitor typeChecker) {
    List<OrderedDiagnostic> diagnostics = new ArrayList<>();
    int insertionOrder = 0;
    for (IdentificationError error : identification.getErrors()) {
      diagnostics.add(new OrderedDiagnostic(error.getLine(), insertionOrder++, error.getMessage()));
    }
    for (TypeWarning warning : typeChecker.getWarnings()) {
      diagnostics.add(new OrderedDiagnostic(warning.line(), insertionOrder++, warning.message()));
    }
    for (TypeError error : typeChecker.getErrors()) {
      diagnostics.add(new OrderedDiagnostic(error.line(), insertionOrder++, error.message()));
    }
    diagnostics.sort(
        Comparator.comparingInt(OrderedDiagnostic::line)
            .thenComparingInt(OrderedDiagnostic::insertionOrder));
    return diagnostics;
  }

  private static String formatOrderedDiagnostics(List<OrderedDiagnostic> diagnostics) {
    StringBuilder outputBuilder = new StringBuilder();
    for (OrderedDiagnostic diagnostic : diagnostics) {
      outputBuilder.append(diagnostic.message()).append("\n");
    }
    return outputBuilder.toString();
  }

  private List<Path> listSortedTxtFiles(Path dir) {
    try (Stream<Path> stream = Files.list(dir)) {
      return stream
          .filter(Files::isRegularFile)
          .filter(this::isTxtFile)
          .sorted(Comparator.comparing(path -> path.getFileName().toString()))
          .toList();
    } catch (IOException e) {
      fail("Listagem de " + dir, "sucesso", e.getMessage());
      return List.of();
    }
  }

  private boolean isTxtFile(Path path) {
    return path.getFileName().toString().endsWith(".txt");
  }

  private String readFile(Path path) {
    try {
      return Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException e) {
      fail("Leitura do arquivo: " + path, "sucesso", e.getMessage());
      return "";
    }
  }

  private String normalizeNewlines(String text) {
    return text.replace("\r\n", "\n");
  }

  private void assertTrue(String description, boolean condition) {
    if (condition) {
      pass(description);
    } else {
      fail(description, "true", "false");
    }
  }

  private void assertFalse(String description, boolean condition) {
    if (!condition) {
      pass(description);
    } else {
      fail(description, "false", "true");
    }
  }

  private void assertEqual(String description, Object expected, Object actual) {
    if (Objects.equals(expected, actual)) {
      pass(description);
    } else {
      fail(description, String.valueOf(expected), String.valueOf(actual));
    }
  }

  private void pass(String description) {
    testsPassed++;
    System.out.printf("  [PASS] %s%n", description);
  }

  private void fail(String description, String expected, String actual) {
    testsFailed++;
    System.err.printf("  [FAIL] %s%n", description);
    System.err.printf("         Esperado : %s%n", expected);
    System.err.printf("         Obtido   : %s%n", actual);
  }

  private void printHeader() {
    System.out.println("==========================================================");
    System.out.println("  INICIANDO SUÍTE DE TESTES - VERIFICADOR DE TIPOS (CP5)  ");
    System.out.println("==========================================================");
  }

  private void printSummary() {
    System.out.println("----------------------------------------------------------");
    System.out.printf("Testes Concluídos: %d Aprovados, %d Falhas%n", testsPassed, testsFailed);
    System.out.println("==========================================================");
  }

  private void evaluateExitStatus() {
    if (testsFailed > 0) {
      System.err.printf("FALHA: %d teste(s) de Verificação de Tipos não passaram.%n", testsFailed);
      System.exit(1);
    } else {
      System.out.println("RESULTADO: TODOS OS TESTES DE TIPOS PASSARAM COM SUCESSO!");
    }
  }

  private record OrderedDiagnostic(int line, int insertionOrder, String message) {}
}
