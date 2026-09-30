package lumina.lexer;

import java.util.List;
import java.util.Objects;
import lumina.lexer.token.Token;
import lumina.lexer.token.TokenType;

public final class ScannerTest {

  private int testsPassed = 0;
  private int testsFailed = 0;

  public static void main(String[] args) {
    ScannerTest testSuite = new ScannerTest();
    testSuite.runAllTests();
  }

  public void runAllTests() {
    printHeader();

    // 1. Casos Válidos das 5 Categorias de Token (Seção 3.4)
    testValidIdentifierToken();
    testValidKeywordToken();
    testValidNumericLiteralToken();
    testValidStringToken();
    testValidOperatorToken();

    // 2. Casos de Erro Léxico Obrigatórios (Seção 3.4)
    testErrorUnterminatedStringAtEof();
    testErrorUnterminatedStringAtNewline();
    testErrorInvalidCharacterOutsideAlphabet();

    // 3. Trecho de Código Realista (Seção 3.4)
    testRealisticCodeSnippet();

    printSummary();
    evaluateExitStatus();
  }

  // =========================================================================
  // 1. Casos Válidos das Cinco Categorias de Token
  // =========================================================================

  private void testValidIdentifierToken() {
    // Arrange
    String sourceCode = "total contaItens _contador";
    Scanner scanner = new Scanner(sourceCode);

    // Act
    List<Token> tokens = scanner.scanAllTokens();

    // Assert
    assertEqual("Identificadores - quantidade total de tokens", 4, tokens.size());
    assertTokenEquals("Primeiro identificador", TokenType.IDENTIFIER, "total", tokens.get(0));
    assertTokenEquals("Segundo identificador", TokenType.IDENTIFIER, "contaItens", tokens.get(1));
    assertTokenEquals("Terceiro identificador", TokenType.IDENTIFIER, "_contador", tokens.get(2));
    assertTokenEquals("Fim de arquivo (EOF)", TokenType.EOF, "", tokens.get(3));
    assertTrue(
        "Ausência de erros léxicos em identificadores válidos", scanner.getErrors().isEmpty());
  }

  private void testValidKeywordToken() {
    // Arrange
    String sourceCode = "while if return int";
    Scanner scanner = new Scanner(sourceCode);

    // Act
    List<Token> tokens = scanner.scanAllTokens();

    // Assert
    assertEqual("Palavras reservadas - quantidade total de tokens", 5, tokens.size());
    assertTokenEquals("Palavra reservada while", TokenType.WHILE, "while", tokens.get(0));
    assertTokenEquals("Palavra reservada if", TokenType.IF, "if", tokens.get(1));
    assertTokenEquals("Palavra reservada return", TokenType.RETURN, "return", tokens.get(2));
    assertTokenEquals("Palavra reservada int", TokenType.INT, "int", tokens.get(3));
    assertTokenEquals("Fim de arquivo (EOF)", TokenType.EOF, "", tokens.get(4));
    assertTrue("Ausência de erros léxicos em palavras reservadas", scanner.getErrors().isEmpty());
  }

  private void testValidNumericLiteralToken() {
    // Arrange
    String sourceCode = "42 3.1415";
    Scanner scanner = new Scanner(sourceCode);

    // Act
    List<Token> tokens = scanner.scanAllTokens();

    // Assert
    assertEqual("Literais numéricos - quantidade total de tokens", 3, tokens.size());
    assertTokenEquals("Literal numérico inteiro", TokenType.INT_LITERAL, "42", tokens.get(0));
    assertTokenEquals("Literal numérico double", TokenType.DOUBLE_LITERAL, "3.1415", tokens.get(1));
    assertTokenEquals("Fim de arquivo (EOF)", TokenType.EOF, "", tokens.get(2));
    assertTrue(
        "Ausência de erros léxicos em literais numéricos válidos", scanner.getErrors().isEmpty());
  }

  private void testValidStringToken() {
    // Arrange
    String sourceCode = "\"ok\" \"linha 1\"";
    Scanner scanner = new Scanner(sourceCode);

    // Act
    List<Token> tokens = scanner.scanAllTokens();

    // Assert
    assertEqual("Literais string - quantidade total de tokens", 3, tokens.size());
    assertTokenEquals("String literal ok", TokenType.STRING_LITERAL, "\"ok\"", tokens.get(0));
    assertTokenEquals(
        "String literal linha 1", TokenType.STRING_LITERAL, "\"linha 1\"", tokens.get(1));
    assertTokenEquals("Fim de arquivo (EOF)", TokenType.EOF, "", tokens.get(2));
    assertTrue(
        "Ausência de erros léxicos em literais string válidos", scanner.getErrors().isEmpty());
  }

  private void testValidOperatorToken() {
    // Arrange
    String sourceCode = "+ == <= &&";
    Scanner scanner = new Scanner(sourceCode);

    // Act
    List<Token> tokens = scanner.scanAllTokens();

    // Assert
    assertEqual("Operadores - quantidade total de tokens", 5, tokens.size());
    assertTokenEquals("Operador aritmético +", TokenType.PLUS, "+", tokens.get(0));
    assertTokenEquals(
        "Operador de igualdade == (maximal munch)", TokenType.EQUAL_EQUAL, "==", tokens.get(1));
    assertTokenEquals(
        "Operador relacional <= (maximal munch)", TokenType.LESS_EQUAL, "<=", tokens.get(2));
    assertTokenEquals(
        "Operador lógico && (maximal munch)", TokenType.AMPERSAND_AMPERSAND, "&&", tokens.get(3));
    assertTokenEquals("Fim de arquivo (EOF)", TokenType.EOF, "", tokens.get(4));
    assertTrue("Ausência de erros léxicos em operadores válidos", scanner.getErrors().isEmpty());
  }

  // =========================================================================
  // 2. Casos de Erro Léxico Obrigatórios (Seção 3.4)
  // =========================================================================

  private void testErrorUnterminatedStringAtEof() {
    // Arrange
    String sourceCode = "string msg = \"aberta ate EOF";
    Scanner scanner = new Scanner(sourceCode);

    // Act
    List<Token> tokens = scanner.scanAllTokens();

    // Assert
    boolean hasLexicalErrors = scanner.getErrorReporter().hasErrors();
    assertTrue("Deve registrar erro de string não finalizada até EOF", hasLexicalErrors);
    assertEqual("Total de tokens gerados com erro até EOF", 5, tokens.size());
    assertTokenEquals(
        "Token de erro léxico emitido", TokenType.ERROR, "\"aberta ate EOF", tokens.get(3));
    assertTokenEquals("Último token é EOF", TokenType.EOF, "", tokens.get(4));
  }

  private void testErrorUnterminatedStringAtNewline() {
    // Arrange
    String sourceCode = "string texto = \"sem fechar na linha\nint x = 10;";
    Scanner scanner = new Scanner(sourceCode);

    // Act
    List<Token> tokens = scanner.scanAllTokens();

    // Assert
    boolean hasLexicalErrors = scanner.getErrorReporter().hasErrors();
    assertTrue("Deve registrar erro de string não finalizada na quebra de linha", hasLexicalErrors);
    assertTokenEquals(
        "Token de erro na quebra de linha",
        TokenType.ERROR,
        "\"sem fechar na linha",
        tokens.get(3));
    assertTokenEquals("Recuperação do scanner: token int", TokenType.INT, "int", tokens.get(4));
    assertTokenEquals(
        "Recuperação do scanner: identificador x", TokenType.IDENTIFIER, "x", tokens.get(5));
    assertTokenEquals("Recuperação do scanner: operador =", TokenType.EQUAL, "=", tokens.get(6));
    assertTokenEquals(
        "Recuperação do scanner: literal 10", TokenType.INT_LITERAL, "10", tokens.get(7));
  }

  private void testErrorInvalidCharacterOutsideAlphabet() {
    // Arrange
    String sourceCode = "int @valor = 50;";
    Scanner scanner = new Scanner(sourceCode);

    // Act
    List<Token> tokens = scanner.scanAllTokens();

    // Assert
    boolean hasLexicalErrors = scanner.getErrorReporter().hasErrors();
    assertTrue("Deve registrar erro de caractere fora do alfabeto", hasLexicalErrors);
    assertTokenEquals("Token inicial int", TokenType.INT, "int", tokens.get(0));
    assertTokenEquals(
        "Token de erro para caractere fora do alfabeto", TokenType.ERROR, "@", tokens.get(1));
    assertTokenEquals(
        "Recuperação do identificador valor", TokenType.IDENTIFIER, "valor", tokens.get(2));
    assertTokenEquals("Operador de atribuição =", TokenType.EQUAL, "=", tokens.get(3));
    assertTokenEquals("Literal numérico 50", TokenType.INT_LITERAL, "50", tokens.get(4));
  }

  // =========================================================================
  // 3. Trecho de Código Realista (Seção 3.4)
  // =========================================================================

  private void testRealisticCodeSnippet() {
    // Arrange
    String sourceCode =
        "int total = 10; /* acumulador inicial */ while (total > 0) total = total - 1; // fim";
    Scanner scanner = new Scanner(sourceCode);

    // Act
    List<Token> tokens = scanner.scanAllTokens();

    // Assert
    boolean isFreeOfErrors = scanner.getErrors().isEmpty();
    assertTrue("Trecho realista não deve apresentar erros léxicos", isFreeOfErrors);
    assertEqual("Quantidade esperada de tokens na linha realista", 18, tokens.size());

    assertTokenEquals("Token 0: int", TokenType.INT, "int", tokens.get(0));
    assertTokenEquals("Token 1: total", TokenType.IDENTIFIER, "total", tokens.get(1));
    assertTokenEquals("Token 2: =", TokenType.EQUAL, "=", tokens.get(2));
    assertTokenEquals("Token 3: 10", TokenType.INT_LITERAL, "10", tokens.get(3));
    assertTokenEquals("Token 4: ;", TokenType.SEMICOLON, ";", tokens.get(4));
    assertTokenEquals("Token 5: while", TokenType.WHILE, "while", tokens.get(5));
    assertTokenEquals("Token 6: (", TokenType.LEFT_PAREN, "(", tokens.get(6));
    assertTokenEquals("Token 7: total", TokenType.IDENTIFIER, "total", tokens.get(7));
    assertTokenEquals("Token 8: >", TokenType.GREATER, ">", tokens.get(8));
    assertTokenEquals("Token 9: 0", TokenType.INT_LITERAL, "0", tokens.get(9));
    assertTokenEquals("Token 10: )", TokenType.RIGHT_PAREN, ")", tokens.get(10));
    assertTokenEquals("Token 11: total", TokenType.IDENTIFIER, "total", tokens.get(11));
    assertTokenEquals("Token 12: =", TokenType.EQUAL, "=", tokens.get(12));
    assertTokenEquals("Token 13: total", TokenType.IDENTIFIER, "total", tokens.get(13));
    assertTokenEquals("Token 14: -", TokenType.MINUS, "-", tokens.get(14));
    assertTokenEquals("Token 15: 1", TokenType.INT_LITERAL, "1", tokens.get(15));
    assertTokenEquals("Token 16: ;", TokenType.SEMICOLON, ";", tokens.get(16));
    assertTokenEquals("Token 17: EOF", TokenType.EOF, "", tokens.get(17));
  }

  // =========================================================================
  // Métodos Auxiliares de Asserção e Apresentação
  // =========================================================================

  private void assertTokenEquals(
      String testDescription, TokenType expectedType, String expectedLexeme, Token actualToken) {
    assertEqual(testDescription + " [Tipo]", expectedType, actualToken.getType());
    assertEqual(testDescription + " [Lexema]", expectedLexeme, actualToken.getLexeme());
  }

  private void assertEqual(String testDescription, Object expected, Object actual) {
    boolean isMatch = Objects.equals(expected, actual);
    if (isMatch) {
      recordPass(testDescription);
    } else {
      String failureDetails = String.format("Esperado: <%s>, Obtido: <%s>", expected, actual);
      recordFailure(testDescription, failureDetails);
    }
  }

  private void assertTrue(String testDescription, boolean condition) {
    if (condition) {
      recordPass(testDescription);
    } else {
      recordFailure(testDescription, "Condição esperada verdadeira, mas avaliou para falsa.");
    }
  }

  private void recordPass(String testDescription) {
    testsPassed++;
    System.out.printf("  [PASS] %s%n", testDescription);
  }

  private void recordFailure(String testDescription, String failureDetails) {
    testsFailed++;
    System.err.printf("  [FAIL] %s - %s%n", testDescription, failureDetails);
  }

  private void printHeader() {
    System.out.println("==========================================================");
    System.out.println("  INICIANDO SUÍTE DE TESTES — ANALISADOR LÉXICO (LUMINA)  ");
    System.out.println("==========================================================");
  }

  private void printSummary() {
    System.out.println("----------------------------------------------------------");
    System.out.printf("Testes Concluídos: %d Aprovados, %d Falhas%n", testsPassed, testsFailed);
    System.out.println("==========================================================");
  }

  private void evaluateExitStatus() {
    boolean hasFailures = testsFailed > 0;
    if (hasFailures) {
      System.err.println("RESULTADO: FALHA NA SUÍTE DE TESTES.");
      System.exit(1);
    }
    System.out.println("RESULTADO: TODOS OS TESTES PASSARAM COM SUCESSO!");
  }
}
