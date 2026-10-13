package lumina.parser;

import java.util.List;
import java.util.Objects;
import lumina.lexer.token.TokenType;
import lumina.parser.error.SyntaxError;

/** Testes do analisador sintático (Parser). */
public final class ParserTest {

  private int testsPassed = 0;
  private int testsFailed = 0;

  public static void main(String[] args) {
    ParserTest suite = new ParserTest();
    suite.runAllTests();
  }

  public void runAllTests() {
    printHeader();

    // 1. Programas válidos
    testValidBasicTypesAndDeclarations();
    testValidControlStructures();
    testValidFunctionsAndProcedures();
    testValidFactorialCanonicalProgram();

    // 2. Programas inválidos
    testInvalidMissingSemicolon();
    testInvalidMalformedAssignment();
    testInvalidFunctionWithoutParentheses();
    testInvalidIfWithoutConditionParentheses();

    // 3. Recuperação em modo pânico
    testPanicModeMultipleErrorsInSingleRun();

    printSummary();
    evaluateExitStatus();
  }

  // 1. Programas válidos

  private void testValidBasicTypesAndDeclarations() {
    String source =
        """
        int globalContador = 0;
        double pi = 3.14159;
        bool ativo = true;
        char inicial = 'L';
        string mensagem = "Lumina Lang";
        int semInicializacao;

        fun void inicializarVariaveis() {
            int localA = 10;
            double localB = 20.5;
            bool localC = false;
            char localD = 'X';
            string localE = "teste";
            localA = localA + 5 * 2;
        }
        """;

    Parser parser = new Parser(source);
    ParseResult result = parser.parse();

    assertTrue("Programa com tipos básicos e declarações deve ser aceito", result.isSuccessful());
    assertEqual("Nenhum erro sintático deve ser reportado", 0, result.errors().size());
  }

  private void testValidControlStructures() {
    String source =
        """
        fun void testarControles(int limite, bool condicao) {
            int i = 0;
            while (i < limite && condicao || !condicao) {
                if (i == 5) {
                    i = i + 2;
                } else {
                    i = i + 1;
                }
            }

            if (i >= 10 && i != 20) {
                i = 0;
            }
        }
        """;

    Parser parser = new Parser(source);
    ParseResult result = parser.parse();

    assertTrue("Programa com estruturas de controle deve ser aceito", result.isSuccessful());
    assertEqual("Nenhum erro sintático deve ser reportado", 0, result.errors().size());
  }

  private void testValidFunctionsAndProcedures() {
    String source =
        """
        fun int somar(int a, int b) {
            return a + b;
        }

        fun double calcularMedia(double x, double y) {
            return (x + y) / 2.0;
        }

        fun void imprimirStatus(string status) {
            return;
        }

        fun void principal() {
            int resultado = somar(10, 20);
            double media = calcularMedia(7.5, 8.5);
            imprimirStatus("Concluido com sucesso");
        }
        """;

    Parser parser = new Parser(source);
    ParseResult result = parser.parse();

    assertTrue(
        "Programa com funções, procedimentos, chamadas e retornos deve ser aceito",
        result.isSuccessful());
    assertEqual("Nenhum erro sintático deve ser reportado", 0, result.errors().size());
  }

  private void testValidFactorialCanonicalProgram() {
    String source =
        """
        fun int calcularFatorial(int n) {
            int resultado = 1;
            int contador = n;

            while (contador > 1) {
                resultado = resultado * contador;
                contador = contador - 1;
            }

            if (resultado >= 1 && resultado != 0) {
                return resultado;
            } else {
                return 0;
            }
        }

        fun void main() {
            int valor = 5;
            double taxa = 3.1415;
            string mensagem = "Fatorial calculado com sucesso: \\"OK\\"\\n";
            int f = calcularFatorial(valor);
        }
        """;

    Parser parser = new Parser(source);
    ParseResult result = parser.parse();

    assertTrue("Programa de fatorial deve ser aceito", result.isSuccessful());
    assertEqual(
        "Nenhum erro sintático deve ser reportado no exemplo de fatorial",
        0,
        result.errors().size());
  }

  // 2. Programas inválidos com erros sintáticos

  private void testInvalidMissingSemicolon() {
    String source =
        """
        fun void teste() {
            int x = 10
            int y = 20;
        }
        """;

    Parser parser = new Parser(source);
    ParseResult result = parser.parse();

    assertFalse("Programa sem ponto e vírgula deve ser rejeitado", result.isSuccessful());
    assertTrue("Deve registrar erro sintático por ausência de ';'", result.hasErrors());
    assertEqual("Deve conter exatamente 1 erro de delimitador ausente", 1, result.errors().size());

    SyntaxError erro = result.errors().get(0);
    assertEqual("Linha do erro deve ser 3 (onde encontrou 'int' em vez de ';')", 3, erro.line());
    assertTrue("Mensagem deve apontar espera de ponto e vírgula", erro.message().contains(";"));
  }

  private void testInvalidMalformedAssignment() {
    String source =
        """
        fun void teste() {
            int x = 0;
            x = ;
            int y = 10;
        }
        """;

    Parser parser = new Parser(source);
    ParseResult result = parser.parse();

    assertFalse("Atribuição sem expressão à direita deve ser rejeitada", result.isSuccessful());
    assertTrue("Deve registrar erro sintático na expressão de atribuição", result.hasErrors());

    SyntaxError erro = result.errors().get(0);
    assertEqual("Linha do erro deve ser 3", 3, erro.line());
    assertTrue("Token causador deve ser ';'", erro.offendingToken().is(TokenType.SEMICOLON));
  }

  private void testInvalidFunctionWithoutParentheses() {
    String source =
        """
        fun int calcularSoma {
            return 1;
        }
        """;

    Parser parser = new Parser(source);
    ParseResult result = parser.parse();

    assertFalse("Declaração de função sem parênteses deve ser rejeitada", result.isSuccessful());
    assertTrue("Deve registrar erro ao esperar '(' no cabeçalho da função", result.hasErrors());

    SyntaxError erro = result.errors().get(0);
    assertEqual("Linha do erro deve ser 1", 1, erro.line());
    assertTrue("Mensagem deve apontar espera de '('", erro.message().contains("("));
  }

  private void testInvalidIfWithoutConditionParentheses() {
    String source =
        """
        fun void teste(int a) {
            if a > 0 {
                a = 0;
            }
        }
        """;

    Parser parser = new Parser(source);
    ParseResult result = parser.parse();

    assertFalse(
        "Comando 'if' sem parênteses na condição deve ser rejeitado", result.isSuccessful());
    assertTrue("Deve reportar erro sintático na estrutura condicional", result.hasErrors());

    SyntaxError erro = result.errors().get(0);
    assertEqual("Linha do erro deve ser 2", 2, erro.line());
    assertTrue("Mensagem deve indicar ausência de '('", erro.message().contains("("));
  }

  // 3. Recuperação em modo pânico

  private void testPanicModeMultipleErrorsInSingleRun() {
    String source =
        """
        fun void rotinaComMultiplosErros() {
            int a = 10 + ;
            int valido1 = 100;
            if x > 0 {
                valido1 = 200;
            }
            int valido2 = 300;
            b = ;
            int finalizado = 400;
        }
        """;

    Parser parser = new Parser(source);
    ParseResult result = parser.parse();

    assertFalse("Programa com múltiplos erros deve ser rejeitado", result.isSuccessful());
    assertTrue(
        "Modo pânico deve encontrar mais de um erro na mesma execução",
        result.errors().size() >= 3);

    List<SyntaxError> erros = result.errors();

    SyntaxError erro1 = erros.get(0);
    assertEqual("Primeiro erro deve estar na linha 2", 2, erro1.line());

    SyntaxError erro2 = erros.get(1);
    assertEqual("Segundo erro deve estar na linha 4", 4, erro2.line());

    SyntaxError erroUltimo = erros.get(erros.size() - 1);
    assertEqual("Último erro deve estar na linha 8", 8, erroUltimo.line());
  }

  // Asserções auxiliares

  private void assertTrue(String testDescription, boolean condition) {
    if (condition) {
      pass(testDescription);
    } else {
      fail(testDescription, "esperava true", "recebeu false");
    }
  }

  private void assertFalse(String testDescription, boolean condition) {
    if (!condition) {
      pass(testDescription);
    } else {
      fail(testDescription, "esperava false", "recebeu true");
    }
  }

  private void assertEqual(String testDescription, Object expected, Object actual) {
    if (Objects.equals(expected, actual)) {
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
    System.out.println("  INICIANDO SUÍTE DE TESTES — ANALISADOR SINTÁTICO LUMINA ");
    System.out.println("==========================================================");
  }

  private void printSummary() {
    System.out.println("----------------------------------------------------------");
    System.out.printf("Testes Concluídos: %d Aprovados, %d Falhas%n", testsPassed, testsFailed);
    System.out.println("==========================================================");
  }

  private void evaluateExitStatus() {
    if (testsFailed > 0) {
      System.err.printf("FALHA: %d teste(s) não passaram.%n", testsFailed);
      System.exit(1);
    } else {
      System.out.println("RESULTADO: TODOS OS TESTES SINTÁTICOS PASSARAM COM SUCESSO!");
    }
  }
}
