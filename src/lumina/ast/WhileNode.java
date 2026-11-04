package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/** Nó de repetição condicional (WhileStmt ::= "while" "(" Expr ")" Block). */
public final class WhileNode extends StmtNode {

  private final ExprNode condition;
  private final BlockNode body;

  public WhileNode(SourcePosition position, ExprNode condition, BlockNode body) {
    super(position);
    this.condition = Objects.requireNonNull(condition, "A condição do while não pode ser nula.");
    this.body = Objects.requireNonNull(body, "O corpo do while não pode ser nulo.");
  }

  public ExprNode getCondition() {
    return condition;
  }

  public BlockNode getBody() {
    return body;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitWhile(this);
  }
}
