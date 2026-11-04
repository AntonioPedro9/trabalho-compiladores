package lumina.ast;

import lumina.common.source.SourcePosition;

/**
 * Classe abstrata base para declarações de escopo global ou local (variáveis e funções). Estende
 * {@link StmtNode} pois declarações de variáveis também podem ocorrer dentro de blocos.
 */
public abstract class DeclNode extends StmtNode {

  protected DeclNode(SourcePosition position) {
    super(position);
  }
}
