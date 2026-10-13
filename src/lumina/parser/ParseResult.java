package lumina.parser;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lumina.parser.error.SyntaxError;

/** Guarda o resultado da análise sintática: se o programa foi aceito e a lista de erros. */
public record ParseResult(boolean isSuccessful, List<SyntaxError> errors) {

  public ParseResult {
    Objects.requireNonNull(errors, "A lista de erros não pode ser nula.");
    errors = Collections.unmodifiableList(errors);
  }

  public static ParseResult accepted() {
    return new ParseResult(true, List.of());
  }

  public static ParseResult rejected(List<SyntaxError> errors) {
    return new ParseResult(false, errors);
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public int getErrorCount() {
    return errors.size();
  }
}
