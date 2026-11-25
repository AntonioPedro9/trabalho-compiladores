package lumina.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lumina.ast.AssignNode;
import lumina.ast.BinNode;
import lumina.ast.BlockNode;
import lumina.ast.BoolLitNode;
import lumina.ast.CallExprNode;
import lumina.ast.CallStmtNode;
import lumina.ast.CharLitNode;
import lumina.ast.DeclNode;
import lumina.ast.DoubleLitNode;
import lumina.ast.ExprNode;
import lumina.ast.FunctionDeclNode;
import lumina.ast.IdNode;
import lumina.ast.IfNode;
import lumina.ast.IntLitNode;
import lumina.ast.Node;
import lumina.ast.ParamNode;
import lumina.ast.ProgramNode;
import lumina.ast.ReturnNode;
import lumina.ast.StmtNode;
import lumina.ast.StringLitNode;
import lumina.ast.TypeNode;
import lumina.ast.UnaryNode;
import lumina.ast.VarDeclNode;
import lumina.ast.WhileNode;
import lumina.symbols.Symbol;
import lumina.symbols.SymbolCategory;
import lumina.types.LuminaType;
import lumina.types.OperatorTypeTable;
import lumina.types.TypeCheckContext;
import lumina.types.TypeCheckContext.FunctionFrame;
import lumina.types.TypeError;
import lumina.types.TypeWarning;

/**
 * Visitor responsável pela verificação de tipos sobre a AST decorada na fase de identificação,
 * anotando expressões e acumulando erros e avisos semânticos em passada única.
 */
public final class TypeCheckerVisitor implements Visitor<LuminaType> {

  private static final String CLEAN_VERIFICATION_OUTPUT = "OK\n";

  private final OperatorTypeTable operatorTable;
  private final TypeCheckContext context;
  private final List<TypeError> errors;
  private final List<TypeWarning> warnings;
  private final List<String> chronologicalDiagnostics;
  private boolean statementGuaranteedReturn;

  public TypeCheckerVisitor() {
    this(new OperatorTypeTable());
  }

  public TypeCheckerVisitor(OperatorTypeTable operatorTable) {
    this.operatorTable =
        Objects.requireNonNull(operatorTable, "A tabela de operadores não pode ser nula.");
    this.context = new TypeCheckContext();
    this.errors = new ArrayList<>();
    this.warnings = new ArrayList<>();
    this.chronologicalDiagnostics = new ArrayList<>();
  }

  /**
   * Executa a verificação de tipos a partir do nó raiz informado.
   *
   * @param root programa ou subárvore da AST
   * @return tipo calculado para o nó raiz
   */
  public LuminaType check(Node root) {
    if (root == null) {
      return LuminaType.VOID;
    }
    return root.accept(this);
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public List<TypeError> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  public boolean hasWarnings() {
    return !warnings.isEmpty();
  }

  public List<TypeWarning> getWarnings() {
    return Collections.unmodifiableList(warnings);
  }

  /**
   * Retorna {@code "OK\n"} caso não existam diagnósticos, ou as mensagens acumuladas ordenadas por
   * ocorrência.
   *
   * @return relatório textual da verificação
   */
  public String formatOutput() {
    if (chronologicalDiagnostics.isEmpty()) {
      return CLEAN_VERIFICATION_OUTPUT;
    }
    StringBuilder diagnosticBuilder = new StringBuilder();
    for (String message : chronologicalDiagnostics) {
      diagnosticBuilder.append(message).append("\n");
    }
    return diagnosticBuilder.toString();
  }

  @Override
  public LuminaType visitProgram(ProgramNode node) {
    for (DeclNode declaration : node.getDeclarations()) {
      declaration.accept(this);
    }
    return LuminaType.VOID;
  }

  @Override
  public LuminaType visitFunctionDecl(FunctionDeclNode node) {
    LuminaType returnType = node.getReturnType().accept(this);
    Set<Node> outerInitialized = context.snapshotInitialized();

    context.enterFunction(node.getName(), returnType);
    for (ParamNode param : node.getParams()) {
      param.accept(this);
    }
    boolean bodyReturns = checkGuaranteesReturn(node.getBody());

    if (isMissingGuaranteedReturn(returnType, bodyReturns)) {
      reportError(TypeError.missingReturnOnAllPaths(node.getLine(), node.getName(), returnType));
    }

    context.exitFunction();
    context.restoreInitialized(outerInitialized);
    statementGuaranteedReturn = false;
    return LuminaType.VOID;
  }

  @Override
  public LuminaType visitParam(ParamNode node) {
    LuminaType paramType = node.getType().accept(this);
    context.markInitialized(node);
    return paramType;
  }

  @Override
  public LuminaType visitVarDecl(VarDeclNode node) {
    LuminaType declaredType = node.getType().accept(this);
    if (!node.hasInitializer()) {
      return LuminaType.VOID;
    }
    LuminaType initializerType = node.getInitializer().accept(this);
    if (!declaredType.isAssignableFrom(initializerType)) {
      reportError(
          TypeError.invalidInitialization(
              node.getLine(), node.getName(), declaredType, initializerType));
    }
    context.markInitialized(node);
    return LuminaType.VOID;
  }

  @Override
  public LuminaType visitType(TypeNode node) {
    return LuminaType.fromTokenType(node.getTypeToken());
  }

  @Override
  public LuminaType visitBlock(BlockNode node) {
    boolean blockReturns = false;
    for (StmtNode statement : node.getStatements()) {
      if (checkGuaranteesReturn(statement)) {
        blockReturns = true;
      }
    }
    statementGuaranteedReturn = blockReturns;
    return LuminaType.VOID;
  }

  @Override
  public LuminaType visitAssign(AssignNode node) {
    LuminaType valueType = node.getValue().accept(this);
    Symbol targetDecl = node.getDecl();
    if (targetDecl == null) {
      return LuminaType.VOID;
    }
    if (!targetDecl.isAssignableStorage()) {
      reportError(
          TypeError.nonAssignableTarget(
              node.getLine(),
              node.getTargetName(),
              targetDecl.getCategory().getDescription(),
              targetDecl.getSemanticType()));
      return LuminaType.VOID;
    }
    LuminaType targetType = targetDecl.getSemanticType();
    if (!targetType.isAssignableFrom(valueType)) {
      reportError(
          TypeError.invalidAssignment(node.getLine(), node.getTargetName(), targetType, valueType));
    }
    context.markInitialized(targetDecl.getDeclarationNode());
    return LuminaType.VOID;
  }

  @Override
  public LuminaType visitCallStmt(CallStmtNode node) {
    List<LuminaType> argumentTypes = evaluateArguments(node.getArguments());
    Symbol calleeDecl = node.getDecl();
    if (calleeDecl == null) {
      return LuminaType.VOID;
    }
    if (!calleeDecl.isCallable()) {
      reportError(
          TypeError.notCallable(node.getLine(), node.getCallee(), calleeDecl.getSemanticType()));
      return LuminaType.VOID;
    }
    validateCallArguments(
        node.getLine(), node.getCallee(), calleeDecl, node.getArguments(), argumentTypes);
    return LuminaType.VOID;
  }

  @Override
  public LuminaType visitIf(IfNode node) {
    verifyBooleanCondition(node.getCondition(), node.getLine(), "if");
    verifyIfBranchesAndMergeInitialization(node);
    return LuminaType.VOID;
  }

  @Override
  public LuminaType visitWhile(WhileNode node) {
    verifyBooleanCondition(node.getCondition(), node.getLine(), "while");
    verifyLoopBodyPreservingInitialization(node.getBody());
    return LuminaType.VOID;
  }

  @Override
  public LuminaType visitReturn(ReturnNode node) {
    LuminaType exprType = evaluateReturnExpression(node);
    if (!context.isInsideFunction()) {
      reportError(TypeError.returnOutsideFunction(node.getLine(), exprType));
      return LuminaType.VOID;
    }
    checkReturnAgainstFunction(node, exprType, context.currentFunction());
    statementGuaranteedReturn = true;
    return LuminaType.VOID;
  }

  @Override
  public LuminaType visitBin(BinNode node) {
    LuminaType leftType = node.getLeft().accept(this);
    LuminaType rightType = node.getRight().accept(this);
    LuminaType resultType = operatorTable.resolveBinary(node.getOperator(), leftType, rightType);

    if (resultType == null) {
      reportError(
          TypeError.binaryOperatorMismatch(
              node.getLine(), node.getOperatorSymbol(), leftType, rightType));
      return recordType(node, LuminaType.ERROR);
    }
    return recordType(node, resultType);
  }

  @Override
  public LuminaType visitUnary(UnaryNode node) {
    LuminaType operandType = node.getOperand().accept(this);
    LuminaType resultType = operatorTable.resolveUnary(node.getOperator(), operandType);

    if (resultType == null) {
      reportError(
          TypeError.unaryOperatorMismatch(node.getLine(), node.getOperatorSymbol(), operandType));
      return recordType(node, LuminaType.ERROR);
    }
    return recordType(node, resultType);
  }

  @Override
  public LuminaType visitCallExpr(CallExprNode node) {
    List<LuminaType> argumentTypes = evaluateArguments(node.getArguments());
    Symbol calleeDecl = node.getDecl();
    if (calleeDecl == null) {
      return recordType(node, LuminaType.ERROR);
    }
    if (!calleeDecl.isCallable()) {
      reportError(
          TypeError.notCallable(node.getLine(), node.getCallee(), calleeDecl.getSemanticType()));
      return recordType(node, LuminaType.ERROR);
    }
    validateCallArguments(
        node.getLine(), node.getCallee(), calleeDecl, node.getArguments(), argumentTypes);
    if (calleeDecl.isProcedure()) {
      reportError(TypeError.procedureInExpression(node.getLine(), node.getCallee()));
      return recordType(node, LuminaType.ERROR);
    }
    return recordType(node, calleeDecl.getSemanticType());
  }

  @Override
  public LuminaType visitId(IdNode node) {
    Symbol decl = node.getDecl();
    if (decl == null) {
      return recordType(node, LuminaType.ERROR);
    }
    if (decl.isCallable()) {
      reportError(
          TypeError.uncalledCallableReference(
              node.getLine(),
              node.getName(),
              decl.getCategory().getDescription(),
              decl.getSemanticType()));
      return recordType(node, LuminaType.ERROR);
    }
    warnIfUninitializedVariableRead(node.getLine(), node.getName(), decl);
    return recordType(node, decl.getSemanticType());
  }

  @Override
  public LuminaType visitIntLit(IntLitNode node) {
    return recordType(node, LuminaType.INT);
  }

  @Override
  public LuminaType visitDoubleLit(DoubleLitNode node) {
    return recordType(node, LuminaType.DOUBLE);
  }

  @Override
  public LuminaType visitStringLit(StringLitNode node) {
    return recordType(node, LuminaType.STRING);
  }

  @Override
  public LuminaType visitCharLit(CharLitNode node) {
    return recordType(node, LuminaType.CHAR);
  }

  @Override
  public LuminaType visitBoolLit(BoolLitNode node) {
    return recordType(node, LuminaType.BOOL);
  }

  private LuminaType recordType(ExprNode node, LuminaType evaluatedType) {
    node.setType(evaluatedType);
    return evaluatedType;
  }

  private boolean checkGuaranteesReturn(Node statement) {
    statementGuaranteedReturn = false;
    statement.accept(this);
    return statementGuaranteedReturn;
  }

  private void verifyBooleanCondition(ExprNode condition, int line, String keyword) {
    LuminaType conditionType = condition.accept(this);
    if (isInvalidConditionType(conditionType)) {
      reportError(TypeError.nonBooleanCondition(line, keyword, conditionType));
    }
  }

  private void verifyIfBranchesAndMergeInitialization(IfNode node) {
    Set<Node> beforeIf = context.snapshotInitialized();
    boolean thenReturns = checkGuaranteesReturn(node.getThenBranch());
    Set<Node> afterThen = context.snapshotInitialized();

    if (!node.hasElseBranch()) {
      context.mergeBranchInitialization(afterThen, thenReturns, beforeIf, false);
      statementGuaranteedReturn = false;
      return;
    }

    context.restoreInitialized(beforeIf);
    boolean elseReturns = checkGuaranteesReturn(node.getElseBranch());
    Set<Node> afterElse = context.snapshotInitialized();
    context.mergeBranchInitialization(afterThen, thenReturns, afterElse, elseReturns);
    statementGuaranteedReturn = areBothBranchesReturning(thenReturns, elseReturns);
  }

  private void verifyLoopBodyPreservingInitialization(BlockNode body) {
    Set<Node> beforeLoop = context.snapshotInitialized();
    body.accept(this);
    context.restoreInitialized(beforeLoop);
    statementGuaranteedReturn = false;
  }

  private LuminaType evaluateReturnExpression(ReturnNode node) {
    if (!node.hasExpression()) {
      return LuminaType.VOID;
    }
    return node.getExpression().accept(this);
  }

  private List<LuminaType> evaluateArguments(List<ExprNode> arguments) {
    List<LuminaType> evaluatedTypes = new ArrayList<>(arguments.size());
    for (ExprNode argument : arguments) {
      evaluatedTypes.add(argument.accept(this));
    }
    return evaluatedTypes;
  }

  private void validateCallArguments(
      int callLine,
      String calleeName,
      Symbol calleeDecl,
      List<ExprNode> argumentNodes,
      List<LuminaType> actualTypes) {
    List<LuminaType> expectedTypes = calleeDecl.getParameterTypes();
    if (hasArityMismatch(expectedTypes, actualTypes)) {
      reportError(
          TypeError.arityMismatch(callLine, calleeName, expectedTypes.size(), actualTypes.size()));
      return;
    }
    validateEachArgumentType(calleeName, argumentNodes, expectedTypes, actualTypes);
  }

  private void validateEachArgumentType(
      String calleeName,
      List<ExprNode> argumentNodes,
      List<LuminaType> expectedTypes,
      List<LuminaType> actualTypes) {
    for (int paramIndex = 0; paramIndex < expectedTypes.size(); paramIndex++) {
      LuminaType expectedType = expectedTypes.get(paramIndex);
      LuminaType actualType = actualTypes.get(paramIndex);
      if (!expectedType.isAssignableFrom(actualType)) {
        int argumentLine = argumentNodes.get(paramIndex).getLine();
        reportError(
            TypeError.argumentTypeMismatch(
                argumentLine, paramIndex + 1, calleeName, expectedType, actualType));
      }
    }
  }

  private void checkReturnAgainstFunction(
      ReturnNode node, LuminaType exprType, FunctionFrame currentFunc) {
    if (currentFunc.isProcedure()) {
      checkProcedureReturn(node, exprType, currentFunc);
      return;
    }
    checkValueFunctionReturn(node, exprType, currentFunc);
  }

  private void checkProcedureReturn(
      ReturnNode node, LuminaType exprType, FunctionFrame currentProc) {
    if (hasUnexpectedReturnValue(node, exprType)) {
      reportError(
          TypeError.voidProcedureReturningValue(node.getLine(), currentProc.name(), exprType));
    }
  }

  private void checkValueFunctionReturn(
      ReturnNode node, LuminaType exprType, FunctionFrame currentFunc) {
    LuminaType expectedReturn = currentFunc.returnType();
    if (!node.hasExpression()) {
      reportError(
          TypeError.emptyReturnInFunction(node.getLine(), currentFunc.name(), expectedReturn));
      return;
    }
    if (!expectedReturn.isAssignableFrom(exprType)) {
      reportError(
          TypeError.returnTypeMismatch(
              node.getLine(), currentFunc.name(), expectedReturn, exprType));
    }
  }

  private void warnIfUninitializedVariableRead(int line, String name, Symbol decl) {
    if (isUninitializedVariable(decl)) {
      reportWarning(TypeWarning.possiblyUninitializedVariable(line, name, decl.getSemanticType()));
    }
  }

  private boolean isUninitializedVariable(Symbol decl) {
    return decl.getCategory() == SymbolCategory.VARIABLE
        && !context.isInitialized(decl.getDeclarationNode());
  }

  private boolean isMissingGuaranteedReturn(LuminaType returnType, boolean bodyReturns) {
    return !returnType.isVoid() && !returnType.isError() && !bodyReturns;
  }

  private boolean areBothBranchesReturning(boolean thenReturns, boolean elseReturns) {
    return thenReturns && elseReturns;
  }

  private boolean hasUnexpectedReturnValue(ReturnNode node, LuminaType exprType) {
    return node.hasExpression() && !exprType.isError();
  }

  private boolean isInvalidConditionType(LuminaType conditionType) {
    return !conditionType.isError() && !conditionType.isBoolean();
  }

  private boolean hasArityMismatch(List<LuminaType> expectedTypes, List<LuminaType> actualTypes) {
    return expectedTypes.size() != actualTypes.size();
  }

  private void reportError(TypeError error) {
    errors.add(error);
    chronologicalDiagnostics.add(error.message());
  }

  private void reportWarning(TypeWarning warning) {
    warnings.add(warning);
    chronologicalDiagnostics.add(warning.message());
  }
}
