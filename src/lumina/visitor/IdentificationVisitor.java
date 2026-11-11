package lumina.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import lumina.lexer.token.TokenType;
import lumina.symbols.IdentificationError;
import lumina.symbols.ScopedSymbolTable;
import lumina.symbols.Symbol;
import lumina.symbols.SymbolCategory;
import lumina.symbols.SymbolTable;

/**
 * Passada de Identificação (Checkpoint 4 — Parte 3). Percorre a AST via double dispatch ({@code
 * Visitor<Void>}) sem uso de {@code instanceof} ou {@code switch}, gerenciando escopos através da
 * interface {@link SymbolTable}, registrando declarações, decorando os nós de uso com o ponteiro
 * {@code decl} para seu {@link Symbol} e acumulando erros semânticos de redeclaração e de nomes não
 * declarados.
 */
public final class IdentificationVisitor implements Visitor<Void> {

  private final SymbolTable table;
  private final List<IdentificationError> errors;

  public IdentificationVisitor() {
    this(new ScopedSymbolTable());
  }

  public IdentificationVisitor(SymbolTable table) {
    this.table = Objects.requireNonNull(table, "A tabela de símbolos não pode ser nula.");
    this.errors = new ArrayList<>();
  }

  public void identify(Node root) {
    if (root == null) {
      return;
    }
    root.accept(this);
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public List<IdentificationError> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  /**
   * Produz a saída determinística da passada de identificação (AST decorada e, se houver erros, a
   * seção de erros no formato documentado) para conferência com {@code test/esperado/}.
   */
  public String formatOutput(Node root) {
    if (root == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    AstPrinterVisitor printer = new AstPrinterVisitor();
    sb.append(printer.print(root));
    if (hasErrors()) {
      sb.append("---\n");
      for (IdentificationError error : errors) {
        sb.append(error.getMessage()).append("\n");
      }
    }
    return sb.toString();
  }

  @Override
  public Void visitProgram(ProgramNode node) {
    for (DeclNode decl : node.getDeclarations()) {
      decl.accept(this);
    }
    return null;
  }

  @Override
  public Void visitVarDecl(VarDeclNode node) {
    if (node.hasInitializer()) {
      node.getInitializer().accept(this);
    }
    node.getType().accept(this);
    Symbol symbol =
        new Symbol(
            node.getName(),
            SymbolCategory.VARIABLE,
            node,
            table.level(),
            node.getType().getTypeToken());
    declareOrReportRedeclaration(symbol, node.getLine());
    return null;
  }

  @Override
  public Void visitFunctionDecl(FunctionDeclNode node) {
    node.getReturnType().accept(this);
    SymbolCategory category = resolveCallableCategory(node.getReturnType());
    Symbol functionSymbol =
        new Symbol(
            node.getName(), category, node, table.level(), node.getReturnType().getTypeToken());
    declareOrReportRedeclaration(functionSymbol, node.getLine());

    table.openScope();
    for (ParamNode param : node.getParams()) {
      param.accept(this);
    }
    visitBlockStatementsInCurrentScope(node.getBody());
    table.closeScope();
    return null;
  }

  @Override
  public Void visitParam(ParamNode node) {
    node.getType().accept(this);
    Symbol paramSymbol =
        new Symbol(
            node.getName(),
            SymbolCategory.PARAMETER,
            node,
            table.level(),
            node.getType().getTypeToken());
    declareOrReportRedeclaration(paramSymbol, node.getLine());
    return null;
  }

  @Override
  public Void visitType(TypeNode node) {
    return null;
  }

  @Override
  public Void visitBlock(BlockNode node) {
    table.openScope();
    visitBlockStatementsInCurrentScope(node);
    table.closeScope();
    return null;
  }

  @Override
  public Void visitAssign(AssignNode node) {
    Symbol resolved = resolveIdentifierOrReport(node.getTargetName(), node.getLine());
    node.setDecl(resolved);
    node.getValue().accept(this);
    return null;
  }

  @Override
  public Void visitCallStmt(CallStmtNode node) {
    Symbol resolved = resolveIdentifierOrReport(node.getCallee(), node.getLine());
    node.setDecl(resolved);
    for (ExprNode arg : node.getArguments()) {
      arg.accept(this);
    }
    return null;
  }

  @Override
  public Void visitIf(IfNode node) {
    node.getCondition().accept(this);
    node.getThenBranch().accept(this);
    if (node.hasElseBranch()) {
      node.getElseBranch().accept(this);
    }
    return null;
  }

  @Override
  public Void visitWhile(WhileNode node) {
    node.getCondition().accept(this);
    node.getBody().accept(this);
    return null;
  }

  @Override
  public Void visitReturn(ReturnNode node) {
    if (node.hasExpression()) {
      node.getExpression().accept(this);
    }
    return null;
  }

  @Override
  public Void visitBin(BinNode node) {
    node.getLeft().accept(this);
    node.getRight().accept(this);
    return null;
  }

  @Override
  public Void visitUnary(UnaryNode node) {
    node.getOperand().accept(this);
    return null;
  }

  @Override
  public Void visitCallExpr(CallExprNode node) {
    Symbol resolved = resolveIdentifierOrReport(node.getCallee(), node.getLine());
    node.setDecl(resolved);
    for (ExprNode arg : node.getArguments()) {
      arg.accept(this);
    }
    return null;
  }

  @Override
  public Void visitId(IdNode node) {
    Symbol resolved = resolveIdentifierOrReport(node.getName(), node.getLine());
    node.setDecl(resolved);
    return null;
  }

  @Override
  public Void visitIntLit(IntLitNode node) {
    return null;
  }

  @Override
  public Void visitDoubleLit(DoubleLitNode node) {
    return null;
  }

  @Override
  public Void visitStringLit(StringLitNode node) {
    return null;
  }

  @Override
  public Void visitCharLit(CharLitNode node) {
    return null;
  }

  @Override
  public Void visitBoolLit(BoolLitNode node) {
    return null;
  }

  private void visitBlockStatementsInCurrentScope(BlockNode block) {
    for (StmtNode stmt : block.getStatements()) {
      stmt.accept(this);
    }
  }

  private SymbolCategory resolveCallableCategory(TypeNode returnType) {
    if (isVoidReturnType(returnType)) {
      return SymbolCategory.PROCEDURE;
    }
    return SymbolCategory.FUNCTION;
  }

  private boolean isVoidReturnType(TypeNode returnType) {
    return returnType.getTypeToken() == TokenType.VOID;
  }

  private void declareOrReportRedeclaration(Symbol symbol, int declarationLine) {
    boolean declaredSuccessfully = table.declare(symbol);
    if (!declaredSuccessfully) {
      Symbol existing = table.lookupLocal(symbol.getName());
      int previousLine = existing != null ? existing.getDeclarationLine() : declarationLine;
      errors.add(IdentificationError.redeclared(declarationLine, symbol.getName(), previousLine));
    }
  }

  private Symbol resolveIdentifierOrReport(String identifierName, int useLine) {
    Symbol resolved = table.lookup(identifierName);
    if (resolved == null) {
      errors.add(IdentificationError.undeclared(useLine, identifierName));
    }
    return resolved;
  }
}
