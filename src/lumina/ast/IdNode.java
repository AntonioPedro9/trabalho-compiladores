package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.symbols.Symbol;
import lumina.visitor.Visitor;

/**
 * Nó de referência a identificador em expressão (Primary ::= IDENTIFIER). Decorado na Passada de
 * Identificação com o ponteiro {@code decl} para a entrada da Tabela de Símbolos.
 */
public final class IdNode extends ExprNode {

  private final String name;
  public Symbol decl;

  public IdNode(SourcePosition position, String name) {
    super(position);
    this.name = Objects.requireNonNull(name, "O nome do identificador não pode ser nulo.");
  }

  public String getName() {
    return name;
  }

  public Symbol getDecl() {
    return decl;
  }

  public void setDecl(Symbol decl) {
    this.decl = decl;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitId(this);
  }
}
