package lumina;

import lumina.ast.AstTest;
import lumina.lexer.ScannerTest;
import lumina.parser.ParserTest;
import lumina.symbols.SymbolTableTest;
import lumina.types.OperatorRulesTest;
import lumina.visitor.IdentificationTest;
import lumina.visitor.TypeCheckerTest;

/**
 * Executa todas as suítes de testes do compilador (Scanner, Parser, AST, Tabela de Símbolos,
 * Identificação e Verificador de Tipos).
 */
public final class TestRunner {

  private TestRunner() {}

  public static void main(String[] args) {
    System.out.println(
        "================================================================================");
    System.out.println(
        "   SUÍTE DE TESTES (SCANNER, PARSER, AST, IDENTIFICAÇÃO E VERIFICAÇÃO DE TIPOS) ");
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

    System.out.println("\n>>> TESTES DA TABELA DE SÍMBOLOS (CP4)");
    SymbolTableTest symbolTableSuite = new SymbolTableTest();
    symbolTableSuite.runAllTests();

    System.out.println("\n>>> TESTES DA PASSADA DE IDENTIFICAÇÃO (CP4)");
    IdentificationTest identificationSuite = new IdentificationTest();
    identificationSuite.runAllTests();

    System.out.println("\n>>> TESTES ISOLADOS DA TABELA DE OPERADORES (CP5)");
    OperatorRulesTest operatorRulesSuite = new OperatorRulesTest();
    operatorRulesSuite.runAllTests();

    System.out.println("\n>>> TESTES DO VERIFICADOR DE TIPOS (CP5)");
    TypeCheckerTest typeCheckerSuite = new TypeCheckerTest();
    typeCheckerSuite.runAllTests();

    System.out.println(
        "\n================================================================================");
    System.out.println(
        "                     TODOS OS TESTES FORAM EXECUTADOS!                          ");
    System.out.println(
        "================================================================================");
  }
}
