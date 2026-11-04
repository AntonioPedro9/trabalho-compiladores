package lumina.ast;

import lumina.common.source.SourcePosition;

/** Classe abstrata base para comandos (statements) da linguagem Lumina. */
public abstract class StmtNode extends Node {

  protected StmtNode(SourcePosition position) {
    super(position);
  }
}
