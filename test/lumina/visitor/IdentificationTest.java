package lumina.visitor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import lumina.ast.AssignNode;
import lumina.ast.BinNode;
import lumina.ast.BlockNode;
import lumina.ast.CallStmtNode;
import lumina.ast.FunctionDeclNode;
import lumina.ast.IdNode;
import lumina.ast.IfNode;
import lumina.ast.ProgramNode;
import lumina.ast.ReturnNode;
import lumina.ast.VarDeclNode;
import lumina.ast.WhileNode;
import lumina.lexer.token.TokenType;
import lumina.parser.ParseResult;
import lumina.parser.Parser;
import lumina.symbols.IdentificationError;
import lumina.symbols.Symbol;
import lumina.symbols.SymbolCategory;

/**
 * Suíte de testes da Passada de Identificação e Decoração da AST (Checkpoint 4 — Seção 5). Valida
 * os três programas obrigatórios contra {@code test/esperado/}, as cinco decisões de projeto,
 * múltiplos erros na ordem do texto, procedimento recursivo e invisibilidade após o fechamento de
 * bloco.
 */
public final class IdentificationTest {

  private int testsPassed = 0;
  private int testsFailed = 0;

  public static void main(String[] args) {
    IdentificationTest suite = new IdentificationTest();
    suite.runAllTests();
  }

  public void runAllTests() {
    printHeader();

    // 1. Três programas obrigatórios conferidos contra test/esperado/
    testMandatoryValidShadowingProgramMatchesExpected();
    testMandatoryRedeclarationProgramMatchesExpected();
    testMandatoryUndeclaredProgramMatchesExpected();

    // 2. Verificação direta dos ponteiros decl e Symbol.type / Symbol.level na AST decorada
    testAstDecorationPointersAndSymbolAttributes();

    // 3. Um programa por decisão de projeto (Decisões 1 a 5)
    testDecision1ShadowingVsSameScopeRedeclaration();
    testDecision2DeclareBeforeUseAndSelfInitializer();
    testDecision3MutualRecursionDisallowed();
    testDecision4ParamAndFunctionBodyShareScope();
    testDecision5SingleNamespaceForVariablesAndFunctions();

    // 4. Pelo menos 3 erros numa rodada na ordem do texto, procedimento recursivo e pós-bloco
    testAtLeastThreeErrorsReportedInSourceOrder();
    testRecursiveProcedureResolvesToItself();
    testInnerVariableInvisibleAfterBlockCloses();

    // 5. Robustez (AST nula / programas com erro sintático não lançam NPE)
    testNullAstSafety();

    printSummary();
    evaluateExitStatus();
  }

  private void testMandatoryValidShadowingProgramMatchesExpected() {
    String source = readFile("test/programas/cp4_sombreamento_valido.lum");
    String expected = readFile("test/esperado/cp4_sombreamento_valido.txt");

    ParseResult parsed = new Parser(source).parse();
    assertTrue(
        "Programa válido com sombreamento deve fazer parse sem erros", parsed.isSuccessful());

    IdentificationVisitor visitor = new IdentificationVisitor();
    visitor.identify(parsed.ast());

    assertFalse(
        "Programa válido com sombreamento não deve ter erros de identificação",
        visitor.hasErrors());
    assertEqual(
        "Saída decorada de cp4_sombreamento_valido.lum deve coincidir com"
            + " test/esperado/cp4_sombreamento_valido.txt",
        normalizeNewlines(expected),
        normalizeNewlines(visitor.formatOutput(parsed.ast())));
  }

  private void testMandatoryRedeclarationProgramMatchesExpected() {
    String source = readFile("test/programas/cp4_redeclaracao.lum");
    String expected = readFile("test/esperado/cp4_redeclaracao.txt");

    ParseResult parsed = new Parser(source).parse();
    assertTrue("Programa de redeclaração deve ter sintaxe válida", parsed.isSuccessful());

    IdentificationVisitor visitor = new IdentificationVisitor();
    visitor.identify(parsed.ast());

    assertTrue("Programa de redeclaração deve reportar erros", visitor.hasErrors());
    assertEqual(
        "Saída de cp4_redeclaracao.lum deve coincidir com test/esperado/cp4_redeclaracao.txt",
        normalizeNewlines(expected),
        normalizeNewlines(visitor.formatOutput(parsed.ast())));
  }

  private void testMandatoryUndeclaredProgramMatchesExpected() {
    String source = readFile("test/programas/cp4_nao_declarado.lum");
    String expected = readFile("test/esperado/cp4_nao_declarado.txt");

    ParseResult parsed = new Parser(source).parse();
    assertTrue("Programa com nome não declarado deve ter sintaxe válida", parsed.isSuccessful());

    IdentificationVisitor visitor = new IdentificationVisitor();
    visitor.identify(parsed.ast());

    assertTrue("Programa com nome não declarado deve reportar erros", visitor.hasErrors());
    assertEqual(
        "Saída de cp4_nao_declarado.lum deve coincidir com test/esperado/cp4_nao_declarado.txt",
        normalizeNewlines(expected),
        normalizeNewlines(visitor.formatOutput(parsed.ast())));
  }

  private void testAstDecorationPointersAndSymbolAttributes() {
    String source = readFile("test/programas/cp4_sombreamento_valido.lum");
    ProgramNode ast = new Parser(source).parse().ast();
    IdentificationVisitor visitor = new IdentificationVisitor();
    visitor.identify(ast);

    VarDeclNode globalTaxaDecl = (VarDeclNode) ast.getDeclarations().get(0);
    FunctionDeclNode funcDecl = (FunctionDeclNode) ast.getDeclarations().get(1);
    BlockNode funcBody = funcDecl.getBody();

    WhileNode whileNode = (WhileNode) funcBody.getStatements().get(1);
    VarDeclNode localTaxaDecl = (VarDeclNode) whileNode.getBody().getStatements().get(0);
    AssignNode assignBaseInWhile = (AssignNode) whileNode.getBody().getStatements().get(2);
    BinNode baseMinusTaxa = (BinNode) assignBaseInWhile.getValue();
    IdNode taxaInsideWhile = (IdNode) baseMinusTaxa.getRight();

    ReturnNode returnNode = (ReturnNode) funcBody.getStatements().get(2);
    BinNode totalPlusTaxa = (BinNode) returnNode.getExpression();
    IdNode taxaAfterWhile = (IdNode) totalPlusTaxa.getRight();

    Symbol innerSymbol = taxaInsideWhile.getDecl();
    Symbol outerSymbol = taxaAfterWhile.getDecl();

    assertTrue("Ponteiro decl no uso interno não deve ser nulo", innerSymbol != null);
    assertSame(
        "Uso de 'taxa' dentro do while deve apontar para o VarDeclNode da linha 6",
        localTaxaDecl,
        innerSymbol.getDeclarationNode());
    assertEqual("Nível de taxa@6 deve ser 2", 2, innerSymbol.getLevel());
    assertEqual(
        "Tipo reservado em taxa@6 (i.decl.type) deve ser INT",
        TokenType.INT,
        taxaInsideWhile.decl.type);

    assertTrue("Ponteiro decl no uso após fechar o while não deve ser nulo", outerSymbol != null);
    assertSame(
        "Uso de 'taxa' na linha 13 deve apontar para o VarDeclNode global da linha 1",
        globalTaxaDecl,
        outerSymbol.getDeclarationNode());
    assertEqual("Nível de taxa@1 deve ser 0", 0, outerSymbol.getLevel());
  }

  private void testDecision1ShadowingVsSameScopeRedeclaration() {
    String source =
        """
        int valor = 100;
        fun void testar() {
            int valor = 50;
            int valor = 60;
            if (valor > 0) {
                int valor = 10;
            }
        }
        """;
    ProgramNode ast = new Parser(source).parse().ast();
    IdentificationVisitor visitor = new IdentificationVisitor();
    visitor.identify(ast);

    List<IdentificationError> errors = visitor.getErrors();
    assertEqual("Decisão 1: deve reportar exatamente 1 erro (na linha 4)", 1, errors.size());
    assertEqual(
        "Decisão 1: mensagem exata de redeclaração na linha 4",
        "linha 4: 'valor' já declarado neste escopo (linha 3)",
        errors.get(0).getMessage());
  }

  private void testDecision2DeclareBeforeUseAndSelfInitializer() {
    String source = readFile("test/programas/cp4_decisao2_declarar_antes_de_usar.lum");
    ProgramNode ast = new Parser(source).parse().ast();
    IdentificationVisitor visitor = new IdentificationVisitor();
    visitor.identify(ast);

    List<IdentificationError> errors = visitor.getErrors();
    assertEqual("Decisão 2: deve reportar 2 erros de uso antes da declaração", 2, errors.size());
    assertEqual(
        "Decisão 2 (1º erro): 'b' usado antes de declarar",
        "linha 2: 'b' não declarado",
        errors.get(0).getMessage());
    assertEqual(
        "Decisão 2 (2º erro): 'c' usado no próprio inicializador antes de declarar",
        "linha 4: 'c' não declarado",
        errors.get(1).getMessage());
  }

  private void testDecision3MutualRecursionDisallowed() {
    String source = readFile("test/programas/cp4_decisao3_recursao_mutua.lum");
    ProgramNode ast = new Parser(source).parse().ast();
    IdentificationVisitor visitor = new IdentificationVisitor();
    visitor.identify(ast);

    List<IdentificationError> errors = visitor.getErrors();
    assertEqual("Decisão 3: chamada antecipada a 'impar' deve gerar 1 erro", 1, errors.size());
    assertEqual(
        "Decisão 3: mensagem deve indicar 'impar' não declarado na linha 5",
        "linha 5: 'impar' não declarado",
        errors.get(0).getMessage());
  }

  private void testDecision4ParamAndFunctionBodyShareScope() {
    String source = readFile("test/programas/cp4_decisao4_parametro_corpo.lum");
    ProgramNode ast = new Parser(source).parse().ast();
    IdentificationVisitor visitor = new IdentificationVisitor();
    visitor.identify(ast);

    List<IdentificationError> errors = visitor.getErrors();
    assertEqual(
        "Decisão 4: variável local no topo da função colidindo com parâmetro deve gerar 1 erro",
        1,
        errors.size());
    assertEqual(
        "Decisão 4: mensagem deve apontar conflito com o parâmetro da linha 1",
        "linha 2: 'a' já declarado neste escopo (linha 1)",
        errors.get(0).getMessage());
  }

  private void testDecision5SingleNamespaceForVariablesAndFunctions() {
    String source = readFile("test/programas/cp4_decisao5_espaco_nomes.lum");
    ProgramNode ast = new Parser(source).parse().ast();
    IdentificationVisitor visitor = new IdentificationVisitor();
    visitor.identify(ast);

    List<IdentificationError> errors = visitor.getErrors();
    assertEqual(
        "Decisão 5: função global com mesmo nome de variável global deve gerar 1 erro",
        1,
        errors.size());
    assertEqual(
        "Decisão 5: mensagem de conflito no espaço de nomes único",
        "linha 3: 'conflito' já declarado neste escopo (linha 1)",
        errors.get(0).getMessage());
  }

  private void testAtLeastThreeErrorsReportedInSourceOrder() {
    String source = readFile("test/programas/cp4_multiplos_erros.lum");
    ProgramNode ast = new Parser(source).parse().ast();
    IdentificationVisitor visitor = new IdentificationVisitor();
    visitor.identify(ast);

    List<IdentificationError> errors = visitor.getErrors();
    assertEqual("Múltiplos erros: deve acumular os 4 erros na rodada", 4, errors.size());
    assertEqual(
        "Erro #1 (linha 2)",
        "linha 2: 'a' já declarado neste escopo (linha 1)",
        errors.get(0).getMessage());
    assertEqual(
        "Erro #2 (linha 4)",
        "linha 4: 'p' já declarado neste escopo (linha 4)",
        errors.get(1).getMessage());
    assertEqual("Erro #3 (linha 5)", "linha 5: 'b' não declarado", errors.get(2).getMessage());
    assertEqual("Erro #4 (linha 6)", "linha 6: 'd' não declarado", errors.get(3).getMessage());
  }

  private void testRecursiveProcedureResolvesToItself() {
    String source = readFile("test/programas/cp4_procedimento_recursivo.lum");
    ProgramNode ast = new Parser(source).parse().ast();
    IdentificationVisitor visitor = new IdentificationVisitor();
    visitor.identify(ast);

    assertFalse("Procedimento recursivo válido não deve gerar erros", visitor.hasErrors());

    FunctionDeclNode procDecl = (FunctionDeclNode) ast.getDeclarations().get(0);
    IfNode ifNode = (IfNode) procDecl.getBody().getStatements().get(0);
    CallStmtNode recursiveCall = (CallStmtNode) ifNode.getThenBranch().getStatements().get(0);

    assertTrue("Chamada recursiva deve ter decl preenchido", recursiveCall.getDecl() != null);
    assertSame(
        "Chamada recursiva deve apontar para o próprio FunctionDeclNode",
        procDecl,
        recursiveCall.getDecl().getDeclarationNode());
    assertEqual(
        "Categoria do procedimento recursivo deve ser PROCEDURE",
        SymbolCategory.PROCEDURE,
        recursiveCall.getDecl().getCategory());
  }

  private void testInnerVariableInvisibleAfterBlockCloses() {
    String source = readFile("test/programas/cp4_uso_apos_fechamento_bloco.lum");
    ProgramNode ast = new Parser(source).parse().ast();
    IdentificationVisitor visitor = new IdentificationVisitor();
    visitor.identify(ast);

    List<IdentificationError> errors = visitor.getErrors();
    assertEqual(
        "Variável declarada dentro do bloco if não pode ser vista após o '}'", 1, errors.size());
    assertEqual(
        "Erro de variável fora de escopo na linha 6",
        "linha 6: 'interno' não declarado",
        errors.get(0).getMessage());

    FunctionDeclNode func = (FunctionDeclNode) ast.getDeclarations().get(0);
    AssignNode assignAfterBlock = (AssignNode) func.getBody().getStatements().get(1);
    IdNode unresolvedId = (IdNode) assignAfterBlock.getValue();
    assertTrue("Nó de uso não resolvido deve manter decl == null", unresolvedId.getDecl() == null);
  }

  private void testNullAstSafety() {
    IdentificationVisitor visitor = new IdentificationVisitor();
    visitor.identify(null);
    assertFalse(
        "Passada sobre AST nula não deve lançar exceção nem registrar erro", visitor.hasErrors());
    assertEqual("formatOutput(null) deve retornar string vazia", "", visitor.formatOutput(null));
  }

  private String readFile(String relativePath) {
    Path path = Paths.get(relativePath);
    if (!Files.exists(path)) {
      fail("Leitura do arquivo: " + relativePath, "arquivo existente", "não encontrado");
      return "";
    }
    try {
      return Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException e) {
      fail("Leitura do arquivo: " + relativePath, "sucesso", e.getMessage());
      return "";
    }
  }

  private String normalizeNewlines(String text) {
    return text.replace("\r\n", "\n");
  }

  private void assertTrue(String testDescription, boolean condition) {
    if (condition) {
      pass(testDescription);
    } else {
      fail(testDescription, "true", "false");
    }
  }

  private void assertFalse(String testDescription, boolean condition) {
    if (!condition) {
      pass(testDescription);
    } else {
      fail(testDescription, "false", "true");
    }
  }

  private void assertEqual(String testDescription, Object expected, Object actual) {
    if (Objects.equals(expected, actual)) {
      pass(testDescription);
    } else {
      fail(testDescription, String.valueOf(expected), String.valueOf(actual));
    }
  }

  private void assertSame(String testDescription, Object expected, Object actual) {
    if (expected == actual) {
      pass(testDescription);
    } else {
      fail(testDescription, String.valueOf(expected), String.valueOf(actual));
    }
  }

  private void pass(String testDescription) {
    testsPassed++;
    System.out.printf("  [PASS] %s%n", testDescription);
  }

  private void fail(String testDescription, String expected, String actual) {
    testsFailed++;
    System.err.printf("  [FAIL] %s%n", testDescription);
    System.err.printf("         Esperado : %s%n", expected);
    System.err.printf("         Obtido   : %s%n", actual);
  }

  private void printHeader() {
    System.out.println("==========================================================");
    System.out.println("  INICIANDO SUÍTE DE TESTES — IDENTIFICAÇÃO (CP4)         ");
    System.out.println("==========================================================");
  }

  private void printSummary() {
    System.out.println("----------------------------------------------------------");
    System.out.printf("Testes Concluídos: %d Aprovados, %d Falhas%n", testsPassed, testsFailed);
    System.out.println("==========================================================");
  }

  private void evaluateExitStatus() {
    if (testsFailed > 0) {
      System.err.printf("FALHA: %d teste(s) de Identificação não passaram.%n", testsFailed);
      System.exit(1);
    } else {
      System.out.println("RESULTADO: TODOS OS TESTES DE IDENTIFICAÇÃO PASSARAM COM SUCESSO!");
    }
  }
}
