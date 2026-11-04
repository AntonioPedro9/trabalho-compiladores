package lumina.ast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/** Nó raiz da AST representando um programa completo em Lumina (Program ::= DeclList EOF). */
public final class ProgramNode extends Node {

  private final List<DeclNode> declarations;

  public ProgramNode(SourcePosition position, List<DeclNode> declarations) {
    super(position);
    Objects.requireNonNull(declarations, "A lista de declarações não pode ser nula.");
    this.declarations = Collections.unmodifiableList(declarations);
  }

  public List<DeclNode> getDeclarations() {
    return declarations;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitProgram(this);
  }
}
