package lumina.types;

import java.util.Objects;
import lumina.ast.BinNode;
import lumina.ast.BoolLitNode;
import lumina.ast.CharLitNode;
import lumina.ast.DoubleLitNode;
import lumina.ast.ExprNode;
import lumina.ast.IdNode;
import lumina.ast.IntLitNode;
import lumina.ast.StringLitNode;
import lumina.ast.UnaryNode;
import lumina.common.source.SourcePosition;
import lumina.lexer.token.TokenType;
import lumina.visitor.TypeCheckerVisitor;

/**
 * Testes unitários isolados da tabela de operadores, exercitando os nós {@link BinNode} e {@link
 * UnaryNode} diretamente sem passar pelo parser.
 */
public final class OperatorRulesTest {

  private static final SourcePosition DEFAULT_POSITION = new SourcePosition(1, 1);

  private static final LuminaType[] VALUE_TYPES = {
    LuminaType.INT, LuminaType.DOUBLE, LuminaType.BOOL, LuminaType.CHAR, LuminaType.STRING
  };

  private int testsPassed = 0;
  private int testsFailed = 0;

  public static void main(String[] args) {
    OperatorRulesTest suite = new OperatorRulesTest();
    suite.runAllTests();
  }

  public void runAllTests() {
    printHeader();

    testArithmeticOperatorsPlusMinusStarSlash();
    testModuloOperatorPercent();
    testRelationalOrderingOperators();
    testEqualityOperators();
    testLogicalBinaryOperators();
    testUnaryNumericAndLogicalOperators();
    testErrorTypePropagationSuppressesCascade();

    printSummary();
    evaluateExitStatus();
  }

  private void testArithmeticOperatorsPlusMinusStarSlash() {
    TokenType[] operators = {TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH};
    for (TokenType operator : operators) {
      for (LuminaType left : VALUE_TYPES) {
        for (LuminaType right : VALUE_TYPES) {
          verifyBinaryRule(operator, left, right, expectedStandardArithmetic(left, right));
        }
      }
    }
  }

  private void testModuloOperatorPercent() {
    for (LuminaType left : VALUE_TYPES) {
      for (LuminaType right : VALUE_TYPES) {
        LuminaType expected = areBothIntegers(left, right) ? LuminaType.INT : null;
        verifyBinaryRule(TokenType.PERCENT, left, right, expected);
      }
    }
  }

  private void testRelationalOrderingOperators() {
    TokenType[] operators = {
      TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL
    };
    for (TokenType operator : operators) {
      for (LuminaType left : VALUE_TYPES) {
        for (LuminaType right : VALUE_TYPES) {
          verifyBinaryRule(operator, left, right, expectedRelational(left, right));
        }
      }
    }
  }

  private void testEqualityOperators() {
    TokenType[] operators = {TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL};
    for (TokenType operator : operators) {
      for (LuminaType left : VALUE_TYPES) {
        for (LuminaType right : VALUE_TYPES) {
          verifyBinaryRule(operator, left, right, expectedEquality(left, right));
        }
      }
    }
  }

  private void testLogicalBinaryOperators() {
    TokenType[] operators = {TokenType.AMPERSAND_AMPERSAND, TokenType.PIPE_PIPE};
    for (TokenType operator : operators) {
      for (LuminaType left : VALUE_TYPES) {
        for (LuminaType right : VALUE_TYPES) {
          LuminaType expected = areBothBooleans(left, right) ? LuminaType.BOOL : null;
          verifyBinaryRule(operator, left, right, expected);
        }
      }
    }
  }

  private void testUnaryNumericAndLogicalOperators() {
    TokenType[] numericUnaries = {TokenType.PLUS, TokenType.MINUS};
    for (TokenType operator : numericUnaries) {
      for (LuminaType operand : VALUE_TYPES) {
        LuminaType expected = operand.isNumeric() ? operand : null;
        verifyUnaryRule(operator, operand, expected);
      }
    }

    for (LuminaType operand : VALUE_TYPES) {
      LuminaType expected = operand.isBoolean() ? LuminaType.BOOL : null;
      verifyUnaryRule(TokenType.BANG, operand, expected);
    }
  }

  private void testErrorTypePropagationSuppressesCascade() {
    IdNode unresolvedId = new IdNode(DEFAULT_POSITION, "naoDeclarado");
    BinNode errorPlusInt =
        new BinNode(DEFAULT_POSITION, TokenType.PLUS, unresolvedId, literalFor(LuminaType.INT));
    UnaryNode notError = new UnaryNode(DEFAULT_POSITION, TokenType.BANG, errorPlusInt);

    TypeCheckerVisitor checker = new TypeCheckerVisitor();
    LuminaType topType = checker.check(notError);

    assertEqual("Propagação de erro no topo deve resultar em <erro>", LuminaType.ERROR, topType);
    assertEqual(
        "Subexpressão BinNode com operando <erro> deve gravar <erro>",
        LuminaType.ERROR,
        errorPlusInt.getType());
    assertFalse(
        "Operadores sobre <erro> não devem emitir novas mensagens em cascata", checker.hasErrors());
  }

  private void verifyBinaryRule(
      TokenType operator, LuminaType leftType, LuminaType rightType, LuminaType expectedResult) {
    BinNode node =
        new BinNode(DEFAULT_POSITION, operator, literalFor(leftType), literalFor(rightType));
    TypeCheckerVisitor checker = new TypeCheckerVisitor();
    LuminaType actualResult = checker.check(node);
    String ruleLabel =
        String.format("Binário (%s %s %s)", leftType, operator.getDescription(), rightType);

    if (isAcceptedRule(expectedResult)) {
      verifyAcceptedExpression(ruleLabel, expectedResult, actualResult, node, checker);
      return;
    }
    verifyRejectedExpression(ruleLabel, actualResult, node, checker);
  }

  private void verifyUnaryRule(
      TokenType operator, LuminaType operandType, LuminaType expectedResult) {
    UnaryNode node = new UnaryNode(DEFAULT_POSITION, operator, literalFor(operandType));
    TypeCheckerVisitor checker = new TypeCheckerVisitor();
    LuminaType actualResult = checker.check(node);
    String ruleLabel = String.format("Unário (%s%s)", operator.getDescription(), operandType);

    if (isAcceptedRule(expectedResult)) {
      verifyAcceptedExpression(ruleLabel, expectedResult, actualResult, node, checker);
      return;
    }
    verifyRejectedExpression(ruleLabel, actualResult, node, checker);
  }

  private void verifyAcceptedExpression(
      String label,
      LuminaType expectedResult,
      LuminaType actualResult,
      ExprNode node,
      TypeCheckerVisitor checker) {
    assertEqual(label + " [retorno]", expectedResult, actualResult);
    assertEqual(label + " [gravado no nó]", expectedResult, node.getType());
    assertFalse(label + " [sem erros]", checker.hasErrors());
  }

  private void verifyRejectedExpression(
      String label, LuminaType actualResult, ExprNode node, TypeCheckerVisitor checker) {
    assertEqual(label + " recusado -> <erro>", LuminaType.ERROR, actualResult);
    assertEqual(label + " recusado [gravado no nó]", LuminaType.ERROR, node.getType());
    assertEqual(label + " recusado [1 erro emitido]", 1, checker.getErrors().size());
  }

  private LuminaType expectedStandardArithmetic(LuminaType left, LuminaType right) {
    if (!areBothNumeric(left, right)) {
      return null;
    }
    return involvesDouble(left, right) ? LuminaType.DOUBLE : LuminaType.INT;
  }

  private LuminaType expectedRelational(LuminaType left, LuminaType right) {
    if (isOrderComparablePair(left, right)) {
      return LuminaType.BOOL;
    }
    return null;
  }

  private LuminaType expectedEquality(LuminaType left, LuminaType right) {
    if (isEqualityComparablePair(left, right)) {
      return LuminaType.BOOL;
    }
    return null;
  }

  private boolean isOrderComparablePair(LuminaType left, LuminaType right) {
    return areBothNumeric(left, right) || areBothChars(left, right);
  }

  private boolean isEqualityComparablePair(LuminaType left, LuminaType right) {
    return left == right || areBothNumeric(left, right);
  }

  private boolean isAcceptedRule(LuminaType expectedResult) {
    return expectedResult != null;
  }

  private boolean areBothIntegers(LuminaType left, LuminaType right) {
    return left == LuminaType.INT && right == LuminaType.INT;
  }

  private boolean areBothBooleans(LuminaType left, LuminaType right) {
    return left == LuminaType.BOOL && right == LuminaType.BOOL;
  }

  private boolean areBothChars(LuminaType left, LuminaType right) {
    return left == LuminaType.CHAR && right == LuminaType.CHAR;
  }

  private boolean areBothNumeric(LuminaType left, LuminaType right) {
    return left.isNumeric() && right.isNumeric();
  }

  private boolean involvesDouble(LuminaType left, LuminaType right) {
    return left == LuminaType.DOUBLE || right == LuminaType.DOUBLE;
  }

  private ExprNode literalFor(LuminaType type) {
    if (type == LuminaType.INT) {
      return new IntLitNode(DEFAULT_POSITION, 10, "10");
    }
    if (type == LuminaType.DOUBLE) {
      return new DoubleLitNode(DEFAULT_POSITION, 2.5, "2.5");
    }
    if (type == LuminaType.BOOL) {
      return new BoolLitNode(DEFAULT_POSITION, true);
    }
    if (type == LuminaType.CHAR) {
      return new CharLitNode(DEFAULT_POSITION, "'a'");
    }
    return new StringLitNode(DEFAULT_POSITION, "\"lumina\"");
  }

  private void assertFalse(String description, boolean condition) {
    if (!condition) {
      testsPassed++;
    } else {
      fail(description, "false", "true");
    }
  }

  private void assertEqual(String description, Object expected, Object actual) {
    if (Objects.equals(expected, actual)) {
      testsPassed++;
    } else {
      fail(description, String.valueOf(expected), String.valueOf(actual));
    }
  }

  private void fail(String description, String expected, String actual) {
    testsFailed++;
    System.err.printf("  [FAIL] %s%n", description);
    System.err.printf("         Esperado : %s%n", expected);
    System.err.printf("         Obtido   : %s%n", actual);
  }

  private void printHeader() {
    System.out.println("==========================================================");
    System.out.println("  INICIANDO SUÍTE DE TESTES - REGRAS DE OPERADORES (CP5)  ");
    System.out.println("==========================================================");
  }

  private void printSummary() {
    System.out.printf(
        "  [PASS] %d asserções isoladas da tabela de operadores verificadas sem Parser%n",
        testsPassed);
    System.out.println("----------------------------------------------------------");
    System.out.printf("Testes Concluídos: %d Aprovados, %d Falhas%n", testsPassed, testsFailed);
    System.out.println("==========================================================");
  }

  private void evaluateExitStatus() {
    if (testsFailed > 0) {
      System.err.printf("FALHA: %d teste(s) de Regras de Operadores falharam.%n", testsFailed);
      System.exit(1);
    } else {
      System.out.println("RESULTADO: TODAS AS REGRAS ISOLADAS DE OPERADORES PASSARAM!");
    }
  }
}
