package lumina;

import lumina.lexer.ScannerTest;
import lumina.parser.ParserTest;

/** Executa todos os testes do compilador (Scanner e Parser). */
public final class TestRunner {

  private TestRunner() {}

  public static void main(String[] args) {
    System.out.println(
        "================================================================================");
    System.out.println(
        "                   SUÍTE DE TESTES (SCANNER E PARSER)                           ");
    System.out.println(
        "================================================================================\n");

    System.out.println(">>> TESTES DO SCANNER");
    ScannerTest scannerSuite = new ScannerTest();
    scannerSuite.runAllTests();

    System.out.println("\n>>> TESTES DO PARSER");
    ParserTest parserSuite = new ParserTest();
    parserSuite.runAllTests();

    System.out.println(
        "\n================================================================================");
    System.out.println(
        "                     TODOS OS TESTES FORAM EXECUTADOS!                          ");
    System.out.println(
        "================================================================================");
  }
}
