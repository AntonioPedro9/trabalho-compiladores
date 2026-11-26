package lumina.ast;

import lumina.common.source.SourcePosition;
import lumina.types.LuminaType;

/**
 * Classe abstrata base para todas as expressões da linguagem Lumina, guardando o tipo calculado na
 * verificação semântica.
 */
public abstract class ExprNode extends Node {

  private LuminaType type;

  protected ExprNode(SourcePosition position) {
    super(position);
  }

  public LuminaType getType() {
    return type;
  }

  public void setType(LuminaType type) {
    this.type = type;
  }
}
