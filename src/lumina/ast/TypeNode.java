package lumina.ast;

import java.util.Objects;
import lumina.common.source.SourcePosition;
import lumina.lexer.token.TokenType;
import lumina.visitor.Visitor;

/**
 * Nó representando um especificador de tipo primitivo ou de retorno (int, double, bool, char,
 * string ou void).
 */
public final class TypeNode extends Node {

  private final TokenType typeToken;

  public TypeNode(SourcePosition position, TokenType typeToken) {
    super(position);
    this.typeToken = Objects.requireNonNull(typeToken, "O token de tipo não pode ser nulo.");
  }

  public TokenType getTypeToken() {
    return typeToken;
  }

  public String getTypeName() {
    return typeToken.getDescription();
  }

  @Override
  public <T> T accept(Visitor<T> v) {
    return v.visitType(this);
  }
}
