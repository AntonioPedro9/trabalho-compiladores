package lumina.types;

import java.util.Objects;

/**
 * Representa um erro semântico de tipo encontrado durante a análise contextual, guardando a linha e
 * a mensagem formatada.
 */
public record TypeError(int line, String message) {

  public TypeError {
    Objects.requireNonNull(message, "A mensagem do erro não pode ser nula.");
  }

  public static TypeError binaryOperatorMismatch(
      int line, String operator, LuminaType leftType, LuminaType rightType) {
    return new TypeError(
        line,
        String.format(
            "linha %d: operador '%s' incompatível com %s e %s",
            line, operator, leftType, rightType));
  }

  public static TypeError unaryOperatorMismatch(int line, String operator, LuminaType operandType) {
    return new TypeError(
        line,
        String.format(
            "linha %d: operador unário '%s' incompatível com %s", line, operator, operandType));
  }

  public static TypeError nonBooleanCondition(
      int line, String statementKeyword, LuminaType actualType) {
    return new TypeError(
        line,
        String.format(
            "linha %d: condição de '%s' deve ser bool, mas é %s",
            line, statementKeyword, actualType));
  }

  public static TypeError invalidInitialization(
      int line, String variableName, LuminaType expectedType, LuminaType actualType) {
    return new TypeError(
        line,
        String.format(
            "linha %d: inicialização de '%s': esperado %s, veio %s",
            line, variableName, expectedType, actualType));
  }

  public static TypeError invalidAssignment(
      int line, String targetName, LuminaType expectedType, LuminaType actualType) {
    return new TypeError(
        line,
        String.format(
            "linha %d: atribuição a '%s': esperado %s, veio %s",
            line, targetName, expectedType, actualType));
  }

  public static TypeError nonAssignableTarget(
      int line, String targetName, String categoryDescription, LuminaType symbolType) {
    return new TypeError(
        line,
        String.format(
            "linha %d: '%s' é %s (%s) e não pode receber atribuição",
            line, targetName, categoryDescription, symbolType));
  }

  public static TypeError uncalledCallableReference(
      int line, String identifier, String categoryDescription, LuminaType returnType) {
    return new TypeError(
        line,
        String.format(
            "linha %d: '%s' é %s (%s) e não pode ser usado sem chamada '()'",
            line, identifier, categoryDescription, returnType));
  }

  public static TypeError notCallable(int line, String identifier, LuminaType actualType) {
    return new TypeError(
        line,
        String.format(
            "linha %d: '%s' do tipo %s não é função ou procedimento",
            line, identifier, actualType));
  }

  public static TypeError procedureInExpression(int line, String procedureName) {
    return new TypeError(
        line,
        String.format(
            "linha %d: procedimento '%s' é void e não pode ser usado em expressão",
            line, procedureName));
  }

  public static TypeError arityMismatch(
      int line, String calleeName, int expectedCount, int actualCount) {
    return new TypeError(
        line,
        String.format(
            "linha %d: '%s' espera %d argumento(s), recebeu %d",
            line, calleeName, expectedCount, actualCount));
  }

  public static TypeError argumentTypeMismatch(
      int line,
      int argumentIndexOneBased,
      String calleeName,
      LuminaType expectedType,
      LuminaType actualType) {
    return new TypeError(
        line,
        String.format(
            "linha %d: argumento %d de '%s': esperado %s, veio %s",
            line, argumentIndexOneBased, calleeName, expectedType, actualType));
  }

  public static TypeError voidProcedureReturningValue(
      int line, String procedureName, LuminaType actualType) {
    return new TypeError(
        line,
        String.format(
            "linha %d: 'return' em procedimento '%s': esperado void, veio %s",
            line, procedureName, actualType));
  }

  public static TypeError emptyReturnInFunction(
      int line, String functionName, LuminaType expectedType) {
    return new TypeError(
        line,
        String.format(
            "linha %d: 'return' vazio em '%s': esperado %s, veio void",
            line, functionName, expectedType));
  }

  public static TypeError returnTypeMismatch(
      int line, String functionName, LuminaType expectedType, LuminaType actualType) {
    return new TypeError(
        line,
        String.format(
            "linha %d: 'return' em '%s': esperado %s, veio %s",
            line, functionName, expectedType, actualType));
  }

  public static TypeError returnOutsideFunction(int line, LuminaType actualType) {
    return new TypeError(
        line, String.format("linha %d: 'return' fora de função (veio %s)", line, actualType));
  }

  public static TypeError missingReturnOnAllPaths(
      int line, String functionName, LuminaType returnType) {
    return new TypeError(
        line,
        String.format(
            "linha %d: função '%s' do tipo %s não retorna valor em todos os caminhos",
            line, functionName, returnType));
  }

  @Override
  public String toString() {
    return message;
  }
}
