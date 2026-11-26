package lumina.types;

import java.util.Objects;

/** Representa um aviso semântico emitido para variáveis potencialmente não inicializadas. */
public record TypeWarning(int line, String variableName, String message) {

  public TypeWarning {
    Objects.requireNonNull(variableName, "O nome da variável não pode ser nulo.");
    Objects.requireNonNull(message, "A mensagem de aviso não pode ser nula.");
  }

  public static TypeWarning possiblyUninitializedVariable(
      int line, String variableName, LuminaType variableType) {
    return new TypeWarning(
        line,
        variableName,
        String.format(
            "linha %d: aviso: variável '%s' (%s) pode ser usada sem valor inicial",
            line, variableName, variableType));
  }

  @Override
  public String toString() {
    return message;
  }
}
