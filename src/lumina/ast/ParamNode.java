package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/** Nó representando um parâmetro formal de função ou procedimento (Param ::= Type IDENTIFIER). */
public final class ParamNode extends Node {

  private final TypeNode type;
  private final String name;

  public ParamNode(SourcePosition position, TypeNode type, String name) {
    super(position);
    this.type = Objects.requireNonNull(type, "O tipo do parâmetro não pode ser nulo.");
    this.name = Objects.requireNonNull(name, "O nome do parâmetro não pode ser nulo.");
  }

  public TypeNode getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitParam(this);
  }
}
