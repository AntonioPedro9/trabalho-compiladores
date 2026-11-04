package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.lexer.token.TokenType;
import lumina.visitor.Visitor;

/** Nó de operação unária prefixa (Unary ::= "!" Unary | "-" Unary | "+" Unary). */
public final class UnaryNode extends ExprNode {

  private final TokenType operator;
  private final ExprNode operand;

  public UnaryNode(SourcePosition position, TokenType operator, ExprNode operand) {
    super(position);
    this.operator = Objects.requireNonNull(operator, "O operador unário não pode ser nulo.");
    this.operand = Objects.requireNonNull(operand, "O operando unário não pode ser nulo.");
  }

  public TokenType getOperator() {
    return operator;
  }

  public String getOperatorSymbol() {
    return operator.getDescription();
  }

  public ExprNode getOperand() {
    return operand;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitUnary(this);
  }
}
