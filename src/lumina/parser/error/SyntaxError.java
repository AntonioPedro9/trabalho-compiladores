package lumina.parser.error;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.lexer.token.Token;

/**
 * Representa um erro sintático encontrado durante o parsing, guardando a mensagem, a posição no
 * código-fonte e o token onde ocorreu.
 */
public record SyntaxError(String message, SourcePosition position, Token offendingToken) {

  public SyntaxError {
    Objects.requireNonNull(message, "A mensagem do erro não pode ser nula.");
    Objects.requireNonNull(position, "A coordenada de posição não pode ser nula.");
    Objects.requireNonNull(offendingToken, "O token do erro não pode ser nulo.");
  }

  public int line() {
    return position.line();
  }

  public int column() {
    return position.column();
  }

  @Override
  public String toString() {
    return String.format(
        "Erro Sintático [linha %d, coluna %d]: %s (encontrado: '%s' [%s])",
        line(), column(), message, offendingToken.getLexeme(), offendingToken.getType().name());
  }
}
