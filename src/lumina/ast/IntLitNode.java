package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/** Nó de literal inteiro (Primary ::= INT_LITERAL). */
public final class IntLitNode extends ExprNode {

  private final int value;
  private final String rawLexeme;

  public IntLitNode(SourcePosition position, int value, String rawLexeme) {
    super(position);
    this.value = value;
    this.rawLexeme = Objects.requireNonNull(rawLexeme, "O lexema não pode ser nulo.");
  }

  public int getValue() {
    return value;
  }

  public String getRawLexeme() {
    return rawLexeme;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitIntLit(this);
  }
}
