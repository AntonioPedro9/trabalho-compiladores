package lumina.ast;

import lumina.common.source.SourcePosition;

/**
 * Classe abstrata base para todas as expressões da linguagem Lumina. Projetada para receber a
 * anotação de tipo nas etapas futuras de Análise Semântica sem alterar a estrutura da árvore.
 */
public abstract class ExprNode extends Node {

  protected ExprNode(SourcePosition position) {
    super(position);
  }
}
