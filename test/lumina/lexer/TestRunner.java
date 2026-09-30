package lumina.lexer;

/** Ponto de entrada padrão para execução da suíte completa de testes unitários do scanner. */
public final class TestRunner {

  public static void main(String[] args) {
    ScannerTest testSuite = new ScannerTest();
    testSuite.runAllTests();
  }
}
