package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/**
 * Nó de seleção condicional (IfStmt ::= "if" "(" Expr ")" Block ElseOpt). O atributo {@code
 * elseBranch} pode ser {@code null} quando não há cláusula {@code else}.
 */
public final class IfNode extends StmtNode {

  private final ExprNode condition;
  private final BlockNode thenBranch;
  private final BlockNode elseBranch;

  public IfNode(
      SourcePosition position, ExprNode condition, BlockNode thenBranch, BlockNode elseBranch) {
    super(position);
    this.condition = Objects.requireNonNull(condition, "A condição do if não pode ser nula.");
    this.thenBranch = Objects.requireNonNull(thenBranch, "O bloco then não pode ser nulo.");
    this.elseBranch = elseBranch;
  }

  public ExprNode getCondition() {
    return condition;
  }

  public BlockNode getThenBranch() {
    return thenBranch;
  }

  public BlockNode getElseBranch() {
    return elseBranch;
  }

  public boolean hasElseBranch() {
    return elseBranch != null;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitIf(this);
  }
}
