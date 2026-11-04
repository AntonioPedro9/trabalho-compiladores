package lumina;

import lumina.ast.AstTest;
import lumina.lexer.ScannerTest;
import lumina.parser.ParserTest;

/** Executa todos os testes do compilador (Scanner, Parser e AST/Visitors). */
public final class TestRunner {

  private TestRunner() {}

  public static void main(String[] args) {
    System.out.println(
        "================================================================================");
    System.out.println(
        "               SUÍTE DE TESTES (SCANNER, PARSER E AST/VISITOR)                  ");
    System.out.println(
        "================================================================================\n");

    System.out.println(">>> TESTES DO SCANNER (CP1)");
    ScannerTest scannerSuite = new ScannerTest();
    scannerSuite.runAllTests();

    System.out.println("\n>>> TESTES DO PARSER (CP2)");
    ParserTest parserSuite = new ParserTest();
    parserSuite.runAllTests();

    System.out.println("\n>>> TESTES DA AST E VISITORS (CP3)");
    AstTest astSuite = new AstTest();
    astSuite.runAllTests();

    System.out.println(
        "\n================================================================================");
    System.out.println(
        "                     TODOS OS TESTES FORAM EXECUTADOS!                          ");
    System.out.println(
        "================================================================================");
  }
}
