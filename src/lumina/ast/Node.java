package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.visitor.Visitor;

/**
 * Classe abstrata base de todos os nós da Árvore de Sintaxe Abstrata (AST) da linguagem Lumina.
 * Guarda a coordenada (linha e coluna) do primeiro token que originou o nó e define o ponto de
 * entrada do padrão Visitor via double dispatch.
 */
public abstract class Node {

  private final SourcePosition position;

  protected Node(SourcePosition position) {
    this.position = Objects.requireNonNull(position, "A posição do nó não pode ser nula.");
  }

  public SourcePosition getPosition() {
    return position;
  }

  public int getLine() {
    return position.line();
  }

  public int getColumn() {
    return position.column();
  }

  public abstract <T> T accept(Visitor<T> v);
}
