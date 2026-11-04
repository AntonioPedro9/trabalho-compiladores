package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/** Nó de literal de cadeia de caracteres (Primary ::= STRING_LITERAL). */
public final class StringLitNode extends ExprNode {

  private final String rawLexeme;

  public StringLitNode(SourcePosition position, String rawLexeme) {
    super(position);
    this.rawLexeme = Objects.requireNonNull(rawLexeme, "O lexema da string não pode ser nulo.");
  }

  public String getRawLexeme() {
    return rawLexeme;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitStringLit(this);
  }
}
