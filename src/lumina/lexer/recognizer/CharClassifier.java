package lumina.lexer.recognizer;

/** Utilitário responsável pela classificação de caracteres de entrada da linguagem Lumina. */
public final class CharClassifier {

  /** Construtor privado para evitar instanciação da classe utilitária. */
  private CharClassifier() {}

  /**
   * Verifica se o caractere pode iniciar um identificador (letra ASCII ou '_').
   *
   * @param c caractere a ser verificado
   * @return {@code true} se for válido como início de identificador, {@code false} caso contrário
   */
  public static boolean isIdentifierStart(char c) {
    return isAsciiLetter(c) || c == '_';
  }

  /**
   * Verifica se o caractere pode compor um identificador (letra ASCII, dígito ou '_').
   *
   * @param c caractere a ser verificado
   * @return {@code true} se for válido como parte de identificador, {@code false} caso contrário
   */
  public static boolean isIdentifierPart(char c) {
    return isIdentifierStart(c) || isDigit(c);
  }

  /**
   * Verifica se o caractere é um dígito decimal ('0' a '9').
   *
   * @param c caractere a ser verificado
   * @return {@code true} se for dígito decimal, {@code false} caso contrário
   */
  public static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  /**
   * Verifica se o caractere é um espaço em branco (' ', '\t', '\r', '\n').
   *
   * @param c caractere a ser verificado
   * @return {@code true} se for espaço em branco, {@code false} caso contrário
   */
  public static boolean isWhitespace(char c) {
    return c == ' ' || c == '\t' || c == '\r' || c == '\n';
  }

  /**
   * Verifica se o caractere é o início de um operador ou delimitador da linguagem Lumina.
   *
   * @param c caractere a ser verificado
   * @return {@code true} se for início de operador ou delimitador, {@code false} caso contrário
   */
  public static boolean isOperatorOrDelimiterStart(char c) {
    return switch (c) {
      case '+',
          '-',
          '*',
          '/',
          '%',
          '=',
          '!',
          '<',
          '>',
          '&',
          '|',
          '(',
          ')',
          '{',
          '}',
          '[',
          ']',
          ';',
          ',',
          ':' ->
          true;
      default -> false;
    };
  }

  /**
   * Verifica se o caractere é uma letra do alfabeto ASCII ('a'-'z' ou 'A'-'Z').
   *
   * @param c caractere a ser verificado
   * @return {@code true} se for letra ASCII, {@code false} caso contrário
   */
  private static boolean isAsciiLetter(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }
}
