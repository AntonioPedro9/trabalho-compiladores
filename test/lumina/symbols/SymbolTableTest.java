package lumina.symbols;

import java.util.Objects;
import lumina.ast.IntLitNode;
import lumina.ast.TypeNode;
import lumina.ast.VarDeclNode;
import lumina.common.source.SourcePosition;
import lumina.lexer.token.TokenType;

/**
 * Suíte de testes unitários isolados da {@link SymbolTable} (Checkpoint 4 — Seção 5). Cobre
 * declaração e busca, sombreamento e restauração após {@code closeScope}, redeclaração preservando
 * o primeiro símbolo, visibilidade estrita de {@code lookupLocal}, retorno {@code null} para nomes
 * inexistentes, {@code level()} e segurança de {@code closeScope()} sem escopo de bloco aberto.
 */
public final class SymbolTableTest {

  private int testsPassed = 0;
  private int testsFailed = 0;

  public static void main(String[] args) {
    SymbolTableTest suite = new SymbolTableTest();
    suite.runAllTests();
  }

  public void runAllTests() {
    printHeader();

    testInitialGlobalLevelAndDeclareLookup();
    testShadowingAndRestorationAfterCloseScope();
    testRedeclarationInSameScopeKeepsFirstSymbol();
    testLookupLocalDoesNotSeeOuterScope();
    testLookupNonExistentReturnsNullWithoutException();
    testLevelProgressionAcrossMultipleNestedScopes();
    testCloseScopeAtGlobalLevelThrowsDocumentedException();

    printSummary();
    evaluateExitStatus();
  }

  private void testInitialGlobalLevelAndDeclareLookup() {
    SymbolTable table = new ScopedSymbolTable();
    assertEqual("Nível inicial deve ser 0 (global)", 0, table.level());

    Symbol globalX = createVarSymbol("x", 1, table.level());
    assertTrue("Primeira declaração de 'x' no global deve retornar true", table.declare(globalX));
    assertSame("lookup('x') deve devolver o símbolo declarado", globalX, table.lookup("x"));
    assertSame(
        "lookupLocal('x') no global deve devolver o símbolo declarado",
        globalX,
        table.lookupLocal("x"));
  }

  private void testShadowingAndRestorationAfterCloseScope() {
    SymbolTable table = new ScopedSymbolTable();
    Symbol globalX = createVarSymbol("x", 1, 0);
    table.declare(globalX);

    table.openScope();
    assertEqual("Nível após 1º openScope deve ser 1", 1, table.level());

    Symbol localX = createVarSymbol("x", 5, 1);
    assertTrue("Sombreamento de 'x' no nível 1 deve ser permitido (true)", table.declare(localX));
    assertSame("lookup('x') no nível 1 deve devolver x@5 (sombreado)", localX, table.lookup("x"));
    assertEqual("Linha do símbolo sombreado deve ser 5", 5, table.lookup("x").getDeclarationLine());

    table.openScope();
    Symbol innerX = createVarSymbol("x", 8, 2);
    assertTrue("Sombreamento de 'x' no nível 2 deve ser permitido (true)", table.declare(innerX));
    assertSame("lookup('x') no nível 2 deve devolver x@8", innerX, table.lookup("x"));

    table.closeScope();
    assertSame("Após fechar nível 2, lookup('x') deve restaurar x@5", localX, table.lookup("x"));

    table.closeScope();
    assertEqual("Após fechar nível 1, nível volta a 0", 0, table.level());
    assertSame(
        "Após fechar nível 1, lookup('x') deve restaurar x@1 (global)", globalX, table.lookup("x"));
  }

  private void testRedeclarationInSameScopeKeepsFirstSymbol() {
    SymbolTable table = new ScopedSymbolTable();
    Symbol firstA = createVarSymbol("a", 2, 0);
    Symbol secondA = createVarSymbol("a", 7, 0);

    assertTrue("1ª declaração de 'a' deve devolver true", table.declare(firstA));
    assertFalse("Redeclaração de 'a' no mesmo escopo deve devolver false", table.declare(secondA));
    assertSame("A primeira declaração (a@2) deve permanecer na tabela", firstA, table.lookup("a"));
    assertEqual(
        "Linha de 'a' preservado deve continuar sendo 2",
        2,
        table.lookup("a").getDeclarationLine());
  }

  private void testLookupLocalDoesNotSeeOuterScope() {
    SymbolTable table = new ScopedSymbolTable();
    Symbol globalVar = createVarSymbol("externa", 1, 0);
    table.declare(globalVar);

    table.openScope();
    assertTrue(
        "lookupLocal('externa') no nível 1 deve devolver null",
        table.lookupLocal("externa") == null);
    assertSame(
        "lookup('externa') no nível 1 deve enxergar o escopo externo",
        globalVar,
        table.lookup("externa"));
    table.closeScope();
  }

  private void testLookupNonExistentReturnsNullWithoutException() {
    SymbolTable table = new ScopedSymbolTable();
    assertTrue(
        "lookup de nome inexistente no global deve devolver null",
        table.lookup("inexistente") == null);
    assertTrue(
        "lookupLocal de nome inexistente deve devolver null",
        table.lookupLocal("inexistente") == null);

    table.openScope();
    assertTrue(
        "lookup de nome inexistente em escopo aninhado deve devolver null",
        table.lookup("naoExiste") == null);
    assertTrue("lookup(null) deve devolver null com segurança", table.lookup(null) == null);
    assertTrue(
        "lookupLocal(null) deve devolver null com segurança", table.lookupLocal(null) == null);
    table.closeScope();
  }

  private void testLevelProgressionAcrossMultipleNestedScopes() {
    SymbolTable table = new ScopedSymbolTable();
    assertEqual("Nível inicial = 0", 0, table.level());
    table.openScope();
    assertEqual("Nível após 1º openScope = 1", 1, table.level());
    table.openScope();
    assertEqual("Nível após 2º openScope = 2", 2, table.level());
    table.openScope();
    assertEqual("Nível após 3º openScope = 3", 3, table.level());

    table.closeScope();
    assertEqual("Nível após 1º closeScope = 2", 2, table.level());
    table.closeScope();
    assertEqual("Nível após 2º closeScope = 1", 1, table.level());
    table.closeScope();
    assertEqual("Nível após 3º closeScope = 0", 0, table.level());
  }

  private void testCloseScopeAtGlobalLevelThrowsDocumentedException() {
    SymbolTable table = new ScopedSymbolTable();
    boolean threwIllegalState = false;
    try {
      table.closeScope();
    } catch (IllegalStateException ex) {
      threwIllegalState = ex.getMessage() != null && !ex.getMessage().isEmpty();
    }
    assertTrue(
        "closeScope() sem escopo de bloco aberto deve lançar IllegalStateException explicada (nunca"
            + " NPE)",
        threwIllegalState);
    assertEqual("Tabela deve permanecer consistente no nível 0", 0, table.level());
  }

  private Symbol createVarSymbol(String name, int line, int level) {
    SourcePosition pos = new SourcePosition(line, 1);
    TypeNode typeNode = new TypeNode(pos, TokenType.INT);
    VarDeclNode declNode = new VarDeclNode(pos, typeNode, name, new IntLitNode(pos, 0, "0"));
    return new Symbol(name, SymbolCategory.VARIABLE, declNode, level, TokenType.INT);
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
    System.out.println("  INICIANDO SUÍTE DE TESTES — SYMBOL TABLE (CP4)          ");
    System.out.println("==========================================================");
  }

  private void printSummary() {
    System.out.println("----------------------------------------------------------");
    System.out.printf("Testes Concluídos: %d Aprovados, %d Falhas%n", testsPassed, testsFailed);
    System.out.println("==========================================================");
  }

  private void evaluateExitStatus() {
    if (testsFailed > 0) {
      System.err.printf("FALHA: %d teste(s) de SymbolTable não passaram.%n", testsFailed);
      System.exit(1);
    } else {
      System.out.println("RESULTADO: TODOS OS TESTES DE SYMBOL TABLE PASSARAM COM SUCESSO!");
    }
  }
}
