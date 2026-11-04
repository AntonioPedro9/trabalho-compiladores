package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.lexer.token.TokenType;
import lumina.visitor.Visitor;

/**
 * Nó de operação binária (lógica, de igualdade, relacional, aditiva ou multiplicativa). Preserva a
 * precedência e a associatividade à esquerda construídas pelo parser.
 */
public final class BinNode extends ExprNode {

  private final TokenType operator;
  private final ExprNode left;
  private final ExprNode right;

  public BinNode(SourcePosition position, TokenType operator, ExprNode left, ExprNode right) {
    super(position);
    this.operator = Objects.requireNonNull(operator, "O operador binário não pode ser nulo.");
    this.left = Objects.requireNonNull(left, "O operando esquerdo não pode ser nulo.");
    this.right = Objects.requireNonNull(right, "O operando direito não pode ser nulo.");
  }

  public TokenType getOperator() {
    return operator;
  }

  public String getOperatorSymbol() {
    return operator.getDescription();
  }

  public ExprNode getLeft() {
    return left;
  }

  public ExprNode getRight() {
    return right;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitBin(this);
  }
}
