package lumina.types;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lumina.lexer.token.TokenType;

/**
 * Tabela de compatibilidade e tipo resultante para os operadores binários e unários da linguagem
 * Lumina.
 */
public final class OperatorTypeTable {

  private final Map<BinarySignature, LuminaType> binaryRules;
  private final Map<UnarySignature, LuminaType> unaryRules;

  public OperatorTypeTable() {
    Map<BinarySignature, LuminaType> binaryTable = new LinkedHashMap<>();
    Map<UnarySignature, LuminaType> unaryTable = new LinkedHashMap<>();

    registerArithmeticRules(binaryTable);
    registerRelationalRules(binaryTable);
    registerEqualityRules(binaryTable);
    registerLogicalRules(binaryTable);
    registerUnaryRules(unaryTable);

    this.binaryRules = Collections.unmodifiableMap(binaryTable);
    this.unaryRules = Collections.unmodifiableMap(unaryTable);
  }

  /**
   * Retorna o tipo resultante da operação binária, {@link LuminaType#ERROR} se algum operando já
   * possuir erro, ou {@code null} quando a combinação não é suportada.
   *
   * @param operator token do operador binário
   * @param left tipo do operando esquerdo
   * @param right tipo do operando direito
   * @return tipo do resultado ou {@code null} se incompatível
   */
  public LuminaType resolveBinary(TokenType operator, LuminaType left, LuminaType right) {
    Objects.requireNonNull(operator, "O operador binário não pode ser nulo.");
    if (hasErrorOperand(left, right)) {
      return LuminaType.ERROR;
    }
    return binaryRules.get(new BinarySignature(operator, left, right));
  }

  /**
   * Retorna o tipo resultante da operação unária, {@link LuminaType#ERROR} se o operando já possuir
   * erro, ou {@code null} quando o tipo não é aceito pelo operador.
   *
   * @param operator token do operador unário
   * @param operand tipo do operando
   * @return tipo do resultado ou {@code null} se incompatível
   */
  public LuminaType resolveUnary(TokenType operator, LuminaType operand) {
    Objects.requireNonNull(operator, "O operador unário não pode ser nulo.");
    if (isErrorType(operand)) {
      return LuminaType.ERROR;
    }
    return unaryRules.get(new UnarySignature(operator, operand));
  }

  private void registerArithmeticRules(Map<BinarySignature, LuminaType> table) {
    TokenType[] standardArithmetic = {
      TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH
    };
    for (TokenType operator : standardArithmetic) {
      registerBinary(table, operator, LuminaType.INT, LuminaType.INT, LuminaType.INT);
      registerBinary(table, operator, LuminaType.DOUBLE, LuminaType.DOUBLE, LuminaType.DOUBLE);
      registerBinary(table, operator, LuminaType.INT, LuminaType.DOUBLE, LuminaType.DOUBLE);
      registerBinary(table, operator, LuminaType.DOUBLE, LuminaType.INT, LuminaType.DOUBLE);
    }
    registerBinary(table, TokenType.PERCENT, LuminaType.INT, LuminaType.INT, LuminaType.INT);
  }

  private void registerRelationalRules(Map<BinarySignature, LuminaType> table) {
    TokenType[] relationalOperators = {
      TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL
    };
    for (TokenType operator : relationalOperators) {
      registerBinary(table, operator, LuminaType.INT, LuminaType.INT, LuminaType.BOOL);
      registerBinary(table, operator, LuminaType.DOUBLE, LuminaType.DOUBLE, LuminaType.BOOL);
      registerBinary(table, operator, LuminaType.INT, LuminaType.DOUBLE, LuminaType.BOOL);
      registerBinary(table, operator, LuminaType.DOUBLE, LuminaType.INT, LuminaType.BOOL);
      registerBinary(table, operator, LuminaType.CHAR, LuminaType.CHAR, LuminaType.BOOL);
    }
  }

  private void registerEqualityRules(Map<BinarySignature, LuminaType> table) {
    TokenType[] equalityOperators = {TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL};
    LuminaType[] comparableSameTypes = {
      LuminaType.INT, LuminaType.DOUBLE, LuminaType.BOOL, LuminaType.CHAR, LuminaType.STRING
    };
    for (TokenType operator : equalityOperators) {
      for (LuminaType type : comparableSameTypes) {
        registerBinary(table, operator, type, type, LuminaType.BOOL);
      }
      registerBinary(table, operator, LuminaType.INT, LuminaType.DOUBLE, LuminaType.BOOL);
      registerBinary(table, operator, LuminaType.DOUBLE, LuminaType.INT, LuminaType.BOOL);
    }
  }

  private void registerLogicalRules(Map<BinarySignature, LuminaType> table) {
    registerBinary(
        table, TokenType.AMPERSAND_AMPERSAND, LuminaType.BOOL, LuminaType.BOOL, LuminaType.BOOL);
    registerBinary(table, TokenType.PIPE_PIPE, LuminaType.BOOL, LuminaType.BOOL, LuminaType.BOOL);
  }

  private void registerUnaryRules(Map<UnarySignature, LuminaType> table) {
    registerUnary(table, TokenType.PLUS, LuminaType.INT, LuminaType.INT);
    registerUnary(table, TokenType.PLUS, LuminaType.DOUBLE, LuminaType.DOUBLE);
    registerUnary(table, TokenType.MINUS, LuminaType.INT, LuminaType.INT);
    registerUnary(table, TokenType.MINUS, LuminaType.DOUBLE, LuminaType.DOUBLE);
    registerUnary(table, TokenType.BANG, LuminaType.BOOL, LuminaType.BOOL);
  }

  private void registerBinary(
      Map<BinarySignature, LuminaType> table,
      TokenType operator,
      LuminaType left,
      LuminaType right,
      LuminaType result) {
    table.put(new BinarySignature(operator, left, right), result);
  }

  private void registerUnary(
      Map<UnarySignature, LuminaType> table,
      TokenType operator,
      LuminaType operand,
      LuminaType result) {
    table.put(new UnarySignature(operator, operand), result);
  }

  private boolean hasErrorOperand(LuminaType left, LuminaType right) {
    return isErrorType(left) || isErrorType(right);
  }

  private boolean isErrorType(LuminaType type) {
    return type == null || type.isError();
  }

  private record BinarySignature(TokenType operator, LuminaType left, LuminaType right) {}

  private record UnarySignature(TokenType operator, LuminaType operand) {}
}
