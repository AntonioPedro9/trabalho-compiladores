package lumina.ast;

import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/** Nó de literal lógico booleano (Primary ::= "true" | "false"). */
public final class BoolLitNode extends ExprNode {

  private final boolean value;

  public BoolLitNode(SourcePosition position, boolean value) {
    super(position);
    this.value = value;
  }

  public boolean getValue() {
    return value;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitBoolLit(this);
  }
}
