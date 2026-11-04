package lumina.parser;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lumina.ast.ProgramNode;
import lumina.parser.error.SyntaxError;

/**
 * Guarda o resultado da análise sintática: se o programa foi aceito, a raiz da AST (nula quando
 * houver erros sintáticos) e a lista imutável de erros encontrados.
 */
public record ParseResult(boolean isSuccessful, ProgramNode ast, List<SyntaxError> errors) {

  public ParseResult {
    Objects.requireNonNull(errors, "A lista de erros não pode ser nula.");
    errors = Collections.unmodifiableList(errors);
    if (!isSuccessful || !errors.isEmpty()) {
      ast = null;
    }
  }

  public static ParseResult accepted(ProgramNode ast) {
    return new ParseResult(
        true,
        Objects.requireNonNull(ast, "A AST de um programa aceito não pode ser nula."),
        List.of());
  }

  public static ParseResult rejected(List<SyntaxError> errors) {
    return new ParseResult(false, null, errors);
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public boolean hasAst() {
    return isSuccessful && ast != null && errors.isEmpty();
  }

  public int getErrorCount() {
    return errors.size();
  }
}
