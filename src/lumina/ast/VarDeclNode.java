package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/**
 * Nó de declaração de variável (VarDecl ::= Type IDENTIFIER VarInitOpt ";"). O atributo {@code
 * initializer} pode ser {@code null} quando a variável é declarada sem valor inicial.
 */
public final class VarDeclNode extends DeclNode {

  private final TypeNode type;
  private final String name;
  private final ExprNode initializer;

  public VarDeclNode(SourcePosition position, TypeNode type, String name, ExprNode initializer) {
    super(position);
    this.type = Objects.requireNonNull(type, "O tipo da variável não pode ser nulo.");
    this.name = Objects.requireNonNull(name, "O nome da variável não pode ser nulo.");
    this.initializer = initializer;
  }

  public TypeNode getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public ExprNode getInitializer() {
    return initializer;
  }

  public boolean hasInitializer() {
    return initializer != null;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitVarDecl(this);
  }
}
