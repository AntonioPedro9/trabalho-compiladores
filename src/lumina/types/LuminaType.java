package lumina.types;

import lumina.lexer.token.TokenType;

/**
 * Representa os tipos semânticos da linguagem Lumina, incluindo {@link #VOID} para procedimentos e
 * {@link #ERROR} para conter erros em cascata.
 */
public enum LuminaType {
  INT("int"),
  DOUBLE("double"),
  BOOL("bool"),
  CHAR("char"),
  STRING("string"),
  VOID("void"),
  ERROR("<erro>");

  private final String description;

  LuminaType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public boolean isError() {
    return this == ERROR;
  }

  public boolean isVoid() {
    return this == VOID;
  }

  public boolean isBoolean() {
    return this == BOOL;
  }

  public boolean isNumeric() {
    return this == INT || this == DOUBLE;
  }

  /**
   * Verifica se um valor de {@code sourceType} pode ser atribuído a um destino deste tipo,
   * aceitando promoção implícita de {@code int} para {@code double}.
   *
   * @param sourceType tipo da expressão de origem
   * @return {@code true} se os tipos forem compatíveis para atribuição
   */
  public boolean isAssignableFrom(LuminaType sourceType) {
    if (involvesError(sourceType)) {
      return true;
    }
    if (involvesVoid(sourceType)) {
      return false;
    }
    if (this == sourceType) {
      return true;
    }
    return isImplicitIntToDoubleWidening(sourceType);
  }

  /**
   * Converte o token de tipo sintático no {@link LuminaType} correspondente.
   *
   * @param tokenType token do especificador de tipo
   * @return tipo semântico equivalente, ou {@link #ERROR} caso inválido
   */
  public static LuminaType fromTokenType(TokenType tokenType) {
    if (tokenType == null) {
      return ERROR;
    }
    if (tokenType == TokenType.INT) {
      return INT;
    }
    if (tokenType == TokenType.DOUBLE) {
      return DOUBLE;
    }
    if (tokenType == TokenType.BOOL) {
      return BOOL;
    }
    if (tokenType == TokenType.CHAR) {
      return CHAR;
    }
    if (tokenType == TokenType.STRING) {
      return STRING;
    }
    if (tokenType == TokenType.VOID) {
      return VOID;
    }
    return ERROR;
  }

  private boolean involvesError(LuminaType other) {
    return this.isError() || other == null || other.isError();
  }

  private boolean involvesVoid(LuminaType other) {
    return this.isVoid() || other.isVoid();
  }

  private boolean isImplicitIntToDoubleWidening(LuminaType sourceType) {
    return this == DOUBLE && sourceType == INT;
  }

  @Override
  public String toString() {
    return description;
  }
}
