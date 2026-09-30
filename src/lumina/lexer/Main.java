package lumina.lexer;

/**
 * Ponto de entrada CLI para execução do Analisador Léxico (Checkpoint 1). Mantido no pacote
 * lumina.lexer para retrocompatibilidade total com comandos existentes, delegando a execução para a
 * classe principal {@link lumina.Main}.
 */
public final class Main {

  /** Construtor privado para evitar instanciação da classe utilitária. */
  private Main() {}

  /**
   * Encaminha os argumentos de execução para a classe principal.
   *
   * @param args argumentos de linha de comando
   */
  public static void main(String[] args) {
    lumina.Main.main(args);
  }
}
