package lumina.parser.error;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lumina.lexer.token.Token;

/** Guarda e imprime os erros sintáticos encontrados pelo parser. */
public final class SyntaxErrorReporter {

  private final List<SyntaxError> errors = new ArrayList<>();

  public SyntaxErrorReporter() {}

  public void report(String message, Token offendingToken) {
    Objects.requireNonNull(message, "A mensagem de erro não pode ser nula.");
    Objects.requireNonNull(offendingToken, "O token causador não pode ser nulo.");
    errors.add(new SyntaxError(message, offendingToken.getPosition(), offendingToken));
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public int getErrorCount() {
    return errors.size();
  }

  public List<SyntaxError> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  public void printReport(PrintStream out) {
    Objects.requireNonNull(out, "O fluxo de saída não pode ser nulo.");
    for (SyntaxError error : errors) {
      out.println(error);
    }
  }

  public void clear() {
    errors.clear();
  }
}
