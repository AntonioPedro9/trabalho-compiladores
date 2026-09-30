package lumina.lexer.error;

import java.util.Objects;
import lumina.common.source.SourcePosition;

/**
 * Representa um erro léxico com mensagem descritiva, coordenada no código-fonte e lexema causador.
 *
 * @param message mensagem descritiva do erro
 * @param position coordenada do erro no código-fonte
 * @param offendingLexeme trecho ou lexema que gerou a anomalia
 */
public record LexicalError(String message, SourcePosition position, String offendingLexeme) {

  /**
   * Construtor canônico com validação de campos obrigatórios.
   *
   * @param message mensagem descritiva do erro
   * @param position coordenada do erro no código-fonte
   * @param offendingLexeme trecho ou lexema que gerou a anomalia
   */
  public LexicalError {
    Objects.requireNonNull(message, "A mensagem de erro não pode ser nula.");
    Objects.requireNonNull(position, "A posição do erro não pode ser nula.");
    offendingLexeme = offendingLexeme != null ? offendingLexeme : "";
  }

  /**
   * Cria um erro léxico a partir de coordenadas inteiras de linha e coluna.
   *
   * @param message mensagem descritiva do erro
   * @param line linha onde o erro ocorreu
   * @param column coluna onde o erro ocorreu
   * @param offendingLexeme trecho que gerou a anomalia
   */
  public LexicalError(String message, int line, int column, String offendingLexeme) {
    this(message, new SourcePosition(line, column), offendingLexeme);
  }

  /**
   * Retorna a mensagem descritiva do erro léxico.
   *
   * @return mensagem de erro
   */
  public String getMessage() {
    return message;
  }

  /**
   * Retorna a coordenada do erro no código-fonte.
   *
   * @return posição do erro
   */
  public SourcePosition getPosition() {
    return position;
  }

  /**
   * Retorna o número da linha onde o erro ocorreu.
   *
   * @return número da linha
   */
  public int getLine() {
    return position.line();
  }

  /**
   * Retorna o número da coluna onde o erro ocorreu.
   *
   * @return número da coluna
   */
  public int getColumn() {
    return position.column();
  }

  /**
   * Retorna o trecho do código-fonte causador do erro léxico.
   *
   * @return lexema causador da anomalia
   */
  public String getOffendingLexeme() {
    return offendingLexeme;
  }

  /**
   * Retorna a representação textual formatada do erro léxico.
   *
   * @return mensagem formatada com coordenadas e trecho causador
   */
  @Override
  public String toString() {
    if (offendingLexeme.isEmpty()) {
      return String.format("%s Erro lexico: %s", position, message);
    }
    return String.format("%s Erro lexico: %s [trecho: \"%s\"]", position, message, offendingLexeme);
  }
}
