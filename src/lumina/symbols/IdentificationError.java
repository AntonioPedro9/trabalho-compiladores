package lumina.symbols;

import java.util.Objects;

/**
 * Representa um erro contextual detectado durante a Passada de Identificação (redeclaração no mesmo
 * escopo ou uso de identificador não declarado). Produz mensagens em formato determinístico:
 *
 * <ul>
 *   <li>{@code linha X: 'nome' já declarado neste escopo (linha Y)}
 *   <li>{@code linha X: 'nome' não declarado}
 * </ul>
 */
public final class IdentificationError {

  private final int line;
  private final String identifier;
  private final String message;

  private IdentificationError(int line, String identifier, String message) {
    this.line = line;
    this.identifier = Objects.requireNonNull(identifier, "O identificador não pode ser nulo.");
    this.message = Objects.requireNonNull(message, "A mensagem não pode ser nula.");
  }

  public static IdentificationError redeclared(int line, String identifier, int previousLine) {
    String formatted =
        String.format(
            "linha %d: '%s' já declarado neste escopo (linha %d)", line, identifier, previousLine);
    return new IdentificationError(line, identifier, formatted);
  }

  public static IdentificationError undeclared(int line, String identifier) {
    String formatted = String.format("linha %d: '%s' não declarado", line, identifier);
    return new IdentificationError(line, identifier, formatted);
  }

  public int getLine() {
    return line;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return message;
  }
}
