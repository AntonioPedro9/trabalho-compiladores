package lumina.ast;

import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/**
 * Nó de comando de retorno (ReturnStmt ::= "return" ExprOpt ";"). O atributo {@code expression}
 * pode ser {@code null} em retornos vazios de procedimentos {@code void}.
 */
public final class ReturnNode extends StmtNode {

  private final ExprNode expression;

  public ReturnNode(SourcePosition position, ExprNode expression) {
    super(position);
    this.expression = expression;
  }

  public ExprNode getExpression() {
    return expression;
  }

  public boolean hasExpression() {
    return expression != null;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitReturn(this);
  }
}
