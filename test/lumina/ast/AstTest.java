package lumina.ast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import lumina.lexer.token.TokenType;
import lumina.parser.ParseResult;
import lumina.parser.Parser;
import lumina.parser.error.SyntaxError;
import lumina.visitor.AstPrinterVisitor;
import lumina.visitor.SourceReprinterVisitor;

/**
 * Suíte de testes da Árvore de Sintaxe Abstrata (AST) e dos Visitors (Checkpoint 3 — Seção 5).
 * Cobre comparação com árvores esperadas escritas à mão em {@code test/esperado/}, precedência e
 * associatividade, descarte de parênteses redundantes, ida e volta (round-trip) e regressão do CP2.
 */
public final class AstTest {

  private int testsPassed = 0;
  private int testsFailed = 0;

  public static void main(String[] args) {
    AstTest suite = new AstTest();
    suite.runAllTests();
  }

  public void runAllTests() {
    printHeader();

    // 1. Comparação com árvores esperadas em test/esperado/ (pelo menos 3 programas válidos)
    testExpectedAstBasicTypes();
    testExpectedAstControlStructures();
    testExpectedAstFunctionsAndProcedures();
    testExpectedAstFactorialCanonical();

    // 2. Precedência e associatividade em testes próprios (a + b * c, a - b - c, a - (b - c))
    testOperatorPrecedencePlusAndStar();
    testLeftAssociativityMinus();
    testParenthesesOverrideAssociativity();

    // 3. A AST não é a árvore de derivação: parênteses redundantes (((a)))
    testRedundantParenthesesProduceIdenticalAst();

    // 4. Ida e volta (round-trip) para todos os programas válidos
    testRoundTripAllValidPrograms();

    // 5. Regressão do CP2: programas inválidos e múltiplos erros sem lançar exceção e com AST nula
    testRegressionInvalidProgramsAndPanicMode();

    printSummary();
    evaluateExitStatus();
  }

  // 1. Programas válidos comparados com test/esperado/

  private void testExpectedAstBasicTypes() {
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

    verifyExpectedTreeFile(
        "AST do programa de tipos básicos deve coincidir com test/esperado/tipos.txt",
        source,
        "test/esperado/tipos.txt");
  }

  private void testExpectedAstControlStructures() {
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

    verifyExpectedTreeFile(
        "AST do programa de estruturas de controle deve coincidir com test/esperado/controle.txt",
        source,
        "test/esperado/controle.txt");
  }

  private void testExpectedAstFunctionsAndProcedures() {
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

    verifyExpectedTreeFile(
        "AST do programa de funções e procedimentos deve coincidir com test/esperado/funcoes.txt",
        source,
        "test/esperado/funcoes.txt");
  }

  private void testExpectedAstFactorialCanonical() {
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

    verifyExpectedTreeFile(
        "AST do programa canônico de fatorial deve coincidir com test/esperado/fatorial.txt",
        source,
        "test/esperado/fatorial.txt");
  }

  // 2. Precedência e associatividade

  private void testOperatorPrecedencePlusAndStar() {
    ExprNode expr = parseSingleInitializerExpression("int r = a + b * c;");
    AstPrinterVisitor printer = new AstPrinterVisitor();

    String expected =
        """
        Bin(+)
          Id(a)
          Bin(*)
            Id(b)
            Id(c)
        """;

    assertEqual(
        "Precedência: 'a + b * c' deve agrupar '*' abaixo de '+'",
        normalizeNewlines(expected),
        normalizeNewlines(printer.print(expr)));
  }

  private void testLeftAssociativityMinus() {
    ExprNode expr = parseSingleInitializerExpression("int r = a - b - c;");
    AstPrinterVisitor printer = new AstPrinterVisitor();

    String expected =
        """
        Bin(-)
          Bin(-)
            Id(a)
            Id(b)
          Id(c)
        """;

    assertEqual(
        "Associatividade à esquerda: 'a - b - c' deve gerar '(a - b) - c'",
        normalizeNewlines(expected),
        normalizeNewlines(printer.print(expr)));
  }

  private void testParenthesesOverrideAssociativity() {
    ExprNode expr = parseSingleInitializerExpression("int r = a - (b - c);");
    AstPrinterVisitor printer = new AstPrinterVisitor();

    String expected =
        """
        Bin(-)
          Id(a)
          Bin(-)
            Id(b)
            Id(c)
        """;

    assertEqual(
        "Parênteses explícitos: 'a - (b - c)' deve colocar 'b - c' como filho direito",
        normalizeNewlines(expected),
        normalizeNewlines(printer.print(expr)));
  }

  // 3. Parênteses redundantes: (((a))) vs a

  private void testRedundantParenthesesProduceIdenticalAst() {
    ExprNode exprWithParens = parseSingleInitializerExpression("int r = (((a)));");
    ExprNode exprWithoutParens = parseSingleInitializerExpression("int r = a;");

    AstPrinterVisitor printer = new AstPrinterVisitor();
    String printedWithParens = normalizeNewlines(printer.print(exprWithParens));
    String printedWithoutParens = normalizeNewlines(printer.print(exprWithoutParens));

    assertEqual(
        "Expressão '(((a)))' não deve gerar nós de parênteses, produzindo apenas 'Id(a)'",
        "Id(a)\n",
        printedWithParens);
    assertEqual(
        "Árvore de '(((a)))' deve ter exatamente a mesma saída de impressor que 'a'",
        printedWithoutParens,
        printedWithParens);
  }

  // 4. Ida e volta (round-trip)

  private void testRoundTripAllValidPrograms() {
    String[] validSources = {
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
      """,
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
      """,
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
      """,
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
      """
    };

    AstPrinterVisitor printer = new AstPrinterVisitor();
    SourceReprinterVisitor reprinter = new SourceReprinterVisitor();

    for (int i = 0; i < validSources.length; i++) {
      String label = "Programa válido #" + (i + 1);
      ParseResult firstParse = new Parser(validSources[i]).parse();
      assertTrue(label + ": 1º parse deve ser aceito", firstParse.isSuccessful());
      assertTrue(label + ": 1º parse deve produzir AST não nula", firstParse.hasAst());

      String firstTreeDump = printer.print(firstParse.ast());
      String firstReprintedSource = reprinter.reprint(firstParse.ast());

      ParseResult secondParse = new Parser(firstReprintedSource).parse();
      assertTrue(
          label + ": 2º parse (sobre código reimpresso) deve ser aceito",
          secondParse.isSuccessful());
      assertTrue(label + ": 2º parse deve produzir AST não nula", secondParse.hasAst());

      String secondTreeDump = printer.print(secondParse.ast());
      String secondReprintedSource = reprinter.reprint(secondParse.ast());

      assertEqual(
          label + " (Round-Trip): 1ª e 2ª ASTs devem ter a mesma saída no impressor",
          firstTreeDump,
          secondTreeDump);
      assertEqual(
          label + " (Round-Trip): 2º texto reimpresso deve ser idêntico ao 1º texto reimpresso",
          firstReprintedSource,
          secondReprintedSource);
    }
  }

  // 5. Regressão do CP2

  private void testRegressionInvalidProgramsAndPanicMode() {
    String missingSemicolon =
        """
        fun void teste() {
            int x = 10
            int y = 20;
        }
        """;
    ParseResult res1 = new Parser(missingSemicolon).parse();
    assertFalse("Regressão CP2 (sem ';'): deve ser rejeitado", res1.isSuccessful());
    assertTrue("Regressão CP2 (sem ';'): AST deve ser nula em caso de erro", res1.ast() == null);
    assertEqual(
        "Regressão CP2 (sem ';'): linha do erro deve ser 3", 3, res1.errors().get(0).line());

    String malformedAssign =
        """
        fun void teste() {
            int x = 0;
            x = ;
            int y = 10;
        }
        """;
    ParseResult res2 = new Parser(malformedAssign).parse();
    assertFalse("Regressão CP2 (atribuição inválida): deve ser rejeitado", res2.isSuccessful());
    assertTrue("Regressão CP2 (atribuição inválida): AST deve ser nula", res2.ast() == null);
    assertTrue(
        "Regressão CP2 (atribuição inválida): token causador deve ser ';'",
        res2.errors().get(0).offendingToken().is(TokenType.SEMICOLON));

    String multipleErrors =
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
    ParseResult resPanic = new Parser(multipleErrors).parse();
    assertFalse("Regressão CP2 (modo pânico): deve ser rejeitado", resPanic.isSuccessful());
    assertTrue("Regressão CP2 (modo pânico): AST deve ser nula", resPanic.ast() == null);
    List<SyntaxError> erros = resPanic.errors();
    assertTrue("Regressão CP2 (modo pânico): deve reportar >= 3 erros", erros.size() >= 3);
    assertEqual("Regressão CP2 (modo pânico): 1º erro na linha 2", 2, erros.get(0).line());
    assertEqual("Regressão CP2 (modo pânico): 2º erro na linha 4", 4, erros.get(1).line());
    assertEqual(
        "Regressão CP2 (modo pânico): último erro na linha 8",
        8,
        erros.get(erros.size() - 1).line());
  }

  // Utilitários de teste

  private void verifyExpectedTreeFile(String description, String source, String relativeFilePath) {
    ParseResult result = new Parser(source).parse();
    assertTrue(description + " (parse aceito)", result.isSuccessful());
    assertTrue(description + " (AST presente)", result.hasAst());

    AstPrinterVisitor printer = new AstPrinterVisitor();
    String actualTree = normalizeNewlines(printer.print(result.ast()));
    String expectedTree = normalizeNewlines(readExpectedFile(relativeFilePath));

    assertEqual(description, expectedTree, actualTree);
  }

  private ExprNode parseSingleInitializerExpression(String varDeclSource) {
    ParseResult result = new Parser(varDeclSource).parse();
    assertTrue("Declaração de expressão deve ser aceita: " + varDeclSource, result.isSuccessful());
    VarDeclNode varDecl = (VarDeclNode) result.ast().getDeclarations().get(0);
    return varDecl.getInitializer();
  }

  private String readExpectedFile(String relativePath) {
    Path path = Paths.get(relativePath);
    if (!Files.exists(path)) {
      fail("Leitura do arquivo esperado: " + relativePath, "arquivo existente", "não encontrado");
      return "";
    }
    try {
      return Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException e) {
      fail("Leitura do arquivo esperado: " + relativePath, "sucesso", e.getMessage());
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
    System.out.println("  INICIANDO SUÍTE DE TESTES — AST E VISITORS (LUMINA)     ");
    System.out.println("==========================================================");
  }

  private void printSummary() {
    System.out.println("----------------------------------------------------------");
    System.out.printf("Testes Concluídos: %d Aprovados, %d Falhas%n", testsPassed, testsFailed);
    System.out.println("==========================================================");
  }

  private void evaluateExitStatus() {
    if (testsFailed > 0) {
      System.err.printf("FALHA: %d teste(s) de AST não passaram.%n", testsFailed);
      System.exit(1);
    } else {
      System.out.println("RESULTADO: TODOS OS TESTES DE AST E VISITORS PASSARAM COM SUCESSO!");
    }
  }
}
