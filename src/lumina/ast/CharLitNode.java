package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/** Nó de literal de caractere (Primary ::= CHAR_LITERAL). */
public final class CharLitNode extends ExprNode {

  private final String rawLexeme;

  public CharLitNode(SourcePosition position, String rawLexeme) {
    super(position);
    this.rawLexeme = Objects.requireNonNull(rawLexeme, "O lexema do char não pode ser nulo.");
  }

  public String getRawLexeme() {
    return rawLexeme;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitCharLit(this);
  }
}
