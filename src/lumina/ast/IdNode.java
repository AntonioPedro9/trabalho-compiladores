package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/**
 * Nó de referência a identificador em expressão (Primary ::= IDENTIFIER). Projetado para apontar
 * para a entrada correspondente da Tabela de Símbolos nas próximas etapas sem mudar a estrutura da
 * árvore.
 */
public final class IdNode extends ExprNode {

  private final String name;

  public IdNode(SourcePosition position, String name) {
    super(position);
    this.name = Objects.requireNonNull(name, "O nome do identificador não pode ser nulo.");
  }

  public String getName() {
    return name;
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitId(this);
  }
}
