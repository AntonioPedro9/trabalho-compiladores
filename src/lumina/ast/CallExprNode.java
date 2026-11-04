package lumina.ast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/** Nó de chamada de função dentro de uma expressão (Primary ::= IDENTIFIER "(" ArgListOpt ")"). */
public final class CallExprNode extends ExprNode {

  private final String callee;
  private final List<ExprNode> arguments;

  public CallExprNode(SourcePosition position, String callee, List<ExprNode> arguments) {
    super(position);
    this.callee = Objects.requireNonNull(callee, "O nome da função não pode ser nulo.");
    Objects.requireNonNull(arguments, "A lista de argumentos não pode ser nula.");
    this.arguments = Collections.unmodifiableList(arguments);
  }

  public String getCallee() {
    return callee;
  }

  public List<ExprNode> getArguments() {
    return arguments;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitCallExpr(this);
  }
}
