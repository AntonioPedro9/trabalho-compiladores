package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/** Nó de literal de ponto flutuante (Primary ::= DOUBLE_LITERAL). */
public final class DoubleLitNode extends ExprNode {

  private final double value;
  private final String rawLexeme;

  public DoubleLitNode(SourcePosition position, double value, String rawLexeme) {
    super(position);
    this.value = value;
    this.rawLexeme = Objects.requireNonNull(rawLexeme, "O lexema não pode ser nulo.");
  }

  public double getValue() {
    return value;
  }

  public String getRawLexeme() {
    return rawLexeme;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitDoubleLit(this);
  }
}
