package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.symbols.Symbol;
import lumina.visitor.Visitor;

/** Nó de comando de atribuição (AssignOrCallStmt ::= IDENTIFIER "=" Expr ";"). */
public final class AssignNode extends StmtNode {

  private final String targetName;
  private final ExprNode value;
  public Symbol decl;

  public AssignNode(SourcePosition position, String targetName, ExprNode value) {
    super(position);
    this.targetName =
        Objects.requireNonNull(targetName, "O identificador de destino não pode ser nulo.");
    this.value = Objects.requireNonNull(value, "A expressão atribuída não pode ser nula.");
  }

  public String getTargetName() {
    return targetName;
  }

  public ExprNode getValue() {
    return value;
  }

  public Symbol getDecl() {
    return decl;
  }

  public void setDecl(Symbol decl) {
    this.decl = decl;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitAssign(this);
  }
}
