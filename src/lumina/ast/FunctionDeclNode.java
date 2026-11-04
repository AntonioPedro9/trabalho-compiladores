package lumina.ast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/**
 * Nó de declaração de função ou procedimento (FunctionDecl ::= "fun" ReturnType IDENTIFIER "("
 * ParamListOpt ")" Block).
 */
public final class FunctionDeclNode extends DeclNode {

  private final TypeNode returnType;
  private final String name;
  private final List<ParamNode> params;
  private final BlockNode body;

  public FunctionDeclNode(
      SourcePosition position,
      TypeNode returnType,
      String name,
      List<ParamNode> params,
      BlockNode body) {
    super(position);
    this.returnType = Objects.requireNonNull(returnType, "O tipo de retorno não pode ser nulo.");
    this.name = Objects.requireNonNull(name, "O nome da função não pode ser nulo.");
    Objects.requireNonNull(params, "A lista de parâmetros não pode ser nula.");
    this.params = Collections.unmodifiableList(params);
    this.body = Objects.requireNonNull(body, "O bloco da função não pode ser nulo.");
  }

  public TypeNode getReturnType() {
    return returnType;
  }

  public String getName() {
    return name;
  }

  public List<ParamNode> getParams() {
    return params;
  }

  public BlockNode getBody() {
    return body;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitFunctionDecl(this);
  }
}
